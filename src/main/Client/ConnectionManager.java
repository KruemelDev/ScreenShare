package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class ConnectionManager {
    private Socket serverSocket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;


    public static ConnectionManager instance;

    public ConnectionManager(){
        instance = this;
    }
    public void ConnectToServer(String ip, int port){
        if(serverSocket == null){
            try{
                serverSocket = new Socket(ip, port);
                this.in = new DataInputStream(serverSocket.getInputStream());
                this.out = new DataOutputStream(serverSocket.getOutputStream());
                WriteMessage("HeyHo");
            }
            catch(IOException e){
                WindowManager.instance.ShowErrorMessage("Es konnte keine Verbindung zum Server aufgebaut werden", "Verbindungsprobleme");
                serverSocket = null;
                return;
            }

        }
        else{
            return;
        }

    }
    public void WriteMessage(String message){
        try {
            this.out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
