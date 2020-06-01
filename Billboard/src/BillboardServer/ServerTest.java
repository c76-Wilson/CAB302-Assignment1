package BillboardServer;

public class ServerTest {
    public static void main(String args[]) {
        Client client = new Client();
        try {
//            TestGetCurrentBillboard();
            String sessionToken = client.TestLogin();
//            TestCreateBillboard(sessionToken);
//            TestScheduleBillboard(sessionToken);
//            TestViewSchedule(sessionToken);
            client.TestCreateUser(sessionToken);
            client.TestSetUserPermissions(sessionToken);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
