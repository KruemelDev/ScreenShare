package com.kruemel.screenshare.client;

public class Client {

    public static void main(String[] args) {
        WindowManager windowManager = new WindowManager("Screen Share", 1280, 720);
        windowManager.initConnectionWindow();
        new ConnectionHandler();
    }

    public static void resetToConnectMenu(){
        ConnectionHandler.instance.CloseConnection();
        ConnectionHandler.instance.availableClients = new String[0];
        ConnectionHandler.instance.socket = null;
        ConnectionHandler.instance.in = null;
        ConnectionHandler.instance.out = null;
        ConnectionHandler.instance.screenShare = false;
        WindowManager.instance.firstScreenShareFrame = true;
        WindowManager.instance.watchScreenAllow = false;
        WindowManager.instance.frame.getContentPane().removeAll();
        WindowManager.instance.frame.repaint();
        WindowManager.instance.AddConnectionFields();
    }

}
