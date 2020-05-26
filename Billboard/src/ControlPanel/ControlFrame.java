package ControlPanel;
import Helper.Password;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ControlFrame implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JButton submit;
    private JTextField username;
    private JPasswordField password;
    private JLabel userLabel;
    private JLabel passLabel;
    private JLabel failedLogin;
    private GridBagConstraints grid;
    private String sessionToken;

    public ControlFrame(String title, boolean loginTrue){
        if(loginTrue){
            frame = new JFrame(title);
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            setupProcessForm();
        }
    }

    public ControlFrame (String title){
        frame = new JFrame(title);
        frame.setSize(720, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        setupLoginForm();
    }

    private JPanel setupProcessForm() {
        setupTest();
        return panel;
    }

    private void setupTest() {
        panel = new JPanel(new GridBagLayout());
        frame.add(panel);
        JLabel test = new JLabel("Hello Gamers");
        grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 5;
        panel.add(test, grid);
    }

    public JPanel setupLoginForm(){
        setupInputs();
        setupButton();
        return panel;
    }

    private void setupInputs(){
        panel = new JPanel(new GridBagLayout());
        grid = new GridBagConstraints();
        username = new JTextField(20);
        password = new JPasswordField(20);
        userLabel = new JLabel("Username:");
        passLabel = new JLabel("Password:");
        username.setMinimumSize(new Dimension(50, 10));
        password.setMinimumSize(new Dimension(50, 10));
        frame.add(panel);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 0;
        panel.add(userLabel, grid);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 0;
        panel.add(username, grid);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 1;
        panel.add(passLabel, grid);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 1;
        panel.add(password, grid);
    }

    private void setupButton(){
        submit = new JButton("Login");
        submit.addActionListener(this);
        String UN = username.getText();
        String PW = new String(password.getPassword());
        try {
            submit.setActionCommand(testLogin(UN, PW));
        } catch (Exception e) {
            e.printStackTrace();
        }
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 2;
        panel.add(submit, grid);
    }

    private String testLogin(String user, String pass) throws Exception{
        Password before = new Password();
        String hashed = before.hash(pass);
        LoginRequest login = new LoginRequest(user, hashed);
        Socket socket = new Socket("127.0.0.1", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(login);
        ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInput.readObject();
        if(obj.getClass() == String.class){
            storeSessionToken((String) obj);
            return "Login True";
        } else if (obj.getClass() == ErrorMessage.class){
            System.out.println(((ErrorMessage) obj).getErrorMessage());
            return "Login False";
        } else {
            return "Couldn't find element";
        }
    }

    private void storeSessionToken(String token) {
        sessionToken = token;
    }

    @Override
    public void actionPerformed(ActionEvent event){
        String user = username.getText();
        String pass = new String(password.getPassword());
        System.out.println(user);
        System.out.println(pass);
        String cmd = event.getActionCommand();
        if (cmd == "Login True") {
            username = null;
            password = null;
            passLabel = null;
            userLabel = null;
            panel = null;
            frame.dispose();
            new ControlFrame("Process Form", true);
        } else if (cmd == "Login False") {
            failedLogin = new JLabel("Incorrect Username or Password");
            panel.add(failedLogin);
        } else if (cmd == "Couldn't find element"){
            failedLogin = new JLabel("Check your Username / Password aren't null");
            panel.add(failedLogin);
        } else {
            failedLogin = new JLabel("Check the code Code Monkeys");
            panel.add(failedLogin);
        }
    }
}
