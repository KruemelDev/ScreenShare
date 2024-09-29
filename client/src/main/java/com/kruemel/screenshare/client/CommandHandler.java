package com.kruemel.screenshare.client;

import java.io.EOFException;
import java.io.IOException;

import com.kruemel.screenshare.dto.*;

public class CommandHandler implements Runnable {

    @Override
    public void run() {
        String message;
        while (true) {
            try {
                message = ConnectionHandler.instance.in.readUTF();
            } catch (EOFException e){
                WindowManager.resetToConnectMenu();
                break;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            catch (Exception e) {
                WindowManager.resetToConnectMenu();
                return;
            }
            switch (message){
                case "close":
                    WindowManager.resetToConnectMenu();
                    return;
                case "transferScreen":
                    break;
            }
        }
    }


}
