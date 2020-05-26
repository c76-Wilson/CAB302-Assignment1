package BillboardServer;

import Helper.Password;
import Helper.Requests.CurrentBillboardRequest;
import Helper.Requests.LoginRequest;
import Helper.Requests.Request;
import Helper.Responses.ErrorMessage;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
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
            Statement statement = con.createStatement();

            ResultSet scheduleResult = statement.executeQuery(String.format("SELECT * FROM schedules WHERE StartTime <= \"%s\" AND (StartTime + Duration) >= \"%s\" ORDER BY ID DESC LIMIT 1", LocalDateTime.now(), LocalDateTime.now()));

            if (scheduleResult.next()){
                ResultSet billboardResult = statement.executeQuery(String.format("SELECT XML FROM billboards WHERE ScheduleID = %d LIMIT 1", scheduleResult.getInt("ID")));

                if (billboardResult.next()){
                    return billboardResult.getString("XML");
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
        // If login request
        else if (request.getClass() == LoginRequest.class){
            // make SQL query to get user for a given name
            LoginRequest loginRequest = (LoginRequest)request;

            Statement statement = con.createStatement();

            ResultSet userResult = statement.executeQuery(String.format("SELECT Password FROM users WHERE Name = \"%s\" LIMIT 1", loginRequest.getUserName()));

            // If user exists, check password
            if (userResult.next()){
                if (Password.authenticatePassword(loginRequest.getHashedPassword(), userResult.getString("Password"))){
                    // If password is correct, return session token and store in DB
                    // Generate session token
                    byte[] randomBytes = new byte[24];
                    new SecureRandom().nextBytes(randomBytes);
                    String sessionToken = Base64.getUrlEncoder().encodeToString(randomBytes);

                    System.out.println(sessionToken);

                    statement.executeQuery(String.format("UPDATE users SET SessionToken = \"%s\" WHERE Name = \"%s\"", sessionToken, loginRequest.getUserName()));

                    return sessionToken;
                }
            }
            // Else return error
            else{
                return new ErrorMessage("Incorrect username!");
            }
        }
        return null;
    }
}

