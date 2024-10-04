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

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ConnectionHandler.instance.RequestAvailableClients();
                ScreenShareConnectionMenu();
            }
        });
        panel.add(refreshButton);

        JList<String> clientNameList = new JList<String>(ConnectionHandler.instance.availableClients);
        clientNameList.setPreferredSize(new Dimension(200, 50));
        clientNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(clientNameList);

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.out.println("clientNameList.getSelectedValue()");
                ConnectionHandler.instance.ReqestScreenShare(clientNameList.getSelectedValue());
                ScreenShareConnectionMenu();
            }
        });
        panel.add(connectButton);
        //add connect button for screen transfer
        frame.add(panel);
        frame.setVisible(true);

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
        }
    }

}
