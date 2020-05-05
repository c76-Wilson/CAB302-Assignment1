package ControlPanel;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Panel extends JFrame implements Runnable {
    private JFrame panel;
    private JPanel panelMain;
    private JLabel firstSchedule;
    private JLabel firstLabel;
    private JPanel editingPane;
    private JPanel settingsPane;
    private JLabel secondLabel;
    private JLabel secondSchedule;
    private JLabel mainViewer;
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

    // Frame Variables
    Dimension frameSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int WIDTH = frameSize.width;
    private int HEIGHT = frameSize.height;
    Dimension frame = new Dimension(WIDTH,HEIGHT);

    public Panel(String title) throws IOException {
        super(title);

        run();
    }

    /**
     * This Method created the Gui Frame Defaults
     */
    public void setFrameDefaults() {
        setPreferredSize(frame);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocation(200, 200);
        panelMain = returnPanel();
        editingPane = returnPanel();
        settingsPane = returnPanel();
        setVisible(true);
    }

    private JPanel returnPanel() {
        JPanel dummy = new JPanel();

        return dummy;
    }

    public void renderImage(String image_dir) {
        mainViewer = setLabel();
        ImageIcon icon = new ImageIcon(Panel.class.getResource(image_dir));
        mainViewer.setIcon(icon);
    }

    private JLabel setLabel() {
        JLabel temp = new JLabel();

        return temp;
    }

    /**
     * public static void main(String[] args) {
     * SwingUtilities.invokeLater(new Runnable() {
     * public void run() {
     * FileExtractorGUI gui = new FileExtractorGUI();
     * JFrame frame = new JFrame();
     * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     * frame.getContentPane().add(gui);
     * frame.pack();
     * frame.setVisible(true);
     * }
     * });
     * }
     */
    @Override
    public void run() {
        setFrameDefaults();
    }

    public static void main(String[] args) throws IOException{
        JFrame first = new Panel("First Test");
        ((Panel) first).renderImage("C:/Users/Pierce/Pictures/itsame.png");
    }
}

