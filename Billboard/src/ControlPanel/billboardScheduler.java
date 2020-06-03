package ControlPanel;

import Helper.Requests.ScheduleBillboardRequest;
import Helper.Responses.ErrorMessage;

import javax.swing.*;
import java.awt.*;
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

public class billboardScheduler {

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

    String date;

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
            billboardName = setBillboardName.getText();
            if(billboardName == null || billboardName.length() == 0){
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
}

