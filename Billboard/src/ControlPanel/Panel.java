package ControlPanel;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class Panel {

    private JLabel sixthLabel;
    private JLabel sixthSchedule;
    private JLabel seventhSchedule;
    private JPanel panelMain;
    private JLabel firstLabel;
    private JPanel editingPane;
    private JPanel settingsPane;
    private JLabel secondLabel;
    private JLabel fourthLabel;
    private JLabel thirdLabel;
    private JComboBox selectWeekCombo;
    private JButton addSchedButton;
    private JLabel firstSchedule;
    private JLabel secondSchedule;
    private JLabel thirdSchedule;
    private JLabel fourthSchedule;
    private JLabel fifthLabel;
    private JLabel fifthSchedule;
    private JLabel seventhLabel;
    private JLabel mainViewer;

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                JFrame testFrame = new PanelFrame("Hello");
                testFrame.setSize(1280, 720);
                testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                testFrame.setVisible(true);
            }

        });
    }
}

