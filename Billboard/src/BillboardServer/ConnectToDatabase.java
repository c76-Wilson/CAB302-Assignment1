package BillboardServer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class ConnectToDatabase {
    public static Connection connect(String url, String schema, String username, String password){
        try{
            // Initialize Drivers
            Class.forName("org.mariadb.jdbc.Driver");

            try {

                //Get Connection to Database - throw error if it doesn't exist
                Connection con = DriverManager.getConnection(
                        url + schema, username, password);

                //Create SQL statements to check if tables exist
                createTablesIfNotExists(con);

                return con;
            }
            catch(Exception e){
                if (e.getMessage().contains("Unknown database")){
                    System.out.println("Could not find database!");
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }

        return null;
    }

    public static void createTablesIfNotExists(Connection con) {
        try {
            //Run Create Table If Not Exists Query
            Statement createTables = con.createStatement();
            createTables.execute("CREATE TABLE IF NOT EXISTS `billboards` (`ID` int(11) NOT NULL,  `Name` varchar(250) NOT NULL,  `XML` text NOT NULL,  `IsDefault` bit(1) NOT NULL DEFAULT b'0',  `CreatorID` int(11) NOT NULL DEFAULT 0,  `ScheduleID` int(11) DEFAULT NULL,  PRIMARY KEY (`ID`));");
            createTables.execute("CREATE TABLE IF NOT EXISTS `schedules` (`ID` int(11) NOT NULL,  `BillboardID` int(11) NOT NULL,  `StartTime` datetime DEFAULT NULL,  `Duration` time DEFAULT NULL,  `RecurringEvery` time DEFAULT NULL,  `CreatorID` int(11) NOT NULL,  PRIMARY KEY (`ID`));");
            createTables.execute("CREATE TABLE IF NOT EXISTS `users` (  `ID` int(11) NOT NULL AUTO_INCREMENT,  `Name` varchar(250) NOT NULL,  `Password` varchar(64) NOT NULL, `SessionToken` varchar(64), `TokenLastUsed` datetime, `Permissions` set('Create Billboard','Edit Billboard','Schedule Billboard','Edit Users','Administrator') NOT NULL,  PRIMARY KEY (`ID`));");
            createTables.execute("ALTER TABLE `billboards` ADD KEY IF NOT EXISTS `Billboard_CreatorID` (`CreatorID`),  ADD KEY IF NOT EXISTS `Billboard_ScheduleID` (`ScheduleID`),  ADD CONSTRAINT `Billboard_CreatorID` FOREIGN KEY IF NOT EXISTS (`CreatorID`) REFERENCES `users` (`ID`),  ADD CONSTRAINT `Billboard_ScheduleID` FOREIGN KEY IF NOT EXISTS (`ScheduleID`) REFERENCES `schedules` (`ID`);");
            createTables.execute("ALTER TABLE `schedules` ADD KEY IF NOT EXISTS `Schedule_BillboardID` (`BillboardID`),  ADD KEY IF NOT EXISTS `Schedule_CreatorID` (`CreatorID`),  ADD CONSTRAINT `Schedule_BillboardID` FOREIGN KEY IF NOT EXISTS (`BillboardID`) REFERENCES `billboards` (`ID`),  ADD CONSTRAINT `Schedule_CreatorID` FOREIGN KEY IF NOT EXISTS (`CreatorID`) REFERENCES `users` (`ID`);");

        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            //Create SuperUser if no users exist
            Statement createUsersTable = con.createStatement();
            ResultSet userResults = createUsersTable.executeQuery("SELECT COUNT(*) FROM users");

            while(userResults.next()) {
                if (userResults.getInt(1) == 0) {
                    String pass = "root";

                    String hashedPass = Password.getSaltedHash(pass);

                    boolean test = Password.check(pass, hashedPass);

                    Statement createSuperUser = con.createStatement();
                    createSuperUser.executeQuery("INSERT INTO users (Name, Password, Permissions) VALUES('admin', '" + hashedPass + ", 'Create Billboard,Edit Billboard,Schedule Billboard,Edit Users,Administrator')");
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
