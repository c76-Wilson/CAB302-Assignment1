package ControlPanel;

import Helper.Billboard;
import Helper.SessionToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame{
    // Session Token
    SessionToken sessionToken;
    int serverPort;
    String serverIP;

    // Panels
    JPanel mainPanel = new JPanel();;
    JPanel menu;
    BillboardList billboardList;
    UserList userList;
    ScheduleList scheduleList;
    ChangePassword changePassword;

    // Layout
    CardLayout layout = new CardLayout();

    // Components
    JButton billboards;
    JButton schedules;
    JButton users;
    JButton setPassword;
    JButton logout;

    JButton mainMenu;

    public MainMenu(Dimension size, SessionToken sessionToken, String serverIP, int serverPort){
        super("Main Menu");
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(size);

        menu = new JPanel();

        mainPanel.setLayout(layout);

        initMenu();

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        requestFocus();
    }

    private void initMenu() {
        menu.setLayout(new GridLayout(5, 1));

        billboards = new JButton("Billboards");
        billboards.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                billboardList = new BillboardList(getSize(), sessionToken, serverIP, serverPort);
                billboardList.add(mainMenu);
                mainPanel.add(billboardList, "Billboards");
                layout.show(mainPanel, "Billboards");
            }
        });
        schedules = new JButton("Schedule");
        schedules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scheduleList = new ScheduleList(getSize(), sessionToken, serverIP, serverPort);
                scheduleList.add(mainMenu);
                mainPanel.add(scheduleList, "Schedule");
                layout.show(mainPanel, "Schedule");
            }
        });
        users = new JButton("Users");
        users.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userList = new UserList(getSize(), sessionToken, serverIP, serverPort);
                userList.add(mainMenu);
                mainPanel.add(userList, "Users");
                layout.show(mainPanel, "Users");
            }
        });
        setPassword = new JButton("Change Your Password");
        setPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (changePassword == null) {
                    changePassword = new ChangePassword(getSize(), serverIP, serverPort, sessionToken);
                    changePassword.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    changePassword.setVisible(true);
                    changePassword.setTitle("Change Password");
                }
                else if (!changePassword.isVisible()){
                    changePassword = new ChangePassword(getSize(), serverIP, serverPort, sessionToken);
                    changePassword.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    changePassword.setVisible(true);
                    changePassword.setTitle("Change Password");
                }
                changePassword = new ChangePassword(getSize(), serverIP, serverPort, sessionToken);
                changePassword.add(mainMenu);
            }
        });
        logout = new JButton("Logout");
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sessionToken = null;

                Login login = new Login();
                dispose();
                login.setVisible(true);
            }
        });
        mainMenu = new JButton("Main Menu");
        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layout.show(mainPanel, "Menu");
            }
        });

        menu.add(billboards);
        menu.add(schedules);
        menu.add(users);
        menu.add(setPassword);
        menu.add(logout);


        mainPanel.add(menu, "Menu");

        add(mainPanel);
        layout.show(mainPanel, "Menu");
    }
}
