package BillboardServer;

public class ServerTest {
    public static void main(String args[]) {
        Client client = new Client();
        try {
            client.TestGetCurrentBillboard();
            String sessionToken = client.TestLogin();
            client.TestCreateBillboard(sessionToken);
//            client.TestScheduleBillboard(sessionToken);
//            client.TestViewSchedule(sessionToken);
//            client.TestCreateUser(sessionToken);
//            client.TestSetUserPermissions(sessionToken);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
