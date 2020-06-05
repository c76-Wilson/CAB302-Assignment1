package ControlPanel;

import Helper.Password;
import Helper.Requests.ScheduleBillboardRequest;
import Helper.Requests.SetUserPasswordRequest;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class ChangePassword extends JDialog {
    // Panels
    JPanel inputPanel;
    JPanel recurrencePanel;

    // Components
    JLabel setPasswordLabel;
    JPasswordField setPasswordField;
    JButton submitButton;

    // Other
    String serverIP;
    int serverPort;
    SessionToken sessionToken;
    String userName;

    public ChangePassword(Dimension size, String serverIP, int serverPort, SessionToken sessionToken){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;

        setSize(size);
        initComponents();
    }

    public ChangePassword(Dimension size, String serverIP, int serverPort, SessionToken sessionToken, String userName){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;
        this.userName = userName;

        setSize(size);
        initComponents();
    }

    private void initComponents(){
        setLayout(new BorderLayout());

        // Components
        setPasswordLabel = new JLabel("New Password: ");
        setPasswordField = new JPasswordField(20);
        setPasswordField.setMinimumSize(new Dimension(50, 10));

        // Submit button
        submitButton = new JButton("Change Password");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!new String(setPasswordField.getPassword()).isBlank()){
                    SetUserPasswordRequest request = new SetUserPasswordRequest(userName != null ? userName : sessionToken.getUserName(), Password.hash(new String(setPasswordField.getPassword())), sessionToken.getSessionToken());

                    updatePassword(request);
                }
                else{
                    JOptionPane.showMessageDialog((JFrame) SwingUtilities.getRoot((Component)e.getSource()),
                            "Password cannot be blank!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Initialise input panel
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());

        // Add main details to panel
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.LINE_AXIS));

        passPanel.add(setPasswordLabel);
        passPanel.add(setPasswordField);

        inputPanel.add(passPanel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        inputPanel.add(submitButton, constraints);

        add(inputPanel);
    }

    private void updatePassword(SetUserPasswordRequest request){

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(request);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Boolean.class) {
                JOptionPane.showMessageDialog(this,
                        "Password changed!",
                        "Success",
                        JOptionPane.PLAIN_MESSAGE);

                this.setModalityType(ModalityType.MODELESS);
                this.setVisible(false);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        ((ErrorMessage) obj).getErrorMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
