package BillboardServer;

import java.sql.*;

public class ConnectToDatabase {
    public static void connect(String url, String schema, String username, String password){
        try{
            // Initialize Drivers
            Class.forName("org.mariadb.jdbc.Driver");

            try {

                //Get Connection to Database - throw error if it doesn't exist
                Connection con = DriverManager.getConnection(
                        url + schema, username, password);

                //Create SQL statements to check if tables exist
                createTablesIfNotExists(con);

                con.close();
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
    }

    public static void createTablesIfNotExists(Connection con) {

        try {
            //Run Create Table If Not Exists Query
            Statement createUsersTable = con.createStatement();
            createUsersTable.execute("CREATE TABLE IF NOT EXISTS `billboards` (`ID` int(11) NOT NULL,  `Name` varchar(250) NOT NULL,  `XML` text NOT NULL,  `IsDefault` bit(1) NOT NULL DEFAULT b'0',  `CreatorID` int(11) NOT NULL DEFAULT 0,  `ScheduleID` int(11) DEFAULT NULL,  PRIMARY KEY (`ID`));");
            createUsersTable.execute("CREATE TABLE IF NOT EXISTS `schedules` (`ID` int(11) NOT NULL,  `BillboardID` int(11) NOT NULL,  `StartTime` datetime DEFAULT NULL,  `Duration` time DEFAULT NULL,  `RecurringEvery` time DEFAULT NULL,  `CreatorID` int(11) NOT NULL,  PRIMARY KEY (`ID`));");
            createUsersTable.execute("CREATE TABLE IF NOT EXISTS `users` (  `ID` int(11) NOT NULL AUTO_INCREMENT,  `Name` varchar(250) NOT NULL,  `Password` varchar(64) NOT NULL,  `Permissions` set('Create Billboard','Edit Billboard','Schedule Billboard','Edit Users','Administrator') NOT NULL,  PRIMARY KEY (`ID`));");
            createUsersTable.execute("ALTER TABLE `billboards` ADD KEY IF NOT EXISTS `Billboard_CreatorID` (`CreatorID`),  ADD KEY IF NOT EXISTS `Billboard_ScheduleID` (`ScheduleID`),  ADD CONSTRAINT `Billboard_CreatorID` FOREIGN KEY IF NOT EXISTS (`CreatorID`) REFERENCES `users` (`ID`),  ADD CONSTRAINT `Billboard_ScheduleID` FOREIGN KEY IF NOT EXISTS (`ScheduleID`) REFERENCES `schedules` (`ID`);");
            createUsersTable.execute("ALTER TABLE `schedules` ADD KEY IF NOT EXISTS `Schedule_BillboardID` (`BillboardID`),  ADD KEY IF NOT EXISTS `Schedule_CreatorID` (`CreatorID`),  ADD CONSTRAINT `Schedule_BillboardID` FOREIGN KEY IF NOT EXISTS (`BillboardID`) REFERENCES `billboards` (`ID`),  ADD CONSTRAINT `Schedule_CreatorID` FOREIGN KEY IF NOT EXISTS (`CreatorID`) REFERENCES `users` (`ID`);");
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            //Create SuperUser if no users exist
            Statement createUsersTable = con.createStatement();
            ResultSet userResults = createUsersTable.executeQuery("SELECT COUNT(*) FROM users");

            while(userResults.next()) {
                if (userResults.getInt(1) == 0) {
                    Statement createSuperUser = con.createStatement();
                    createSuperUser.executeQuery("INSERT INTO users (Name, Password, Permissions) VALUES('admin', PASSWORD('password'), 'Create Billboard,Edit Billboard,Schedule Billboard,Edit Users,Administrator')");
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
