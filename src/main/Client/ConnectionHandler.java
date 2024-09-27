package main.Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ConnectionHandler {
    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;


    public static ConnectionHandler instance;

    public ConnectionHandler(){
        instance = this;
    }
    public void ConnectToServer(String ip, int port, String name){
        if(socket == null){
                try{
                socket = new Socket(ip, port);
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());
                WriteMessage(name);

                Thread commandHandlerThread = new Thread(new CommandHandler());
                commandHandlerThread.start();
                WindowManager.instance.frame.getContentPane().removeAll();
                WindowManager.instance.frame.repaint();
                WindowManager.instance.ScreenShareConnectionMenu();
            }
            catch(IOException e){
                WindowManager.instance.ShowErrorMessage("Es konnte keine Verbindung zum Server aufgebaut werden", "Verbindungsprobleme");
                socket = null;
                return;
            }
            catch(NullPointerException e){
                    return;
            }

        }
        else{
            return;
        }

    }

    synchronized public void CloseConnection(){
        if (socket != null){
            WriteMessage("closeConnection");
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public String[] GetAvailableClients(){

        WriteMessage("getClients");
        String availableClients;
        ArrayList<String> clientsNames = new ArrayList<>();
        try {
            availableClients = in.readUTF();
        } catch (Exception e) {
            WindowManager.resetToConnectMenu();
            return null;
        }

        return availableClients.split("\\|");
    }


    public void WriteMessage(String message) {
        try {
            if (out != null) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {

            WindowManager.instance.ShowErrorMessage(
                    "Lost connection to server",
                    "Connection Error"
            );
            WindowManager.resetToConnectMenu();
        }

    }


}
