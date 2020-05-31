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

            TestGetCurrentBillboard();
            TestLoginAndSchedule();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void TestGetCurrentBillboard() throws Exception{
        CurrentBillboardRequest request = new CurrentBillboardRequest();
        Socket socket = new Socket("127.0.0.1", 4444);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(request);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        String xml = (String)clientInputStream.readObject();
    }

    private static void TestLoginAndSchedule() throws Exception{
        LoginRequest request = new LoginRequest("admin", Password.hash("root"));
        Socket socket = new Socket("127.0.0.1", 4444);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(request);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInputStream.readObject();

        if (obj.getClass() == String.class){

            String sessionToken = (String)obj;

            Socket socket1 = new Socket("127.0.0.1", 4444);

            CreateEditBillboardRequest createEditBillboardRequest = new CreateEditBillboardRequest(sessionToken, "Test", new String(Files.readAllBytes(Paths.get("src\\BillboardServer\\error.xml"))));

            output = new ObjectOutputStream(socket1.getOutputStream());

            output.writeObject(createEditBillboardRequest);

            clientInputStream = new ObjectInputStream(socket1.getInputStream());
            Object createObject = clientInputStream.readObject();

            if (obj.getClass() == String.class){
                Socket socket2 = new Socket("127.0.0.1", 4444);

                ScheduleBillboardRequest scheduleBillboardRequest = new ScheduleBillboardRequest("Test", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), sessionToken);

                output = new ObjectOutputStream(socket2.getOutputStream());

                output.writeObject(scheduleBillboardRequest);

                clientInputStream = new ObjectInputStream(socket2.getInputStream());
                Object scheduleObject = clientInputStream.readObject();

                if (obj.getClass() == String.class){
                    Socket socket3 = new Socket("127.0.0.1", 4444);

                    ViewScheduleRequest viewScheduleRequest = new ViewScheduleRequest(sessionToken);

                    output = new ObjectOutputStream(socket3.getOutputStream());

                    output.writeObject(viewScheduleRequest);

                    clientInputStream = new ObjectInputStream(socket3.getInputStream());
                    Object viewObject = clientInputStream.readObject();
                }
                else if (obj.getClass() == ErrorMessage.class){
                    System.out.println(((ErrorMessage)obj).getErrorMessage());
                }
            }
            else if (obj.getClass() == ErrorMessage.class){
                System.out.println(((ErrorMessage)obj).getErrorMessage());
            }


        }
        else if (obj.getClass() == ErrorMessage.class){
            System.out.println(((ErrorMessage)obj).getErrorMessage());
        }
    }
}
