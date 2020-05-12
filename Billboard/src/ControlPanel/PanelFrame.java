package ControlPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelFrame extends JFrame{
    public PanelFrame(String title){
        super(title);

        //Set Layout
        setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();

        //Create Widgets
        JLabel viewer = new JLabel("Viewer");
        JButton schedButton = new JButton("+");
        JLabel firstSched = new JLabel("First");

        //Add Widgets to Pane
        Container cont = getContentPane();
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = 0;
        //Make viewer display dummy icon
        viewer.setIcon(new ImageIcon(getClass().getResource(
                "/Images/CAB320_Billboard_ERROR.jpg"))
                                                            );
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
}
