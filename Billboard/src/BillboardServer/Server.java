package BillboardServer;

import Helper.Billboard;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

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
    static List<SessionToken> sessionTokens = new LinkedList<>();

    Server(Socket currentSocket) {
        this.currentSocket = currentSocket;

        Properties properties = new Properties();
        try {
            properties = getDBProperties();
        } catch (IOException e) {
            System.out.println(e);
        }
        this.con = ConnectToDatabase.connect(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.schema"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
    }

    public static void main(String args[]) {
        try {
            runServer();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void runServer() throws Exception {
        port = getServerPort();

        ServerSocket serverSocket = new ServerSocket(port);

        for (; ; ) {
            Socket socket = serverSocket.accept();

            System.out.println(socket.getInetAddress() + " connected to server!");

            new Thread(new Server(socket)).start();
        }
    }

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

    public static Properties getDBProperties() throws IOException {
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

    public static int getServerPort() {
        try {
            Properties properties = new Properties();

            String propFileName = "server.props";

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

    public static Object evaluateRequest(Request request, Connection con) throws Exception {
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

                return ((SessionToken) obj).getSessionToken();
            }

            return obj;
        }
        // If list billboards request
        else if (request.getClass() == ListBillboardsRequest.class) {
            if (checkSessionToken(((ListBillboardsRequest) request).getSessionToken())) {

            } else {
                return new ErrorMessage("Invalid session token!");
            }
        }
        // If get billboard info request
        else if (request.getClass() == GetBillboardRequest.class) {

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
                    Evaluate.EvaluateCreateEditBillboard(con, new Billboard(actualRequest.getBillboardName(), actualRequest.getBillboardContents(), sessionToken.getUserName()));
                } else {
                    return new ErrorMessage("Insufficient permissions");
                }
            } else {
                return new ErrorMessage("Invalid session token!");
            }
        }
        // If delete billboard request
        else if (request.getClass() == DeleteBillboardRequest.class) {

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
                    return Evaluate.EvaluateScheduleBillboard(con, scheduleRequest.getBillboardName(), scheduleRequest.getScheduleTime(), scheduleRequest.getDuration(), scheduleRequest.getRecurring(), getSessionToken(scheduleRequest.getSessionToken()).getUserName());
                } else {
                    return new ErrorMessage("Insufficient permissions!");
                }
            } else {
                return new ErrorMessage("Invalid session token!");
            }
        }
        // If remove billboard from schedule request
        else if (request.getClass() == RemoveFromScheduleRequest.class) {

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
                if (checkPermissions(con, getSessionToken(setUserPasswordRequest.getSessionToken()), userPermissions)){
                    // Evaluate request and return result
                    return Evaluate.EvaluateSetUserPassword(con, setUserPasswordRequest);
                }
                else {
                    return new ErrorMessage("Insufficient permissions!");
                }
            } else {
                return new ErrorMessage("Invalid session token!");
            }
        }
        return null;
    }

    public static SessionToken getSessionToken(String token) {
        // Gets a session token object from the string
        for (SessionToken sessionToken : sessionTokens) {
            if (sessionToken.getSessionToken().equals(token)) {
                return sessionToken;
            }
        }

        return null;
    }

    public static boolean checkSessionToken(String token) {
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

    public static boolean checkPermissions(Connection con, SessionToken token, LinkedList<String> requiredPermissions) throws Exception {
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
}

