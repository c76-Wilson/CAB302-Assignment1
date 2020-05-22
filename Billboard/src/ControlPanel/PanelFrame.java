package ControlPanel;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class PanelFrame extends JFrame{
    private JLabel viewer;
    private JButton schedButton;
    private JLabel firstSched;
    private int num_panels = 1;

    public Dimension frameDim = new Dimension(1280, 720);

    public PanelFrame(String title){
        super(title);

        //Set Layout
        setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();

        //Create Widgets
        viewer = new JLabel("Viewer");
        schedButton = new JButton("+");
        firstSched = new JLabel("First");

        //Add Widgets to Pane
        Container cont = getContentPane();
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = 0;
        //Make viewer display dummy icon
        try {
            createImageLocal(viewer, "C:/CAB302/2020/Assignment/Billboard/src/Images/302_ERROR.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        cont.add(viewer, grid);
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = 1;
        cont.add(schedButton, grid);
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = 2;
        cont.add(firstSched, grid);
        firstSched.setVisible(false);

        //Add Actions
        schedButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                firstSched.setVisible(true);
            }
        });
    }

    private Dimension getScaledImage(BufferedImage img, Dimension bounds)
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

        return new Dimension(new_w, new_h);
    }

    //pull image from local project and instantiate onto a JLabel
    private void createImageLocal(JLabel imagePanel, String base_image) throws IOException
    {
        Dimension bounds = new Dimension(WIDTH,HEIGHT/num_panels);
        imagePanel.setPreferredSize(bounds);

        byte[] buffImage = Base64.getMimeDecoder().decode(base_image);

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(buffImage));
        Dimension img_dimen = getScaledImage(img,bounds);

        Image temp_img = img.getScaledInstance((int)img_dimen.getWidth(),
                (int)img_dimen.getHeight(),
                Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(temp_img);
        imagePanel.setIcon(icon);
    }
}
