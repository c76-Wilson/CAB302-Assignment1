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

public class Server {

    static int port;
    static List<SessionToken> sessionTokens;

    public static void main(String args[]){
        try {
            runServer();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void runServer() throws Exception {
        Properties properties = new Properties();
        try{
            properties = getDBProperties();
        } catch (IOException e) {
            System.out.println(e);
        }

        if (properties != null) {
            Connection con = ConnectToDatabase.connect(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.schema"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));

            port = getServerPort();

            ServerSocket serverSocket = new ServerSocket(port);

            // Assign list of session tokens
            sessionTokens = new LinkedList<>();

            for(;;){
                Socket socket = serverSocket.accept();

                System.out.println(socket.getInetAddress() + " connected to server!");

                ObjectInputStream objInputStream = new ObjectInputStream(socket.getInputStream());
                Request request = (Request)objInputStream.readObject();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(evaluateRequest(request, con));

                socket.close();
            }
        }
    }

    public static Properties getDBProperties() throws IOException{
        try{
            Properties properties = new Properties();

            String propFileName = "db.props";

            InputStream inputStream = Server.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            return properties;
        }
        catch (Exception e){
            System.out.println(e);
        }

        return null;
    }

    public static int getServerPort() throws IOException{
        try{
            Properties properties = new Properties();

            String propFileName = "server.props";

            InputStream inputStream = Server.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            return Integer.parseInt(properties.getProperty("port"));
        }
        catch (Exception e){
            System.out.println(e);
        }

        return 0;
    }

    public static Object evaluateRequest(Request request, Connection con) throws Exception {
        // If current billboard request
        if (request.getClass() == CurrentBillboardRequest.class){
            Object obj = Evaluate.EvaluateCurrentBillboard(con);

            if (obj.getClass() == Billboard.class) {
                return ((Billboard)obj).getXml();
            }
            else{
                return obj;
            }
        }
        // If login request
        else if (request.getClass() == LoginRequest.class){
            Object obj = Evaluate.EvaluateLogin(con, (LoginRequest)request);

            // Add to session token list if object type is session token
            if (obj.getClass() == SessionToken.class){
                sessionTokens.add((SessionToken)obj);

                return ((SessionToken)obj).getSessionToken();
            }

            return obj;
        }
        // If list billboards request
        else if (request.getClass() == ListBillboardsRequest.class){
            if (checkSessionToken(((ListBillboardsRequest)request).getSessionToken())){

            }
            else{
                return new ErrorMessage("Invalid session token!");
            }
        }
        // If get billboard info request
        else if (request.getClass() == GetBillboardRequest.class){

        }
        // If create/edit billboard request
        else if (request.getClass() == CreateEditBillboardRequest.class){
            CreateEditBillboardRequest actualRequest = (CreateEditBillboardRequest)request;

            // Grab actual session token & check if valid
            if (checkSessionToken(actualRequest.getSessionToken())) {
                SessionToken sessionToken = getSessionToken(actualRequest.getSessionToken());

                // Figure out what permissions are required
                LinkedList<String> permissionsRequired = new LinkedList<String>();

                // Check if billboard exists
                Object obj = Evaluate.EvaluateGetBillboard(con, actualRequest.getBillboardName());

                if (obj.getClass() == Billboard.class) {
                    Billboard billboard = (Billboard) obj;

                    if (!billboard.getCreatorName().equals(sessionToken.getUserName()) || (Evaluate.EvaluateCurrentBillboard(con).getClass() == Billboard.class && (Billboard) Evaluate.EvaluateCurrentBillboard(con) == billboard)){
                        permissionsRequired.add("Edit Billboard");
                    }
                    else{
                        permissionsRequired.add("Create Billboard");
                    }
                }
                // If not create it
                else {
                    permissionsRequired.add("Create Billboard");
                }

                // Check if user has required permissions
                if (checkPermissions(con, sessionToken, permissionsRequired)){
                    // Run insert or update
                    Evaluate.EvaluateCreateEditBillboard(con, new Billboard(actualRequest.getBillboardName(), actualRequest.getBillboardContents(), sessionToken.getUserName()));
                }
                else{
                    return new ErrorMessage("Insufficient permissions");
                }
            }
        }
        // If delete billboard request
        else if (request.getClass() == DeleteBillboardRequest.class){

        }
        // If view schedule request
        else if (request.getClass() == ViewScheduleRequest.class){

        }
        return null;
    }

    public static SessionToken getSessionToken(String token){
        // Gets a session token object from the string
        for (SessionToken sessionToken : sessionTokens){
            String tokenen = sessionToken.getSessionToken();
            if (sessionToken.getSessionToken().equals(token)){
                return sessionToken;
            }
        }

        return null;
    }

    public static boolean checkSessionToken(String token){
        // Check sessionTokens list for token and update last used if found
        SessionToken sessionToken = getSessionToken(token);

        if (sessionToken != null && (sessionToken.getLastUsed().isAfter(LocalDateTime.now().minusHours(24)))){
            sessionToken.setLastUsed(LocalDateTime.now());
            return true;
        }

        // Return false if not found in list
        return false;
    }

    public static boolean checkPermissions(Connection con, SessionToken token, LinkedList<String> requiredPermissions) throws Exception{
        // Check if user associated with session token has required permissions
        // Check permissions table to see if required permissions exist
        Statement statement = con.createStatement();

        String sql = String.format("SELECT * FROM user_permissions WHERE `UserName` = \"%s\" AND", token.getUserName());

        for (String permission : requiredPermissions){
            if (permission != requiredPermissions.getFirst()){
                sql = sql.concat(" OR");
            }
            else {
                sql = sql.concat(" (");
            }

            sql = sql.concat(String.format(" `PermissionName` = \"%s\"", permission));

            if (permission == requiredPermissions.getLast()){
                sql = sql.concat(");");
            }
        }

        ResultSet userResult = statement.executeQuery(sql);

        if (userResult.last()){
            if (userResult.getRow() == requiredPermissions.size()) {
                return true;
            }
        }

        return false;
    }
}

