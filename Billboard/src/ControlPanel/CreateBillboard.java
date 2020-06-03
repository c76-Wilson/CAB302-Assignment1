package ControlPanel;

import BillboardViewer.Billboard_Viewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private Image billboardImage;

    public CreateBillboard(Dimension size) {
        initInputs(size);
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
        messageText = new JTextField("Billboard Message");
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
        imageURL.setEnabled(false);
        infoTextColour = new JButton("Choose Information Colour");
        infoTextColour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoColour = JColorChooser.showDialog(CreateBillboard.this, "Choose Information Colour", infoColour);
            }
        });
        infoText = new JTextField("Billboard Information");
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
                    viewer = new PreviewViewer(messageText.getText(), messageColour, infoText.getText(), infoColour, imageURL.getText(), backgroundColour, new Dimension(viewerPanel.getWidth(), viewerPanel.getHeight()));
                }
                else if (!useURL.isSelected() && !imageLocation.getText().isBlank()){
                    File file = new File(imageLocation.getText());
                    try (FileInputStream imageInFile = new FileInputStream(imageLocation.getText())) {
                        // Reading a Image file from file system
                        byte imageData[] = new byte[(int) file.length()];
                        imageInFile.read(imageData);
                        String base64Image = Base64.getEncoder().encodeToString(imageData);

                        viewer = new PreviewViewer(messageText.getText(), messageColour, infoText.getText(), infoColour, base64Image, backgroundColour, new Dimension(viewerPanel.getWidth(), viewerPanel.getHeight()));
                    } catch (IOException ioe) {
                        System.out.println("Exception while reading the Image " + ioe);
                    }
                }

                if (viewer != null){
                    viewerPanel.remove(viewer);

                    viewerPanel.add(viewer);

                    pack();
                }
            }
        });
        submitBillboard = new JButton("Submit Billboard");

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

        constraints.gridy = 5;
        inputPanel.add(submitBillboard, constraints);
        constraints.gridx = 1;
        inputPanel.add(previewBillboard, constraints);

        constraints.gridy = 4;
        constraints.gridwidth = 2;
        JSeparator separator = new JSeparator();
        inputPanel.add(separator, constraints);

        add(inputPanel, BorderLayout.SOUTH);

        viewerPanel = new JPanel();
        add(viewerPanel, BorderLayout.CENTER);
    }


}
