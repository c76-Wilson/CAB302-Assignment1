package ControlPanel;

import Helper.Billboard;
import Helper.Requests.DeleteBillboardRequest;
import Helper.Requests.GetBillboardRequest;
import Helper.Requests.ListBillboardsRequest;
import Helper.Responses.ErrorMessage;
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
import java.util.LinkedList;

public class BillboardList extends JPanel {
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

    public BillboardList(Dimension size, SessionToken sessionToken, String serverIP, int serverPort) {
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(size);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initComponents(getBillboards());
    }

    private void initComponents(LinkedList<Billboard> billboards) {
        DefaultListModel<Billboard> billboardsModel = new DefaultListModel<>();
        billboardsModel.addAll(billboards);

        billboardList = new JList<Billboard>(billboardsModel);

        billboardList.setVisibleRowCount(8);
        billboardList.setCellRenderer(new BillboardRenderer());
        billboardList.setPrototypeCellValue(new Billboard("XXXXXXXXXXX", "XXXXXXXXXXX"));
        billboardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billboardList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {

                    if (billboardList.getSelectedIndex() == -1) {
                        //No selection, disable fire button.
                        editButton.setEnabled(false);

                    } else {
                        //Selection, enable the fire button.
                        editButton.setEnabled(true);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(billboardList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);

        editButton = new JButton("Edit");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createBillboard == null) {
                    Billboard billboard = getBillboard(billboardList.getSelectedValue().getName());
                    createBillboard = new CreateBillboard(getSize(), serverIP, serverPort, sessionToken, billboard.getName(), billboard.getXml());
                    createBillboard.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                }
                else if (!createBillboard.isVisible()){
                    Billboard billboard = getBillboard(billboardList.getSelectedValue().getName());
                    createBillboard = new CreateBillboard(getSize(), serverIP, serverPort, sessionToken, billboard.getName(), billboard.getXml());
                    createBillboard.setVisible(true);
                }

                DefaultListModel<Billboard> newModel = new DefaultListModel<>();
                newModel.addAll(getBillboards());

                billboardList.setModel(newModel);
                billboardList.updateUI();
                scrollPane.updateUI();
                validate();
                repaint();
            }
        });

        createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createBillboard == null) {
                    createBillboard = new CreateBillboard(getSize(), serverIP, serverPort, sessionToken);
                    createBillboard.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    createBillboard.setVisible(true);
                }
                else if (!createBillboard.isVisible()){
                    createBillboard = new CreateBillboard(getSize(), serverIP, serverPort, sessionToken);
                    createBillboard.setVisible(true);
                }

                DefaultListModel<Billboard> newModel = new DefaultListModel<>();
                newModel.addAll(getBillboards());

                billboardList.setModel(newModel);
                billboardList.updateUI();
                scrollPane.updateUI();
                validate();
                repaint();
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Billboard billboard = getBillboard(billboardList.getSelectedValue().getName());
                deleteBillboard(billboard);

                DefaultListModel<Billboard> newModel = new DefaultListModel<>();
                newModel.addAll(getBillboards());

                billboardList.setModel(newModel);
                billboardList.updateUI();
                scrollPane.updateUI();
                validate();
                repaint();
            }
        });
        add(createButton);
        add(editButton);
        add(deleteButton);
    }

    private void deleteBillboard(Billboard billboard){
        DeleteBillboardRequest deleteBillboardRequest = new DeleteBillboardRequest(sessionToken.getSessionToken(), billboard.getName());

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(deleteBillboardRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Boolean.class) {
                JOptionPane.showMessageDialog(this,
                        "Billboard deleted",
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

    private Billboard getBillboard(String name) {
        GetBillboardRequest getBillboardRequest = new GetBillboardRequest(sessionToken.getSessionToken(), name);

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(getBillboardRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Billboard.class) {
                return (Billboard)obj;
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

    private LinkedList<Billboard> getBillboards() {
        ListBillboardsRequest getBillboardsRequest = new ListBillboardsRequest(sessionToken.getSessionToken());

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(getBillboardsRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == LinkedList.class) {
                return (LinkedList<Billboard>) obj;
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

class BillboardPanel extends JPanel{
    private static final int GBC_I = 3;
    private Billboard billboard;
    private JLabel nameLabel = new JLabel();
    private JLabel creatorLabel = new JLabel();

    public BillboardPanel() {
        setLayout(new GridBagLayout());
        add(new JLabel("Name:"), createGbc(0, 0));
        add(nameLabel, createGbc(1, 0));
        add(new JLabel("Creator:"), createGbc(0, 1));
        add(creatorLabel, createGbc(1, 1));

        setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
    }

    public final void setBillboard(Billboard billboard) {
        this.billboard = billboard;
        nameLabel.setText(billboard.getName());
        creatorLabel.setText(billboard.getCreatorName());
    }

    public Billboard getBillboard() {
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

class BillboardRenderer extends BillboardPanel implements ListCellRenderer<Billboard>{
    @Override
    public Component getListCellRendererComponent(JList<? extends Billboard> list, Billboard value, int index, boolean isSelected, boolean cellHasFocus) {
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

