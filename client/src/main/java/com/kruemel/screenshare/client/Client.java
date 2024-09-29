package com.kruemel.screenshare.client;

public class Client {

    public static void main(String[] args) {
        WindowManager windowManager = new WindowManager("Screen Share", 1280, 720);
        windowManager.initConnectionWindow();
        new ConnectionHandler();
    }

}
