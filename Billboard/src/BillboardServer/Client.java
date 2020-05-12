package BillboardServer;

import Helper.Requests.CurrentBillboardRequest;
import Helper.Requests.GetBillboardRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    public static void main(String args[]){
        try {
            CurrentBillboardRequest request = new CurrentBillboardRequest();
            Socket socket = new Socket("127.0.0.1", 4444);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(request);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
