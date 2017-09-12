package com.tagtraum.ideajad;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version @version@,  $Id: IdeaJadImpl.java,v 1.5 2004/07/07 03:42:53 hendriks73 Exp $
 */
public class IdeaJadImpl implements IdeaJad, FileEditorManagerListener {

    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.ideajad.localStrings");
    private static int projectIdCounter;
    private JadOptions jadOptions;
    private OptionsPanel configComponent;
    private Project project;
    private int projectId;
    private Icon icon;
    private static final int YES = 0;

    public IdeaJadImpl(Project project) {
        synchronized (IdeaJadImpl.class) {
            projectId = projectIdCounter++;
        }
        jadOptions = new JadOptions();
        Logger logger = Logger.getInstance(getClass().getName());
        logger.info("IdeaJad: (c) 2002-@year@ tagtraum industries incorporated - http://www.tagtraum.com/");
        logger.info("IdeaJad: Initialized IdeaJad [" + projectId + "]");
        this.project = project;
        this.icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("configurableDecompiler.png")));
    }

    public void fileOpened(FileEditorManager source, VirtualFile file) {
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        selectedFileChanged(event);
    }

    public void selectedFileChanged(final FileEditorManagerEvent e) {
        if (e.getNewFile() != null && "class".equals(e.getNewFile().getExtension())) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    int exitCode = YES;
                    if (getJadOptions().isConfirmNavigationTriggeredDecompile()) exitCode = Messages.showYesNoDialog(localStrings.getString("message.decompile_class"), "IdeaJad-Plugin", Messages.getQuestionIcon());
                    if (exitCode == YES) {
                        DecompileAction da = (DecompileAction) ActionManager.getInstance().getAction(DecompileAction.ID);
                        da.actionPerformed(project, e.getNewFile());
                    }
                }
            });
        }
    }

    public JadOptions getJadOptions() {
        return jadOptions;
    }

    public String getDisplayName() {
        return "IdeaJad";
    }

    public Icon getIcon() {
        return icon;
    }

    public String getHelpTopic() {
        return null;
    }

    public synchronized JComponent createComponent() {
        if (configComponent == null) {
            configComponent = new OptionsPanel(project, jadOptions);
        }
        return configComponent;
    }

    public synchronized boolean isModified() {
        if (configComponent == null) return false;
        return configComponent.isModified(jadOptions);
    }

    public synchronized void apply() throws ConfigurationException {
        if (configComponent != null) jadOptions = configComponent.getJadOptions();
    }

    public synchronized void reset() {
        if (configComponent != null) configComponent.setJadOptions(jadOptions);
    }

    public synchronized void disposeUIResources() {
        configComponent = null;
    }

    public void initComponent() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.addFileEditorManagerListener(this);
    }

    public void disposeComponent() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.removeFileEditorManagerListener(this);
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void readExternal(Element element) throws InvalidDataException {
        Logger.getInstance("IDEA-Config").info("readExternal(Element element)");
        jadOptions.readExternal(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        Logger.getInstance("IDEA-Config").info("writeExternal(Element element)");
        jadOptions.writeExternal(element);
    }

    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public String getProjectId() {
        return Integer.toString(projectId);
    }

}
