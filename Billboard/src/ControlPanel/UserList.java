package ControlPanel;

import Helper.Requests.SetUserPermissionsRequest;
import Helper.User;
import Helper.Requests.DeleteUserRequest;
import Helper.Requests.GetUserPermissionsRequest;
import Helper.Requests.ListUsersRequest;
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

public class UserList extends JPanel {
    // Server/authentication
    private final SessionToken sessionToken;
    private final int serverPort;
    private final String serverIP;

    // Dialogs
    private CreateUser createUsers;
    private ChangePassword changePassword;
    private ChangePermissions changePerms;

    // Components
    JList<User> userList;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton permsButton;

    public UserList(Dimension userSize, SessionToken sessionToken, String serverIP, int serverPort) {
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(userSize);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initComponents(getUsers());
    }

    private void initComponents(LinkedList<User> users) {
        DefaultListModel<User> usersModel = new DefaultListModel<>();
        usersModel.addAll(users);

        userList = new JList<>(usersModel);

        userList.setVisibleRowCount(8);
        userList.setCellRenderer(new UserRenderer());
        userList.setPrototypeCellValue(new User("XXXXXXXXXXX", 1));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {

                    if (userList.getSelectedIndex() == -1) {
                        //No selection, disable fire button.
                        editButton.setEnabled(false);

                    } else {
                        //Selection, enable the fire button.
                        editButton.setEnabled(true);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);

        editButton = new JButton("Set Password");
        editButton.addActionListener(e -> {
            if (changePassword == null) {
                User user = userList.getSelectedValue();
                changePassword = new ChangePassword(getSize(), serverIP, serverPort, sessionToken, user.getName());
                changePassword.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                changePassword.setVisible(true);
                changePassword.setTitle("Set Password");
            }
            else if (!changePassword.isVisible()){
                User user = userList.getSelectedValue();
                changePassword = new ChangePassword(getSize(), serverIP, serverPort, sessionToken, user.getName());
                changePassword.setVisible(true);
            }
        });

        createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createUsers == null) {
                    createUsers = new CreateUser(getSize(), serverIP, serverPort, sessionToken);
                    createUsers.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    createUsers.setVisible(true);
                    createUsers.setTitle("Create User");
                }
                else if (!createUsers.isVisible()){
                    createUsers = new CreateUser(getSize(), serverIP, serverPort, sessionToken);
                    createUsers.setVisible(true);
                }

                DefaultListModel<User> newModel = new DefaultListModel<>();
                newModel.addAll(getUsers());

                userList.setModel(newModel);
                userList.updateUI();
                scrollPane.updateUI();
                validate();
                repaint();
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            User user = userList.getSelectedValue();
            if (user.getName().equals(sessionToken.getUserName())){
                JOptionPane.showMessageDialog(UserList.this,
                        "Can't delete your own account!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            else {
                deleteUser(user);
            }

            DefaultListModel<User> newModel = new DefaultListModel<>();
            newModel.addAll(getUsers());

            userList.setModel(newModel);
            userList.updateUI();
            scrollPane.updateUI();
            validate();
            repaint();
        });

        permsButton = new JButton("Change Permissions");
        permsButton.addActionListener(e ->{
            User user = userList.getSelectedValue();
            if (user.getName().equals(sessionToken.getUserName())){
                JOptionPane.showMessageDialog(UserList.this,
                        "Can't Change your own Account Permissions!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            else {
                if (changePerms == null) {
                    changePerms = new ChangePermissions(getSize(), serverIP, serverPort, sessionToken, user.getName());
                    changePerms.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    changePerms.setVisible(true);
                    changePerms.setTitle("Change User Permissions");
                }
                else if (!changePassword.isVisible()){
                    changePerms = new ChangePermissions(getSize(), serverIP, serverPort, sessionToken, user.getName());
                    changePerms.setVisible(true);
                }
            }
        });
        add(createButton);
        add(editButton);
        add(permsButton);
        add(deleteButton);
    }

    private void deleteUser(User user){
        DeleteUserRequest deleteuserRequest = new DeleteUserRequest(sessionToken.getSessionToken(), user.getName());

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(deleteuserRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Boolean.class) {
                JOptionPane.showMessageDialog(this,
                        "User deleted",
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

    private User getUserPerms(String name) {
        GetUserPermissionsRequest getUserRequest = new GetUserPermissionsRequest(sessionToken.getSessionToken(), name);

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(getUserRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == User.class) {
                return (User)obj;
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

    private LinkedList<User> getUsers() {
        ListUsersRequest getUsersRequest = new ListUsersRequest(sessionToken.getSessionToken());

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(getUsersRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == LinkedList.class) {
                return (LinkedList<User>) obj;
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

class UserPanel extends JPanel{
    private static final int GBC_I = 3;
    private User user;
    private JLabel nameLabel = new JLabel();
    private JLabel creatorLabel = new JLabel();

    public UserPanel() {
        setLayout(new GridBagLayout());
        add(new JLabel("Username:"), createGbc(0, 0));
        add(nameLabel, createGbc(1, 0));
        add(new JLabel("Billboards Created:"), createGbc(0, 1));
        add(creatorLabel, createGbc(1, 1));

        setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
    }

    public final void setUser(User user) {
        this.user = user;
        nameLabel.setText(user.getName());
        creatorLabel.setText(String.valueOf(user.getBillboardsCreated()));
    }

    public User getUser() {
        return user;
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

class UserRenderer extends UserPanel implements ListCellRenderer<User>{
    @Override
    public Component getListCellRendererComponent(JList<? extends User> list, User value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected){
            setBackground(Color.LIGHT_GRAY);
        }
        else{
            setBackground(null);
        }
        setUser(value);
        return this;
    }

}

