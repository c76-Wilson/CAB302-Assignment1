package ControlPanel;
import javax.swing.*;

public class Panel extends JFrame {
    private static JFrame panel;
    private JPanel panelMain;
    private JLabel firstSchedule;
    private JLabel firstLabel;
    private JPanel editingPane;
    private JPanel settingsPane;
    private JLabel secondLabel;
    private JLabel secondSchedule;
    private static JLabel mainViewer;
    private JComboBox selectWeekCombo;
    private JLabel fourthLabel;
    private JLabel thirdLabel;
    private JButton addSchedButton;
    private JLabel thirdSchedule;
    private JLabel fourthSchedule;
    private JLabel fifthLabel;
    private JLabel fifthSchedule;
    private JLabel sixthLabel;
    private JLabel sixthSchedule;
    private JLabel seventhLabel;
    private JLabel seventhSchedule;

    public static void setFrameDefaults(){
        JFrame frame = panel;
        panel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setSize(1280, 720);
        panel.setLocation(200, 200);
    }

    public static void renderImage(String image_dir){
        JLabel mainView = mainViewer;
        ImageIcon icon = new ImageIcon(Panel.class.getResource(image_dir));
        mainView.setIcon(icon);
    }

    static class panelTest extends Panel {
        public static void main(String[] args) {
            setFrameDefaults();
            renderImage("C:/Users/Pierce/Pictures/itsame.png");
        }

        /**
         * public static void main(String[] args) {
         *     SwingUtilities.invokeLater(new Runnable() {
         *         public void run() {
         *             FileExtractorGUI gui = new FileExtractorGUI();
         *             JFrame frame = new JFrame();
         *             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         *             frame.getContentPane().add(gui);
         *             frame.pack();
         *             frame.setVisible(true);
         *         }
         *     });
         * }
         */
    }
