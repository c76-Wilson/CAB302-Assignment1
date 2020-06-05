package BillboardServer;

import Helper.Billboard;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Server implements Runnable {

    static int port = getServerPort();
    Connection con;
    Socket currentSocket;
    static LinkedList<SessionToken> sessionTokens = new LinkedList<>();

    /**
     * Constructor for multi-threaded server - takes the current socket for the instance to run on
     * and a list of existing session tokens
     * @param currentSocket The current socket for this threaded server instance
     * @param tokens The current list of tokens
     */
    Server(Socket currentSocket, LinkedList<SessionToken> tokens) {
        this.currentSocket = currentSocket;
        this.sessionTokens = tokens;

        Properties properties = new Properties();

        properties = getDBProperties();

        this.con = ConnectToDatabase.connect(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.schema"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
    }

    public static void main(String args[]) {
        runServer();
    }

    /**
     * runServer() method - gets the server port and creates accepts connections to the server
     */
    public static void runServer(){
        try {
            port = getServerPort();

            ServerSocket serverSocket = new ServerSocket(port);

            for (; ; ) {
                Socket socket = serverSocket.accept();

                System.out.println(socket.getInetAddress() + " connected to server!");

                new Thread(new Server(socket, sessionTokens)).start();
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * Runnable interface implementation - for threaded connections
     */
    public void run() {
        try {
            ObjectInputStream objInputStream = new ObjectInputStream(currentSocket.getInputStream());
            Request request = (Request) objInputStream.readObject();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(currentSocket.getOutputStream());
            objectOutputStream.writeObject(evaluateRequest(request, con));

            currentSocket.close();
        } catch (Exception e) {

        }
    }

    /**
     * Gets the database connection properties from a file and returns them
     * @return Returns a properties object with the database properties
     */
    public static Properties getDBProperties() {
        try {
            Properties properties = new Properties();

            String propFileName = "db.props";

            InputStream inputStream = Server.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            return properties;
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    /**
     * Gets the server port from a file and returns it
     * @return returns the port retrieved from the server.props file
     */
    public static int getServerPort() {
        try {
            Properties properties = new Properties();

            String propFileName = "../server.props";

            InputStream inputStream = Server.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            return Integer.parseInt(properties.getProperty("port"));
        } catch (Exception e) {
            System.out.println(e);
        }

        return 0;
    }

    /**
     * Method for evaluating requests - takes a Request object (from the ObjectInputStream) and a database connection
     * and evaluates the request according to its type. It then returns an Object based on the result.
     *
     * @param request The ambiguous request to be evaluated
     * @param con Database connection
     * @return Returns the result of the request evaluation
     */
    public static Object evaluateRequest(Request request, Connection con){
        try {
            // If current billboard request
            if (request.getClass() == CurrentBillboardRequest.class) {
                Object obj = Evaluate.EvaluateCurrentBillboard(con);

                if (obj.getClass() == Billboard.class) {
                    return ((Billboard) obj).getXml();
                } else {
                    return obj;
                }
            }
            // If login request
            else if (request.getClass() == LoginRequest.class) {
                Object obj = Evaluate.EvaluateLogin(con, (LoginRequest) request);

                // Add to session token list if object type is session token
                if (obj.getClass() == SessionToken.class) {
                    sessionTokens.add((SessionToken) obj);

                    return ((SessionToken) obj);
                }

                return obj;
            }
            // If list billboards request
            else if (request.getClass() == ListBillboardsRequest.class) {
                ListBillboardsRequest billboardsRequest = (ListBillboardsRequest) request;
                // Check session token
                if (checkSessionToken(billboardsRequest.getSessionToken())) {
                    return Evaluate.EvaluateListBillboards(con);
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If get billboard info request
            else if (request.getClass() == GetBillboardRequest.class) {
                GetBillboardRequest getBillboardRequest = (GetBillboardRequest) request;
                // Check session token
                if (checkSessionToken(getBillboardRequest.getSessionToken())) {
                    return Evaluate.EvaluateGetBillboard(con, getBillboardRequest.getBillboardName());
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If create/edit billboard request
            else if (request.getClass() == CreateEditBillboardRequest.class) {
                CreateEditBillboardRequest actualRequest = (CreateEditBillboardRequest) request;

                // Grab actual session token & check if valid
                if (checkSessionToken(actualRequest.getSessionToken())) {
                    SessionToken sessionToken = getSessionToken(actualRequest.getSessionToken());

                    // Figure out what permissions are required
                    LinkedList<String> permissionsRequired = new LinkedList<String>();

                    // Check if billboard exists
                    Object obj = Evaluate.EvaluateGetBillboard(con, actualRequest.getBillboardName());

                    if (obj.getClass() == Billboard.class) {
                        Billboard billboard = (Billboard) obj;

                        if (!billboard.getCreatorName().equals(sessionToken.getUserName()) || (Evaluate.EvaluateCurrentBillboard(con).getClass() == Billboard.class && (Billboard) Evaluate.EvaluateCurrentBillboard(con) == billboard)) {
                            permissionsRequired.add("Edit Billboard");
                        } else {
                            permissionsRequired.add("Create Billboard");
                        }
                    }
                    // If not create it
                    else {
                        permissionsRequired.add("Create Billboard");
                    }

                    // Check if user has required permissions
                    if (checkPermissions(con, sessionToken, permissionsRequired)) {
                        // Run insert or update
                        return Evaluate.EvaluateCreateEditBillboard(con, new Billboard(actualRequest.getBillboardName(), actualRequest.getBillboardContents(), sessionToken.getUserName()));
                    } else {
                        return new ErrorMessage("Insufficient permissions");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If delete billboard request
            else if (request.getClass() == DeleteBillboardRequest.class) {
                DeleteBillboardRequest deleteBillboardRequest = (DeleteBillboardRequest) request;

                // Grab actual session token & check if valid
                if (checkSessionToken(deleteBillboardRequest.getSessionToken())) {
                    SessionToken sessionToken = getSessionToken(deleteBillboardRequest.getSessionToken());

                    // Figure out what permissions are required
                    LinkedList<String> permissionsRequired = new LinkedList<String>();

                    // Check if billboard exists
                    Object obj = Evaluate.EvaluateGetBillboard(con, deleteBillboardRequest.getBillboardName());

                    // If it does - check what permissions are required
                    if (obj.getClass() == Billboard.class) {
                        Billboard billboard = (Billboard) obj;

                        // If not creator or creator and it is currently scheduled, 'Edit Billboard' required, otherwise 'Create Billboard'
                        if (!billboard.getCreatorName().equals(sessionToken.getUserName()) || (Evaluate.EvaluateCurrentBillboard(con).getClass() == Billboard.class && (Billboard) Evaluate.EvaluateCurrentBillboard(con) == billboard)) {
                            permissionsRequired.add("Edit Billboard");
                        } else {
                            permissionsRequired.add("Create Billboard");
                        }

                        // Check if user has required permissions
                        if (checkPermissions(con, sessionToken, permissionsRequired)) {
                            // Run insert or update
                            return Evaluate.EvaluateDeleteBillboard(con, billboard.getName());
                        } else {
                            return new ErrorMessage("Insufficient permissions");
                        }
                    }
                    // If not return the error
                    else {
                        return (ErrorMessage) obj;
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If view schedule request
            else if (request.getClass() == ViewScheduleRequest.class) {
                ViewScheduleRequest scheduleRequest = (ViewScheduleRequest) request;
                // Check session token
                if (checkSessionToken(scheduleRequest.getSessionToken())) {
                    // Check permissions
                    LinkedList<String> permissions = new LinkedList<String>();
                    permissions.add("Schedule Billboard");

                    if (checkPermissions(con, getSessionToken(scheduleRequest.getSessionToken()), permissions)) {
                        return Evaluate.EvaluateViewSchedule(con);
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If schedule billboard request
            else if (request.getClass() == ScheduleBillboardRequest.class) {
                ScheduleBillboardRequest scheduleRequest = (ScheduleBillboardRequest) request;
                // Check session token
                if (checkSessionToken(scheduleRequest.getSessionToken())) {
                    // Check permissions
                    LinkedList<String> permissions = new LinkedList<String>();
                    permissions.add("Schedule Billboard");

                    if (checkPermissions(con, getSessionToken(scheduleRequest.getSessionToken()), permissions)) {
                        return Evaluate.EvaluateScheduleBillboard(con, scheduleRequest, getSessionToken(scheduleRequest.getSessionToken()).getUserName());
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If remove billboard from schedule request
            else if (request.getClass() == RemoveFromScheduleRequest.class) {
                RemoveFromScheduleRequest removeFromScheduleRequest = (RemoveFromScheduleRequest) request;
                // Check session token
                if (checkSessionToken(removeFromScheduleRequest.getSessionToken())) {
                    // Check requester has 'Schedule Billboard' permission
                    LinkedList<String> userPermissions = new LinkedList<>();
                    userPermissions.add("Schedule Billboard");

                    if (checkPermissions(con, getSessionToken(removeFromScheduleRequest.getSessionToken()), userPermissions)) {
                        // Evaluate and run SQL - return result (either ErrorMessage or list of users
                        return Evaluate.EvaluateRemoveFromSchedule(con, removeFromScheduleRequest);
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If list users request
            else if (request.getClass() == ListUsersRequest.class) {
                ListUsersRequest listUsersRequest = (ListUsersRequest) request;
                // Check session token
                if (checkSessionToken(listUsersRequest.getSessionToken())) {
                    // Check requester has 'Edit Users' permission
                    LinkedList<String> userPermissions = new LinkedList<>();
                    userPermissions.add("Edit Users");

                    if (checkPermissions(con, getSessionToken(listUsersRequest.getSessionToken()), userPermissions)) {
                        // Evaluate and run SQL - return result (either ErrorMessage or list of users
                        return Evaluate.EvaluateListUsers(con);
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If create user request
            else if (request.getClass() == CreateUserRequest.class) {
                CreateUserRequest userRequest = (CreateUserRequest) request;
                // Check session token
                if (checkSessionToken(userRequest.getSessionToken())) {
                    // Check permissions
                    LinkedList<String> permissions = new LinkedList<String>();
                    permissions.add("Edit Users");

                    if (checkPermissions(con, getSessionToken(userRequest.getSessionToken()), permissions)) {
                        return Evaluate.EvaluateCreateUser(con, userRequest);
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If get permissions request
            else if (request.getClass() == GetUserPermissionsRequest.class) {
                GetUserPermissionsRequest getUserPermissionsRequest = (GetUserPermissionsRequest) request;
                // Check session token
                if (checkSessionToken(getUserPermissionsRequest.getSessionToken())) {
                    // Check permissions
                    LinkedList<String> permissions = new LinkedList<String>();

                    // Check if checking own permissions
                    if (!getSessionToken(getUserPermissionsRequest.getSessionToken()).getUserName().equals(getUserPermissionsRequest.getUserName())) {
                        permissions.add("Edit Users");
                    }

                    // Check permissions and return requested users permissions
                    if (checkPermissions(con, getSessionToken(getUserPermissionsRequest.getSessionToken()), permissions)) {
                        return Evaluate.EvaluateGetPermissions(con, getUserPermissionsRequest);
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If set user permissions request
            else if (request.getClass() == SetUserPermissionsRequest.class) {
                SetUserPermissionsRequest setPermissionsRequest = (SetUserPermissionsRequest) request;
                // Check Session Token
                if (checkSessionToken(setPermissionsRequest.getSessionToken())) {
                    // Check for Edit Users permission
                    LinkedList<String> permissions = new LinkedList<>();
                    permissions.add("Edit Users");

                    if (checkPermissions(con, getSessionToken(setPermissionsRequest.getSessionToken()), permissions)) {
                        return Evaluate.EvaluateSetUserPermissions(con, setPermissionsRequest);
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If set user password request
            else if (request.getClass() == SetUserPasswordRequest.class) {
                SetUserPasswordRequest setUserPasswordRequest = (SetUserPasswordRequest) request;
                // Check Session token
                if (checkSessionToken(setUserPasswordRequest.getSessionToken())) {
                    LinkedList<String> userPermissions = new LinkedList<>();
                    // Decide what permissions are required
                    if (setUserPasswordRequest.getUserName() == getSessionToken(setUserPasswordRequest.getSessionToken()).getUserName()) {
                        // If setting own password - no required permissions
                    } else {
                        // Else 'Edit Users' required
                        userPermissions.add("Edit Users");
                    }

                    // Check permissions
                    if (checkPermissions(con, getSessionToken(setUserPasswordRequest.getSessionToken()), userPermissions)) {
                        // Evaluate request and return result
                        return Evaluate.EvaluateSetUserPassword(con, setUserPasswordRequest);
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If delete user
            else if (request.getClass() == DeleteUserRequest.class) {
                DeleteUserRequest deleteUserRequest = (DeleteUserRequest) request;
                // Check Session token
                if (checkSessionToken(deleteUserRequest.getSessionToken())) {
                    LinkedList<String> userPermissions = new LinkedList<>();
                    // Decide what permissions are required
                    if (deleteUserRequest.getUserName().equals(getSessionToken(deleteUserRequest.getSessionToken()).getUserName())) {
                        // If deleting own account - throw error (shouldn't need to - this should be checked at the panel level)
                        return new ErrorMessage("Can't delete own account!");
                    } else {
                        // Else 'Edit Users' required
                        userPermissions.add("Edit Users");
                    }

                    // Check permissions
                    if (checkPermissions(con, getSessionToken(deleteUserRequest.getSessionToken()), userPermissions)) {
                        // Delete deleted users session token if it exists
                        deleteSessionToken(null, deleteUserRequest.getUserName());
                        // Evaluate request and return result
                        return Evaluate.EvaluateDeleteUser(con, deleteUserRequest, getSessionToken(deleteUserRequest.getSessionToken()).getUserName());
                    } else {
                        return new ErrorMessage("Insufficient permissions!");
                    }
                } else {
                    return new ErrorMessage("Invalid session token!");
                }
            }
            // If logout
            else if (request.getClass() == LogOutRequest.class) {
                LogOutRequest logOutRequest = (LogOutRequest) request;

                // Delete session token
                deleteSessionToken(logOutRequest.getSessionToken(), null);

                return true;
            }
            return new ErrorMessage("Request not recognised!");
        }
        catch(Exception e){
            return new ErrorMessage(e.getMessage());
        }
    }

    /**
     * Gets the session token based on the token value - returns null if it can't find it
     * @param token The token value of the session token to be retrieved
     * @return Returns a session token object if it exists - otherwise null
     */
    private static SessionToken getSessionToken(String token) {
        // Gets a session token object from the string
        for (SessionToken sessionToken : sessionTokens) {
            if (sessionToken.getSessionToken().equals(token)) {
                return sessionToken;
            }
        }

        return null;
    }

    /**
     * Gets the session token based on the user name associated with the token - returns null if it can't find it
     * @param name The name value of the session token to be retrieved
     * @return Returns a session token object if it exists - otherwise null
     */
    private static SessionToken getSessionTokenByName(String name) {
        // Gets a session token object from the string
        for (SessionToken sessionToken : sessionTokens) {
            if (sessionToken.getUserName().equals(name)) {
                return sessionToken;
            }
        }

        return null;
    }

    /**
     * Checks the session token exists and is valid - removes it and returns false if it is expired or nonexistent - otherwise returns true
     * @param token The token value of the token to be checked
     * @return Returns true if the token is valid - otherwise returns false
     */
    private static boolean checkSessionToken(String token) {
        // Check sessionTokens list for token and update last used if found
        SessionToken sessionToken = getSessionToken(token);

        if (sessionToken != null && sessionToken.getLastUsed().isAfter(LocalDateTime.now().minusHours(24))) {
            sessionToken.setLastUsed(LocalDateTime.now());
            return true;
        }
        else if (sessionToken != null && sessionToken.getLastUsed().isBefore(LocalDateTime.now().minusHours(24))){
            // If session token is older than 24hrs - delete it from list (conserve memory)
            sessionTokens.remove(sessionToken);
        }

        // Return false if not found in list
        return false;
    }

    /**
     * Deletes a session token from memory if it exists.
     * @param token Token value of the session token to delete
     * @param userName Name value of the session token to delete - can be used in place of the token value
     */
    private static void deleteSessionToken(String token, String userName) {
        SessionToken sessionToken;

        // Grab session token if it exists - check via token or username
        if ((token == null || !token.equals("")) && (userName != null || !userName.equals(""))){
            sessionToken = getSessionTokenByName(userName);
        }
        else {
            sessionToken = getSessionToken(token);
        }

        // Remove it if it exists
        if (sessionToken != null){
            sessionTokens.remove(sessionToken);
        }
    }

    /**
     * Checks if a given session token (or user) has the permissions given in the list.
     * Returns true if the user has permissions, false if not
     * @param con Database connection
     * @param token Session token object for user permissions are being checked for
     * @param requiredPermissions List of permissions required of the user
     * @return Returns true if user has permissions - otherwise returns false
     */
    private static boolean checkPermissions(Connection con, SessionToken token, LinkedList<String> requiredPermissions){
        try {
            // Check if user associated with session token has required permissions
            // Check permissions table to see if required permissions exist
            if (requiredPermissions.size() > 0) {
                Statement statement = con.createStatement();

                String sql = String.format("SELECT * FROM user_permissions WHERE `UserName` = \"%s\" AND", token.getUserName());

                for (String permission : requiredPermissions) {
                    if (permission != requiredPermissions.getFirst()) {
                        sql = sql.concat(" OR");
                    } else {
                        sql = sql.concat(" (");
                    }

                    sql = sql.concat(String.format(" `PermissionName` = \"%s\"", permission));

                    if (permission == requiredPermissions.getLast()) {
                        sql = sql.concat(");");
                    }
                }

                ResultSet userResult = statement.executeQuery(sql);

                if (userResult.last()) {
                    if (userResult.getRow() == requiredPermissions.size()) {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }
}

