package com.tagtraum.ideajad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version @version@,  $Id: JLink.java,v 1.2 2003/11/13 21:45:43 hendriks73 Exp $
 */
public class JLink extends JButton {

    public JLink(String url) {
        super(url);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorderPainted(false);
        setForeground(Color.blue);
        setMargin(new Insets(0, 4, 0, 4));
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Runtime.getRuntime().exec(new String[]{"RUNDLL32", "url.dll,FileProtocolHandler", getText()});
                } catch (IOException e1) {
                    String message = e1.getMessage() == null ? e1.toString() : e1.getMessage();
                    JOptionPane.showMessageDialog(JLink.this, "Failed to open URL " + getText() + System.getProperty("line.separator") + message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
