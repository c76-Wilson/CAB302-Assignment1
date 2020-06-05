package ControlPanel;

import Helper.Billboard;
import Helper.SessionToken;

import javax.swing.*;

public class ScheduleList extends JPanel {
    // Server/authentication
    SessionToken sessionToken;
    int serverPort;
    String serverIP;

    // Dialogs
    JDialog createBillboard;

    // Components
    JList<Billboard> billboardList;
    JButton createButton;
    JButton editButton;
    JButton deleteButton;
}
