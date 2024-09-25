package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        frame.setVisible(true);
        frame.setResizable(true);
        addInputFields();
    }

    public void ShowErrorMessage(String message, String title){
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);

    }

    private void addInputFields(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());
        JTextField ipTextField = new JTextField("IP-Adresse", 1);

        JTextField portTextField = new JTextField("Port",1);

        ipTextField.setPreferredSize(new Dimension(200, 50));
        ipTextField.setMaximumSize(new Dimension(200, 50));
        ipTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(ipTextField);

        portTextField.setPreferredSize(new Dimension(200, 50));
        portTextField.setMaximumSize(new Dimension(200, 50));
        portTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(portTextField);

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectionManager.instance.ConnectToServer(ipTextField.getText(), Integer.parseInt(portTextField.getText()));
            }
        });
        panel.add(connectButton);

        panel.add(Box.createVerticalGlue());

        frame.pack();
        frame.add(panel);
        frame.setVisible(true);

    }
}
