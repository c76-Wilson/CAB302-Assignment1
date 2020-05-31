package BillboardServer;

import Helper.Password;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

public class Client {
    public static void main(String args[]){
        try {
            LoginRequest request = new LoginRequest("admin", Password.hash("root"));
            Socket socket = new Socket("127.0.0.1", 4444);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(request);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == String.class){
                String sessionToken = (String)obj;

                Socket socket2 = new Socket("127.0.0.1", 4444);

                ScheduleBillboardRequest createEditBillboardRequest = new ScheduleBillboardRequest("Test", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), sessionToken, Duration.ofDays(1));

                output = new ObjectOutputStream(socket2.getOutputStream());

                output.writeObject(createEditBillboardRequest);

                clientInputStream = new ObjectInputStream(socket2.getInputStream());
                Object createObject = clientInputStream.readObject();
            }
            else if (obj.getClass() == ErrorMessage.class){
                System.out.println(((ErrorMessage)obj).getErrorMessage());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
