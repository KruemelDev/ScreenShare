package com.kruemel.screenshare.client;

import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class WindowManager {
    public JFrame frame;
    private String windowName;
    private int width;
    private int height;


    public static WindowManager instance = null;
    public boolean watchScreenAllow = true;

    private boolean firstScreenShareFrame = false;

    private boolean stopWatching = false;

    public WindowManager(String windowName, int width, int height) {
        instance = this;
        this.windowName = windowName;
        this.width = width;
        this.height = height;

    }
    public void initConnectionWindow(){
        frame = new JFrame(this.windowName);
        frame.setSize(new Dimension(width, height));
        frame.setPreferredSize(new Dimension(width, height));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(true);
        //frame.setLayout(new BorderLayout());
        AddConnectionFields();
    }

    public void ShowErrorMessage(String message, String title){
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);

    }

    public void AddConnectionFields(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());
        JTextField ipTextField = new JTextField("IP-Adresse", 1);

        JTextField portTextField = new JTextField("Port",1);

        JTextField nameTextField = new JTextField("Name", 1);

        ipTextField.setPreferredSize(new Dimension(200, 50));
        ipTextField.setMaximumSize(new Dimension(200, 50));
        ipTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(ipTextField);

        portTextField.setPreferredSize(new Dimension(200, 50));
        portTextField.setMaximumSize(new Dimension(200, 50));
        portTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(portTextField);

        nameTextField.setPreferredSize(new Dimension(200, 50));
        nameTextField.setMaximumSize(new Dimension(200, 50));
        nameTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameTextField);

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try{
                    ConnectionHandler.instance.ConnectToServer(ipTextField.getText(), Integer.parseInt(portTextField.getText()), nameTextField.getText());
                }catch (NumberFormatException e){
                    ShowErrorMessage("Port must be an integer", "Port must be an integer");
                }

            }
        });
        panel.add(connectButton);
        panel.add(Box.createVerticalGlue());

        frame.pack();
        frame.add(panel);
        frame.setVisible(true);

    }
    public void ScreenShareConnectionMenu() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel listPanel = new JPanel();
        JList<String> clientNameList = new JList<>(ConnectionHandler.instance.availableClients);
        clientNameList.setPreferredSize(new Dimension(200, 50));
        clientNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(clientNameList);

        JPanel screenShareConnectButtons = new JPanel();
        screenShareConnectButtons.setLayout(new BoxLayout(screenShareConnectButtons, BoxLayout.LINE_AXIS));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ConnectionHandler.instance.RequestAvailableClients();
                ScreenShareConnectionMenu();
            }
        });

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.out.println(clientNameList.getSelectedValue());
                ConnectionHandler.instance.RequestScreenShare(clientNameList.getSelectedValue());
                WindowManager.instance.watchScreenAllow = true;
            }
        });

        screenShareConnectButtons.add(Box.createHorizontalGlue());
        screenShareConnectButtons.add(refreshButton);
        screenShareConnectButtons.add(Box.createHorizontalStrut(10));
        screenShareConnectButtons.add(connectButton);
        screenShareConnectButtons.add(Box.createHorizontalGlue());

        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.PAGE_AXIS));
        verticalPanel.add(listPanel);
        verticalPanel.add(Box.createVerticalStrut(20));
        verticalPanel.add(screenShareConnectButtons);

        JPanel screenShareSettings = new JPanel();
        screenShareSettings.setLayout(new BoxLayout(screenShareSettings, BoxLayout.PAGE_AXIS));

        screenShareSettings.setBorder(new EmptyBorder(30, 0, 0, 40));

        JLabel settingsTitle = new JLabel("Screen Share Settings");
        settingsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        settingsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel fpsLabel = new JLabel("FPS:");
        fpsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField fpsField = new JTextField("30");
        fpsField.setMaximumSize(new Dimension(100, 25));

        JLabel qualityLabel = new JLabel("Quality:");
        qualityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        String[] qualityOptions = {"10%", "30%", "50%", "70%", "100%"};
        JComboBox<String> qualityComboBox = new JComboBox<>(qualityOptions);
        qualityComboBox.setSelectedIndex(2);
        qualityComboBox.setMaximumSize(new Dimension(100, 25));

        JButton applyButton = new JButton("Apply");
        applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String fpsValue = fpsField.getText();
                int fps;
                try {
                    fps = Integer.parseInt(fpsValue);
                }
                catch (NumberFormatException e) {
                    fps = 1;
                }
                String qualityValue = (String) qualityComboBox.getSelectedItem();
                if(qualityValue == null) return;

                float quality = Float.parseFloat(qualityValue.replace("%", "")) / 100;
                if (ShareScreen.instance == null){
                    ConnectionHandler.instance.fps = fps;
                    ConnectionHandler.instance.quality = quality;
                    return;
                }
                ShareScreen.instance.changeScreenShareSettings(quality, fps);
                if(fps > 30) fpsField.setText("30");
                if (fps <= 0) fpsField.setText("1");
            }
        });
        JButton stopScreenShare = new JButton("Stop Sharing");
        stopScreenShare.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopScreenShare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ConnectionHandler.instance.ScreenShareStop();
            }
        });

        screenShareSettings.add(settingsTitle);
        screenShareSettings.add(Box.createVerticalStrut(10));
        screenShareSettings.add(fpsLabel);
        screenShareSettings.add(fpsField);
        screenShareSettings.add(Box.createVerticalStrut(10));
        screenShareSettings.add(qualityLabel);
        screenShareSettings.add(qualityComboBox);
        screenShareSettings.add(Box.createVerticalStrut(20));
        screenShareSettings.add(applyButton);
        if(ConnectionHandler.instance.screenShare){
            screenShareSettings.add(Box.createVerticalStrut(25));
            screenShareSettings.add(stopScreenShare);
        }

        screenShareSettings.add(Box.createVerticalGlue());



        mainPanel.add(verticalPanel, BorderLayout.CENTER);
        mainPanel.add(screenShareSettings, BorderLayout.EAST);

        frame.add(mainPanel);
        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();
    }


    public void ScreenShareDisplay(String base64Image) {
        watchScreenAllow = true;
        stopWatching = false;

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel screenPanel = new JPanel();
        JPanel settingsPanel = new JPanel();


        byte[] imageBytes;
        try{
            imageBytes = Base64.getDecoder().decode(base64Image);
        }
        catch (IllegalArgumentException e){
            return;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
        BufferedImage image;
        try{
            image = ImageIO.read(byteArrayInputStream);
        }
        catch (IOException e){
            watchScreenAllow = false;
            firstScreenShareFrame = false;
            ConnectionHandler.instance.screenShare = false;
            ConnectionHandler.instance.StopWatchingScreen();
            WindowManager.instance.ScreenShareConnectionMenu();
            return;
        }
        if (image == null){
            return;
        }

        double scaleX = (double) frame.getWidth() / (image.getWidth() + 200);
        double scaleY = (double) frame.getHeight() / (image.getHeight() + 200);
        double scale = Math.min(scaleX, scaleY);

        int newImageWidth = (int) (image.getWidth() * scale);
        int newImageHeight = (int) (image.getHeight() * scale);

        Image newImage = image.getScaledInstance(newImageWidth, newImageHeight, Image.SCALE_FAST);
        ImageIcon imageIcon = new ImageIcon(newImage);
        JLabel label = new JLabel(imageIcon);
        screenPanel.removeAll();
        screenPanel.add(label);



        JButton stopButton = new JButton("Stop Watching");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                stopWatching = true;
                watchScreenAllow = false;
                firstScreenShareFrame = false;
                ConnectionHandler.instance.screenShare = false;
                ConnectionHandler.instance.StopWatchingScreen();
                WindowManager.instance.ScreenShareConnectionMenu();
                return;
            }
        });

        if(stopWatching) return;

        settingsPanel.removeAll();
        settingsPanel.add(stopButton);

        mainPanel.add(settingsPanel, BorderLayout.WEST);
        mainPanel.add(screenPanel, BorderLayout.CENTER);

        if (!firstScreenShareFrame) {
            frame.add(mainPanel);
            frame.setVisible(true);
            firstScreenShareFrame = true;
        } else {
            frame.getContentPane().removeAll();
            frame.add(mainPanel);
        }

        frame.revalidate();
        frame.repaint();


    }

    public void RequestForTransferScreenPopUp(String name){
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Do you want allow Screen Sharing from " + name + "?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );


        if (result == JOptionPane.YES_OPTION) {
            ConnectionHandler.instance.SendScreenShareAcception(name);
            ConnectionHandler.instance.ScreenShareStart();

        }

    }

}
