package BillboardServer;

import Helper.*;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedList;

public class Evaluate {
    /**
     * Takes a database connection as a parameter - returns either the currently scheduled billboard or the placeholder file
     * @param con Database connection
     * @return Current billboard or placeholder xml
     */
    public static Object EvaluateCurrentBillboard(Connection con){
        try {
            // Update schedules for recurrence before evaluating current billboard
            UpdateNextScheduled(con);

            Statement statement = con.createStatement();

            String sql = String.format("SELECT * FROM schedules WHERE StartTime <= NOW() AND (StartTime + INTERVAL Duration MINUTE) >= NOW() ORDER BY ID DESC LIMIT 1");

            ResultSet scheduleResult = statement.executeQuery(sql);

            if (scheduleResult.next()) {
                ResultSet billboardResult = statement.executeQuery(String.format("SELECT * FROM billboards WHERE Name = '%s' LIMIT 1", scheduleResult.getString("BillboardName")));

                if (billboardResult.next()) {
                    return ConvertResultSetToBillboard(billboardResult);
                } else {
                    return new ErrorMessage("Could not find billboard for this schedule!");
                }
            } else {
                String contents = new String(Files.readAllBytes(Paths.get("src\\BillboardServer\\error.xml")));

                return contents;
            }
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection - updates any schedules that are in the past and have repetition to their new start time
     * @param con Database connection
     */
    private static void UpdateNextScheduled(Connection con){
        try {
            // Update schedules where passed schedule time and recurring
            Statement statement = con.createStatement();
            String sql = String.format("UPDATE schedules SET StartTime = StartTime + INTERVAL RecurringEvery MINUTE WHERE ID IN (SELECT ID FROM schedules WHERE RecurringEvery IS NOT null AND StartTime + INTERVAL Duration MINUTE <= NOW());");
            statement.executeQuery(sql);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Takes LoginRequest and db connection as parameters - checks if username and password match a user and if so
     * returns a session token - else returns error
     * @param con Database connection
     * @param request Login Request object
     * @return Session token if login successful - otherwise return ErrorMessage
     */
    public static Object EvaluateLogin(Connection con, LoginRequest request){
        try {
            // make SQL query to get user for a given name
            Statement statement = con.createStatement();

            ResultSet userResult = statement.executeQuery(String.format("SELECT Password FROM users WHERE Name = \"%s\" LIMIT 1", request.getUserName()));

            // If user exists, check password
            if (userResult.next()) {
                if (Password.authenticatePassword(request.getHashedPassword(), userResult.getString("Password"))) {
                    // If password is correct, return session token and store in DB
                    // Generate session token
                    byte[] randomBytes = new byte[24];
                    new SecureRandom().nextBytes(randomBytes);
                    String sessionToken = Base64.getUrlEncoder().encodeToString(randomBytes);

                    return new SessionToken(request.getUserName(), sessionToken);
                } else {
                    return new ErrorMessage("Incorrect password!");
                }
            }
            // Else return error
            else {
                return new ErrorMessage("Incorrect username!");
            }
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes db connection as parameter - returns a list of names and creators of all billboards in the database
     * @param con Database connection
     * @return List of billboards if successful, otherwise return ErrorMessage
     */
    public static Object EvaluateListBillboards(Connection con){
        try {
            Statement statement = con.createStatement();

            ResultSet billboardResult = statement.executeQuery("SELECT Name, CreatorName, XML FROM billboards");

            LinkedList<Billboard> billboards = new LinkedList<>();

            while (billboardResult.next()) {
                billboards.add(new Billboard(billboardResult.getString("Name"), billboardResult.getString("CreatorName")));
            }

            return billboards;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes db connection and billboard name as parameters - returns all billboard information if billboard exists - otherwise error
     * @param con Database connection
     * @param billboardName Name of billboard to be retrieved
     * @return Billboard object with billboard contents if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateGetBillboard(Connection con, String billboardName){
        try {
            Statement statement = con.createStatement();

            String sql = String.format("SELECT * FROM billboards WHERE Name = \"%s\"", billboardName);

            ResultSet scheduleResult = statement.executeQuery(sql);

            if (scheduleResult.next()) {
                return ConvertResultSetToBillboard(scheduleResult);
            }

            return new ErrorMessage("Could not find billboard with that name!");
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Converts a resultset into a billboard (ONLY CALL WHEN RESULTSET IS OF TYPE BILLBOARD)
     * @param resultSet Takes result set of billboard information
     * @return Returns billboard object
     * @throws Exception Throws exception if resultSet doesn't parse to billboard
     */
    private static Billboard ConvertResultSetToBillboard(ResultSet resultSet) throws Exception{
        return new Billboard(resultSet.getString("Name"), resultSet.getString("XML"), resultSet.getString("CreatorName"));
    }

    /**
     * Takes a db connection and Billboard object as parameters, inserts billboard into database
     * @param con Database connection
     * @param billboard Billboard object to be inserted
     * @return Returns true if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateCreateEditBillboard(Connection con, Billboard billboard){
        try {
            Statement statement = con.createStatement();
            String escapedXML = EscapeString(billboard.getXml());
            String sql = String.format("INSERT INTO billboards(Name, XML, CreatorName) VALUES ('%s', '%s', '%s') ON DUPLICATE KEY UPDATE XML = '%s';", billboard.getName(), escapedXML, billboard.getCreatorName(), escapedXML);
            // Insert or update billboard
            statement.execute(sql);
            return true;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes db connection as parameter, returns all scheduled billboards for the next 7 days
     * @param con Database connection
     * @return a list of billboards scheduled in the next 7 days if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateViewSchedule(Connection con){
        UpdateNextScheduled(con);

        try {
            Statement statement = con.createStatement();
            String sql = "SELECT b.Name, b.CreatorName, s.StartTime, s.Duration FROM schedules s LEFT JOIN billboards b ON s.BillboardName = b.Name WHERE s.StartTime + INTERVAL s.Duration MINUTE >= NOW() AND s.StartTime < NOW() + INTERVAL 7 DAY ORDER BY s.StartTime ASC;";

            ResultSet scheduleResult = statement.executeQuery(sql);

            LinkedList<ScheduledBillboard> billboards = new LinkedList<>();

            while (scheduleResult.next()) {
                billboards.add(new ScheduledBillboard(scheduleResult.getString("Name"), scheduleResult.getString("CreatorName"), scheduleResult.getTimestamp("StartTime").toLocalDateTime(), Duration.parse(String.format("PT%dM", scheduleResult.getInt("Duration")))));
            }

            return billboards;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes db connection and schedule properties as parameters - inserts schedule into database
     * @param con Database connection
     * @param request Schedule billboard request
     * @param creatorName Name of creator of this schedule
     * @return Returns true if successful - otherwise returns error message
     */
    public static Object EvaluateScheduleBillboard(Connection con, ScheduleBillboardRequest request, String creatorName){
        try {
            String sql = String.format("INSERT INTO schedules (BillboardName, StartTime, Duration, RecurringEvery, CreatorName) VALUES ('%s', '%s', %d, %d, '%s')", request.getBillboardName(), request.getScheduleTime(), request.getDuration().toMinutes(), request.getRecurring() == null ? null : request.getRecurring().toMinutes(), creatorName);
            // Insert or update billboard
            Statement statement = con.createStatement();

            statement.execute(sql);

            return true;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes db connection and Create User Request as parameters - inserts the new user and creates the permissions
     * @param con Database connection
     * @param request Create user request to be evaluated
     * @return Returns true if successful - otherwise returns error message
     */
    public static Object EvaluateCreateUser(Connection con, CreateUserRequest request){
        try {
            // Generate query to add user
            String sql = String.format("INSERT INTO users (Name, Password) VALUES ('%s', '%s');", request.getUserName(), Password.getSaltedHash(request.getHashedPassword()));

            // Insert user
            Statement statement = con.createStatement();

            statement.executeQuery(sql);

            // Generate permissions SQL
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

            // Return true if successful
            return true;
        }
        catch (Exception e){
            if (e.getMessage().contains("Duplicate entry")) {
                return new ErrorMessage("User already exists!");
            }
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Escapes quotes(") and parenthesis (') from a string (for storing xml)
     * @param input Input string to be escaped
     * @return Returns escaped string
     */
    private static String EscapeString(String input){
        return input.replace("\"", "\\\"").replace("'", "''");
    }

    /**
     * Takes a db connection and set user permissions request as parameters
     * Deletes existing permissions then inserts new ones into database
     * @param con Database connection
     * @param request Set user permissions request to be evaluated
     * @return returns true if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateSetUserPermissions(Connection con, SetUserPermissionsRequest request) {
        // SQL query to delete old permissions
        String sql = String.format("DELETE FROM user_permissions WHERE UserName = '%s';", request.getUserName());

        try {
            Statement statement = con.createStatement();

            statement.execute(sql);

            // Generate and run SQL for adding new permissions
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

            // Return true if successful
            return true;
        }
        catch (SQLException e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection and returns a list of usernames and how many billboards a user has created
     * @param con Database connection
     * @return Returns a list of users if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateListUsers(Connection con){
        // SQL query to get usernames and how many billboards each user has created
        String sql = "SELECT users.Name, count(billboards.Name) as number_of_billboards from users left join billboards on (billboards.CreatorName = users.Name) group by users.Name";

        try {
            // Create statement
            Statement statement = con.createStatement();

            ResultSet userSet = statement.executeQuery(sql);

            LinkedList<User> users = new LinkedList<>();

            // Add all results to a list of users and return
            while (userSet.next()){
                users.add(new User(userSet.getString("Name"), userSet.getInt("number_of_billboards")));
            }

            return users;
        }
        catch (SQLException e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection and set password request, then gets the salted hash of the provided password and updates the users details
     * @param con Database connection
     * @param request Set user password request to be evaluated
     * @return returns true if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateSetUserPassword(Connection con, SetUserPasswordRequest request) {
        try {
            // SQL query to insert new password
            String sql = String.format("UPDATE users SET Password = '%s' WHERE Name = '%s';", Password.getSaltedHash(request.getHashedPassword()), request.getUserName());

            Statement statement = con.createStatement();

            statement.execute(sql);

            // Return true if successful
            return true;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection and String billboard name as parameters and deletes the schedules associated with the billboard then deletes the billboard.
     * @param con Database connection
     * @param name Name of billboard to be deleted
     * @return Returns true if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateDeleteBillboard(Connection con, String name) {
        try{
            // SQL to delete associated schedules
            String sql = String.format("DELETE FROM schedules WHERE BillboardName = '%s';", name);

            Statement statement = con.createStatement();

            statement.execute(sql);
            // SQL query to delete billboard
            sql = String.format("DELETE FROM billboards WHERE Name = '%s';", name);

            statement.execute(sql);

            // Return true if successful
            return true;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection and remove schedule request, then deletes from the schedule based on billboard name and schedule time
     * @param con Database connection
     * @param request Remove from schedule request to be evaluated
     * @return Returns true if successful - otherwise returns an ErrorMessage
     */
    public static Object EvaluateRemoveFromSchedule(Connection con, RemoveFromScheduleRequest request) {
        try{
            // SQL query to delete this scheduling of a billboard
            String sql = String.format("DELETE FROM schedules WHERE BillboardName = '%s' AND StartTime = '%s'", request.getBillboardName(), request.getScheduleTime());

            // Execute query
            Statement statement = con.createStatement();

            statement.execute(sql);

            // Return true if successful
            return true;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection and get permissions request and returns a list of strings of the users permissions
     * @param con Database connection
     * @param request Get user permissions request to evaluate
     * @return Returns a list of permissions if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateGetPermissions(Connection con, GetUserPermissionsRequest request) {
        try{
            // SQL query to get user permissions
            String sql = String.format("SELECT * FROM user_permissions WHERE UserName = '%s'", request.getUserName());

            // Execute query
            Statement statement = con.createStatement();

            ResultSet permissionSet = statement.executeQuery(sql);

            // Create a list of permissions
            LinkedList<String> permissions = new LinkedList<>();

            while (permissionSet.next()){
                permissions.add(permissionSet.getString("PermissionName"));
            }

            // Return list of permissions
            return permissions;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Takes a db connection, delete user request and requesting user as parameters
     * Updates any existing billboards and schedules that were created by the deleted user and changes the created by field to the deleting user
     * Then deletes user permissions associated with the user and then deletes the user
     * @param con Database connection
     * @param request Delete user request to evaluate
     * @param requestingUser User requesting the deletion
     * @return Returns true if successful - otherwise returns ErrorMessage
     */
    public static Object EvaluateDeleteUser(Connection con, DeleteUserRequest request, String requestingUser) {
        try{
            // SQL query to move all billboards from deleted user to deleting user
            String sql = String.format("UPDATE billboards SET CreatorName = '%s' WHERE CreatorName = '%s'; \r\n", requestingUser, request.getUserName());
            // SQL query to move all schedules from deleted user to deleting user
            sql = sql.concat(String.format("UPDATE schedules SET CreatorName = '%s' WHERE CreatorName = '%s'; \r\n", requestingUser, request.getUserName()));
            // SQL query to delete all permissions associated with deleted user (saving storage)
            sql = sql.concat(String.format("DELETE FROM user_permissions WHERE UserName = '%s'; \r\n", request.getUserName()));
            // SQL query to delete user
            sql = sql.concat(String.format("DELETE FROM users WHERE Name = '%s'", request.getUserName()));

            // Execute query
            Statement statement = con.createStatement();

            statement.execute(sql);

            // Return true if succeeded
            return true;
        }
        catch (Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }
}
