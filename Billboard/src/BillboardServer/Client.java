package BillboardServer;

import Helper.Password;
import Helper.Requests.CurrentBillboardRequest;
import Helper.Requests.GetBillboardRequest;
import Helper.Requests.LoginRequest;
import Helper.Responses.ErrorMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
                System.out.println((String)obj);
            }
            else if (obj.getClass() == ErrorMessage.class){
                System.out.println(((ErrorMessage)obj).getErrorMessage());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
