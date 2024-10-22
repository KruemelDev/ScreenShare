package com.kruemel.screenshare.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.util.Base64;
import java.util.Objects;

public class ShareScreen extends Thread {

    private volatile float quality;
    private volatile int fps;

    public static ShareScreen instance;
    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        if (quality > 1.0f) {
            this.quality = 1.0f;
        }
        else if (quality < 0.0f) {
            this.quality = 0.1f;
        }
        else{
            this.quality = quality;
        }


    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        if(fps <= 30 && fps > 0)this.fps = fps;
        if (fps > 30) this.fps = 30;
        if(fps <= 0) this.fps = 1;
    }



    public ShareScreen(float quality, int fps){
        setQuality(quality);
        setFps(fps);
        instance = this;
    }
    public void run(){
        while (ConnectionHandler.instance.screenShare){
            Share();
        }

    }

    public void Share(){
        try {

            BufferedImage screenshot = captureScreenshot();
            BufferedImage mouseScreenshot = placeMouseOnScreenShot(screenshot);
            float quality = getQuality();
            String base64String = compressAndConvertToBase64(mouseScreenshot, quality);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json;

            int chunkSize = 1024;
            for (int i = 0; i < base64String.length(); i += chunkSize) {
                String chunk = base64String.substring(i, Math.min(i + chunkSize, base64String.length()));
                Packet screenPacket;
                try {
                    screenPacket = new Packet("getScreen", chunk);
                    json = ow.writeValueAsString(screenPacket);

                } catch(JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                ConnectionHandler.instance.WriteMessage(json);
            }

            Packet imageCompletePacket = new Packet("getScreen", "fullImage");
            try{
                json = ow.writeValueAsString(imageCompletePacket);
            } catch(JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            ConnectionHandler.instance.WriteMessage(json);
            Thread.sleep(Math.round(1000f/fps));

        } catch (AWTException | IOException e) {
            ConnectionHandler.instance.screenShare = false;
            e.printStackTrace();
            //Packet packet = new Packet();
            //TODO logik fürs beenden von screenshare
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage placeMouseOnScreenShot(BufferedImage screenshot) {

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        int mouseX = (int) mouseLocation.getX();
        int mouseY = (int) mouseLocation.getY();

        Graphics2D graphics = screenshot.createGraphics();

        Image cursor;
        try {
            cursor = ImageIO.read(Objects.requireNonNull(ShareScreen.class.getResourceAsStream("/redSquare.jpg")));
        } catch (IOException e) {
            throw new RuntimeException("Could not load cursor image", e);
        }

        graphics.drawImage(cursor, mouseX, mouseY, 25, 25, null);
        graphics.dispose();

        return screenshot;
    }


    private static BufferedImage captureScreenshot() throws AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }
    private static String compressAndConvertToBase64(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(byteArrayOutputStream);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();

        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    public void changeScreenShareSettings(float quality, int fps){
        setFps(fps);
        setQuality(quality);
    }


}
