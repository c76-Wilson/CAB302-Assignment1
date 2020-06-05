package ControlPanel;

import Helper.Requests.ScheduleBillboardRequest;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class ScheduleBillboard extends JDialog {
    // Panels
    JPanel inputPanel;
    JPanel recurrencePanel;

    // Components
    JLabel timeLabel;
    JSpinner timeSpinner;
    JLabel durationLabel;
    JSpinner durationSpinner;
    JLabel recurrenceLabel;
    JCheckBox recurrenceCheckbox;
    JRadioButton recurDaily;
    JRadioButton recurHourly;
    JRadioButton recurMinutely;
    JSpinner minuteRecurrenceSpinner;
    JButton submitButton;

    // Other
    String serverIP;
    int serverPort;
    SessionToken sessionToken;
    String billboardName;

    public ScheduleBillboard(Dimension size, String serverIP, int serverPort, SessionToken sessionToken, String billboardName){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;
        this.billboardName = billboardName;

        setSize(size);
        setupSchedule();
    }

    private void setupSchedule(){
        setLayout(new BorderLayout());

        // Schedule time
        timeLabel = new JLabel("Schedule Time");
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "dd/MM/yyyy HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());

        // Schedule duration
        durationLabel = new JLabel("Schedule Duration");
        durationSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor durationEditor = new JSpinner.DateEditor(durationSpinner, "mm");
        durationSpinner.setEditor(durationEditor);
        durationSpinner.setValue(new Date());

        // Recurrence
        recurrenceLabel = new JLabel(" minutes.");
        recurrenceLabel.setEnabled(false);
        recurrenceCheckbox = new JCheckBox("Enable Recurrence");
        recurrenceCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    recurrenceLabel.setEnabled(true);
                    recurrencePanel.setEnabled(true);
                    recurDaily.setEnabled(true);
                    recurHourly.setEnabled(true);
                    recurMinutely.setEnabled(true);
                }
                else{
                    recurrenceLabel.setEnabled(false);
                    recurrencePanel.setEnabled(false);
                    recurDaily.setEnabled(false);
                    recurHourly.setEnabled(false);
                    recurMinutely.setEnabled(false);
                }
            }
        });
        recurDaily = new JRadioButton("Recur Daily");
        recurDaily.setEnabled(false);
        recurHourly = new JRadioButton("Recur Hourly");
        recurHourly.setEnabled(false);
        recurMinutely = new JRadioButton("Recur every: ");
        recurMinutely.setEnabled(false);
        recurMinutely.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    minuteRecurrenceSpinner.setEnabled(true);
                }
                else{
                    minuteRecurrenceSpinner.setEnabled(false);
                }
            }
        });

        minuteRecurrenceSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor recurrenceEditor = new JSpinner.DateEditor(minuteRecurrenceSpinner, "mm");
        minuteRecurrenceSpinner.setEditor(recurrenceEditor);
        minuteRecurrenceSpinner.setEnabled(false);

        // Submit button
        submitButton = new JButton("Submit Schedule");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((Date)timeSpinner.getValue()).before(new Date())){
                    JOptionPane.showMessageDialog(ScheduleBillboard.this,
                            "Schedule time must be in the future!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                else{
                    Calendar durationCalendar = Calendar.getInstance();
                    durationCalendar.setTime(((Date)durationSpinner.getValue()));
                    if (durationCalendar.get(Calendar.MINUTE) <= 0){
                        JOptionPane.showMessageDialog(ScheduleBillboard.this,
                                "Duration must be greater than 0!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        ScheduleBillboardRequest scheduleBillboardRequest = null;
                        if (recurrenceCheckbox.isSelected()) {
                            Calendar recurrenceCalendar = Calendar.getInstance();
                            recurrenceCalendar.setTime((Date) minuteRecurrenceSpinner.getValue());
                            if (recurMinutely.isSelected() && (recurrenceCalendar.get(Calendar.MINUTE) <= durationCalendar.get(Calendar.MINUTE))) {
                                JOptionPane.showMessageDialog(ScheduleBillboard.this,
                                        "Schedule can't recur more frequently than its duration!",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            else if (recurMinutely.isSelected()) {
                                scheduleBillboardRequest = new ScheduleBillboardRequest(billboardName, dateToLocalDateTime(((Date)timeSpinner.getValue())), Duration.parse(String.format("PT%dM", durationCalendar.get(Calendar.MINUTE))), sessionToken.getSessionToken(), Duration.parse(String.format("PT%dM", recurrenceCalendar.get(Calendar.MINUTE))));
                            }
                            else if (recurHourly.isSelected()) {
                                scheduleBillboardRequest = new ScheduleBillboardRequest(billboardName, dateToLocalDateTime(((Date)timeSpinner.getValue())), Duration.parse(String.format("PT%dM", durationCalendar.get(Calendar.MINUTE))), sessionToken.getSessionToken(), Duration.parse("PT60M"));
                            }
                            else if (recurMinutely.isSelected()){
                                scheduleBillboardRequest = new ScheduleBillboardRequest(billboardName, dateToLocalDateTime(((Date)timeSpinner.getValue())), Duration.parse(String.format("PT%dM", durationCalendar.get(Calendar.MINUTE))), sessionToken.getSessionToken(), Duration.parse("PT1440M"));
                            }
                        }
                        else{
                            scheduleBillboardRequest = new ScheduleBillboardRequest(billboardName, dateToLocalDateTime(((Date)timeSpinner.getValue())), Duration.parse(String.format("PT%dM", durationCalendar.get(Calendar.MINUTE))), sessionToken.getSessionToken());
                        }

                        scheduleBillboard(scheduleBillboardRequest);
                    }
                }
            }
        });

        // Initialise input panel
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());

        // Add main details to panel
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.PAGE_AXIS));

        constraints.gridx = 0;
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timePanel.add(timeLabel);
        timePanel.add(timeSpinner);
        inputPanel.add(timePanel, constraints);

        JPanel durationPanel = new JPanel();
        durationPanel.setLayout(new BoxLayout(durationPanel, BoxLayout.PAGE_AXIS));

        constraints.gridx = 1;
        durationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        durationPanel.add(durationLabel);
        durationPanel.add(durationSpinner);
        inputPanel.add(durationPanel, constraints);

        constraints.gridx = 4;
        inputPanel.add(recurrenceCheckbox, constraints);

        // Initialise recurrence panel
        recurrencePanel = new JPanel();
        recurrencePanel.setLayout(new GridBagLayout());

        // Create radiobutton group
        ButtonGroup group = new ButtonGroup();
        group.add(recurDaily);
        group.add(recurHourly);
        group.add(recurMinutely);
        group.add(recurDaily);

        // Add recurrence components to panel
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.LINE_START;
        recurrencePanel.add(recurDaily, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        recurrencePanel.add(recurHourly, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        recurrencePanel.add(recurMinutely, constraints);
        constraints.gridx = 1;
        constraints.gridy = 2;
        recurrencePanel.add(minuteRecurrenceSpinner, constraints);
        constraints.gridx = 2;
        constraints.gridy = 2;
        recurrencePanel.add(recurrenceLabel, constraints);

        constraints.gridx = 5;
        constraints.gridy = 0;
        inputPanel.add(recurrencePanel, constraints);

        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 9;
        inputPanel.add(submitButton, constraints);

        add(inputPanel);
    }

    private LocalDateTime dateToLocalDateTime(Date date){
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    private void scheduleBillboard(ScheduleBillboardRequest request){

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(request);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Boolean.class) {
                JOptionPane.showMessageDialog(this,
                        "Added to schedule",
                        "Success",
                        JOptionPane.PLAIN_MESSAGE);

                this.setModalityType(ModalityType.MODELESS);
                this.setVisible(false);
                this.dispose();
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
}
