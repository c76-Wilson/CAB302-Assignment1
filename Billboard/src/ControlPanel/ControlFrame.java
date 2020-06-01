package ControlPanel;
import Helper.Password;
import Helper.Requests.*;
import Helper.Responses.ErrorMessage;

import javax.swing.*;
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

    //Create Canvas Components
    private JLabel mainCanvas;
    private JColorChooser palette;
    private JLabel setTextLabel;
    private JTextArea setText;
    private JLabel setPosition;
    private JSpinner textX;
    private JSpinner textY;
    private JLabel XHeader;
    private JLabel YHeader;
    private JButton addText;
    private JButton saveBillboard;
    private JLabel characterCount;

    //Create Canvas Variables
    private int xCoords = 480;
    private int yCoords = 120;
    private String textAdd;
    private int textLength;
    private DefaultStyledDocument textDocument;

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
    private JLabel enableLabel;
    private JTextField setBillboardName;
    private String billboardName = "";
    private int nameChars = 0;
    private JLabel nameCount;

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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            LocalDateTime scheduleTime = LocalDateTime.parse(testDateTime, formatter);
            Duration dur = Duration.ofMinutes(durMins);
            Duration rep = Duration.ofMinutes(repeatMins);
            ScheduleBillboardRequest billboard;
            if(repeatMins > 0){
                billboard = new ScheduleBillboardRequest(billboardName, scheduleTime, dur, sessionToken, rep);
            } else {
                billboard = new ScheduleBillboardRequest(billboardName, scheduleTime, dur, sessionToken);
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
            errorBox.showMessageDialog(frame, "Make sure the Date and Time of Scheduling " +
                    "is after today's date and is a VALID date", "Invalid Date", JOptionPane.WARNING_MESSAGE);
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
                        testDateTime = testDate + " 0" + hour + ":0" + minute + " pm";
                    } else if (!meridiem){
                        testDateTime = testDate + " 0" + hour + ":0" + minute + " am";
                    }
                } else if (minute >= 10){
                    if(meridiem){
                        testDateTime = testDate + " 0" + hour + ":" + minute + " pm";
                    } else if (!meridiem){
                        testDateTime = testDate + " 0" + hour + ":" + minute + " am";
                    }
                }
            } else if (hour >= 10){
                if(minute < 10){
                    if(meridiem){
                        testDateTime = testDate + " " + hour + ":0" + minute + " pm";
                    } else if (!meridiem){
                        testDateTime = testDate + " " + hour + ":0" + minute + " am";
                    }
                } else if (minute >= 10){
                    if(meridiem){
                        testDateTime = testDate + " " + hour + ":" + minute + " pm";
                    } else if (!meridiem){
                        testDateTime = testDate + " " + hour + ":" + minute + " am";
                    }
                }
            }
            LocalDateTime today = LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            String todayDate = today.format(format);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            Date inputDate = sdf.parse(testDateTime);
            Date checkDate = sdf.parse(todayDate);
            if(inputDate.before(checkDate)){
                return false;
            } else {
                Matcher matcher = datePattern.matcher(testDate);
                return matcher.matches();
            }
        } catch(ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    private void setupEditing() {
        setTextLabel = new JLabel("Set Text");
        setTextLabel.setForeground(new Color(70, 70 ,150));
        Font setF = setTextLabel.getFont();
        setTextLabel.setFont(setF.deriveFont(setF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 10;
        panel.add(setTextLabel, grid);

        setText = new JTextArea(8, 12);
        setText.setLineWrap(true);
        setText.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null || setText.getText().length() >= 120) {
                    return;
                }

                super.insertString(offs, str, a);
            }
        });
        setText.getDocument().putProperty("Area", setText);
        setText.getDocument().addDocumentListener(new TextListener());
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 11;
        panel.add(setText, grid);

        characterCount = new JLabel(textLength + " / 120 Characters");
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 12;
        panel.add(characterCount, grid);

        setPosition = new JLabel("Set Position");
        setPosition.setForeground(new Color(70, 70 ,150));
        Font posF = setPosition.getFont();
        setPosition.setFont(posF.deriveFont(posF.getStyle() | Font.ITALIC));
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 13;
        panel.add(setPosition, grid);

        XHeader = new JLabel("X");
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 14;
        panel.add(XHeader, grid);

        YHeader = new JLabel("Y");
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 10;
        grid.gridy = 14;
        panel.add(YHeader, grid);

        SpinnerNumberModel xModel = new SpinnerNumberModel(xCoords, 0, 960, 1);
        textX = new JSpinner(xModel);
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 15;
        panel.add(textX, grid);

        SpinnerNumberModel yModel = new SpinnerNumberModel(yCoords, 0, 240, 1);
        textY = new JSpinner(yModel);
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 10;
        grid.gridy = 15;
        panel.add(textY, grid);

        addText = new JButton("Add Text!");
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 9;
        grid.gridy = 16;
        panel.add(addText, grid);
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
        dayCheck.setEnabled(false);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 2;
        grid.gridy = 7;
        panel.add(dayCheck, grid);

        hourCheck = new JCheckBox();
        hourCheck.setEnabled(false);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 3;
        grid.gridy = 7;
        panel.add(hourCheck, grid);

        repetitionModel = new SpinnerNumberModel(repeatMins, durMins + 1, 59, 1);
        repetitionMins = new JSpinner(repetitionModel);
        repetitionMins.setEnabled(false);
        repetitionMins.setValue(0);
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 4;
        grid.gridy = 7;
        panel.add(repetitionMins, grid);

        enableRep = new JCheckBox();
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 6;
        panel.add(enableRep, grid);

        enableLabel = new JLabel("Repetition?");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 5;
        grid.gridy = 5;
        panel.add(enableLabel, grid);
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
        setBillboardName = new JTextField(50);
        setBillboardName.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null || setBillboardName.getText().length() >= 50) {
                    return;
                }

                super.insertString(offs, str, a);
            }
        });
        setBillboardName.getDocument().putProperty("Area", setBillboardName);
        setBillboardName.getDocument().addDocumentListener(new TextListener());
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 8;
        grid.gridy = 0;
        panel.add(setBillboardName, grid);

        nameCount = new JLabel(nameChars + " / 50 Characters");
        grid.fill = GridBagConstraints.VERTICAL;
        grid.gridx = 9;
        grid.gridy = 0;
        panel.add(nameCount, grid);

        mainCanvas = new JLabel("Test");
        mainCanvas.setForeground(new Color(50, 50, 50));
        mainCanvas.setMaximumSize(new Dimension(960, 240));
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 8;
        grid.gridy = 1;
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
        grid.gridheight = 7;
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

    class TextListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setBillboardName)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            } else if (area.equals(setText)){
                textLength = doc.getLength();
                characterCount.setText(textLength + " / 120 Characters");
            }
        }

        public void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setBillboardName)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            } else if (area.equals(setText)){
                textLength = doc.getLength();
                characterCount.setText(textLength + " / 120 Characters");
            }
        }

        public void changedUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            Object area = doc.getProperty("Area");
            if(area.equals(setBillboardName)){
                nameChars = doc.getLength();
                nameCount.setText(nameChars + " / 50 Characters");
            } else if (area.equals(setText)){
                textLength = doc.getLength();
                characterCount.setText(textLength + " / 120 Characters");
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
                }
            }
        }

    }

    class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand() == "New"){

            } else if (e.getActionCommand() == "Import"){

            } else if (e.getActionCommand() == "Export"){

            } else if (e.getActionCommand() == "Create"){

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
            grid.gridy = 5;
            failedLogin.setVisible(true);
            panel.add(failedLogin);
            frame.revalidate();
        }
    }
}
