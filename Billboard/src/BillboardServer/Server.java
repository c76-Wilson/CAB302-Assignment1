package BillboardServer;

import java.io.*;
import java.net.*;

public class Server {
    int port;

    public Server(int port){
        this.port = port;
    }

    public void startServer(){
        try (
                ServerSocket serverSocket = new ServerSocket(this.port);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println(serverSocket.getInetAddress().getHostAddress());
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
