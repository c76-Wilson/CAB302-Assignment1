package BillboardServer;

import Helper.Billboard;
import Helper.Password;
import Helper.Requests.CreateUserRequest;
import Helper.Requests.LoginRequest;
import Helper.Requests.SetUserPermissionsRequest;
import Helper.Responses.ErrorMessage;
import Helper.ScheduledBillboard;
import jdk.jfr.Timespan;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;

public class Evaluate {
    public static Object EvaluateCurrentBillboard(Connection con) throws Exception{
        // Update schedules for recurrence before evaluating current billboard
        UpdateNextScheduled(con);

        Statement statement = con.createStatement();

        String sql = String.format("SELECT * FROM schedules WHERE StartTime <= NOW() AND (StartTime + INTERVAL Duration MINUTE) >= NOW() ORDER BY ID DESC LIMIT 1");

        ResultSet scheduleResult = statement.executeQuery(sql);

        if (scheduleResult.next()){
            ResultSet billboardResult = statement.executeQuery(String.format("SELECT * FROM billboards WHERE ScheduleID = %d LIMIT 1", scheduleResult.getInt("ID")));

            if (billboardResult.next()){
                return ConvertResultSetToBillboard(billboardResult);
            }
            else{
                throw new Exception("Could not find billboard for this schedule!");
            }
        }
        else{
            String contents = new String(Files.readAllBytes(Paths.get("src\\BillboardServer\\error.xml")));

            return contents;
        }
    }

    private static void UpdateNextScheduled(Connection con) throws Exception{
        // Update schedules where passed schedule time and recurring
        Statement statement = con.createStatement();
        String sql = String.format("UPDATE schedules SET StartTime = StartTime + INTERVAL RecurringEvery MINUTE WHERE ID IN (SELECT ID FROM schedules WHERE RecurringEvery != null AND StartTime + INTERVAL Duration MINUTE <= NOW());");
        statement.executeQuery(sql);
    }

    public static Object EvaluateLogin(Connection con, LoginRequest request)throws Exception{
        // make SQL query to get user for a given name
        Statement statement = con.createStatement();

        ResultSet userResult = statement.executeQuery(String.format("SELECT Password FROM users WHERE Name = \"%s\" LIMIT 1", request.getUserName()));

        // If user exists, check password
        if (userResult.next()){
            if (Password.authenticatePassword(request.getHashedPassword(), userResult.getString("Password"))){
                // If password is correct, return session token and store in DB
                // Generate session token
                byte[] randomBytes = new byte[24];
                new SecureRandom().nextBytes(randomBytes);
                String sessionToken = Base64.getUrlEncoder().encodeToString(randomBytes);

                return new SessionToken(request.getUserName(), sessionToken);
            }
            else{
                return new ErrorMessage("Incorrect password!");
            }
        }
        // Else return error
        else{
            return new ErrorMessage("Incorrect username!");
        }
    }

    public static Object EvaluateListBillboards(Connection con) throws Exception{
        Statement statement = con.createStatement();

        ResultSet billboardResult = statement.executeQuery("SELECT billboards.Name, users.Name, schedules.StartTime FROM billboards LEFT JOIN users ON billboards.CreatorID = users.ID LEFT JOIN schedules ON billboards.ScheduleID = schedules.ID");

        return null;
    }

    public static Object EvaluateGetBillboard(Connection con, String billboardName) throws Exception{
        Statement statement = con.createStatement();

        String sql = String.format("SELECT * FROM billboards WHERE Name = \"%s\"", billboardName);

        ResultSet scheduleResult = statement.executeQuery(sql);

        if (scheduleResult.next()){
            return ConvertResultSetToBillboard(scheduleResult);
        }

        return new ErrorMessage("Could not find billboard with that name!");
    }

    private static Billboard ConvertResultSetToBillboard(ResultSet resultSet) throws Exception{
        return new Billboard(resultSet.getString("Name"), resultSet.getString("XML"), resultSet.getString("CreatorName"), resultSet.getInt("ScheduleID"));
    }

