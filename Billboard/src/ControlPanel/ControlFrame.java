package ControlPanel;
import Helper.Password;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
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
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JLabel mainCanvas;
    private JColorChooser palette;
    private GridBagConstraints canvasGrid;

    public ControlFrame(String title, boolean loginTrue){
        if(loginTrue){
            frame = new JFrame(title);
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setupProcessForm();
            frame.setVisible(true);
        }
    }

    public ControlFrame (String title){
        frame = new JFrame(title);
        frame.setSize(720, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupLoginForm();
        frame.setVisible(true);
    }

    private void setupProcessForm() {
        panel = new JPanel(new GridBagLayout());
        grid = new GridBagConstraints();
        setupMenu();
        setupCanvas();
        setupColourPalette();
        frame.add(panel);
    }

    private void setupCanvas() {
        mainCanvas = new JLabel("Test");
        mainCanvas.setForeground(new Color(255, 225, 255));
        mainCanvas.setMaximumSize(new Dimension(960, 240));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 0;
        panel.add(mainCanvas, grid);
        mainCanvas.setVisible(true);
    }

    private void setupColourPalette() {
        palette = new JColorChooser();
        palette.getSelectionModel().addChangeListener(e -> {
            Color colour = palette.getColor();
            mainCanvas.setForeground(colour);
        });
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 0;
        panel.add(palette, grid);
        palette.setVisible(true);
    }

    private void setupMenu() {
        prepareMenu();
        createMenu();
    }

    public void setupLoginForm(){
        setupInputs();
        setupButton();
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
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 2;
        panel.add(submit, grid);
    }

    private boolean testLogin(String user, String pass) throws Exception{
        String hashed = Password.hash(pass);
        LoginRequest login = new LoginRequest(user, hashed);
        Socket socket = new Socket("localhost", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(login);
        ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInput.readObject();
        if(obj.getClass() == String.class){
            storeSessionToken((String) obj);
            return true;
        } else {
            System.out.println(((ErrorMessage) obj).getErrorMessage());
            return false;
        }
    }

    private void storeSessionToken(String token) {
        sessionToken = token;
    }

    private void prepareMenu(){
        headerLabel = new JLabel("",JLabel.CENTER );
        statusLabel = new JLabel("",JLabel.CENTER);
        statusLabel.setSize(350,100);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        frame.add(headerLabel);
        frame.add(controlPanel);
        frame.add(statusLabel);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
    }

    private void createMenu(){
        //create a menu bar
        final JMenuBar menuBar = new JMenuBar();

        //create menus
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        final JMenu aboutMenu = new JMenu("About");
        final JMenu linkMenu = new JMenu("Links");

        //create menu items
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setActionCommand("New");

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setActionCommand("Open");

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setActionCommand("Save");

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setActionCommand("Exit");

        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setActionCommand("Cut");

        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setActionCommand("Copy");

        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setActionCommand("Paste");

        MenuItemListener menuItemListener = new MenuItemListener();

        newMenuItem.addActionListener(menuItemListener);
        openMenuItem.addActionListener(menuItemListener);
        saveMenuItem.addActionListener(menuItemListener);
        exitMenuItem.addActionListener(menuItemListener);
        cutMenuItem.addActionListener(menuItemListener);
        copyMenuItem.addActionListener(menuItemListener);
        pasteMenuItem.addActionListener(menuItemListener);

        final JCheckBoxMenuItem showWindowMenu = new JCheckBoxMenuItem("Show About", true);
        showWindowMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if(showWindowMenu.getState()){
                    menuBar.add(aboutMenu);
                } else {
                    menuBar.remove(aboutMenu);
                }
            }
        });
        final JRadioButtonMenuItem showLinksMenu = new JRadioButtonMenuItem(
                "Show Links", true);
        showLinksMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if(menuBar.getMenu(3)!= null){
                    menuBar.remove(linkMenu);
                    frame.repaint();
                } else {
                    menuBar.add(linkMenu);
                    frame.repaint();
                }
            }
        });
        //add menu items to menus
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(showWindowMenu);
        fileMenu.addSeparator();
        fileMenu.add(showLinksMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);

        //add menu to menubar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(aboutMenu);
        menuBar.add(linkMenu);

        //add menubar to the frame
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand() == "Exit"){
                frame.dispose();
            } else {
                statusLabel.setText(e.getActionCommand() + " JMenuItem clicked.");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event){
        String user = username.getText();
        String pass = new String(password.getPassword());
        boolean loginPass = false;
        try {
            loginPass = testLogin(user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (loginPass == true) {
            username = null;
            password = null;
            passLabel = null;
            userLabel = null;
            panel = null;
            frame.dispose();
            new ControlFrame("Billboard Control Panel", true);
        } else if (loginPass == false) {
            if(failedLogin == null){
                failedLogin = new JLabel("Incorrect Username or Password");
                failedLogin.setForeground(Color.RED);
                grid.fill = GridBagConstraints.VERTICAL;
                grid.gridx = 1;
                grid.gridy = 3;
                failedLogin.setVisible(true);
                panel.add(failedLogin, grid);
                frame.revalidate();
            } else {

            }
        } else {
            failedLogin = new JLabel("Check the code Code Monkeys");
            grid.fill = GridBagConstraints.VERTICAL;
            failedLogin.setForeground(Color.RED);
            grid.gridx = 1;
            grid.gridy = 5;
            failedLogin.setVisible(true);
            panel.add(failedLogin);
            frame.revalidate();
        }
    }
}
