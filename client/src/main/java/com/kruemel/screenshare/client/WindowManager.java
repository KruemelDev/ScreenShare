package com.kruemel.screenshare.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class WindowManager {
    public JFrame frame;
    private String windowName;
    private int width;
    private int height;

    public static WindowManager instance = null;


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
    public void ScreenShareConnectionMenu(){
        frame.getContentPane().removeAll();
        frame.repaint();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        System.out.println("screenShareconnectionmenu");
        JList<String> clientNameList = new JList<String>(ConnectionHandler.instance.availableClients);
        clientNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientNameList.add(panel);
        frame.add(panel);
        frame.setVisible(true);

    }

    public static void resetToConnectMenu(){
        ConnectionHandler.instance.CloseConnection();
        ConnectionHandler.instance.availableClients = new String[0];
        ConnectionHandler.instance.socket = null;
        ConnectionHandler.instance.in = null;
        ConnectionHandler.instance.out = null;
        WindowManager.instance.frame.getContentPane().removeAll();
        WindowManager.instance.frame.repaint();
        WindowManager.instance.AddConnectionFields();
    }
}
