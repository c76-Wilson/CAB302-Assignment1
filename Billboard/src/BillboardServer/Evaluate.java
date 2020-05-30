package BillboardServer;

import Helper.Billboard;
import Helper.Password;
import Helper.Requests.LoginRequest;
import Helper.Responses.ErrorMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

public class Evaluate {
    public static Object EvaluateCurrentBillboard(Connection con) throws Exception{
        Statement statement = con.createStatement();

        String sql = String.format("SELECT * FROM schedules WHERE (StartTime <= \"%s\" AND (StartTime + Duration) >= \"%s\") OR (NextOccurrence <= \"%s\" AND (NextOccurrence + Duration) >= \"%s\") ORDER BY ID DESC LIMIT 1", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

        ResultSet scheduleResult = statement.executeQuery(sql);

        if (scheduleResult.next()){

            // If next occurrence is the current time - update starting to nextoccurrence, and set nextoccurrence to be the next occurrence
            if (scheduleResult.getTimestamp("NextOccurrence").toLocalDateTime().isBefore(LocalDateTime.now())){
                LocalDateTime currentStartTime = scheduleResult.getTimestamp("NextOccurrence").toLocalDateTime();

                statement.executeQuery(String.format("UPDATE schedules SET StartTime = \"%s\", NextOccurrence = NextOccurrence + CAST(RecurringEvery AS DATETIME) WHERE ID = %d", currentStartTime, scheduleResult.getInt("ID")));
            }

            ResultSet billboardResult = statement.executeQuery(String.format("SELECT XML FROM billboards WHERE ScheduleID = %d LIMIT 1", scheduleResult.getInt("ID")));

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
        String sql = String.format("DELIMITER $ \r\n" +
                "BEGIN NOT ATOMIC\r\n" +
                "IF EXISTS(SELECT * FROM billboards where Name=\"%s\") THEN UPDATE billboards SET XML='%s' where Name=\"%s\";\r\n" +
                "ELSE insert into billboards(Name, XML, CreatorID) values('%s', '%s', '%s');\r\n" +
                "END IF;\r\n" +
                "END $\r\n" +
                "DELIMITER ;", billboard.getName(), escapedXML, billboard.getName(), billboard.getName(), escapedXML, billboard.getCreatorName());
        // Insert or update billboard
        statement.executeQuery(sql);
        return true;
    }

    private static String EscapeString(String input){
        return input.replace("\"", "\\\"").replace("'", "''");
    }
}
