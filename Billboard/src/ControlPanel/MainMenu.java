package ControlPanel;

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
    ScheduleCalendar scheduleCalendar;
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

    public MainMenu(SessionToken sessionToken, String serverIP, int serverPort){
        super("Main Menu");
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(1280, 720);

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
        billboards.addActionListener(e -> {
            billboardList = new BillboardList(getSize(), sessionToken, serverIP, serverPort);
            billboardList.add(mainMenu);
            mainPanel.add(billboardList, "Billboards");
            layout.show(mainPanel, "Billboards");
            billboardList.setVisible(true);
            billboardList.setTitle("Billboards");
        });
        schedules = new JButton("Schedule");
<<<<<<< HEAD
        schedules.addActionListener(e -> {
            scheduleList = new ScheduleList(getSize(), sessionToken, serverIP, serverPort);
            scheduleList.add(mainMenu);
            mainPanel.add(scheduleList, "Schedule");
            layout.show(mainPanel, "Schedule");
=======
        schedules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scheduleCalendar = new ScheduleCalendar(getSize(), sessionToken, serverIP, serverPort);
                scheduleCalendar.add(mainMenu, BorderLayout.SOUTH);
                mainPanel.add(scheduleCalendar, "Schedule");
                layout.show(mainPanel, "Schedule");
            }
>>>>>>> a42626de6163c8344a0738680e8c42b279230b3b
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
        setPassword.addActionListener(e -> {
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
        });
        logout = new JButton("Logout");
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sessionToken = null;

                ControlPanel login = new ControlPanel();
                dispose();
                login.setVisible(true);
            }
        });
        mainMenu = new JButton("Main Menu");
        mainMenu.addActionListener(e -> layout.show(mainPanel, "Menu"));

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
