package ControlPanel;
import Helper.Password;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlFrame implements ActionListener {

    //Create main Components
    private JFrame frame;
    private JPanel panel;
    private GridBagConstraints grid;

    //Create Login Components
    private JButton submit;
    private JTextField username;
    private JPasswordField password;
    private JLabel userLabel;
    private JLabel passLabel;
    private JLabel failedLogin;
    private String sessionToken;

    //Create Menu Components
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;

    //Create Time Components
    private JLabel timeLabel;
    private JLabel timeYear;
    private JLabel timeMonth;
    private JLabel timeDay;
    private JLabel timeHour;
    private JLabel timeMinute;
    private JLabel timeA;
    private JSpinner yearSpinner;
    private JSpinner monthSpinner;
    private JSpinner daySpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JRadioButton amRadio;
    private JRadioButton pmRadio;

    //Create Scheduling components
    private JSpinner durationSpin;
    private JLabel durationLabel;
    private JLabel durationMins;
    private JLabel repetitionLabel;
    private JLabel repetitionDay;
    private JLabel repetitionHour;
    private JLabel repetitionMinutes;
    private JCheckBox dayCheck;
    private JCheckBox hourCheck;
    private JSpinner repetitionMins;
    private JButton scheduleButton;
    private JCheckBox enableRep;

    //Create User Components
    private JFrame userFrame;
    private JPanel userPanel;
    private GridBagConstraints userGrid;
    private JLabel labelUser;
    private JLabel labelPass;
    private JPasswordField setPassword;
    private JLabel enableLabel;
    private JTextField setUsername;
    private JCheckBox editName;
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

    //Create time variables
    private int year = 2020;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int durMins = 1;
    private int repeatMins = 30;
    private boolean meridiem = false;
    private boolean repeatDay = false;
    private boolean repeatHour = false;
    private String testDateTime = "";

    //Create dummy JLabel
    private JLabel filler;

    //Create global repetition spinner Model
    SpinnerNumberModel repetitionModel;

    //Create Date Regex Pattern
    String date;
    private static final String dateRegex =
            "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)" +
                    "(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})" +
                    "$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)" +
                    "?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))" +
                    "$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)" +
                    "(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";

    private static final Pattern datePattern = Pattern.compile(dateRegex);

    public ControlFrame (String title){
        frame = new JFrame(title);
        frame.setSize(720, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupLoginForm();
        frame.setVisible(true);
    }

    private void setupProcessForm() {
        panel = new JPanel(new GridBagLayout());
        grid = new GridBagConstraints();
        setupMenu();
        setupScheduling();
        frame.add(panel);
    }

    private void setupScheduling() {
        setupTime();
        setupTimeLabels();
        setupTimeSpinners();
        setupDurationLabels();
        setupMeridiem();
        setupRepetition();

        scheduleButton = new JButton("Schedule!");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 9;
        panel.add(scheduleButton, grid);

        TimeListener timeL = new TimeListener();
        SpinListener spinL = new SpinListener();

        amRadio.addItemListener(timeL);
        pmRadio.addItemListener(timeL);
        enableRep.addItemListener(timeL);
        dayCheck.addItemListener(timeL);
        hourCheck.addItemListener(timeL);
        yearSpinner.addChangeListener(spinL);
        monthSpinner.addChangeListener(spinL);
        daySpinner.addChangeListener(spinL);
        hourSpinner.addChangeListener(spinL);
        minuteSpinner.addChangeListener(spinL);
        durationSpin.addChangeListener(spinL);
        repetitionMins.addChangeListener(spinL);
        scheduleButton.addActionListener(e -> scheduleBillboard());
    }

    private void scheduleBillboard() {
        if (day < 10 && month < 10){
            date = "0" + day + "/0" + month + "/" + year;
        } else if(day < 10){
            date = "0" + day + "/" + month + "/" + year;
        } else if (month < 10){
            date = day + "/0" + month + "/" + year;
        } else {
            date = day + "/" + month + "/" + year;
        }
        if(validateDate(date)){
            if(repeatDay == true){
                repeatMins = 1440;
            } else if(repeatHour == true){
                repeatMins = 60;
            } else {
                repeatMins = (Integer) repetitionMins.getValue();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime scheduleTime = LocalDateTime.parse(testDateTime, formatter);
            Duration dur = Duration.ofMinutes(durMins);
            Duration rep = Duration.ofMinutes(repeatMins);
            ScheduleBillboardRequest billboard;
            if(repeatMins > 0){
                billboard = new ScheduleBillboardRequest(nameUser, scheduleTime, dur, sessionToken, rep);
            } else {
                billboard = new ScheduleBillboardRequest(nameUser, scheduleTime, dur, sessionToken);
            }
            Object obj = null;
            try {
                obj = scheduleTest(billboard);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (obj.getClass() == Boolean.class){
                JOptionPane successBox = new JOptionPane();
                ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/Checkmark_green.jpg"));
                successBox.showMessageDialog(frame, "Billboard Successfully Scheduled!", "Billboard Scheduled", JOptionPane.INFORMATION_MESSAGE, icon);
                frame.validate();
            }
            else if (obj.getClass() == ErrorMessage.class){
                JOptionPane failBox = new JOptionPane();
                failBox.showMessageDialog(frame, "<html>Billboard Not Scheduled! ERROR in connecting to server<br/>"
                                 + "<i>" + ((ErrorMessage) obj).getErrorMessage() + "<i/><html/>",
                        "Billboard Didn't Schedule", JOptionPane.WARNING_MESSAGE);
                frame.validate();
            }


        } else if (!validateDate(date)){
            UIManager ui = new UIManager();
            ui.put("OptionPane.messageForeground", Color.RED);
            JOptionPane errorBox = new JOptionPane();
            errorBox.showMessageDialog(frame, "Make sure the Schedule is after Today's Date and Time " +
                    "is a VALID date and has a Billboard Name", "Invalid Schedule", JOptionPane.WARNING_MESSAGE);
            frame.validate();
        }
    }

    private Object scheduleTest(ScheduleBillboardRequest billboard) throws Exception{
        Socket socket = new Socket("localhost", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(billboard);
        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInputStream.readObject();
        return obj;
    }

    private boolean validateDate(String testDate){
        try{
            if(hour < 10){
                if(minute < 10){
                    if(meridiem){
                        testDateTime = testDate + " " + (hour + 12) + ":0" + minute;
                    } else if (!meridiem){
                        testDateTime = testDate + " 0" + hour + ":0" + minute;
                    }
                } else if (minute >= 10){
                    if(meridiem){
                        testDateTime = testDate + " " + (hour + 12) + ":" + minute;
                    } else if (!meridiem){
                        testDateTime = testDate + " 0" + hour + ":" + minute;
                    }
                }
            } else if (hour >= 10 && hour < 12){
                if(minute < 10){
                    if(meridiem){
                        testDateTime = testDate + " " + (hour + 12) + ":0" + minute;
                    } else if (!meridiem){
                        testDateTime = testDate + " " + hour + ":0" + minute;
                    }
                } else if (minute >= 10){
                    if(meridiem){
                        testDateTime = testDate + " " + (hour + 12) + ":" + minute;
                    } else if (!meridiem){
                        testDateTime = testDate + " " + hour + ":" + minute;
                    }
                }
            } else if (hour == 12){
                if(minute < 10){
                    if(meridiem){
                        testDateTime = testDate + " " + hour + ":0" + minute;
                    } else if (!meridiem){
                        testDateTime = testDate + " " +  "00:0" + minute;
                    }
                } else if (minute >= 10){
                    if(meridiem){
                        testDateTime = testDate + " " + hour + ":" + minute;
                    } else if (!meridiem){
                        testDateTime = testDate + " " + "00:" + minute;
                    }
                }
            }
            nameUser = setUsername.getText();
            if(nameUser == null || nameUser.length() == 0){
                return false;
            } else {
                LocalDateTime today = LocalDateTime.now();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String todayDate = today.format(format);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date inputDate = sdf.parse(testDateTime);
                Date checkDate = sdf.parse(todayDate);
                if(inputDate.before(checkDate)){
                    return false;
                } else {
                    Matcher matcher = datePattern.matcher(testDate);
                    return matcher.matches();
                }
            }
        } catch(ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    private void setupRepetition() {
        repetitionLabel = new JLabel("Set Repetition");
        repetitionLabel.setForeground(new Color(70, 70 ,150));
        Font repF = repetitionLabel.getFont();
        repetitionLabel.setFont(repF.deriveFont(repF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 6;
        panel.add(repetitionLabel, grid);

        repetitionDay = new JLabel("Day");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 7;
        panel.add(repetitionDay, grid);

        repetitionHour = new JLabel("Hour");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 7;
        panel.add(repetitionHour, grid);

        repetitionMinutes = new JLabel("Minutes");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 7;
        panel.add(repetitionMinutes, grid);

        dayCheck = new JCheckBox();
        dayCheck.setEnabled(false);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 8;
        panel.add(dayCheck, grid);

        hourCheck = new JCheckBox();
        hourCheck.setEnabled(false);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 8;
        panel.add(hourCheck, grid);

        repetitionModel = new SpinnerNumberModel(repeatMins, durMins + 1, 59, 1);
        repetitionMins = new JSpinner(repetitionModel);
        repetitionMins.setEnabled(false);
        repetitionMins.setValue(0);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 8;
        panel.add(repetitionMins, grid);

        enableRep = new JCheckBox();
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 7;
        panel.add(enableRep, grid);

        enableLabel = new JLabel("Repetition?");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 6;
        panel.add(enableLabel, grid);
    }

    private void setupMeridiem(){
        amRadio = new JRadioButton("Am");
        amRadio.setVerticalTextPosition(SwingConstants.BOTTOM);
        amRadio.setHorizontalTextPosition(SwingConstants.CENTER);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 4;
        panel.add(amRadio, grid);
        amRadio.setVisible(true);

        pmRadio = new JRadioButton("Pm");
        pmRadio.setVerticalTextPosition(SwingConstants.BOTTOM);
        pmRadio.setHorizontalTextPosition(SwingConstants.CENTER);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 7;
        grid.gridy = 4;
        panel.add(pmRadio, grid);
        pmRadio.setVisible(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(amRadio);
        bg.add(pmRadio);
        if(meridiem){
            pmRadio.setSelected(true);
        } else if(!meridiem){
            amRadio.setSelected(true);
        }
    }

    private void setupDurationLabels() {
        durationLabel = new JLabel("Set Duration");
        durationLabel.setForeground(new Color(70, 70 ,150));
        Font durF = durationLabel.getFont();
        durationLabel.setFont(durF.deriveFont(durF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 6;
        panel.add(durationLabel, grid);

        durationMins = new JLabel("Minutes");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 7;
        panel.add(durationMins, grid);

        SpinnerNumberModel durationModel = new SpinnerNumberModel(durMins, 1, 59, 1);
        durationSpin = new JSpinner(durationModel);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 8;
        panel.add(durationSpin, grid);

        filler = new JLabel("I");
        filler.setForeground(panel.getBackground());
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 5;
        panel.add(filler, grid);
    }

    private void setupTimeLabels() {
        timeLabel = new JLabel("Set Time");
        timeLabel.setForeground(new Color(70, 70 ,150));
        Font timeF = timeLabel.getFont();
        timeLabel.setFont(timeF.deriveFont(timeF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 1;
        panel.add(timeLabel, grid);
        timeLabel.setVisible(true);

        timeYear = new JLabel("Year");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 3;
        panel.add(timeYear, grid);
        timeYear.setVisible(true);

        timeMonth = new JLabel("Month");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 3;
        panel.add(timeMonth, grid);
        timeMonth.setVisible(true);

        timeDay = new JLabel("Day");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 3;
        panel.add(timeDay, grid);
        timeDay.setVisible(true);

        timeHour = new JLabel("Hour");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 3;
        panel.add(timeHour, grid);
        timeHour.setVisible(true);

        timeMinute = new JLabel("Minutes");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 3;
        panel.add(timeMinute, grid);
        timeMinute.setVisible(true);

        timeA = new JLabel("Meridiem?");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 6;
        grid.gridy = 3;
        panel.add(timeA, grid);
        timeA.setVisible(true);
    }

    private void setupTimeSpinners() {
        SpinnerNumberModel yearModel = new SpinnerNumberModel(year, year, year + 1, 1);
        yearSpinner = new JSpinner(yearModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(editor);

        SpinnerNumberModel monthModel = new SpinnerNumberModel(month, 1, 12, 1);
        monthSpinner = new JSpinner(monthModel);

        SpinnerNumberModel dayModel = new SpinnerNumberModel(day, 1, 31, 1);
        daySpinner = new JSpinner(dayModel);

        SpinnerNumberModel hourModel = new SpinnerNumberModel(hour, 1, 12, 1);
        hourSpinner = new JSpinner(hourModel);

        SpinnerNumberModel minuteModel = new SpinnerNumberModel(minute, 0, 59, 1);
        minuteSpinner = new JSpinner(minuteModel);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 4;
        panel.add(yearSpinner, grid);
        yearSpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 4;
        panel.add(monthSpinner, grid);
        monthSpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 4;
        panel.add(daySpinner, grid);
        daySpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 4;
        panel.add(hourSpinner, grid);
        hourSpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 4;
        panel.add(minuteSpinner, grid);
        minuteSpinner.setVisible(true);
    }

    private void setupTime() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime lastMin = today.truncatedTo(ChronoUnit.HOURS)
                .plusMinutes(1 * (today.getMinute() / 1));
        DateTimeFormatter formatYear = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter formatMonth = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter formatDay = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("hh");
        DateTimeFormatter formatMinute = DateTimeFormatter.ofPattern("mm");
        DateTimeFormatter formatMerid = DateTimeFormatter.ofPattern("a");
        year = Integer.parseInt(lastMin.format(formatYear));
        month = Integer.parseInt(lastMin.format(formatMonth));
        day = Integer.parseInt(lastMin.format(formatDay));
        hour = Integer.parseInt(lastMin.format(formatHour));
        minute = Integer.parseInt(lastMin.format(formatMinute));
        String dummyMerid = lastMin.format(formatMerid);
        if(dummyMerid.equals("pm")){
            meridiem = true;
        } else if (dummyMerid.equals("am")){
            meridiem = false;
        } else {
            System.out.println("Check the Code, Code Monkeys!");
        }
    }

    private void setupMenu() {
        prepareMenu();
        createMenu();
    }

    public void setupLoginForm(){
        setupInputs();
        setupButton();
    }

    private void setupInputs(){
        panel = new JPanel(new GridBagLayout());
        grid = new GridBagConstraints();
        username = new JTextField(20);
        password = new JPasswordField(20);
        userLabel = new JLabel("Username:");
        passLabel = new JLabel("Password:");
        username.setMinimumSize(new Dimension(50, 10));
        password.setMinimumSize(new Dimension(50, 10));
        frame.add(panel);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 0;
        panel.add(userLabel, grid);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 0;
        panel.add(username, grid);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 1;
        panel.add(passLabel, grid);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 1;
        panel.add(password, grid);
    }

    private void setupButton(){
        submit = new JButton("Login");
        submit.addActionListener(this);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 2;
        panel.add(submit, grid);
    }

    private boolean testLogin(String user, String pass) throws Exception{
        String hashed = Password.hash(pass);
        LoginRequest login = new LoginRequest(user, hashed);
        Socket socket = new Socket("localhost", 4444);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(login);
        ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
        Object obj = clientInput.readObject();
        if(obj.getClass() == String.class){
            storeSessionToken((String) obj);
            return true;
        } else {
            System.out.println(((ErrorMessage) obj).getErrorMessage());
            return false;
        }
    }

    private void storeSessionToken(String token) {
        sessionToken = token;
    }

    private void prepareMenu(){
        headerLabel = new JLabel("",JLabel.CENTER );
        statusLabel = new JLabel("",JLabel.CENTER);
        statusLabel.setSize(350,100);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        frame.add(headerLabel);
        frame.add(controlPanel);
        frame.add(statusLabel);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
    }

    private void createMenu(){
        //create a menu bar
        final JMenuBar menuBar = new JMenuBar();

        //create menus
        JMenu fileMenu = new JMenu("File");
        JMenu usersMenu = new JMenu("Users");
        JMenu viewMenu = new JMenu("View");
        final JMenu aboutMenu = new JMenu("About");

        //create menu items
        JMenuItem newMenuItem = new JMenuItem("New Billboard");
        newMenuItem.setActionCommand("New");

        JMenuItem importMenuItem = new JMenuItem("Import Billboard");
        importMenuItem.setActionCommand("Import");

        JMenuItem exportMenuItem = new JMenuItem("Export Billboard");
        exportMenuItem.setActionCommand("Export");

        JMenuItem createMenuItem = new JMenuItem("Create Users");
        createMenuItem.setActionCommand("Create");

        JMenuItem editMenuItem = new JMenuItem("Edit Users");
        editMenuItem.setActionCommand("Edit");

        JMenuItem viewBillboardItem = new JMenuItem("View Billboards");
        viewBillboardItem.setActionCommand("View Billboard");

        JMenuItem viewScheduleItem = new JMenuItem("View Schedule");
        viewScheduleItem.setActionCommand("View Schedule");

        JMenuItem viewUserItem = new JMenuItem("View Users");
        viewUserItem.setActionCommand("View User");

        JMenuItem aboutUsMenu = new JMenuItem("About Us");
        aboutUsMenu.setActionCommand("About");

        MenuItemListener menuItemListener = new MenuItemListener();

        newMenuItem.addActionListener(menuItemListener);
        importMenuItem.addActionListener(menuItemListener);
        exportMenuItem.addActionListener(menuItemListener);
        createMenuItem.addActionListener(menuItemListener);
        editMenuItem.addActionListener(menuItemListener);
        viewBillboardItem.addActionListener(menuItemListener);
        viewScheduleItem.addActionListener(menuItemListener);
        viewUserItem.addActionListener(menuItemListener);
        aboutUsMenu.addActionListener(menuItemListener);


        //add menu items to menus
        fileMenu.add(newMenuItem);
        fileMenu.add(importMenuItem);
        fileMenu.add(exportMenuItem);

        usersMenu.add(createMenuItem);
        usersMenu.add(editMenuItem);

        viewMenu.add(viewBillboardItem);
        viewMenu.add(viewScheduleItem);
        viewMenu.add(viewUserItem);

        aboutMenu.add(aboutUsMenu);

        //add menu to menubar
        menuBar.add(fileMenu);
        menuBar.add(usersMenu);
        menuBar.add(viewMenu);
        menuBar.add(aboutMenu);

        //add menubar to the frame
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    private void createUser() {
        userFrame = new JFrame("Create User");
        userFrame.setSize(700, 500);
        userFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        userPanel = new JPanel(new GridBagLayout());
        userFrame.add(userPanel);
        userPanel.setVisible(true);

        userGrid = new GridBagConstraints();

        labelUser = new JLabel("Username: ");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 1;
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
        userGrid.gridx = 2;
        userGrid.gridy = 0;
        userPanel.add(setUsername, userGrid);

        nameCount = new JLabel(nameChars + " / 50 Characters");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 3;
        userGrid.gridy = 0;
        userPanel.add(nameCount, userGrid);

        labelPass = new JLabel("Password: ");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 1;
        userGrid.gridy = 1;
        userPanel.add(labelPass, userGrid);

        setPassword = new JPasswordField(20);
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 2;
        userGrid.gridy = 1;
        userPanel.add(setPassword, userGrid);

        enableCreate = new JCheckBox("Create Billboards");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 0;
        userGrid.gridy = 2;
        userPanel.add(enableCreate, userGrid);

        enableEdit = new JCheckBox("Edit All Billboards");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 1;
        userGrid.gridy = 2;
        userPanel.add(enableEdit, userGrid);

        enableSchedule = new JCheckBox("Schedule Billboards");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 3;
        userGrid.gridy = 2;
        userPanel.add(enableSchedule, userGrid);

        enableUser = new JCheckBox("Edit Users");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 4;
        userGrid.gridy = 2;
        userPanel.add(enableUser, userGrid);

        userMake = new JButton("Create User");
        userGrid.fill = GridBagConstraints.VERTICAL;
        userGrid.gridx = 2;
        userGrid.gridy = 3;
        userPanel.add(userMake, userGrid);

        TimeListener timeL = new TimeListener();
        enableCreate.addItemListener(timeL);
        enableEdit.addItemListener(timeL);
        enableUser.addItemListener(timeL);
        enableSchedule.addItemListener(timeL);

        userMake.addActionListener(e -> {
            try {
                makeUser();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        userFrame.setVisible(true);
    }

    private void makeUser() throws Exception {
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
        CreateUserRequest userRequest = new CreateUserRequest(nameUser, perms, hashed, sessionToken);
        System.out.println(userRequest.toString());


    }

    class TextListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setUsername)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            }
        }

        public void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
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

    class SpinListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent event){
            Object spin = event.getSource();
            if(spin.equals(yearSpinner)){
                year = (Integer) yearSpinner.getValue();
            } else if (spin.equals(monthSpinner)){
                month = (Integer) monthSpinner.getValue();
            } else if (spin.equals(daySpinner)){
                day = (Integer) daySpinner.getValue();
            } else if (spin.equals(hourSpinner)){
                hour = (Integer) hourSpinner.getValue();
            } else if (spin.equals(minuteSpinner)){
                minute = (Integer) minuteSpinner.getValue();
            } else if (spin.equals(durationSpin)){
                durMins = (Integer) durationSpin.getValue();
                if(durMins == 59) {
                    repetitionModel.setMinimum(durMins + 1);
                    repetitionMins.setModel(repetitionModel);
                    ((JSpinner.DefaultEditor) repetitionMins.getEditor()).getTextField().setEditable(false);
                    repetitionMins.setToolTipText("You cannot have repetition the same as the duration, try using the Hour Checkbox!");
                }else if(repeatMins == durMins){
                    repeatMins++;
                    repetitionMins.setValue(repeatMins);
                } else {
                    repetitionMins.setToolTipText(null);
                    ((JSpinner.DefaultEditor) repetitionMins.getEditor()).getTextField().setEditable(true);
                }
            } else if (spin.equals(repetitionMins)){
                if((Integer) repetitionMins.getValue() == 0){

                } else {
                    repeatMins = (Integer) repetitionMins.getValue();
                }
            } else {
                System.out.println("Huh, how'd you end up here?");
            }
        }
    }

    class TimeListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event){
            Object mer = event.getItem();
            Object repeats = event.getItemSelectable();
            if(mer.equals(amRadio)){
                meridiem = false;
            } else if (mer.equals(pmRadio)){
                meridiem = true;
            }
            if (repeats.equals(enableRep)){
                hourCheck.setEnabled(true);
                dayCheck.setEnabled(true);
                repetitionMins.setEnabled(true);
                repetitionMins.setValue(repeatMins);
            } else if (repeats.equals(dayCheck)){
                repeatDay = true;
                repeatHour = false;
                hourCheck.setEnabled(false);
                hourCheck.setBackground(new Color(255, 150, 150));
                dayCheck.setBackground(new Color(150, 255, 150));
                repetitionMins.setValue(0);
                repetitionMins.setBackground(new Color(255, 150, 150));
                ((JSpinner.DefaultEditor) repetitionMins.getEditor()).getTextField().setEditable(false);
                repetitionMins.setToolTipText("Can only have 1 option selected");
                hourCheck.setToolTipText("Can only have 1 option selected");
            } else if (repeats.equals(hourCheck)){
                repeatDay = false;
                repeatHour = true;
                dayCheck.setEnabled(false);
                dayCheck.setBackground(new Color(255, 150, 150));
                hourCheck.setBackground(new Color(150, 255, 150));
                repetitionMins.setValue(0);
                repetitionMins.setBackground(new Color(255, 150, 150));
                ((JSpinner.DefaultEditor) repetitionMins.getEditor()).getTextField().setEditable(false);
                repetitionMins.setToolTipText("Can only have 1 option selected");
                dayCheck.setToolTipText("Can only have 1 option selected");
            } else if (repeats.equals(enableCreate)){
                boolCreate = true;
            } else if (repeats.equals(enableEdit)){
                boolEdit = true;
            } else if (repeats.equals(enableUser)){
                boolUser = true;
            } else if (repeats.equals(enableSchedule)){
                boolSchedule = true;
            }
            if(event.getStateChange() == ItemEvent.DESELECTED){
                if(event.getItemSelectable() == enableRep){
                    dayCheck.setEnabled(false);
                    hourCheck.setEnabled(false);
                    repetitionMins.setEnabled(false);
                    repetitionMins.setValue(0);
                } else if(event.getItemSelectable() == dayCheck){
                    repeatDay = false;
                    hourCheck.setEnabled(true);
                    repetitionMins.setValue(repeatMins);
                    repetitionMins.setBackground(panel.getBackground());
                    ((JSpinner.DefaultEditor) repetitionMins.getEditor()).getTextField().setEditable(true);
                    repetitionMins.setToolTipText(null);
                    hourCheck.setBackground(panel.getBackground());
                    hourCheck.setToolTipText(null);
                    dayCheck.setBackground(panel.getBackground());
                } else if(event.getItemSelectable() == hourCheck){
                    repeatHour = false;
                    dayCheck.setEnabled(true);
                    repetitionMins.setValue(repeatMins);
                    repetitionMins.setBackground(panel.getBackground());
                    ((JSpinner.DefaultEditor) repetitionMins.getEditor()).getTextField().setEditable(true);
                    repetitionMins.setToolTipText(null);
                    dayCheck.setBackground(panel.getBackground());
                    dayCheck.setToolTipText(null);
                    hourCheck.setBackground(panel.getBackground());
                } else if (event.getItemSelectable() == enableCreate){
                    boolCreate = false;
                } else if (event.getItemSelectable() == enableEdit){
                    boolEdit = false;
                } else if (event.getItemSelectable() == enableUser){
                    boolUser = false;
                } else if (event.getItemSelectable() == enableSchedule){
                    boolSchedule = false;
                }
            }
        }

    }

    class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand() == "New"){
                //Connor's Billboard Constructor
            } else if (e.getActionCommand() == "Import"){

            } else if (e.getActionCommand() == "Export"){

            } else if (e.getActionCommand() == "Create"){
                createUser();
            } else if (e.getActionCommand() == "Edit"){

            } else if (e.getActionCommand() == "View Billboard"){

            } else if (e.getActionCommand() == "View Schedule"){

            } else if (e.getActionCommand() == "View User"){

            } else if (e.getActionCommand() == "About"){
                JOptionPane aboutBox = new JOptionPane();
                aboutBox.showMessageDialog(frame,
                        "<html>This Project was made by Group_068<br/>" +
                                "(Connor Wilson n10008276)<br/>" +
                                "(Daniel Lawless n87654321)<br/>" +
                                "(Pierce Evans n09990887)<br/>" +
                                "for the CAB302 Assignment 2020<html/>",
                        "About Us",
                        JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event){
        String user = username.getText();
        String pass = new String(password.getPassword());
        boolean loginPass = false;
        try {
            loginPass = testLogin(user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (loginPass == true) {
            username = null;
            password = null;
            passLabel = null;
            userLabel = null;
            panel = null;
            frame.dispose();
            frame = new JFrame("Billboard Control Panel");
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            setupProcessForm();
        } else if (loginPass == false) {
            if(failedLogin == null){
                failedLogin = new JLabel("Incorrect Username or Password");
                failedLogin.setForeground(Color.RED);
                grid.fill = GridBagConstraints.VERTICAL;
                grid.gridx = 1;
                grid.gridy = 3;
                failedLogin.setVisible(true);
                panel.add(failedLogin, grid);
                frame.revalidate();
            } else {

            }
        } else {
            failedLogin = new JLabel("Check the code Code Monkeys");
            grid.fill = GridBagConstraints.VERTICAL;
            failedLogin.setForeground(Color.RED);
            grid.gridx = 1;
            grid.gridy = 3;
            failedLogin.setVisible(true);
            panel.add(failedLogin);
            frame.revalidate();
        }
    }
}
