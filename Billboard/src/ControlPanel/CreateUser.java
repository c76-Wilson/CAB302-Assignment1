package ControlPanel;

import Helper.Password;
import Helper.Requests.CreateUserRequest;
import Helper.Requests.ScheduleBillboardRequest;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.ErrorManager;

public class CreateUser extends JDialog {
    //Create User Components
    private JPanel userPanel;
    private GridBagConstraints userGrid;
    private JLabel labelUser;
    private JLabel labelPass;
    private JPasswordField setPassword;
    private JTextField setUsername;
    private JButton userMake;
    private JCheckBox enableCreate;
    private JCheckBox enableEdit;
    private JCheckBox enableSchedule;
    private JCheckBox enableUser;

    //Create User Variables
    private String nameUser = "";
    private int nameChars = 0;
    private JLabel nameCount;
    private boolean boolCreate;
    private boolean boolEdit;
    private boolean boolUser;
    private boolean boolSchedule;

    // Other
    SessionToken sessionToken;
    String serverIP;
    int serverPort;

    public CreateUser(Dimension size, String serverIP, int serverPort, SessionToken sessionToken) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;
        createUser(size);
    }

    private void createUser(Dimension window) {
        setSize(window);

        userPanel = new JPanel(new GridBagLayout());
        add(userPanel);
        userPanel.setVisible(true);
        setLayout(new GridBagLayout());
        setTitle("Create User");

        userGrid = new GridBagConstraints();

        labelUser = new JLabel("Username: ");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 0;
        userGrid.gridy = 0;
        userPanel.add(labelUser, userGrid);

        setUsername = new JTextField(20);
        setUsername.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null || setUsername.getText().length() >= 50) {
                    return;
                }

                super.insertString(offs, str, a);
            }
        });
        setUsername.getDocument().putProperty("Area", setUsername);
        setUsername.getDocument().addDocumentListener(new TextListener());
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 1;
        userGrid.gridy = 0;
        userGrid.gridwidth = 2;
        userPanel.add(setUsername, userGrid);

        nameCount = new JLabel(nameChars + " / 50 Characters");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 2;
        userGrid.gridy = 0;
        userGrid.gridwidth = 1;
        userPanel.add(nameCount, userGrid);

        labelPass = new JLabel("Password: ");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 0;
        userGrid.gridy = 1;
        userPanel.add(labelPass, userGrid);

        setPassword = new JPasswordField(20);
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 1;
        userGrid.gridy = 1;
        userGrid.gridwidth = 2;
        userPanel.add(setPassword, userGrid);

        enableCreate = new JCheckBox("Create Billboards");
        userGrid.anchor = GridBagConstraints.LINE_START;
        userGrid.gridx = 3;
        userGrid.gridy = 0;
        userGrid.gridwidth = 1;
        userPanel.add(enableCreate, userGrid);

        enableEdit = new JCheckBox("Edit All Billboards");
        userGrid.gridx = 4;
        userGrid.gridy = 0;
        userPanel.add(enableEdit, userGrid);

        enableSchedule = new JCheckBox("Schedule Billboards");
        userGrid.gridx = 3;
        userGrid.gridy = 1;
        userPanel.add(enableSchedule, userGrid);

        enableUser = new JCheckBox("Edit Users");
        userGrid.gridx = 4;
        userGrid.gridy = 1;
        userPanel.add(enableUser, userGrid);

        userMake = new JButton("Create User");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.anchor = GridBagConstraints.CENTER;
        userGrid.gridwidth = GridBagConstraints.REMAINDER;
        userGrid.gridx = 0;
        userGrid.gridy = 3;
        userPanel.add(userMake, userGrid);

        CheckListener checkL = new CheckListener();
        enableCreate.addItemListener(checkL);
        enableEdit.addItemListener(checkL);
        enableUser.addItemListener(checkL);
        enableSchedule.addItemListener(checkL);

        userMake.addActionListener(e -> {
            try {
                makeUser();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    private void makeUser() {
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
        nameUser = setUsername.getText();
        String dummy = String.valueOf(setPassword.getPassword());
        String hashed = Password.hash(dummy);
        String token = sessionToken.getSessionToken();
        CreateUserRequest userRequest = new CreateUserRequest(nameUser, perms, hashed, token);
        Object obj = null;
        try{
            obj = userTest(userRequest);
        } catch (Exception e){
            e.printStackTrace();
        }
        if(obj.getClass() == ErrorMessage.class){
            JOptionPane failBox = new JOptionPane();
            failBox.showMessageDialog(this, "<html>User Not Created! ERROR:<br/>"
                            + "<i>" + ((ErrorMessage) obj).getErrorMessage() + "<i/><html/>",
                    "Couldn't Create User", JOptionPane.WARNING_MESSAGE);
        } else if (obj.getClass() == Boolean.class){
            JOptionPane successBox = new JOptionPane();
            ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/Checkmark_green.jpg"));
            successBox.showMessageDialog(this, "User Successfully Created!",
                    "User Created", JOptionPane.INFORMATION_MESSAGE, icon);
            this.setModalityType(ModalityType.MODELESS);
            this.setVisible(false);
            this.dispose();
        }
    }

    private Object userTest(CreateUserRequest user) throws Exception{
        Socket socket = new Socket("localhost", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(user);
        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInputStream.readObject();
        return obj;
    }

    class TextListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            javax.swing.text.Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setUsername)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            }
        }

        public void removeUpdate(DocumentEvent e) {
            javax.swing.text.Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setUsername)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            }
        }

        public void changedUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setUsername)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            }
        }
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
