package com.kruemel.screenshare.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.util.Base64;

public class ShareScreen extends Thread {

    private volatile float quality;
    private volatile int fps;

    public static ShareScreen instance;

    private ImageWriter imageWriter;
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
        imageWriter = createImageWriter();
    }
    public void run(){
        while (ConnectionHandler.instance.screenShare){
            Share();

        }

    }

    private BufferedImage resizeToFullHD(BufferedImage originalImage) {
        int targetWidth = 1920;
        int targetHeight = 1080;

        if (originalImage.getWidth() <= targetWidth && originalImage.getHeight() <= targetHeight) {
            return originalImage;
        }

        double originalAspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        double targetAspectRatio = (double) targetWidth / targetHeight;

        int newWidth, newHeight;
        if (originalAspectRatio > targetAspectRatio) {
            newWidth = targetWidth;
            newHeight = (int) (targetWidth / originalAspectRatio);
        } else {
            newHeight = targetHeight;
            newWidth = (int) (targetHeight * originalAspectRatio);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }
    private long firstMeasureMilliSeconds;
    private long secondMeasureMilliSeconds;
    private void sendImageInPieces(String base64String){
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
        secondMeasureMilliSeconds = System.currentTimeMillis();
        long distance = secondMeasureMilliSeconds - firstMeasureMilliSeconds;

        int durationBetweenFrames = Math.round((float) 1000/fps);
        if(distance >= durationBetweenFrames){
            ConnectionHandler.instance.WriteMessage(json);
        }
        else{
            try {
                Thread.sleep(durationBetweenFrames - distance);
                ConnectionHandler.instance.WriteMessage(json);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void Share(){
        try {
            firstMeasureMilliSeconds = System.currentTimeMillis();
            BufferedImage screenshot = captureScreenshot();
            BufferedImage mouseScreenshot = placeMouseOnScreenShot(screenshot);
            BufferedImage fullHdImage = resizeToFullHD(mouseScreenshot);

            float quality = getQuality();
            String base64String = compressAndConvertToBase64(fullHdImage, quality);

            sendImageInPieces(base64String);

        } catch (AWTException | IOException e) {
            ConnectionHandler.instance.ScreenShareStop();
            throw new RuntimeException(e);
        }
    }

    private BufferedImage placeMouseOnScreenShot(BufferedImage screenshot) {

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        int mouseX = (int) mouseLocation.getX();
        int mouseY = (int) mouseLocation.getY();

        Graphics2D graphics = screenshot.createGraphics();

        graphics.setColor(new Color(0x3DE83D));
        graphics.fillOval(mouseX, mouseY, 18,18);
        graphics.dispose();

        return screenshot;
    }


    private static BufferedImage captureScreenshot() throws AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }
    private String compressAndConvertToBase64(BufferedImage image, float quality) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(byteArrayOutputStream)) {

            imageWriter.setOutput(ios);
            ImageWriteParam param = imageWriter.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            imageWriter.write(null, new IIOImage(image, null, null), param);

            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new IOException("Error occurred while writing the JPEG image", e);
        }
    }

    private ImageWriter createImageWriter() {
        return ImageIO.getImageWritersByFormatName("jpg").next();
    }

    public void changeScreenShareSettings(float quality, int fps){
        setFps(fps);
        setQuality(quality);
    }


}
