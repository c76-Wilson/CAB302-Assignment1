package BillboardServer;

import Helper.Requests.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Server {

    static int port;

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
            return Evaluate.EvaluateCurrentBillboard(con);
        }
        // If login request
        else if (request.getClass() == LoginRequest.class){
            return Evaluate.EvaluateLogin(con, (LoginRequest)request);
        }
        // If list billboards request
        else if (request.getClass() == ListBillboardsRequest.class){

        }
        // If get billboard info request
        else if (request.getClass() == GetBillboardRequest.class){

        }
        // If create/edit billboard request
        else if (request.getClass() == CreateEditBillboardRequest.class){

        }
        // If delete billboard request
        else if (request.getClass() == DeleteBillboardRequest.class){

        }
        // If view schedule request
        else if (request.getClass() == ViewScheduleRequest.class){

        }
        return null;
    }

    public static boolean checkSessionToken(Connection con, String sessionToken) throws Exception{
        // Create query for session token - return true if session token is valid
        Statement statement = con.createStatement();

        ResultSet tokenResult = statement.executeQuery(String.format("SELECT * FROM users WHERE SessionToken = \"%s\" AND TokenLastUsed >= \"%s\" LIMIT 1", sessionToken, LocalDateTime.now().minusHours(24)));

        if (tokenResult.next()){
            return true;
        }
        else{
            return false;
        }
    }
}

