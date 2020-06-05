package ControlPanel;

import BillboardViewer.Billboard_Viewer;
import Helper.Billboard;
import Helper.Requests.CreateEditBillboardRequest;
import Helper.Responses.ErrorMessage;
import Helper.SessionToken;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class CreateBillboard extends JDialog {
    // Panel constructors
    private JPanel inputPanel;
    private JPanel viewerPanel;
    private PreviewViewer viewer;
    private JPanel namePanel;

    // Components
    private JButton messageTextColour;
    private Color messageColour;
    private JLabel nameLabel;
    private JTextField nameText;
    private JTextField messageText;
    private JCheckBox useURL;
    private JButton openImage;
    private JFileChooser imageFileLocation;
    private JTextField imageLocation;
    private JTextField imageURL;
    private JButton infoTextColour;
    private Color infoColour;
    private JTextField infoText;
    private Color backgroundColour;
    private JButton backgroundColourButton;
    private JButton previewBillboard;
    private JButton submitBillboard;

    // Other
    String xml;
    String base64;
    SessionToken sessionToken;
    String serverIP;
    int serverPort;

    public CreateBillboard(Dimension size, String serverIP, int serverPort, SessionToken sessionToken) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;
        initInputs(size);
    }

    public CreateBillboard(Dimension size, String serverIP, int serverPort, SessionToken sessionToken, String billboardName, String xml) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.sessionToken = sessionToken;
        initInputs(size);
        parseControlFile(xml);
        nameText.setText(billboardName);
        this.setVisible(true);
        previewBillboard.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

    private void initInputs(Dimension size) {
        setSize(size);
        setLayout(new BorderLayout());

        messageTextColour = new JButton("Choose Message Colour");
        messageTextColour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageColour = JColorChooser.showDialog(CreateBillboard.this, "Choose Message Colour", messageColour);
            }
        });
        nameLabel = new JLabel("Billboard Name:");
        nameText = new JTextField("Billboard Name");
        nameText.setForeground(Color.LIGHT_GRAY);
        nameText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().equals("Billboard Name")) {
                    source.setText("");
                    source.setForeground(null);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().isBlank()) {
                    source.setText("Billboard Name");
                    source.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        messageText = new JTextField();
        messageText.setText("Billboard Message");
        messageText.setForeground(Color.LIGHT_GRAY);
        messageText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().equals("Billboard Message")) {
                    source.setText("");
                    source.setForeground(null);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().isBlank()) {
                    source.setText("Billboard Message");
                    source.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        useURL = new JCheckBox("Use URL for Image");
        useURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox)e.getSource();
                if (cb.isSelected()){
                    imageURL.setEnabled(true);
                    openImage.setEnabled(false);
                }
                else{
                    imageURL.setEnabled(false);
                    openImage.setEnabled(true);
                }
            }
        });
        openImage = new JButton("Open Image");
        openImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageFileLocation.showOpenDialog(CreateBillboard.this);
            }
        });
        imageFileLocation = new JFileChooser();
        imageFileLocation.addChoosableFileFilter(new FileNameExtensionFilter(
                "Image files", ImageIO.getReaderFileSuffixes()));
        imageFileLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageLocation.setText(((JFileChooser)e.getSource()).getSelectedFile().getAbsolutePath());
            }
        });
        imageLocation = new JTextField("Image Location");
        imageLocation.setEnabled(false);
        imageURL = new JTextField("Image URL");
        imageURL.setForeground(Color.LIGHT_GRAY);
        imageURL.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().equals("Image URL")) {
                    source.setText("");
                    source.setForeground(null);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().isBlank()) {
                    source.setText("Image URL");
                    source.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        imageURL.setEnabled(false);
        infoTextColour = new JButton("Choose Information Colour");
        infoTextColour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoColour = JColorChooser.showDialog(CreateBillboard.this, "Choose Information Colour", infoColour);
            }
        });
        infoText = new JTextField("Billboard Information");
        infoText.setForeground(Color.LIGHT_GRAY);
        infoText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().equals("Billboard Information")) {
                    source.setText("");
                    source.setForeground(null);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField source = (JTextField)e.getComponent();
                if (source.getText().isBlank()) {
                    source.setText("Billboard Information");
                    source.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        backgroundColourButton = new JButton("Choose Background Colour");
        backgroundColourButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundColour = JColorChooser.showDialog(CreateBillboard.this, "Choose Background Colour", backgroundColour);
            }
        });
        previewBillboard = new JButton("Preview Billboard");
        previewBillboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (useURL.isSelected() && !imageURL.getText().isBlank()){
                    viewer = new PreviewViewer(messageText.getText().equals("Billboard Message") ? null : messageText.getText(), messageColour, infoText.getText().equals("Billboard Information") ? null : infoText.getText(), infoColour, imageURL.getText().equals("Image URL") ? null : imageURL.getText(), backgroundColour, new Dimension(viewerPanel.getWidth(), viewerPanel.getHeight()));
                }
                else if (!useURL.isSelected() && !imageLocation.getText().isBlank() && !imageLocation.getText().equals("Image Location")){
                    String location = imageLocation.getText();
                    File file = new File(imageLocation.getText());
                    try (FileInputStream imageInFile = new FileInputStream(imageLocation.getText())) {
                        // Reading a Image file from file system
                        byte imageData[] = new byte[(int) file.length()];
                        imageInFile.read(imageData);
                        String base64Image = Base64.getEncoder().encodeToString(imageData);

                        viewer = new PreviewViewer(messageText.getText().equals("Billboard Message") ? null : messageText.getText(), messageColour, infoText.getText().equals("Billboard Information") ? null : infoText.getText(), infoColour, base64Image, backgroundColour, new Dimension(viewerPanel.getWidth(), viewerPanel.getHeight()));
                    } catch (IOException ioe) {
                        System.out.println("Exception while reading the Image " + ioe);
                    }
                }
                else if(!useURL.isSelected() && base64 != null && !base64.isBlank()){
                    viewer = new PreviewViewer(messageText.getText().equals("Billboard Message") ? null : messageText.getText(), messageColour, infoText.getText().equals("Billboard Information") ? null : infoText.getText(), infoColour, base64, backgroundColour, new Dimension(viewerPanel.getWidth(), viewerPanel.getHeight()));
                }

                if (viewer != null){
                    viewerPanel.removeAll();
                    viewerPanel.add(viewer);

                    viewerPanel.validate();
                    viewerPanel.repaint();
                }
            }
        });
        submitBillboard = new JButton("Submit Billboard");
        submitBillboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nameText.getText().isBlank() || nameText.getText().equals("Billboard Name")){
                    JOptionPane.showMessageDialog(CreateBillboard.this,
                            "A name is required!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                else if (messageText.getText().isBlank() && infoText.getText().isBlank() && imageURL.getText().isBlank() && imageLocation.getText().isBlank()){
                    JOptionPane.showMessageDialog(CreateBillboard.this,
                            "You must fill at least one field!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    createEditBillboard();
                }
            }
        });

        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridx = 0;
        inputPanel.add(messageTextColour, constraints);
        inputPanel.add(infoTextColour, constraints);
        constraints.gridx = 1;
        inputPanel.add(messageText, constraints);
        inputPanel.add(infoText, constraints);
        inputPanel.add(backgroundColourButton, constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 2;
        inputPanel.add(useURL, constraints);
        inputPanel.add(openImage, constraints);
        inputPanel.add(imageURL, constraints);

        constraints.gridx = 3;
        constraints.gridy = 1;
        inputPanel.add(imageLocation, constraints);

        constraints.gridx = 2;
        constraints.gridy = 5;
        inputPanel.add(submitBillboard, constraints);
        constraints.gridx = 1;
        inputPanel.add(previewBillboard, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        JSeparator separator = new JSeparator();
        inputPanel.add(separator, constraints);

        namePanel = new JPanel();

        namePanel.add(nameLabel);
        namePanel.add(nameText);

        add(namePanel, BorderLayout.NORTH);

        add(inputPanel, BorderLayout.SOUTH);

        viewerPanel = new JPanel();
        add(viewerPanel, BorderLayout.CENTER);


    }

    /**
     * Helper method to parse server XML into a file for the documentBuilder
     * @param xml
     * @return
     * @throws IOException
     */
    private File stringXMLtofileXML(String xml){
        try {
            File newFile = new File("serverXML.txt");
            newFile.createNewFile();
            FileWriter fw = new FileWriter(newFile, false);
            fw.write(xml);
            fw.close();
            return newFile;
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }


    /**
     * This method takes the XML file location, be it local or server derived and parses it into a indexed document
     * to then access the attributes of a correctly formatted XML document. The attributes are passed of to the relevant helper
     * methods for each panel.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    //method to parse an xml file ton determine the properties of the billboard
    public void parseControlFile(String xml) {
        try {
            //initialize file and initialize documentBuilder to parse the xml file into an accessible format
            File xmlFile = stringXMLtofileXML(xml);

            if (xmlFile != null) {
                DocumentBuilderFactory builderFac = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = builderFac.newDocumentBuilder();

                //parse file into document file and instantiate the billboard contents as a node
                Document doc = docBuilder.parse(xmlFile);
                Node billboard = doc.getElementsByTagName("billboard").item(0);

                //test for bg attributes in billboard node
                NamedNodeMap billboardAttributes = billboard.getAttributes();
                if (billboardAttributes.getLength() > 0) {
                    Attr attr_select = (Attr) billboardAttributes.item(0);
                    if (attr_select.getName() == "background") {
                        String color = attr_select.getValue();
                        Color color_decoded = Color.decode(color);
                        backgroundColour = color_decoded;
                    }
                }


                //ascertain the contents of the billboard
                Element nodeElement = (Element) billboard;

                //scan attributes to see if picture is from url or from local
                Node pic = nodeElement.getElementsByTagName("picture").item(0);
                Node msg = nodeElement.getElementsByTagName("message").item(0);
                Node info = nodeElement.getElementsByTagName("information").item(0);

                //if picture in control file, test for input source and set variables
                if (pic != null) {
                    NamedNodeMap attribute_select = pic.getAttributes();
                    Attr data_source = (Attr) attribute_select.item(0);
                    String test_source = data_source.getName();

                    String location = data_source.getValue();

                    if (test_source == "url") {
                        useURL.setSelected(true);
                        imageURL.setEnabled(true);
                        imageURL.setText(location);
                        imageURL.setForeground(Color.BLACK);
                    } else {
                        useURL.setSelected(false);
                        base64 = location;
                    }
                }

                if (msg != null) {
                    //get text value of msg node
                    messageText.setText(msg.getTextContent());
                    messageText.setForeground(Color.BLACK);
                    //get any attributes relevant to msg
                    NamedNodeMap attribute_select = msg.getAttributes();
                    if (attribute_select.getLength() > 0) {
                        //select color attribute and then instantiate msg panel with that color
                        Attr color_code = (Attr) attribute_select.item(0);
                        String str_color_code = color_code.getValue();
                        messageColour = Color.decode(str_color_code);
                    }
                }

                if (info != null) {
                    //get text value of msg node
                    infoText.setText(info.getTextContent());
                    infoText.setForeground(Color.BLACK);
                    //get any attributes relevant to msg
                    NamedNodeMap attribute_select = null;
                    try {
                        attribute_select = info.getAttributes();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (attribute_select.getLength() > 0) {
                        //select color attribute and then instantiate msg panel with that color
                        Attr color_code = (Attr) attribute_select.item(0);
                        String str_color_code = color_code.getValue();
                        infoColour = Color.decode(str_color_code);
                    }
                }
                xmlFile.delete();
            }
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String colourToHex(Color colour){
        return "#"+Integer.toHexString(colour.getRGB()).substring(2).toUpperCase();
    }

    private String parseToXML(){
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";

        if (backgroundColour != null){
            xml = xml.concat(String.format("<billboard background=\"%s\">\r\n", colourToHex(backgroundColour)));
        }
        else{
            xml = xml.concat("<billboard background=\"#FFFFFF\">\r\n");
        }

        if (!messageText.getText().equals("Billboard Message") && !messageText.getText().isBlank()){
            if (messageColour != null) {
                xml = xml.concat(String.format("\t<message colour=\"%s\">%s</message>", colourToHex(messageColour), messageText.getText()));
            }
            else{
                xml = xml.concat(String.format("\t<message colour = \"#000000\">%s</message>", messageText.getText()));
            }
        }

        if (useURL.isSelected() && !imageURL.getText().isBlank()){
            xml = xml.concat(String.format("<picture url=\"%s\" />", imageURL.getText()));
        }
        else if (!useURL.isSelected() && !imageLocation.getText().isBlank() && !imageLocation.getText().equals("Image Location")){
            File file = new File(imageLocation.getText());
            try (FileInputStream imageInFile = new FileInputStream(imageLocation.getText())) {
                // Reading a Image file from file system
                byte imageData[] = new byte[(int) file.length()];
                imageInFile.read(imageData);
                String base64Image = Base64.getEncoder().encodeToString(imageData);

                xml = xml.concat(String.format("\t<picture data=\"%s\" />\r\n", base64Image));
            } catch (IOException ioe) {
                System.out.println("Exception while reading the Image " + ioe);
            }
        }
        else if(!useURL.isSelected() && base64 != null && !base64.isBlank()){
            xml = xml.concat(String.format("\t<picture data=\"%s\" />\r\n", base64));
        }

        if (!infoText.getText().equals("Billboard Information") && !infoText.getText().isBlank()){
            if (infoColour != null) {
                xml = xml.concat(String.format("\t<information colour=\"%s\">%s</information>\r\n", colourToHex(infoColour), infoText.getText()));
            }
            else{
                xml = xml.concat(String.format("\t<information colour = \"#000000\">%s</information>\r\n", infoText.getText()));
            }
        }

        xml = xml.concat("</billboard>");

        return xml;
    }

    private void createEditBillboard(){
        String xml = parseToXML();
        CreateEditBillboardRequest createEditBillboardRequest = new CreateEditBillboardRequest(sessionToken.getSessionToken(), nameText.getText(), xml);

        try {
            Socket socket = new Socket(serverIP, serverPort);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject(createEditBillboardRequest);

            ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = clientInputStream.readObject();

            if (obj.getClass() == Boolean.class) {
                JOptionPane.showMessageDialog(this,
                        "Billboard Created",
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
