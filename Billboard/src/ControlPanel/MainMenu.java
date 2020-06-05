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

    // Layout
    CardLayout layout = new CardLayout();

    // Components
    JButton billboards;
    JButton schedules;
    JButton users;
    JButton logout;

    JButton mainMenu;

    public MainMenu(Dimension size, SessionToken sessionToken, String serverIP, int serverPort){
        super("Main Menu");
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(size);

        menu = new JPanel();
        billboardList = new BillboardList(getSize(), sessionToken, serverIP, serverPort);

        mainPanel.setLayout(layout);

        initMenu();

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        requestFocus();
    }

    private void initMenu() {
        menu.setLayout(new GridLayout(4, 1));

        billboards = new JButton("Billboards");
        billboards.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layout.show(mainPanel, "Billboards");
            }
        });
        schedules = new JButton("Schedules");
        users = new JButton("Users");
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
        menu.add(logout);

        billboardList.add(mainMenu);

        mainPanel.add(menu, "Menu");
        mainPanel.add(billboardList, "Billboards");

        add(mainPanel);
        layout.show(mainPanel, "Menu");
    }
}
