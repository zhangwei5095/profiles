package com.tagtraum.ideajad;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version @version@,  $Id: EventHelper.java,v 1.2 2003/11/13 21:45:43 hendriks73 Exp $
 */
public class EventHelper {

    public static VirtualFile[] getVirtualFiles(AnActionEvent event) {
        return (VirtualFile[]) event.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
    }

    public static Project getProject(AnActionEvent event) {
        return (Project) event.getDataContext().getData(DataConstants.PROJECT);
    }

    public static Editor getEditor(AnActionEvent event) {
        return (Editor) event.getDataContext().getData(DataConstants.EDITOR);
    }

// --Recycle Bin START (11/27/02 8:47 PM):
//    public static OpenFileDescriptor getEditFileDescriptor(AnActionEvent event) {
//        return (OpenFileDescriptor)event.getDataContext().getData(DataConstants.OPEN_FILE_DESCRIPTOR);
//    }
// --Recycle Bin STOP (11/27/02 8:47 PM)

}
