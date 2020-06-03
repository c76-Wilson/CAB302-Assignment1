package ControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

public class MainMenu extends JFrame {
    // Frames
    private CreateBillboard createBillboard;

    // Panels
    JPanel mainPanel;

    // Components
    JButton billboards;
    JButton schedules;
    JButton users;

    public MainMenu(Dimension size){
        super("Main Menu");
        initMenu(size);
    }

    private void initMenu(Dimension size) {
        setLayout(new GridLayout(3, 1));
        setSize(size);
        setBackground(Color.LIGHT_GRAY);

        billboards = new JButton("Billboards");
        billboards.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createBillboard == null) {
                    createBillboard = new CreateBillboard(size);
                    createBillboard.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    createBillboard.setVisible(true);
                }
                else if (!createBillboard.isVisible()){
                    createBillboard.setVisible(true);
                }
            }
        });
        schedules = new JButton("Schedules");
        users = new JButton("Users");

        add(billboards);
        add(schedules);
        add(users);
    }

    public static void main(String[] args){
        MainMenu menu = new MainMenu(new Dimension(1000, 1000));
        menu.setVisible(true);
    }
}
