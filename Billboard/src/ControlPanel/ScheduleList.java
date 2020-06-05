package ControlPanel;

import Helper.Billboard;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;
import Helper.ScheduledBillboard;
import Helper.SessionToken;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class ScheduleList extends JPanel {
    // Server/authentication
    SessionToken sessionToken;
    int serverPort;
    String serverIP;

    // Dialogs
    JDialog createBillboard;

    // Components
    JList<ScheduledBillboard> scheduledBillboardList;
    JButton removeScheduleButton;

    public ScheduleList(Dimension size, SessionToken sessionToken, String serverIP, int serverPort) {
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(size);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initComponents(getScheduledBillboards());
    }

    private void initComponents(LinkedList<ScheduledBillboard> billboards) {
        DefaultListModel<ScheduledBillboard> billboardsModel = new DefaultListModel<>();
        billboardsModel.addAll(billboards);

        scheduledBillboardList = new JList<>(billboardsModel);

        scheduledBillboardList.setVisibleRowCount(8);
        scheduledBillboardList.setCellRenderer(new ScheduleRenderer());
        scheduledBillboardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduledBillboardList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {

                    if (scheduledBillboardList.getSelectedIndex() == -1) {
                        //No selection, disable fire button.
                        removeScheduleButton.setEnabled(false);

                    } else {
                        //Selection, enable the fire button.
                        removeScheduleButton.setEnabled(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(scheduledBillboardList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);

        removeScheduleButton = new JButton("Remove");
        removeScheduleButton.setEnabled(false);
        removeScheduleButton.addActionListener(e -> {
            removeSchedule(scheduledBillboardList.getSelectedValue());

            DefaultListModel<ScheduledBillboard> newModel = new DefaultListModel<>();
            newModel.addAll(getScheduledBillboards());

            scheduledBillboardList.setModel(newModel);
            scheduledBillboardList.updateUI();
            scrollPane.updateUI();
            validate();
            repaint();
        });

        add(removeScheduleButton);
    }

    private void removeSchedule(ScheduledBillboard scheduledBillboard){
        RemoveFromScheduleRequest removeFromScheduleRequest = new RemoveFromScheduleRequest(scheduledBillboard.getName(), Timestamp.valueOf(scheduledBillboard.getScheduleTime()), sessionToken.getSessionToken());

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(removeFromScheduleRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Boolean.class) {
                JOptionPane.showMessageDialog(this,
                        "Billboard showing removed",
                        "Success",
                        JOptionPane.PLAIN_MESSAGE);
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

    private LinkedList<ScheduledBillboard> getScheduledBillboards() {
        ViewScheduleRequest viewScheduleRequest = new ViewScheduleRequest(sessionToken.getSessionToken());

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(viewScheduleRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == LinkedList.class) {
                return (LinkedList<ScheduledBillboard>) obj;
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

        return null;
    }
}

class SchedulePanel extends JPanel{
    private static final int GBC_I = 3;
    private ScheduledBillboard billboard;
    private JLabel nameLabel = new JLabel();
    private JLabel creatorLabel = new JLabel();
    private JLabel scheduleLabel = new JLabel();
    private JLabel durationLabel = new JLabel();

    public SchedulePanel() {
        setLayout(new GridBagLayout());
        add(new JLabel("Name:"), createGbc(0, 0));
        add(nameLabel, createGbc(1, 0));
        add(new JLabel("Creator:"), createGbc(0, 1));
        add(creatorLabel, createGbc(1, 1));
        add(new JLabel("Schedule Time:"), createGbc(0, 2));
        add(scheduleLabel, createGbc(1, 2));
        add(new JLabel("Duration:"), createGbc(0, 3));
        add(durationLabel, createGbc(1, 3));

        setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
    }

    public final void setBillboard(ScheduledBillboard billboard) {
        this.billboard = billboard;
        nameLabel.setText(billboard.getName());
        creatorLabel.setText(billboard.getCreatorName());
        scheduleLabel.setText(billboard.getScheduleTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        durationLabel.setText(String.format("%d minutes", billboard.getScheduleDuration().toMinutes()));
    }

    public ScheduledBillboard getBillboard() {
        return billboard;
    }

    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(GBC_I, GBC_I, GBC_I, GBC_I);
        gbc.insets.left = x != 0 ? 3 * GBC_I : GBC_I;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        return gbc;
    }
}

class ScheduleRenderer extends SchedulePanel implements ListCellRenderer<ScheduledBillboard>{
    @Override
    public Component getListCellRendererComponent(JList<? extends ScheduledBillboard> list, ScheduledBillboard value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected){
            setBackground(Color.LIGHT_GRAY);
        }
        else{
            setBackground(null);
        }
        setBillboard(value);
        return this;
    }
}



