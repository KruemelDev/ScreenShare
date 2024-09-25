package Server;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(1337);
            Socket client = server.accept();
            DataInputStream in = new DataInputStream(client.getInputStream());
            System.out.println(in.readUTF());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}