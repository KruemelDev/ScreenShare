package com.kruemel.screenshare.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

import static com.kruemel.screenshare.dto.Util.dataToJson;

public class ConnectionHandler {
    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;


    public String[] availableClients = new String[0];
    public static ConnectionHandler instance;

    public volatile boolean screenShare = false;
    public String base64ImagePiece = "";

    private int fps = 30;
    private float quality = 0.5f;


    public int getFps() {
        return fps;
    }

    public float getQuality() {
        return quality;
    }


    public ConnectionHandler(){
        instance = this;
    }
    public void ConnectToServer(String ip, int port, String name){
        if(socket == null){
            try{
                socket = new Socket(ip, port);
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());

                WriteMessage(dataToJson("name", name));

                Thread commandHandlerThread = new Thread(new CommandHandler());
                commandHandlerThread.start();
                WindowManager.instance.frame.getContentPane().removeAll();
                WindowManager.instance.frame.repaint();
                RequestAvailableClients();
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
            WriteMessage(dataToJson("closeConnection"));
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    public void StopWatchingScreen(){
        WriteMessage(dataToJson("stopWatching"));
    }
    public void ScreenShareStop(){
        WriteMessage(dataToJson("sharedScreenStop"));
        screenShare = false;
    }
    public void ScreenShareStart(){
        ShareScreen screenShare = new ShareScreen(quality, fps);
        Thread shareScreenThread = new Thread(screenShare);
        this.screenShare = true;
        System.out.println("start screen share");
        shareScreenThread.start();
    }

    public void SendScreenShareAcception(String name){
        WriteMessage(dataToJson("acceptScreenShare", name));
    }

    public void RequestScreenShare(String target) {
        WriteMessage(dataToJson("requestScreenShare", target));
    }

    public void RequestAvailableClients(){
        WriteMessage(dataToJson("getClients"));
    }

    public void ChangeSettings(int fps, float quality){
        this.fps = fps;
        this.quality = quality;
        if (ShareScreen.instance != null){
            ShareScreen.instance.ChangeScreenShareSettings(this.fps, this.quality);
        }
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
            ConnectionHandler.instance.screenShare = false;
            Client.resetToConnectMenu();
        }

    }


}
