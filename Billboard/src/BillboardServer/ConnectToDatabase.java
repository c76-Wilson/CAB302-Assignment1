package BillboardServer;

import Helper.Password;
import java.sql.*;

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
            createTables.execute("CREATE TABLE IF NOT EXISTS `billboards` (`Name` varchar(256) NOT NULL,  `XML` longtext NOT NULL,  `CreatorName` varchar(256) NOT NULL,  `ScheduleID` int(11) DEFAULT NULL,  PRIMARY KEY (`Name`));");
            createTables.execute("CREATE TABLE IF NOT EXISTS `schedules` (`ID` int(11) NOT NULL AUTO_INCREMENT,  `BillboardName` varchar(256) NOT NULL,  `StartTime` datetime NOT NULL, `Duration` varchar(50) NOT NULL,  `RecurringEvery` varchar(50) DEFAULT NULL,  `CreatorName` varchar(256) NOT NULL,  PRIMARY KEY (`ID`));");
            createTables.execute("CREATE TABLE IF NOT EXISTS `users` (`Name` varchar(256) NOT NULL,  `Password` varchar(256) NOT NULL,  PRIMARY KEY (`Name`));");
            createTables.execute("CREATE TABLE IF NOT EXISTS `permissions` (`Name` varchar(256) NOT NULL, PRIMARY KEY (`Name`));");

            // Create permissions if they don't exist
            ResultSet permissionResults = createTables.executeQuery("SELECT * FROM permissions");
            if (!permissionResults.next()){
                createTables.execute("INSERT INTO `permissions` (Name) VALUES('Create Billboard'), ('Edit Billboard'), ('Schedule Billboard'), ('Edit Users');");
            }

            createTables.execute("CREATE TABLE IF NOT EXISTS `user_permissions` (`UserName` varchar(256) NOT NULL, `PermissionName` varchar(256) NOT NULL);");
            createTables.execute("ALTER TABLE `user_permissions` ADD KEY IF NOT EXISTS `UserPermission_UserName` (`UserName`), ADD KEY IF NOT EXISTS `UserPermission_PermissionName` (`PermissionName`), ADD CONSTRAINT `UserPermission_UserName` FOREIGN KEY IF NOT EXISTS (`UserName`) REFERENCES `users` (`Name`), ADD CONSTRAINT `UserPermission_PermissionName` FOREIGN KEY IF NOT EXISTS (`PermissionName`) REFERENCES `permissions` (`Name`);");
            createTables.execute("ALTER TABLE `billboards` ADD KEY IF NOT EXISTS `Billboard_CreatorName` (`CreatorName`),  ADD KEY IF NOT EXISTS `Billboard_ScheduleID` (`ScheduleID`),  ADD CONSTRAINT `Billboard_CreatorName` FOREIGN KEY IF NOT EXISTS (`CreatorName`) REFERENCES `users` (`Name`),  ADD CONSTRAINT `Billboard_ScheduleID` FOREIGN KEY IF NOT EXISTS (`ScheduleID`) REFERENCES `schedules` (`ID`);");
            createTables.execute("ALTER TABLE `schedules` ADD KEY IF NOT EXISTS `Schedule_BillboardName` (`BillboardName`),  ADD KEY IF NOT EXISTS `Schedule_CreatorName` (`CreatorName`),  ADD CONSTRAINT `Schedule_BillboardName` FOREIGN KEY IF NOT EXISTS (`BillboardName`) REFERENCES `billboards` (`Name`),  ADD CONSTRAINT `Schedule_CreatorName` FOREIGN KEY IF NOT EXISTS (`CreatorName`) REFERENCES `users` (`Name`);");

        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            //Create SuperUser if no users exist
            Statement createUsersTable = con.createStatement();
            ResultSet userResults = createUsersTable.executeQuery("SELECT COUNT(*) FROM users");

            while(userResults.next()) {
                if (userResults.getInt(1) == 0) {
                    String pass = Password.hash("root");

                    String hashedPass = Password.getSaltedHash(pass);

                    boolean test = Password.authenticatePassword(pass, hashedPass);

                    Statement createSuperUser = con.createStatement();
                    createSuperUser.execute("INSERT INTO users (Name, Password) VALUES('admin', '" + hashedPass + "')");
                    createSuperUser.execute("INSERT INTO user_permissions (UserName, PermissionName) VALUES('admin', 'Create Billboard'), ('admin', 'Edit Billboard'), ('admin', 'Schedule Billboard'), ('admin', 'Edit Users')");
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
