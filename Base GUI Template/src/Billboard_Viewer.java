import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.border.Border;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;

public class Billboard_Viewer extends JFrame implements Runnable {
    //panels to hold the components in the jframe
    private JPanel mainPanel;
    private JPanel img_panel;
    private JPanel msg_panel;
    private JPanel info_panel;

    //components to diplay the information for the billboard
    private JLabel messageLabel;
    private JTextArea informationLocked;
    private JLabel imagePanel;

    //testing vars
    Color panelColor = Color.BLACK;
    int num_panels = 0;


    //window variables
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int WIDTH = screenSize.width;
    private int HEIGHT = screenSize.height;
    Dimension window = new Dimension(WIDTH,HEIGHT);
    

    public Billboard_Viewer(String name) throws IOException {
        //set title
        super(name);

        run();



    }

    //method to create the GUI window and add the elements to it
    private void createGUI() throws IOException, ParserConfigurationException, SAXException {
        //uncomment to make GUI borderless
        this.setUndecorated(true);

        //set size to the width and height of the primary display
        setPreferredSize(window);

        //set process to terminate upon close
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //code to create panel for required components
        mainPanel = createPanel();


        msg_panel = createPanel();

        img_panel = createPanel();

        info_panel = createPanel();

        //code for testing purposes
        getContentPane().add(mainPanel);

        //parse control file and ascertain information to display
        parseControlFile("control.xml");



        layoutPanels();


        //show JForm
        setVisible(true);

        //set background to def val = black, or parsed color from xml file
        msg_panel.setBackground(panelColor);
        mainPanel.setBackground(panelColor);
        info_panel.setBackground(panelColor);
        img_panel.setBackground(panelColor);
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
    private void createMsgLabel(String msg,Color text_color)
    {
        //create label and fill with supplied text
        messageLabel = new JLabel();
        messageLabel.setText(msg);
        Dimension size = new Dimension(WIDTH,HEIGHT/num_panels);
        messageLabel.setMaximumSize(size);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);

        int font_size = 0;
        Font x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);

        while(true)
        {
            //get current size of label and pad for 10%
            double tmp_width = messageLabel.getPreferredSize().getWidth() + (messageLabel.getPreferredSize().getWidth()/10);
            double tmp_height = messageLabel.getPreferredSize().getHeight() + (messageLabel.getPreferredSize().getHeight()/10);
            //if the temp dimensions are within the container increase font size
            if((tmp_width< WIDTH)& tmp_height < (HEIGHT/num_panels)) {
                font_size++;
                x = new Font("Helvetica",Font.PLAIN,font_size);
                messageLabel.setFont(x);

            }
            else{break;}
        }

