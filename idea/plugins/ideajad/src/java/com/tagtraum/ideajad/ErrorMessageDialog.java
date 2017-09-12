package com.tagtraum.ideajad;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author <init href="mailto:hs@tagtraum.com">Hendrik Schreiber</init>
 * @version @version@,  $Id: ErrorMessageDialog.java,v 1.3 2004/01/22 22:16:35 hendriks73 Exp $
 */
public class ErrorMessageDialog extends DialogWrapper {

    protected String myMessage;
    protected String myOptions[];
    protected int myDefaultOptionIndex;
    protected Icon myIcon;
    private String myErrorMessage;

    private void _init(String title, String message, String errorMessage, String options[], int defaultOption, Icon icon) {
        setTitle(title);
        this.myMessage = message;
        this.myOptions = options;
        this.myDefaultOptionIndex = defaultOption;
        this.myIcon = icon;
        this.myErrorMessage = errorMessage;
        setButtonsAlignment(0);
        init();
    }

    protected Action[] createActions() {
        Action aaction[] = new Action[myOptions.length];
        for (int i = 0; i < myOptions.length; i++) {
            String s = myOptions[i];
            int j = i;
            aaction[i] = new CloseAction(s, j);
            if (i == myDefaultOptionIndex)
                aaction[i].putValue("DefaultAction", ((Object) (Boolean.TRUE)));
        }

        return aaction;
    }

    public void doCancelAction() {
        close(-1);
    }

    protected JComponent createNorthPanel() {
        JPanel jpanel = new JPanel(new BorderLayout(15, 0));
        if (myIcon != null) {
            JLabel jlabel = new JLabel(myIcon);
            java.awt.Container container = new Container();
            container.setLayout(new BorderLayout());
            container.add(jlabel, "North");
            jpanel.add(container, "West");
        }
        if (myMessage != null) {
            JLabel jlabel = new JLabel(myMessage);
            //jlabel.setUI(getUI());
            java.awt.Container container = new Container();
            container.setLayout(new BorderLayout(0, 10));
            container.add(jlabel, "North");
            JTextArea area = new JTextArea(20, 80);
            area.setEditable(false);
            area.setText(myErrorMessage);
            JScrollPane scrollPane = new JScrollPane(area);
            container.add(scrollPane, "Center");
            jpanel.add(container, "Center");
            scrollPane.getVerticalScrollBar().setValue(0);
        }
        return jpanel;
    }

    protected JComponent createCenterPanel() {
        return null;
    }

    public ErrorMessageDialog(Project project, String message, String title, String errorMessage, String as[], int i, Icon icon) {
        super(project, false);
        _init(title, message, errorMessage, as, i, icon);
    }

    public ErrorMessageDialog(Component component, String message, String title, String errorMessage, String as[], int i, Icon icon) {
        super(component, false);
        _init(title, message, errorMessage, as, i, icon);
    }

    public ErrorMessageDialog(String message, String title, String errorMessage, String as[], int i, Icon icon) {
        super(false);
        _init(title, message, errorMessage, as, i, icon);
    }

    private static int showErrorMessageDialog(java.lang.String message, java.lang.String title, String errorMessage, java.lang.String as[], int i) {
        ErrorMessageDialog messagedialog = new ErrorMessageDialog(message, title, errorMessage, as, i, Messages.getErrorIcon());
        messagedialog.show();
        return messagedialog.getExitCode();
    }

    public static void showErrorMessageDialog(String title, String message, String errorMessage) {
        ErrorMessageDialog.showErrorMessageDialog(message, title, errorMessage, new java.lang.String[]{"OK"}, 0);
    }


    private class CloseAction extends AbstractAction {

        private final int a;
        private final ErrorMessageDialog b;

        CloseAction(String s, int i) {
            super(s);
            b = ErrorMessageDialog.this;
            a = i;
        }

        public void actionPerformed(java.awt.event.ActionEvent actionevent) {
            b.close(a);
        }

    }
}
