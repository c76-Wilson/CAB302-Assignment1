package ControlPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Pattern;

public class PreviewViewer extends JPanel{
    private String messageText;
    private Color messageColour;
    private String infoText;
    private Color infoColour;
    private String imageLocation;
    private Color backgroundColour;
    private Dimension panelSize;

    private boolean pictureExists;
    private boolean messageExists;
    private boolean infoExists;

    // Panels
    JPanel messagePanel;
    JPanel infoPanel;
    JPanel imagePanel;

    // Components
    JLabel messageLabel;
    JTextArea informationLabel;
    JLabel imageLabel;

    public PreviewViewer(String messageText, Color messageColour, String infoText, Color infoColour, String imageLocation, Color backgroundColour, Dimension panelSize){
        this.messageText = messageText;
        this.messageColour = messageColour;
        this.infoText = infoText;
        this.infoColour = infoColour;
        this.imageLocation = imageLocation;
        this.backgroundColour = backgroundColour;
        this.panelSize = panelSize;

        this.setupPreview();

        this.setBackground(backgroundColour);
        messagePanel.setBackground(backgroundColour);
        infoPanel.setBackground(backgroundColour);
        imagePanel.setBackground(backgroundColour);
    }

    private void setupPreview(){
        messagePanel = new JPanel();

        imagePanel = new JPanel();

        infoPanel = new JPanel();

        this.backgroundColour = backgroundColour;

        if(imageLocation != null && !imageLocation.isBlank())
        {
            if (Pattern.matches("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,}|[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})", imageLocation)){
                createImageFromURL(imageLocation);
            }
            else{
                createImageLocal(imageLocation);
            }
            pictureExists = true;
        }

        if(messageText != null && !messageText.isBlank()) {
            if(messageColour != null)
            {
                createMsgLabel(messageText,messageColour);
            }
            else{
                createMsgLabel(messageText, Color.WHITE);
            }

            messageExists = true;
        }

        if(infoText != null && !infoText.isBlank()) {
            if(infoColour != null)
            {
                createInfoLabel(infoText,infoColour);
            }
            else{
                createInfoLabel(infoText,Color.BLACK);
            }
            infoExists = true;
        }

        layoutPanels();
    }

    private int getNumPanels(){
        int num_panels = 0;
        if (imageLocation != null && !imageLocation.isBlank()){num_panels++;}
        if (messageText != null && !messageText.isBlank()){num_panels++;}
        if (infoText != null && !infoText.isBlank()){num_panels++;}
        return num_panels;
    }

    private void createImageFromURL(String url){
        Dimension bounds = new Dimension(panelSize.width,panelSize.height/getNumPanels());
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(bounds);

        try {
            URL image_link = new URL(url);
            BufferedImage img = ImageIO.read(image_link);
            Dimension img_dimen = getScaledImage(img,bounds);

            Image temp_img = img.getScaledInstance((int)img_dimen.getWidth(), (int)img_dimen.getHeight(), Image.SCALE_SMOOTH);

            ImageIcon icon = new ImageIcon(temp_img);

            imageLabel.setIcon(icon);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void createImageLocal(String base_image){

        //pull image from local project and instantiate onto a JLabel
        Dimension bounds = new Dimension(panelSize.width,panelSize.height/getNumPanels());
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(bounds);

        try {
            byte[] buffImage = Base64.getDecoder().decode(base_image);

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(buffImage));
            Dimension img_dimen = getScaledImage(img, bounds);

            Image temp_img = img.getScaledInstance((int) img_dimen.getWidth(),
                    (int) img_dimen.getHeight(),
                    Image.SCALE_SMOOTH);

            ImageIcon icon = new ImageIcon(temp_img);

            imageLabel.setIcon(icon);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
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

    //method to create msg object atop JLabel
    private void createMsgLabel(String msg, Color text_color)
    {
        //create label and fill with supplied text
        messageLabel = new JLabel();
        messageLabel.setText(msg);
        Dimension size = new Dimension(panelSize.width,panelSize.height/getNumPanels());
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
            if((tmp_width< panelSize.width)& tmp_height < (panelSize.height/getNumPanels())) {
                font_size++;
                x = new Font("Helvetica",Font.PLAIN,font_size);
                messageLabel.setFont(x);

            }
            else{break;}
        }

        x = new Font("Helvetica",Font.PLAIN,font_size);
        messageLabel.setFont(x);
        messageLabel.setForeground(text_color);
        messageLabel.setBackground(backgroundColour);
    }

    //method to create info object atop JLabel
    private void createInfoLabel(String info,Color text_color)
    {
        informationLabel = new JTextArea();
        informationLabel.setText(info);
        informationLabel.setLineWrap(true);
        Dimension x = new Dimension(panelSize.width - (panelSize.width/10),panelSize.height/getNumPanels());
        informationLabel.setSize(x);
        informationLabel.setWrapStyleWord(true);



        int font_size = 0;
        Font e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLabel.setFont(e);

        while(true)
        {
            double tmp_height = informationLabel.getPreferredSize().getHeight();
            if(tmp_height < panelSize.height/getNumPanels()) {
                font_size++;
                e = new Font("Helvetica",Font.PLAIN,font_size);
                informationLabel.setFont(e);

            }
            else{
                font_size-=1;
                e = new Font("Helvetica",Font.PLAIN,font_size);
                informationLabel.setFont(e);
                break;
            }
        }

        e = new Font("Helvetica",Font.PLAIN,font_size);
        informationLabel.setFont(e);
        informationLabel.setEditable(false);
        informationLabel.setForeground(text_color);
        informationLabel.setBackground(backgroundColour);
    }

    // Preview Viewer Section
    private void layoutPanels()
    {
        //init dimension vars
        Dimension pic_size = null,msg_size = null,info_size = null;

        if(pictureExists&&messageExists&&infoExists) {
            Dimension x = new Dimension(panelSize.width, panelSize.height/getNumPanels());
            pic_size=msg_size=info_size = x;
        }
        else if((pictureExists&&infoExists)&!messageExists){
            pic_size = new Dimension((int)panelSize.getWidth(),(int)(panelSize.getHeight()/3)*2);
            info_size = new Dimension((int)panelSize.getWidth(),(int)(panelSize.getHeight()/3));
        }
        else if((messageExists&&infoExists)&!pictureExists){
            msg_size = new Dimension((int)panelSize.getWidth(),(int)(panelSize.getHeight()/3)*2);
            info_size = new Dimension((int)panelSize.getWidth(),(int)(panelSize.getHeight()/3));
        }

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        //set the size of the panels
        if(messageExists)
        {
            messagePanel.setPreferredSize(msg_size);
            messagePanel.add(messageLabel);
            messagePanel.setLayout(new BoxLayout(messagePanel,BoxLayout.Y_AXIS));
            messagePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
            messagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.add(messagePanel);
        }
        if(pictureExists)
        {
            imagePanel.setPreferredSize(pic_size);
            imagePanel.setLayout(new BoxLayout(imagePanel,BoxLayout.Y_AXIS));
            imagePanel.add(imageLabel);
            if (messageExists || infoExists) {
                this.add(Box.createVerticalGlue());
            }
            this.add(imagePanel);
            imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            if (!messageExists && !infoExists){
                imagePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
            }
            else {
                imagePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            }
        }
        if(infoExists) {
            infoPanel.setPreferredSize(info_size);
            infoPanel.add(informationLabel);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.add(infoPanel);
        }
    }
}