    public static Object EvaluateCreateEditBillboard(Connection con, Billboard billboard) throws Exception{
        Statement statement = con.createStatement();
        String escapedXML = EscapeString(billboard.getXml());
        String sql = String.format("INSERT INTO billboards(Name, XML, CreatorName) VALUES ('%s', '%s', '%s') ON DUPLICATE KEY UPDATE XML = '%s';", billboard.getName(), escapedXML, billboard.getCreatorName(), escapedXML);
        // Insert or update billboard
        statement.execute(sql);
        return true;
    }

    public static Object EvaluateViewSchedule(Connection con) throws Exception{
        Statement statement = con.createStatement();
        String sql = "SELECT b.Name, b.CreatorName, s.StartTime, s.Duration FROM schedules s LEFT JOIN billboards b ON s.BillboardName = b.Name WHERE s.StartTime + INTERVAL s.Duration MINUTE >= NOW() AND s.StartTime < NOW() + INTERVAL 7 DAY;";

        ResultSet scheduleResult = statement.executeQuery(sql);

        LinkedList<ScheduledBillboard> billboards = new LinkedList<>();

        while (scheduleResult.next()){
            billboards.add(new ScheduledBillboard(scheduleResult.getString("Name"), scheduleResult.getString("CreatorName"), scheduleResult.getTimestamp("StartTime").toLocalDateTime(), Duration.parse(String.format("PT%dM", scheduleResult.getInt("Duration")))));
        }

        return billboards;
    }

    public static Object EvaluateScheduleBillboard(Connection con, String billboardName, LocalDateTime scheduleTime, Duration duration, Duration recurrence, String creatorName) throws Exception{
        String sql = String.format("INSERT INTO schedules (BillboardName, StartTime, Duration, RecurringEvery, CreatorName) VALUES ('%s', '%s', %d, %d, '%s')", billboardName, scheduleTime, duration.toMinutes(), recurrence == null ? null : recurrence.toMinutes(), creatorName);
        // Insert or update billboard
        PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        statement.executeUpdate();

        ResultSet insertResults = statement.getGeneratedKeys();

        if (insertResults.next()) {
            String sql2 = String.format("UPDATE billboards SET ScheduleID = %s WHERE Name = '%s';", insertResults.getInt(1), billboardName);
            statement.executeQuery(sql2);
            return true;
        }
        return false;
    }

    public static Object EvaluateCreateUser(Connection con, CreateUserRequest request) throws Exception {
        String sql = String.format("INSERT INTO users (Name, Password) VALUES ('%s', '%s');", request.getUserName(), Password.getSaltedHash(request.getHashedPassword()));

        try {
            Statement statement = con.createStatement();

            statement.executeQuery(sql);

            if (request.getPermissions().size() > 0) {
                String permissionSQL = "INSERT INTO user_permissions (UserName, PermissionName) VALUES";

                for (String permission : request.getPermissions()){
                    if (permission != request.getPermissions().getFirst()){
                        permissionSQL = permissionSQL.concat(",");
                    }
                    permissionSQL = permissionSQL.concat(String.format(" ('%s', '%s')", request.getUserName(), permission));
                }

                permissionSQL = permissionSQL.concat(";");

                statement.executeQuery(permissionSQL);
            }

            return true;
        }
        catch (SQLException e){
            if (e.getMessage().contains("Duplicate entry")) {
                return new ErrorMessage("User already exists!");
            }
            return new ErrorMessage(e.getMessage());
        }
    }

    private static String EscapeString(String input){
        return input.replace("\"", "\\\"").replace("'", "''");
    }

    public static Object EvaluateSetUserPermissions(Connection con, SetUserPermissionsRequest request) {
        String sql = String.format("DELETE FROM user_permissions WHERE UserName = '%s';", request.getUserName());

        try {
            Statement statement = con.createStatement();

            statement.execute(sql);

            if (request.getPermissions().size() > 0) {
                sql = "INSERT INTO user_permissions (UserName, PermissionName) VALUES";

                for (String permission : request.getPermissions()){
                    if (permission != request.getPermissions().getFirst()){
                        sql = sql.concat(",");
                    }
                    sql = sql.concat(String.format(" ('%s', '%s')", request.getUserName(), permission));
                }

                sql = sql.concat(";");

                statement.executeQuery(sql);
            }

            return true;
        }
        catch (SQLException e){
            return new ErrorMessage(e.getMessage());
        }
    }
}
