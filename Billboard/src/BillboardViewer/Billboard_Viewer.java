package BillboardViewer;

import Helper.Password;
import Helper.Requests.CurrentBillboardRequest;
import Helper.Requests.LoginRequest;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.awt.event.*;
import java.net.Socket;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.util.Properties;


public class Billboard_Viewer extends JFrame implements Runnable, KeyListener, MouseListener {
    //panels to hold the components in the jframe
    private JPanel mainPanel;
    private JPanel img_panel;
    private JPanel msg_panel;
    private JPanel info_panel;

    //components to display the information for the billboard
    private JLabel messageLabel;
    private JTextArea informationLocked;
    private JLabel imagePanel;

    //defaults
    Color panelColor = Color.BLACK;
    int num_panels = 0;
    boolean pic_created,msg_created,info_created;
    boolean server_error_g = false;
    public String server_xml;


    //window variables
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int WIDTH = screenSize.width;
    private int HEIGHT = screenSize.height;
    Dimension window = new Dimension(WIDTH,HEIGHT);


    public Billboard_Viewer(String name,boolean server_error,String xml) throws IOException {
        //set title
        super(name);
        if(!server_error){server_xml=xml; run();}
        else
        {server_error_g = true;
        run();}


    }


    public Billboard_Viewer(String name,boolean server_error,String xml,Dimension dimension) throws IOException {
        //set title
        super(name);
        if(!server_error){server_xml=xml; run();}
        else
        {
            server_error_g = true;
            window = dimension;
            run();
        }


    }


    /**
     * Set the parameters of the jFrame and add the event listener's to the panels of the frame
     * @author Daniel Lawless
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    //method to create the GUI window and add the elements to it
    private void createGUI() throws IOException, ParserConfigurationException, SAXException {
        //uncomment to make GUI borderless
        setUndecorated(true);

        //set size to the width and height of the primary display
        setPreferredSize(window);


        //set process to terminate upon close
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //code to create panel for required components
        mainPanel = createPanel();
        mainPanel.setFocusable(true);


        msg_panel = createPanel();
        msg_panel.setFocusable(true);
        msg_panel.addKeyListener(this);
        msg_panel.addMouseListener(this);

        img_panel = createPanel();
        img_panel.addKeyListener(this);
        img_panel.addMouseListener(this);

        info_panel = createPanel();
        info_panel.setFocusable(true);



        //code for testing purposes
        getContentPane().add(mainPanel);


        //parse control file and ascertain information to display
        if(server_error_g) parseControlFile("src\\BillboardViewer\\control.xml",true);
        else{parseControlFile(server_xml,false);};


        layoutPanels();


        //show JForm
        setVisible(true);

        //set background to def val = black, or parsed color from xml file
        msg_panel.setBackground(panelColor);
        mainPanel.setBackground(panelColor);
        info_panel.setBackground(panelColor);
        img_panel.setBackground(panelColor);
    }


    /**
     * method to instantiate panel object
     * @author Daniel Lawless
     * @return
     */
    private JPanel createPanel()
    {
        //create JPanel to be returned
        JPanel temp = new JPanel();


        //return the final object
        return temp;

    }


    /**
     * Instantiate the contents of the MSG panel
     * @author Daniel Lawless
     * @param msg
     * @param text_color
     */
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