        x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);
        messageLabel.setForeground(text_color);
        messageLabel.setBackground(panelColor);




    }

    //method to create info object atop JLabel
    private void createInfoLabel(String info,Color text_color)
    {

        informationLocked = new JTextArea();
        informationLocked.setText(info);
        informationLocked.setLineWrap(true);
        Dimension x = new Dimension(WIDTH - (WIDTH/10),HEIGHT/num_panels);
        informationLocked.setSize(x);
        informationLocked.setWrapStyleWord(true);



        int font_size = 0;
        Font e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLocked.setFont(e);

        while(true)
        {
            double tmp_height = informationLocked.getPreferredSize().getHeight();
            if(tmp_height < HEIGHT/num_panels) {
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
        informationLocked.setEditable(false);
        informationLocked.setForeground(text_color);
        informationLocked.setBackground(panelColor);


    }

    //method to parse an xml file ton determine the properties of the billboard
    public void parseControlFile(String file_path) throws IOException, ParserConfigurationException, SAXException {
        //initialize file and initialize documentBuilder to parse the xml file into an accessible format
        File xmlFile = new File(file_path);
        DocumentBuilderFactory builderFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFac.newDocumentBuilder();

        //parse file into document file and instantiate the billboard contents as a node
        Document doc = docBuilder.parse(xmlFile);
        Node billboard = doc.getElementsByTagName("billboard").item(0);

        //test for bg attributes in billboard node
        NamedNodeMap billboardAttributes = billboard.getAttributes();
        if(billboardAttributes.getLength() > 0)
        {
            Attr attr_select = (Attr) billboardAttributes.item(0);
            if(attr_select.getName() == "background")
            {
                String color = attr_select.getValue();
                Color color_decoded = Color.decode(color);
                panelColor = color_decoded;
            }
        }



        //ascertain the contents of the billboard
        Element nodeElement = (Element) billboard;

            //scan attributes to see if picture is from url or from local
        Node pic = nodeElement.getElementsByTagName("picture").item(0);
        Node msg = nodeElement.getElementsByTagName("message").item(0);
        Node info = nodeElement.getElementsByTagName("information").item(0);
        //test to see which panels need to be active for sizing upon instantiation
        if(pic!=null){num_panels++;}
        if(msg!=null){num_panels++;}
        if(info!=null){num_panels++;}

        //if picture in control file, test for input source and set variables
        if(pic!= null)
        {
            NamedNodeMap attribute_select = pic.getAttributes();
            Attr data_source = (Attr) attribute_select.item(0);
            String test_source = data_source.getName();

            if(test_source == "url")
            {
                String url = data_source.getValue();
                createImageFromURL(url);
            }
            else
            {
                String source = data_source.getValue();
                createImageLocal(source);
            }
        }



        if(msg != null){
            //get text value of msg node
            String text = msg.getTextContent();
            //get any attributes relevant to msg
            NamedNodeMap attribute_select = msg.getAttributes();
            if(attribute_select.getLength()>0)
            {
                //select color attribute and then instantiate msg panel with that color
                Attr color_code = (Attr) attribute_select.item(0);
                String str_color_code = color_code.getValue();
                Color text_color = Color.decode(str_color_code);
                createMsgLabel(text,text_color);
            }



        }

        if(info != null){
            //get text value of msg node
            String text = info.getTextContent();
            //get any attributes relevant to msg
            NamedNodeMap attribute_select = info.getAttributes();
            if(attribute_select.getLength()>0)
            {
                //select color attribute and then instantiate msg panel with that color
                Attr color_code = (Attr) attribute_select.item(0);
                String str_color_code = color_code.getValue();
                Color text_color = Color.decode(str_color_code);
                createInfoLabel(text,text_color);
            }
        }


    }


    private Dimension getScaledImage(BufferedImage img,Dimension bounds)
    {

        int old_w = img.getWidth();
        int old_h = img.getHeight();

        int new_w = old_w;
        int new_h = old_h;

        int scaled_w = bounds.width;
        int scaled_h = bounds.height;

        //check if need to scale width to fit constraints
        if (old_w > scaled_w) {
            new_w = scaled_w;
            //maintain aspect ratio
            new_h = (new_w * old_h) / old_w;
        }

        //check if need to scale width to fit constraints
        if (new_h > scaled_h) {

            new_h = scaled_h;
            //maintain aspect ratio
            new_w = (new_h * old_w) / old_h;
        }
        return new Dimension(new_w,new_h);
    }








    //pull image from local project and instantiate onto a JLabel
    private void createImageLocal(String pathname) throws IOException
    {
        Dimension bounds = new Dimension(WIDTH,HEIGHT/num_panels);
        imagePanel = new JLabel();
        imagePanel.setPreferredSize(bounds);

        BufferedImage img = ImageIO.read(new File(pathname));
        Dimension img_dimen = getScaledImage(img,bounds);

        Image temp_img = img.getScaledInstance((int)img_dimen.getWidth(),
                (int)img_dimen.getHeight(),
                Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(temp_img);

        imagePanel.setIcon(icon);
    }


    private void createImageFromURL(String url) throws IOException {
        Dimension bounds = new Dimension(WIDTH,HEIGHT/num_panels);
        imagePanel = new JLabel();
        imagePanel.setPreferredSize(bounds);

        URL image_link = new URL(url);
        BufferedImage img = ImageIO.read(image_link);
        Dimension img_dimen = getScaledImage(img,bounds);

        Image temp_img = img.getScaledInstance((int)img_dimen.getWidth(),
                (int)img_dimen.getHeight(),
                Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(temp_img);

        imagePanel.setIcon(icon);
    }



    //layout all the JComponents on the panel
    //stage 1: layout pic/msg/info
    //stage 2: make component selection dynamic with params
    private void layoutPanels()
    {
        Dimension panel_dimensions = new Dimension(WIDTH,(int)window.getHeight()/num_panels);

        //set the size of the panels
        msg_panel.setPreferredSize(panel_dimensions);
        info_panel.setPreferredSize(panel_dimensions);
        img_panel.setPreferredSize(panel_dimensions);


        img_panel.add(imagePanel);
        info_panel.add(informationLocked);
        msg_panel.add(messageLabel);


        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        img_panel.setLayout(new BoxLayout(img_panel,BoxLayout.Y_AXIS));
        img_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        img_panel.setAlignmentY(Component.CENTER_ALIGNMENT);
        info_panel.setLayout(new BoxLayout(info_panel,BoxLayout.Y_AXIS));
        info_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        info_panel.setAlignmentY(Component.CENTER_ALIGNMENT);
        msg_panel.setLayout(new BoxLayout(msg_panel,BoxLayout.Y_AXIS));
        msg_panel.setAlignmentY(Component.CENTER_ALIGNMENT);
        msg_panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(msg_panel);
        mainPanel.add(img_panel);
        mainPanel.add(info_panel);

        pack();





    }






    @Override
    public void run() {
        try {
            createGUI();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

        JFrame x = new Billboard_Viewer("Test");

    }
}




