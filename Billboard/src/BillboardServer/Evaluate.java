package BillboardServer;

import Helper.Password;
import Helper.Requests.LoginRequest;
import Helper.Responses.ErrorMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Base64;

public class Evaluate {
    public static Object EvaluateCurrentBillboard(Connection con) throws Exception{
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

    public static Object EvaluateLogin(Connection con, LoginRequest request)throws Exception{
        // make SQL query to get user for a given name
        Statement statement = con.createStatement();

        ResultSet userResult = statement.executeQuery(String.format("SELECT Password FROM users WHERE Name = \"%s\" LIMIT 1", request.getUserName()));

        // If user exists, check password
        if (userResult.next()){
            if (Password.authenticatePassword(request.getHashedPassword(), userResult.getString("Password"))){
                // If password is correct, return session token and store in DB
                // Generate session token
                byte[] randomBytes = new byte[24];
                new SecureRandom().nextBytes(randomBytes);
                String sessionToken = Base64.getUrlEncoder().encodeToString(randomBytes);

                // Update DB with session token details
                statement.executeQuery(String.format("UPDATE users SET SessionToken = \"%s\", TokenLastUsed = \"%s\" WHERE Name = \"%s\"", sessionToken, LocalDateTime.now(), request.getUserName()));

                return sessionToken;
            }
            else{
                return new ErrorMessage("Incorrect password!");
            }
        }
        // Else return error
        else{
            return new ErrorMessage("Incorrect username!");
        }
    }
}
