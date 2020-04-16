<<<<<<< HEAD
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Billboard_Viewer extends JFrame implements Runnable {
    private JPanel mainPanel;
    private JLabel messageLabel;
    private JTextArea informationLocked;
    private JScrollPane informationBox;
    private JLabel imagePanel;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private int WIDTH = screenSize.width;
    private int HEIGHT = screenSize.height;



    public Billboard_Viewer(String name) throws IOException {
        //set title
        super(name);

        run();



    }

    //method to create the GUI window and add the elements to it
    private void createGUI()
    {
        //uncomment to make GUI borderless
        this.setUndecorated(true);

        //set size to the width and height of the primary display
        setSize(WIDTH,HEIGHT);

        //set process to terminate upon close
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //code to create panel for required components
        mainPanel = createPanel();

        createMsgLabel("This is testing text for the msgLabel.");
        createInfoLabel("This is testing text for the infoLabel.");

        //code to test add image to panels
        //create conditional to test for image in xml specs
            try
            {
                createImageLocal("CAB320_Billboard_ERROR.jpg");
            }
            catch(IOException e)
            {
                System.out.print(e.getMessage());
            }

        //code for testing purposes
        getContentPane().add(mainPanel);

        layoutPanels();

        //show JForm
        setVisible(true);
    }


    //method to instantiate a JPanel object
    private JPanel createPanel()
    {
        //create JPanel to be returned
        JPanel temp = new JPanel();

        //return the final object
        return temp;

    }


    //method to create msg object atop JLabel
    private void createMsgLabel(String msg)
    {
        //create label and fill with supplied text
        messageLabel = new JLabel();
        messageLabel.setText(msg);
        Dimension size = new Dimension(WIDTH,HEIGHT/3);
        messageLabel.setMaximumSize(size);

        int font_size = 0;
        Font x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);

        while(true)
        {
            //get current size of label and pad for 5%
            double tmp_width = messageLabel.getPreferredSize().getWidth() + (messageLabel.getPreferredSize().getWidth()/20);
            double tmp_height = messageLabel.getPreferredSize().getHeight() + (messageLabel.getPreferredSize().getHeight()/20);
            if((tmp_width< WIDTH)& tmp_height < (HEIGHT/3)) {
                font_size++;
                x = new Font("Helvetica",Font.PLAIN,font_size);
                messageLabel.setFont(x);

            }
            else{break;}
        }

        x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);




    }

    //method to create info object atop JLabel
    private void createInfoLabel(String info)
    {

        informationLocked = new JTextArea();
        informationLocked.setText(info);
        informationLocked.setLineWrap(true);
        Dimension x = new Dimension(WIDTH,HEIGHT/3);
        informationLocked.setSize(x);
        informationLocked.setBackground(Color.ORANGE);
        informationLocked.setWrapStyleWord(true);



        int font_size = 0;
        Font e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLocked.setFont(e);

        while(true)
        {
            double tmp_height = informationLocked.getPreferredSize().getHeight();
            if(tmp_height < HEIGHT/3) {
                font_size++;
                e = new Font("Helvetica",Font.PLAIN,font_size);
                informationLocked.setFont(e);

            }
            else{
                font_size-=1;
                e = new Font("Helvetica",Font.PLAIN,font_size);
                informationLocked.setFont(e);
                break;
            }
        }

        e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLocked.setFont(e);


        //the intent here is to add the JTextArea to a scrollbar and lock the dimensions of the JTextArea
        //however, the gridbaglayout is fucking with the jscrollpane i think

        //informationBox = new JScrollPane();

        //informationBox.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //informationBox.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    }


    //pull image from local project and instantiate onto a JLabel
    private void createImageLocal(String pathname) throws IOException
    {
        Dimension x = new Dimension(WIDTH,HEIGHT/3);
        imagePanel = new JLabel();
        imagePanel.setPreferredSize(x);

        BufferedImage img = ImageIO.read(new File(pathname));
        Image temp_img = img.getScaledInstance((int)imagePanel.getPreferredSize().getWidth(), (int)imagePanel.getPreferredSize().getHeight(),
                Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(temp_img);

        // transform image for rescaling
        // transform image for rescaling
        //Image temp_img = icon.getImage();

        //do the mathematics to determine the exact dimensions of new scaled image

        // scale it the smooth way

        imagePanel.setIcon(icon);
    }



    //layout all the JComponents on the panel
    //stage 1: layout pic/msg/info
    //stage 2: make component selection dynamic with params
    private void layoutPanels()
    {
        GridBagLayout layout = new GridBagLayout();
        mainPanel.setLayout(layout);
        GridBagConstraints imageConstraint = new GridBagConstraints();



        //Image Constraint
        imageConstraint.fill = GridBagConstraints.VERTICAL;
        imageConstraint.anchor = GridBagConstraints.CENTER;
        imageConstraint.weightx = 100;
        imageConstraint.weighty = 100;

        //Message Constraint
        GridBagConstraints messageConstraint = new GridBagConstraints();
        messageConstraint.fill = GridBagConstraints.VERTICAL;
        messageConstraint.anchor = GridBagConstraints.NORTH;
        messageConstraint.weightx = 100;
        messageConstraint.weighty = 100;

        //Info Constraint
        GridBagConstraints infoConstraint = new GridBagConstraints();
        infoConstraint.fill = GridBagConstraints.VERTICAL;
        infoConstraint.anchor = GridBagConstraints.SOUTH;
        infoConstraint.weightx = 100;
        infoConstraint.weighty = 100;






        addToPanel(mainPanel, imagePanel,imageConstraint,0,1,1,1);
        addToPanel(mainPanel,messageLabel,messageConstraint,0,0,1,1);
        addToPanel(mainPanel,informationLocked,infoConstraint,0,2,1,1);





    }

    //method to add components to a JPanel using the gridbag layout
    private void addToPanel(JPanel jp, Component c, GridBagConstraints
            constraints, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        jp.add(c, constraints);
    }




    @Override
    public void run() {
        createGUI();

    }


    public static void main(String[] args) throws IOException {

        JFrame x = new Billboard_Viewer("Test");

    }
}




=======
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Billboard_Viewer extends JFrame implements Runnable {
    private JPanel mainPanel;
    private JLabel messageLabel;
    private JTextArea informationLocked;
    private JScrollPane informationBox;
    private JLabel imagePanel;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private int WIDTH = screenSize.width;
    private int HEIGHT = screenSize.height;



    public Billboard_Viewer(String name) throws IOException {
        //set title
        super(name);

        run();



    }

    //method to create the GUI window and add the elements to it
    private void createGUI()
    {
        //uncomment to make GUI borderless
        this.setUndecorated(true);

        //set size to the width and height of the primary display
        setSize(WIDTH,HEIGHT);

        //set process to terminate upon close
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //code to create panel for required components
        mainPanel = createPanel();

        createMsgLabel("This is testing text for the msgLabel.");
        createInfoLabel("This is testing text for the infoLabel.");

        //code to test add image to panels
        //create conditional to test for image in xml specs
            try
            {
                createImageLocal("CAB320_Billboard_ERROR.jpg");
            }
            catch(IOException e)
            {
                System.out.print(e.getMessage());
            }

        //code for testing purposes
        getContentPane().add(mainPanel);

        layoutPanels();

        //show JForm
        setVisible(true);
    }


    //method to instantiate a JPanel object
    private JPanel createPanel()
    {
        //create JPanel to be returned
        JPanel temp = new JPanel();

        //return the final object
        return temp;

    }


    //method to create msg object atop JLabel
    private void createMsgLabel(String msg)
    {
        //create label and fill with supplied text
        messageLabel = new JLabel();
        messageLabel.setText(msg);
        Dimension size = new Dimension(WIDTH,HEIGHT/3);
        messageLabel.setMaximumSize(size);

        int font_size = 0;
        Font x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);

        while(true)
        {
            //get current size of label and pad for 5%
            double tmp_width = messageLabel.getPreferredSize().getWidth() + (messageLabel.getPreferredSize().getWidth()/20);
            double tmp_height = messageLabel.getPreferredSize().getHeight() + (messageLabel.getPreferredSize().getHeight()/20);
            if((tmp_width< WIDTH)& tmp_height < (HEIGHT/3)) {
                font_size++;
                x = new Font("Helvetica",Font.PLAIN,font_size);
                messageLabel.setFont(x);

            }
            else{break;}
        }

        x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);




    }

    //method to create info object atop JLabel
    private void createInfoLabel(String info)
    {

        informationLocked = new JTextArea();
        informationLocked.setText(info);
        informationLocked.setLineWrap(true);
        Dimension x = new Dimension(WIDTH,HEIGHT/3);
        informationLocked.setSize(x);
        informationLocked.setBackground(Color.ORANGE);
        informationLocked.setWrapStyleWord(true);



        int font_size = 0;
        Font e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLocked.setFont(e);

        while(true)
        {
            double tmp_height = informationLocked.getPreferredSize().getHeight();
            if(tmp_height < HEIGHT/3) {
                font_size++;
                e = new Font("Helvetica",Font.PLAIN,font_size);
                informationLocked.setFont(e);

            }
            else{
                font_size-=1;
                e = new Font("Helvetica",Font.PLAIN,font_size);
                informationLocked.setFont(e);
                break;
            }
        }

        e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLocked.setFont(e);


        //the intent here is to add the JTextArea to a scrollbar and lock the dimensions of the JTextArea
        //however, the gridbaglayout is fucking with the jscrollpane i think

        //informationBox = new JScrollPane();

        //informationBox.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //informationBox.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    }


    //pull image from local project and instantiate onto a JLabel
    private void createImageLocal(String pathname) throws IOException
    {
        Dimension x = new Dimension(WIDTH,HEIGHT/3);
        imagePanel = new JLabel();
        imagePanel.setPreferredSize(x);

        BufferedImage img = ImageIO.read(new File(pathname));
        Image temp_img = img.getScaledInstance((int)imagePanel.getPreferredSize().getWidth(), (int)imagePanel.getPreferredSize().getHeight(),
                Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(temp_img);

        // transform image for rescaling
        // transform image for rescaling
        //Image temp_img = icon.getImage();

        //do the mathematics to determine the exact dimensions of new scaled image

        // scale it the smooth way

        imagePanel.setIcon(icon);
    }



    //layout all the JComponents on the panel
    //stage 1: layout pic/msg/info
    //stage 2: make component selection dynamic with params
    private void layoutPanels()
    {
        GridBagLayout layout = new GridBagLayout();
        mainPanel.setLayout(layout);
        GridBagConstraints imageConstraint = new GridBagConstraints();



        //Image Constraint
        imageConstraint.fill = GridBagConstraints.VERTICAL;
        imageConstraint.anchor = GridBagConstraints.CENTER;
        imageConstraint.weightx = 100;
        imageConstraint.weighty = 100;

        //Message Constraint
        GridBagConstraints messageConstraint = new GridBagConstraints();
        messageConstraint.fill = GridBagConstraints.VERTICAL;
        messageConstraint.anchor = GridBagConstraints.NORTH;
        messageConstraint.weightx = 100;
        messageConstraint.weighty = 100;

        //Info Constraint
        GridBagConstraints infoConstraint = new GridBagConstraints();
        infoConstraint.fill = GridBagConstraints.VERTICAL;
        infoConstraint.anchor = GridBagConstraints.SOUTH;
        infoConstraint.weightx = 100;
        infoConstraint.weighty = 100;






        addToPanel(mainPanel, imagePanel,imageConstraint,0,1,1,1);
        addToPanel(mainPanel,messageLabel,messageConstraint,0,0,1,1);
        addToPanel(mainPanel,informationLocked,infoConstraint,0,2,1,1);





    }

    //method to add components to a JPanel using the gridbag layout
    private void addToPanel(JPanel jp, Component c, GridBagConstraints
            constraints, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        jp.add(c, constraints);
    }




    @Override
    public void run() {
        createGUI();

    }


    public static void main(String[] args) throws IOException {

        JFrame x = new Billboard_Viewer("Test");

    }
}




>>>>>>> 8018a91f2b4cc47ded114103e989671b1c66e538
