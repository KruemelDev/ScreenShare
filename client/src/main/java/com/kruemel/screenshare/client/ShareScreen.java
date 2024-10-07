package com.kruemel.screenshare.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.util.Base64;

public class ShareScreen extends Thread {


    public void run(){
        while (ConnectionHandler.instance.screenShare){
            Share();
        }

    }

    public void Share(){
        try {

            BufferedImage screenshot = captureScreenshot();
            String base64String = compressAndConvertToBase64(screenshot, 0.5f);

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
            Thread.sleep(200);

        } catch (AWTException | IOException e) {
            ConnectionHandler.instance.screenShare = false;
            e.printStackTrace();
            //Packet packet = new Packet();
            // logik fürs beenden von screenshare
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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


}
