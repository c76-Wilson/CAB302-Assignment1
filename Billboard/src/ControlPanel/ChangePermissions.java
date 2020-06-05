package ControlPanel;

import Helper.Password;
import Helper.Requests.CreateUserRequest;
import Helper.Requests.GetUserPermissionsRequest;
import Helper.Requests.SetUserPermissionsRequest;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;
import Helper.User;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ChangePermissions extends JDialog {

    //Add Global Variables
    private final String serverIP;
    private final int serverPort;
    private final SessionToken sessionToken;

    //Add Components
    private JPanel permsPanel;
    private GridBagConstraints permsGrid;
    private JLabel labelUser;
    private JLabel usernameField;
    private JButton permsSet;
    private JCheckBox enableSchedule;
    private JCheckBox enableCreate;
    private JCheckBox enableEdit;
    private JCheckBox enableUser;

    //Add Boolean variables
    private boolean boolCreate;
    private boolean boolEdit;
    private boolean boolUser;
    private boolean boolSchedule;
    private String userID;


    public ChangePermissions(Dimension size, String serverIP, int serverPort, SessionToken sessionToken, String userID) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;
        this.userID = userID;
        changePerms(size);
    }

    private void changePerms(Dimension dim) {
        setSize(dim);

        permsPanel = new JPanel(new GridBagLayout());
        add(permsPanel);
        permsPanel.setVisible(true);
        setLayout(new GridBagLayout());
        setTitle("Create User");

        permsGrid = new GridBagConstraints();

        labelUser = new JLabel("Username: ");
        permsGrid.fill = GridBagConstraints.VERTICAL;
        permsGrid.gridx = 0;
        permsGrid.gridy = 0;
        permsPanel.add(labelUser, permsGrid);

        usernameField = new JLabel(userID);
        permsGrid.fill = GridBagConstraints.VERTICAL;
        permsGrid.gridx = 1;
        permsGrid.gridy = 0;
        permsPanel.add(usernameField, permsGrid);

        checkStates();

        enableCreate = new JCheckBox("Create Billboards");
        enableCreate.setSelected(boolCreate);
        permsGrid.anchor = GridBagConstraints.LINE_START;
        permsGrid.gridx = 0;
        permsGrid.gridy = 1;
        permsPanel.add(enableCreate, permsGrid);

        enableEdit = new JCheckBox("Edit All Billboards");
        enableEdit.setSelected(boolEdit);
        permsGrid.gridx = 1;
        permsGrid.gridy = 1;
        permsPanel.add(enableEdit, permsGrid);

        enableSchedule = new JCheckBox("Schedule Billboards");
        enableSchedule.setSelected(boolSchedule);
        permsGrid.gridx = 2;
        permsGrid.gridy = 1;
        permsPanel.add(enableSchedule, permsGrid);

        enableUser = new JCheckBox("Edit Users");
        enableUser.setSelected(boolUser);
        permsGrid.gridx = 3;
        permsGrid.gridy = 1;
        permsPanel.add(enableUser, permsGrid);

        permsSet = new JButton("Set Permissions");
        permsGrid.fill = GridBagConstraints.VERTICAL;
        permsGrid.anchor = GridBagConstraints.CENTER;
        permsGrid.gridwidth = GridBagConstraints.REMAINDER;
        permsGrid.gridx = 0;
        permsGrid.gridy = 2;
        permsPanel.add(permsSet, permsGrid);

        CheckListener checkL = new CheckListener();
        enableCreate.addItemListener(checkL);
        enableEdit.addItemListener(checkL);
        enableUser.addItemListener(checkL);
        enableSchedule.addItemListener(checkL);
        permsSet.addActionListener(e -> setPerms());

        setVisible(true);
    }

    private void checkStates() {

        GetUserPermissionsRequest getPermissions = new GetUserPermissionsRequest(
                userID, sessionToken.getSessionToken()
        );
        Object obj = null;
        try{
            obj = permsTest(getPermissions);
        } catch(Exception e){
            e.printStackTrace();
        }
        if(obj.getClass() == ErrorMessage.class){
            JOptionPane failBox = new JOptionPane();
            failBox.showMessageDialog(this, "<html>Couldn't Get User Permissions! ERROR:<br/>"
                            + "<i>" + ((ErrorMessage) obj).getErrorMessage() + "<i/><html/>",
                    "Couldn't Get User Permissions", JOptionPane.WARNING_MESSAGE);
        } else if (obj.getClass() == LinkedList.class){
            LinkedList<String> list = (LinkedList<String>)obj;
            if(list.contains("Create Billboard")){
                boolCreate = true;
            }
            if(list.contains("Edit Billboard")){
                boolEdit = true;
            }
            if(list.contains("Schedule Billboard")){
                boolSchedule = true;
            }
            if(list.contains("Edit Users")){
                boolUser = true;
            }
        }

    }

    private void setPerms() {
        LinkedList<String> perms = new LinkedList<>();
        if(boolCreate == true){
            perms.add("Create Billboard");
        }
        if (boolEdit == true){
            perms.add("Edit Billboard");
        }
        if (boolSchedule == true){
            perms.add("Schedule Billboard");
        }
        if (boolUser == true){
            perms.add("Edit Users");
        }
        String username = usernameField.getText();
        String token = sessionToken.getSessionToken();
        SetUserPermissionsRequest permRequest = new SetUserPermissionsRequest(username, perms, token);
        Object obj = null;
        try{
            obj = permsTest(permRequest);
        } catch (Exception e){
            e.printStackTrace();
        }
        if(obj.getClass() == ErrorMessage.class){
            JOptionPane failBox = new JOptionPane();
            failBox.showMessageDialog(this, "<html>User Permissions Not Set! ERROR:<br/>"
                            + "<i>" + ((ErrorMessage) obj).getErrorMessage() + "<i/><html/>",
                    "Couldn't Set User Permissions", JOptionPane.WARNING_MESSAGE);
        } else if (obj.getClass() == Boolean.class){
            JOptionPane successBox = new JOptionPane();
            ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/Checkmark_green.jpg"));
            successBox.showMessageDialog(this, "User Permissions Successfully Set!",
                    "Permissions Changed", JOptionPane.INFORMATION_MESSAGE, icon);
            this.setModalityType(ModalityType.MODELESS);
            this.setVisible(false);
            this.dispose();
        }
    }

    private Object permsTest(GetUserPermissionsRequest user) throws Exception{
        Socket socket = new Socket("localhost", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(user);
        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInputStream.readObject();
        return obj;
    }

    private Object permsTest(SetUserPermissionsRequest user) throws Exception{
        Socket socket = new Socket("localhost", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(user);
        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInputStream.readObject();
        return obj;
    }

    class CheckListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            Object box = event.getItemSelectable();
            if (box.equals(enableCreate)) {
                boolCreate = true;
            } else if (box.equals(enableEdit)) {
                boolEdit = true;
            } else if (box.equals(enableUser)) {
                boolUser = true;
            } else if (box.equals(enableSchedule)) {
                boolSchedule = true;
            }
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                if (event.getItemSelectable() == enableCreate) {
                    boolCreate = false;
                } else if (event.getItemSelectable() == enableEdit) {
                    boolEdit = false;
                } else if (event.getItemSelectable() == enableUser) {
                    boolUser = false;
                } else if (event.getItemSelectable() == enableSchedule) {
                    boolSchedule = false;
                }
            }
        }
    }

}
