package BillboardServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;

public class SetupServer {
    public static void main(String args[]){
        try {
            runServer();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void runServer() throws SQLException, IOException, ClassNotFoundException {
        Properties properties = new Properties();
        try{
            properties = getDBProperties();
        } catch (IOException e) {
            System.out.println(e);
        }

        if (properties != null) {
            Connection con = ConnectToDatabase.connect(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.schema"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));

            int port = getServerPort();

            ServerSocket serverSocket = new ServerSocket(port);

            for(;;){
                Socket socket = serverSocket.accept();

                System.out.println(socket.getInetAddress() + " connected to server!");

                ObjectInputStream objInputStream = new ObjectInputStream(socket.getInputStream());
                Object obj = objInputStream.readObject();
                System.out.println(obj.toString());

                socket.close();
            }
        }
    }

    public static Properties getDBProperties() throws IOException{
        try{
            Properties properties = new Properties();

            String propFileName = "db.props";

            InputStream inputStream = SetupServer.class.getResourceAsStream(propFileName);

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

            InputStream inputStream = SetupServer.class.getResourceAsStream(propFileName);

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
}

