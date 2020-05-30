package ControlPanel;
import Helper.Password;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

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

    //Create Canvas Components
    private JLabel mainCanvas;
    private JColorChooser palette;
    private JLabel setTextLabel;
    private JTextField setText;
    private JLabel setPosition;
    private JSpinner textX;
    private JSpinner textY;
    private JButton addText;
    private JButton saveBillboard;

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

    //Create List Components
    private JLabel firstLabel;
    private JLabel firstCanvas;
    private JLabel secondLabel;
    private JLabel secondCanvas;
    private JLabel thirdLabel;
    private JLabel thirdCanvas;
    private JLabel fourthLabel;
    private JLabel fourthCanvas;
    private JLabel fifthLabel;
    private JLabel fifthCanvas;
    private JLabel sixthLabel;
    private JLabel sixthCanvas;
    private JLabel seventhLabel;
    private JLabel seventhCanvas;
    private JLabel eighthLabel;
    private JLabel eighthCanvas;

    //Create time variables
    private int year = 2020;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int durMins = 1;
    private int repeatMins = 59;
    private boolean meridiem = false;
    private boolean repeatDay = false;
    private boolean repeatHour = false;

    //Create dummy JLabel
    private JLabel filler;

    public ControlFrame(String title, boolean loginTrue){
        if(loginTrue){
            frame = new JFrame(title);
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setupProcessForm();
            frame.setVisible(true);
        }
    }

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
        setupCanvas();
        setupColourPalette();
        frame.add(panel);
    }

    private void setupScheduling() {
        setupTime();
        setupTimeLabels();
        setupTimeSpinners();
        setupDurationLabels();
        setupMeridiem();
        setupRepetition();
        setupEditing();
        scheduleButton = new JButton("Schedule!");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 8;
        panel.add(scheduleButton, grid);
        TimeListener timeL = new TimeListener();
        amRadio.addItemListener(timeL);
        pmRadio.addItemListener(timeL);
    }

    private void setupEditing() {

    }

    private void setupRepetition() {
        repetitionLabel = new JLabel("Set Repetition");
        repetitionLabel.setForeground(new Color(70, 70 ,150));
        Font repF = repetitionLabel.getFont();
        repetitionLabel.setFont(repF.deriveFont(repF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 5;
        panel.add(repetitionLabel, grid);

        repetitionDay = new JLabel("Day");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 6;
        panel.add(repetitionDay, grid);

        repetitionHour = new JLabel("Hour");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 6;
        panel.add(repetitionHour, grid);

        repetitionMinutes = new JLabel("Minutes");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 6;
        panel.add(repetitionMinutes, grid);

        dayCheck = new JCheckBox();
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 7;
        panel.add(dayCheck, grid);

        hourCheck = new JCheckBox();
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 7;
        panel.add(hourCheck, grid);

        SpinnerNumberModel repetitionModel = new SpinnerNumberModel(repeatMins, durMins, 59, 1);
        repetitionMins = new JSpinner(repetitionModel);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 7;
        panel.add(repetitionMins, grid);
    }

    private void setupMeridiem(){
        amRadio = new JRadioButton("Am");
        amRadio.setVerticalTextPosition(SwingConstants.BOTTOM);
        amRadio.setHorizontalTextPosition(SwingConstants.CENTER);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 3;
        panel.add(amRadio, grid);
        amRadio.setVisible(true);

        pmRadio = new JRadioButton("Pm");
        pmRadio.setVerticalTextPosition(SwingConstants.BOTTOM);
        pmRadio.setHorizontalTextPosition(SwingConstants.CENTER);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 7;
        grid.gridy = 3;
        panel.add(pmRadio, grid);
        pmRadio.setVisible(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(amRadio);
        bg.add(pmRadio);
    }

    private void setupDurationLabels() {
        durationLabel = new JLabel("Set Duration");
        durationLabel.setForeground(new Color(70, 70 ,150));
        Font durF = durationLabel.getFont();
        durationLabel.setFont(durF.deriveFont(durF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 5;
        panel.add(durationLabel, grid);

        durationMins = new JLabel("Minutes");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 6;
        panel.add(durationMins, grid);

        SpinnerNumberModel durationModel = new SpinnerNumberModel(durMins, 1, 59, 1);
        durationSpin = new JSpinner(durationModel);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 7;
        panel.add(durationSpin, grid);

        filler = new JLabel("I");
        filler.setForeground(panel.getBackground());
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 4;
        panel.add(filler, grid);
    }

    private void setupTimeLabels() {
        timeLabel = new JLabel("Set Time");
        timeLabel.setForeground(new Color(70, 70 ,150));
        Font timeF = timeLabel.getFont();
        timeLabel.setFont(timeF.deriveFont(timeF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 0;
        panel.add(timeLabel, grid);
        timeLabel.setVisible(true);

        timeYear = new JLabel("Year");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 0;
        grid.gridy = 2;
        panel.add(timeYear, grid);
        timeYear.setVisible(true);

        timeMonth = new JLabel("Month");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 2;
        panel.add(timeMonth, grid);
        timeMonth.setVisible(true);

        timeDay = new JLabel("Day");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 2;
        panel.add(timeDay, grid);
        timeDay.setVisible(true);

        timeHour = new JLabel("Hour");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 2;
        panel.add(timeHour, grid);
        timeHour.setVisible(true);

        timeMinute = new JLabel("Minutes");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 2;
        panel.add(timeMinute, grid);
        timeMinute.setVisible(true);

        timeA = new JLabel("Meridiem?");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 6;
        grid.gridy = 2;
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
        grid.gridy = 3;
        panel.add(yearSpinner, grid);
        yearSpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 1;
        grid.gridy = 3;
        panel.add(monthSpinner, grid);
        monthSpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 3;
        panel.add(daySpinner, grid);
        daySpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 3;
        panel.add(hourSpinner, grid);
        hourSpinner.setVisible(true);

        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 3;
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

    private void setupCanvas() {
        mainCanvas = new JLabel("Test");
        mainCanvas.setForeground(new Color(50, 50, 50));
        mainCanvas.setMaximumSize(new Dimension(960, 240));
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 8;
        grid.gridy = 0;
        panel.add(mainCanvas, grid);
        mainCanvas.setVisible(true);
    }

    private void setupColourPalette() {
        palette = new JColorChooser();
        palette.getSelectionModel().addChangeListener(e -> {
            Color colour = palette.getColor();
            mainCanvas.setForeground(colour);
        });

        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 8;
        grid.gridy = 10;
        panel.add(palette, grid);
        palette.setVisible(true);
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
        JMenu editMenu = new JMenu("Edit");
        final JMenu aboutMenu = new JMenu("About");
        final JMenu linkMenu = new JMenu("Links");

        //create menu items
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setActionCommand("New");

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setActionCommand("Open");

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setActionCommand("Save");

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setActionCommand("Exit");

        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setActionCommand("Cut");

        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setActionCommand("Copy");

        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setActionCommand("Paste");

        MenuItemListener menuItemListener = new MenuItemListener();

        newMenuItem.addActionListener(menuItemListener);
        openMenuItem.addActionListener(menuItemListener);
        saveMenuItem.addActionListener(menuItemListener);
        exitMenuItem.addActionListener(menuItemListener);
        cutMenuItem.addActionListener(menuItemListener);
        copyMenuItem.addActionListener(menuItemListener);
        pasteMenuItem.addActionListener(menuItemListener);

        final JCheckBoxMenuItem showWindowMenu = new JCheckBoxMenuItem("Show About", true);
        showWindowMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if(showWindowMenu.getState()){
                    menuBar.add(aboutMenu);
                } else {
                    menuBar.remove(aboutMenu);
                }
            }
        });
        final JRadioButtonMenuItem showLinksMenu = new JRadioButtonMenuItem(
                "Show Links", true);
        showLinksMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if(menuBar.getMenu(3)!= null){
                    menuBar.remove(linkMenu);
                    frame.repaint();
                } else {
                    menuBar.add(linkMenu);
                    frame.repaint();
                }
            }
        });
        //add menu items to menus
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(showWindowMenu);
        fileMenu.addSeparator();
        fileMenu.add(showLinksMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);

        //add menu to menubar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(aboutMenu);
        menuBar.add(linkMenu);

        //add menubar to the frame
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    class TimeListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event){
            Object mer = event.getItem();
            if(mer.equals(amRadio)){
                meridiem = false;
            } else if (mer.equals(pmRadio)){
                meridiem = true;
            } else {
                System.out.println("Huh?");
            }
        }
    }

    class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand() == "Exit"){
                frame.dispose();
            } else {
                statusLabel.setText(e.getActionCommand() + " JMenuItem clicked.");
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
            new ControlFrame("Billboard Control Panel", true);
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
            grid.gridy = 5;
            failedLogin.setVisible(true);
            panel.add(failedLogin);
            frame.revalidate();
        }
    }
}
