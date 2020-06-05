package ControlPanel;

import BillboardServer.Server;
import Helper.Password;
import Helper.Requests.LoginRequest;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

public class Login extends JFrame{
    // Server port
    int serverPort;
    String serverIP;

    // Session token
    SessionToken sessionToken;

    // Components
    JTextField username;
    JPasswordField password;
    JLabel userLabel;
    JLabel passLabel;
    JButton submitButton;

    // Panel
    JPanel loginPanel;

    public Login(){
        super("Login");
        getServerIPAndPort();
        setSize(new Dimension(720, 720));
        setupInputs();
    }

    /**
     * Gets the server port from a file and returns it
     * @return returns the port retrieved from the server.props file
     */
    private void getServerIPAndPort() {
        try {
            Properties properties = new Properties();

            String propFileName = "../server.props";

            InputStream inputStream = Server.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            this.serverIP = (String)properties.getProperty("ip");
            this.serverPort = Integer.parseInt(properties.getProperty("port"));
        } catch (Exception e) {
        }
    }

    private void setupInputs(){
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();
        username = new JTextField(20);
        password = new JPasswordField(20);
        userLabel = new JLabel("Username:");
        passLabel = new JLabel("Password:");
        username.setMinimumSize(new Dimension(50, 10));
        password.setMinimumSize(new Dimension(50, 10));
        add(loginPanel);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 0;
        loginPanel.add(userLabel, grid);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 0;
        loginPanel.add(username, grid);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 1;
        loginPanel.add(passLabel, grid);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 1;
        loginPanel.add(password, grid);

        // Button
        submitButton = new JButton("Login");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = username.getText();
                String pass = new String(password.getPassword());
                try {
                    if (tryLogin(user, pass)) {
                        MainMenu menu = new MainMenu(getSize(), sessionToken, serverIP, serverPort);

                        username = null;
                        password = null;
                        passLabel = null;
                        userLabel = null;
                        loginPanel = null;

                        dispose();

                        menu.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog((JFrame) SwingUtilities.getRoot((Component)e.getSource()),
                                "Incorrect username or password",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog((JFrame) SwingUtilities.getRoot((Component)e.getSource()),
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 2;
        loginPanel.add(submitButton, grid);
    }

    private boolean tryLogin(String user, String pass) throws Exception{
        String hashed = Password.hash(pass);
        LoginRequest login = new LoginRequest(user, hashed);
        Socket socket = new Socket(serverIP, serverPort);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(login);
        ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInput.readObject();
        if(obj.getClass() == SessionToken.class){
            sessionToken = ((SessionToken) obj);
            return true;
        } else {
            JOptionPane.showMessageDialog(this,
                    ((ErrorMessage)obj).getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static void main(String[] args){
        Login login = new Login();
        login.setVisible(true);
    }
}
