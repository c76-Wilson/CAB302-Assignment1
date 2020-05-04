package BillboardServer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestServer {
    @Test
    void runServerCorrect() {
        Server server = new Server();

        try {
            Server.runServer();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}