package ControlPanel;
import Helper.Requests.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

public class ControlFrame extends Panel implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JButton submit;
    private JTextField username;
    private JPasswordField password;
    private JLabel userLabel;
    private JLabel passLabel;
    private JLabel failedLogin;
    private GridBagConstraints grid;
    private boolean loginComplete = false;

    public ControlFrame(String title){
        frame = new JFrame(title);
        if(loginComplete == false){
            frame.setSize(720, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            setupLoginForm();
        } else if(loginComplete == true){
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            setupProcessForm();
        }
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

    private void setupPlaceholders(){
        username.requestFocus();
    }

    private void setupButton(){
        submit = new JButton("Login");
        submit.addActionListener(this);
        submit.setActionCommand(testLogin(username.getText(), password.getPassword().toString()));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 2;
        panel.add(submit, grid);
    }

    private String testLogin(String user, String pass){
        //if(Helper.Requests.LoginRequest(user, pass) == true){
        // return "Login True";
        // }
        //else {
        // return "Login False";
        // }
        return "Login True";
    }

    private void txtUserNameFocusGained(FocusEvent evt) {
        String userText = username.getText();
        if(userText.equals("Username")){
            username.setCaretPosition(0);
        }

    }

    private void txtUserNameFocusLost(FocusEvent evt) {
        String userText = username.getText();
        if(userText.equals("")){
            username.setForeground(new java.awt.Color(86, 86, 86));
            username.setCaretPosition(0);
            username.setText("Username");
        }
    }

    private void txtUserNameKeyPressed(KeyEvent evt) {
        String userText = username.getText();
        if(userText.equals("Username")){
            username.setForeground(new java.awt.Color(0, 0, 0));
            username.setText(null);
            username.setCaretPosition(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd == "Login True") {
            loginComplete = true;
            frame.dispose();
            new ControlFrame("Process Form");
        } else if (cmd == "Login False") {
            failedLogin.setText("Incorrect Username or Password");
            panel.add(failedLogin);
        } else {

        }
    }
}
