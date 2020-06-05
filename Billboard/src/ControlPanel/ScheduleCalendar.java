package ControlPanel;

import Helper.Billboard;
import Helper.Requests.ViewScheduleRequest;
import Helper.Responses.ErrorMessage;
import Helper.ScheduledBillboard;
import Helper.SessionToken;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;

public class ScheduleCalendar extends JPanel {
    // Server/authentication
    SessionToken sessionToken;
    int serverPort;
    String serverIP;

    // Components
    static JTable tblCalendar;
    static DefaultTableModel mtblCalendar; //Table model
    static ScheduleList listPanel;
    static int realYear, realMonth, realDay, currentYear, currentMonth;
    LinkedList<ScheduledBillboard> scheduledBillboards;
    LocalDate selectedDate;

    public ScheduleCalendar(Dimension size, SessionToken sessionToken, String serverIP, int serverPort){
        this.sessionToken = sessionToken;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        setSize(size);
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents(){
        //Create controls
        mtblCalendar = new DefaultTableModel(){public boolean isCellEditable(int rowIndex, int mColIndex){return false;}};
        tblCalendar = new JTable(mtblCalendar);

        //Add controls to pane
        JPanel calendarPanel = new JPanel();
        calendarPanel.setLayout(new BoxLayout(calendarPanel, BoxLayout.Y_AXIS));
        calendarPanel.add(tblCalendar.getTableHeader());
        calendarPanel.add(tblCalendar);

        add(calendarPanel, BorderLayout.NORTH);

        GregorianCalendar cal = new GregorianCalendar(); //Create calendar
        realDay = cal.get(GregorianCalendar.DAY_OF_MONTH); //Get day
        realMonth = cal.get(GregorianCalendar.MONTH); //Get month
        realYear = cal.get(GregorianCalendar.YEAR); //Get year
        currentMonth = realMonth; //Match month and year
        currentYear = realYear;

        tblCalendar.getParent().setBackground(tblCalendar.getBackground()); //Set background

        Calendar c = Calendar.getInstance();
        //Add headers
        for (int i=0; i<7; i++){
            mtblCalendar.addColumn(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
            c.roll(Calendar.DATE, true);
        }

        //No resize/reorder
        tblCalendar.getTableHeader().setResizingAllowed(false);
        tblCalendar.getTableHeader().setReorderingAllowed(false);

        //Single cell selection
        tblCalendar.setColumnSelectionAllowed(true);
        tblCalendar.setRowSelectionAllowed(true);
        tblCalendar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCalendar.setSize(getWidth(), 100);

        //Set row/column count
        tblCalendar.setRowHeight(60);
        mtblCalendar.setColumnCount(7);
        mtblCalendar.setRowCount(1);

        //Refresh calendar
        refreshCalendar(realMonth, realYear); //Refresh calendar
    }

    public void refreshCalendar(int month, int year) {
        //Variables
        int nod, som; //Number Of Days, Start Of Month

        //Clear table
        for (int j = 0; j < 7; j++) {
            mtblCalendar.setValueAt(null, 0, j);
        }

        //Get current day
        GregorianCalendar cal = new GregorianCalendar(year, month, GregorianCalendar.DAY_OF_MONTH);

        int colCount = 0;
        //Draw calendar
        for (int i = GregorianCalendar.DAY_OF_MONTH; i <= GregorianCalendar.DAY_OF_MONTH + 6; i++) {

            LocalDate value = LocalDate.now();

            while (value.getDayOfMonth() < i){
                value = value.plusDays(1);
            }
            mtblCalendar.setValueAt(value, 0, colCount);
            colCount++;
        }

        //Apply renderers
        tblCalendar.setDefaultRenderer(tblCalendar.getColumnClass(0), new tblCalendarRenderer());
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

    private void setListPanel(LocalDate date) {
        LinkedList<ScheduledBillboard> selectedBillboards = new LinkedList<>();

        for (ScheduledBillboard billboard : getScheduledBillboards()) {
            if (billboard.getScheduleTime().toLocalDate().equals(date)) {
                selectedBillboards.add(billboard);
            }
        }

        if (listPanel != null) {
            remove(listPanel);
        }

        listPanel = new ScheduleList(sessionToken, serverIP, serverPort, selectedBillboards);

        add(listPanel, BorderLayout.CENTER);

        validate();
        repaint();
    }

    public class tblCalendarRenderer extends DefaultTableCellRenderer {
        public void setValue(Object value) {
            setText((value == null) ? "" : Integer.toString(((LocalDate)value).getDayOfMonth()));
        }
        public Component getTableCellRendererComponent (JTable table, Object value, boolean selected, boolean focused, int row, int column){
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (selected){
                setBackground(Color.LIGHT_GRAY);

                if (!((LocalDate)value).equals(selectedDate)) {
                    selectedDate = (LocalDate) value;

                    setListPanel((LocalDate) value);
                }
            }
            else {
                setBackground(new Color(255, 255, 255));

                if (value != null) {
                    if (((LocalDate)value).getDayOfMonth() == realDay && currentMonth == realMonth && currentYear == realYear) { //Today
                        setBackground(new Color(220, 220, 255));
                    }
                }
            }
            setBorder(null);
            setForeground(Color.black);
            return this;
        }
    }
}