    /**
     * Instantiate the contents of the Information Panel
     * @author Daniel Lawless
     * @param info
     * @param text_color
     */
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
        informationLocked.addKeyListener(this);
        informationLocked.addMouseListener(this);


    }

    /**
     * Helper method to parse server XML into a file for the documentBuilder
     * @author Daniel Lawless
     * @param xml
     * @return
     * @throws IOException
     */
    private File stringXMLtofileXML(String xml) throws IOException {
        File newFile = new File("serverXML.txt");
        newFile.createNewFile();
        FileWriter fw = new FileWriter(newFile,false);
        fw.write(xml);
        fw.close();
        return newFile;
    }


    /**
     * This method takes the XML file location, be it local or server derived and parses it into a indexed document
     * to then access the attributes of a correctly formatted XML document. The attributes are passed of to the relevant helper
     * methods for each panel.
     * @author Daniel Lawless
     * @param file_path
     * @param local
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    //method to parse an xml file ton determine the properties of the billboard
    public void parseControlFile(String file_path,boolean local) throws IOException, ParserConfigurationException, SAXException {
        //initialize file and initialize documentBuilder to parse the xml file into an accessible format
        File xmlFile = null;
        if(local){ xmlFile = new File(file_path);}
        else{
            xmlFile = stringXMLtofileXML(server_xml);
        }
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
        if(pic != null)
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

            pic_created = true;
        }



        if(msg!= null){
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
            msg_created = true;


        }

        if(info != null){
            //get text value of msg node
            String text = info.getTextContent();
            //get any attributes relevant to msg
            NamedNodeMap attribute_select = null;
            try
            {
                attribute_select = info.getAttributes();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if(attribute_select.getLength()>0)
            {
                //select color attribute and then instantiate msg panel with that color
                Attr color_code = (Attr) attribute_select.item(0);
                String str_color_code = color_code.getValue();
                Color text_color = Color.decode(str_color_code);
                createInfoLabel(text,text_color);
            }
            else
                {
                    createInfoLabel(text,Color.WHITE);
                }
            info_created = true;
        }


    }


    /**
     * This method takes a image, rescales it while maintaining a aspect ratio to fit the dimensions of the panel
     *
     * @param img
     * @param bounds
     * @return
     */
    private Dimension getScaledImage(BufferedImage img,Dimension bounds)
    {

        int old_w = img.getWidth();
        int old_h = img.getHeight();

        int new_w = old_w;
        int new_h = old_h;

        int scaled_w = bounds.width;
        int scaled_h = bounds.height;

        //check if need to scale width to fit constraints
        while(true)
        {
            if (new_w > scaled_w) {
                new_w = scaled_w - (scaled_w/4);
                //maintain aspect ratio
                new_h = (new_w * old_h) / old_w;
                break;
            }


            //check if need to scale width to fit constraints
            if (new_h > scaled_h) {

                new_h = scaled_h- (scaled_h/4);
                //maintain aspect ratio
                new_w = (new_h * old_w) / old_h;
                break;
            }
            else
            {
                new_h++;
            }

        }

        return new Dimension(new_w,new_h);
    }


    /**
     * Take a b64 string and instantiate it onto a jLabel
     * @author Daniel Lawless
     * @param base_image
     * @throws IOException
     */
    private void createImageLocal(String base_image) throws IOException
    {
        //pull image from local project and instantiate onto a JLabel
        Dimension bounds = new Dimension(WIDTH,HEIGHT/num_panels);
        imagePanel = new JLabel();
        imagePanel.setPreferredSize(bounds);

        byte[] buffImage = Base64.getDecoder().decode(base_image);

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(buffImage));
        Dimension img_dimen = getScaledImage(img,bounds);

        Image temp_img = img.getScaledInstance((int)img_dimen.getWidth(),
                (int)img_dimen.getHeight(),
                Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(temp_img);

        imagePanel.setIcon(icon);

    }

    /**
     * Take a url and instantiate it onto a jLabel
     * @author Daniel Lawless
     * @param url
     * @throws IOException
     */
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


    /**
     * This method sets the dimensions of the panels based on the amount of panels required and adds them to
     * the main panel via the addComponent method.
     * @author Daniel Lawless
     */
    private void layoutPanels()
    {
        //init dimension vars
        Dimension pic_size,msg_size,info_size;
        pic_size = msg_size = info_size = null;

        if(pic_created&&msg_created&&info_created) {
            Dimension x = new Dimension(WIDTH,(int)window.getHeight()/num_panels);
            pic_size=msg_size=info_size = x;
        }
        else if((pic_created&&info_created)&!msg_created){
            pic_size = new Dimension((int)screenSize.getWidth(),(int)(screenSize.getHeight()/3)*2);
            info_size = new Dimension((int)screenSize.getWidth(),(int)(screenSize.getHeight()/3));
        }
        else if((msg_created&&info_created)&!pic_created){
            pic_size = new Dimension((int)screenSize.getWidth(),(int)(screenSize.getHeight()/3)*2);
            info_size = new Dimension((int)screenSize.getWidth(),(int)(screenSize.getHeight()/3));
        }




            mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
            //set the size of the panels
            if(msg_created)
            {
                msg_panel.setPreferredSize(msg_size);
                msg_panel.add(messageLabel);
                msg_panel.setLayout(new BoxLayout(msg_panel,BoxLayout.Y_AXIS));
                msg_panel.setAlignmentY(Component.CENTER_ALIGNMENT);
                msg_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
                mainPanel.add(msg_panel);

            }
            if(pic_created)
            {
                img_panel.setPreferredSize(pic_size);
                img_panel.add(imagePanel);
                img_panel.setLayout(new BoxLayout(img_panel,BoxLayout.Y_AXIS));
                mainPanel.add(Box.createVerticalGlue());
                mainPanel.add(img_panel);
                img_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
                img_panel.setAlignmentY(Component.BOTTOM_ALIGNMENT);


            }
            if(info_created) {
                info_panel.setPreferredSize(info_size);
                info_panel.add(informationLocked);
                info_panel.setLayout(new BoxLayout(info_panel, BoxLayout.Y_AXIS));
                info_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
                info_panel.setAlignmentY(Component.CENTER_ALIGNMENT);
                mainPanel.add(info_panel);

            }

        pack();



    }


    /**
     * This method overrides the runnable abstract method and catches the programs exceptions
     */
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


    /**
     *
     * This method adds the components passed to the mainpanel using the passed parameters to format
     * the layout
     *
     *
     *
     * @param component
     * @param layout
     * @param layoutConstraints
     * @param row
     * @param column
     * @param width
     * @param height
     */

    private void addComponent(Component component,GridBagLayout layout,GridBagConstraints layoutConstraints, int row,
                              int column, int width, int height) {
        layoutConstraints.gridx = column;
        layoutConstraints.gridy = row;
        layoutConstraints.gridwidth = width;
        layoutConstraints.gridheight = height;
        layout.setConstraints(component, layoutConstraints);
        mainPanel.add(component,layoutConstraints);
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        //init bool and jFrame
        String bill = "";
        JFrame x = null;


        Properties prop = new Properties();
        FileInputStream in = new FileInputStream("src/server.props");
        prop.load(in);
        String port  = prop.getProperty("port");

        //test server connection
        try{ bill = serverRetreival(port); }
        catch(Exception e) { e.printStackTrace();}

        while(true)
        {
            bill = "";
            //if instance already active
            if(x!=null)
            {

                try{ bill = serverRetreival(port); }
                catch(Exception e) { e.printStackTrace();}
                //temp instance
                JFrame e = null;
                if(bill=="")
                {
                    e = new Billboard_Viewer("Error Screen",true,"");
                    x.dispose();
                    x = new Billboard_Viewer("Error Screen",true,"");
                    e.dispose();
                }
                else
                {
                    e = new Billboard_Viewer("Billboard Viewer",
                            false,
                            bill);
                    x.dispose();
                    x = new Billboard_Viewer("Billboard Viewer",
                            false,
                            bill);
                    e.dispose();
                }



            }
            else {
                try{ bill = serverRetreival(port); }
                catch(Exception e) { e.printStackTrace();}
                if (bill == "") {
                    x = new Billboard_Viewer("Error Screen", true, "");
                } else {
                    x = new Billboard_Viewer("Billboard Viewer", false, bill);

                }
            }
            Thread.sleep(15000);
        }



    }


    public static String serverRetreival(String port) throws Exception {

        CurrentBillboardRequest request = new CurrentBillboardRequest();
        Socket socket = new Socket("127.0.0.1", 4444);

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeObject(request);

        ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
        String xml = (String)clientInputStream.readObject();

        return xml;

        
    }



    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==27)
        {
            System.exit(0);
        };

    }
    @Override
    public void mouseClicked(MouseEvent e){
        System.exit(0);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }


    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}




