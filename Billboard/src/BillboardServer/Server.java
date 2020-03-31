package BillboardServer;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Server {
    public static void main(String args[]){

        Properties properties = new Properties();
        try{
            properties = getDBProperties();
        } catch (IOException e) {
            System.out.println(e);
        }

        if (properties != null) {
            ConnectToDatabase.connect(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.schema"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
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
}

