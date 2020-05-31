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
import java.util.LinkedList;

public class Client {

    public static void main(String args[]) {
        try {
//            TestGetCurrentBillboard();
            String sessionToken = TestLogin();
//            TestCreateBillboard(sessionToken);
//            TestScheduleBillboard(sessionToken);
//            TestViewSchedule(sessionToken);
            TestCreateUser(sessionToken);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void TestGetCurrentBillboard() throws Exception {
        CurrentBillboardRequest request = new CurrentBillboardRequest();
        Socket socket = new Socket("127.0.0.1", 4444);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(request);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        String xml = (String) clientInputStream.readObject();
    }

    private static String TestLogin() throws Exception {
        LoginRequest request = new LoginRequest("admin", Password.hash("root"));
        Socket socket = new Socket("127.0.0.1", 4444);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(request);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInputStream.readObject();

        if (obj.getClass() == String.class) {
            return (String)obj;
        }

        return "";
    }

    private static void TestCreateBillboard(String sessionToken) throws Exception {
        Socket socket = new Socket("127.0.0.1", 4444);

        CreateEditBillboardRequest createEditBillboardRequest = new CreateEditBillboardRequest(sessionToken, "Test", new String(Files.readAllBytes(Paths.get("src\\BillboardServer\\error.xml"))));

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(createEditBillboardRequest);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());

        Object createObject = clientInputStream.readObject();
    }

    private static void TestScheduleBillboard(String sessionToken) throws Exception {
        Socket socket = new Socket("127.0.0.1", 4444);

        ScheduleBillboardRequest scheduleBillboardRequest = new ScheduleBillboardRequest("Test", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), sessionToken);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(scheduleBillboardRequest);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object scheduleObject = clientInputStream.readObject();
    }

    private static void TestViewSchedule(String sessionToken) throws Exception {
        Socket socket = new Socket("127.0.0.1", 4444);

        ViewScheduleRequest viewScheduleRequest = new ViewScheduleRequest(sessionToken);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(viewScheduleRequest);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object viewObject = clientInputStream.readObject();
    }

    private static void TestCreateUser(String sessionToken) throws Exception {
        Socket socket = new Socket("127.0.0.1", 4444);

        LinkedList<String> permissions = new LinkedList<>();

        permissions.add("Create Billboard");
        permissions.add("Edit Billboard");
        permissions.add("Schedule Billboard");
        permissions.add("Edit Users");

        CreateUserRequest createUserRequest = new CreateUserRequest("Test User", permissions, Password.hash("password"), sessionToken);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(createUserRequest);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());

        Object createObject = clientInputStream.readObject();
    }
}
