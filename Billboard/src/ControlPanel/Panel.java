package ControlPanel;
import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.event.ActionListener;

public class Panel {

    public static void main(String[] args){
        SwingUtilities.invokeLater( () -> {
            new ControlFrame("Login Form");
        });
    }

}

