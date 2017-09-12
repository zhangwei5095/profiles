package com.tagtraum.ideajad;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.projectRoots.ProjectRootType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.zip.ZipFile;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version 2169.2,  $Id: DecompileAction.java,v 1.10 2004/07/07 03:42:53 hendriks73 Exp $
 */
public class DecompileAction extends AnAction {

    public static final String ID = "Decompile";
    private static String baseDir;
    public static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");
    public static final boolean LINUX = System.getProperty("os.name").toLowerCase().indexOf("linux") != -1;
    public static final boolean MACOSX = System.getProperty("os.name").toLowerCase().indexOf("mac os x") != -1;
    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.ideajad.localStrings");

    public DecompileAction() {
        this(null);
    }

    public DecompileAction(String s) {
        this(s, null, null);
    }

    public DecompileAction(String s, String s1, Icon icon) {
        super(s, s1, icon);
    }

    public void actionPerformed(final AnActionEvent event) {
        Logger.getInstance(getClass().getName()).info("actionPerformed(AnActionEvent): " + event);
        final Project project = EventHelper.getProject(event);
        final VirtualFile[] files = EventHelper.getVirtualFiles(event);
        for (int i = 0; i < files.length; i++) actionPerformed(project, files[i]);
    }

    public void actionPerformed(final Project project, final VirtualFile classFile) {
        Logger.getInstance(getClass().getName()).info("actionPerformed(Project, VirtualFile): " + project + ", " + classFile);
        Logger.getInstance(getClass().getName()).info("classFile.getPath(): " + classFile.getPath());
        final JadOptions options = getOptions(project);
        int indexOfChosenDir = 0;
        String outputPath = options.getOutputDirectory().replace(File.separatorChar, '/');
        //System.out.println("outputPath: " + outputPath);
        VirtualFile decompilationDir = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(outputPath);
        if (decompilationDir != null) {
            // does the decompiled file already exist?
            VirtualFile decompiledFile = getDecompiledFile(classFile, decompilationDir, project);
            if (decompiledFile != null) {
                // TODO: show file
            }
        }

        //System.out.println("decompilationDir: " + decompilationDir);
        if (options.getOutputDirectory() == null || options.getOutputDirectory().length() == 0 || decompilationDir == null) {
            VirtualFile[] decompilationDirs = getDecompilationDirs(project);
            if (decompilationDirs.length == 0) {
                Messages.showMessageDialog(localStrings.getString("message.no_decomp_dir_found"), "IdeaJad-Plugin", Messages.getErrorIcon());
                return;
            }
            if (decompilationDirs.length > 1) {
                String[] dirStrings = getStringArray(decompilationDirs);
                int idx = getPreselectedIndex(dirStrings, options);
                indexOfChosenDir = Messages.showChooseDialog(localStrings.getString("message.choose_target_dir"), "IdeaJad-Plugin", dirStrings, dirStrings[idx], Messages.getQuestionIcon());
            }
            if (indexOfChosenDir >= 0) {
                decompilationDir = decompilationDirs[indexOfChosenDir];
                options.setOutputDirectory(decompilationDir.getPresentableUrl());
            }
        }
        if (indexOfChosenDir >= 0) {
            final IdeaJadImpl ideaJad = (IdeaJadImpl) project.getComponent(IdeaJad.class);
            try {
                long start = System.currentTimeMillis();
                String execPath = getBaseDir() + getJadExecutable();
                if (!WINDOWS) {
                    // try to make sure that the jad executable is really executable
                    try {
                        Process p = Runtime.getRuntime().exec(new String[]{"chmod", "u+x", execPath});
                        new StreamPumper(p.getErrorStream()).start();
                        new StreamPumper(p.getInputStream()).start();
                        p.waitFor();
                    } catch (IOException e) {
                        // if this fails, most likely the execution of jad fails, too
                        // therefore we fail silently
                        Logger.getInstance(getClass().getName()).error(e);
                    }
                }
                Jad jad = new Jad(execPath);
                String fileToDecompile = getFilesToDecompile(classFile, ideaJad.getProjectId());
                try {
                    jad.decompile(ideaJad.getJadOptions(), new String[]{fileToDecompile});
                } catch (final JadException je) {
                    Logger.getInstance(getClass().getName()).info(je);
                    ErrorMessageDialog.showErrorMessageDialog("IdeaJad-Plugin", localStrings.getString("message.unexpected_output"), je.getMessage() + System.getProperty("line.separator") + System.getProperty("line.separator") + je.getOutput());
                }
                if (isJar(classFile)) postProcess(options, start, new File(ideaJad.getJadOptions().getOutputDirectory()), decompilationDir);
                if (!isJar(classFile)) showDecompiledFile(decompilationDir, classFile, project, options);
            } catch (InterruptedException e) {
                Logger.getInstance(getClass().getName()).error(e);
            } catch (final Exception e) {
                Logger.getInstance(getClass().getName()).error(e);
                Messages.showMessageDialog(e.toString(), "IdeaJad-Plugin", Messages.getErrorIcon());
            }
        }
    }

    /**
     * @return the path to the executble - if we are on an unknown platform, return /bin/jad
     */
    private static String getJadExecutable() {
        if (WINDOWS) return "/bin/windows/jad.exe";
        else if (LINUX) return "/bin/linux/jad";
        else if (MACOSX) return "/bin/macosx/jad";
        else return "/bin/jad";
    }

    /**
     * This is a hack to mark decompiled as last modified without knowing their names.
     * We just mark all that have been modified since the time decompilation started.
     * Dirty, dirty, dirty...
     */
    private void postProcess(JadOptions options, long start, File base, final VirtualFile decompilationDir) {
        if (options.isLineNumbersAsComments() && options.isSort()) sort(base,start);
        if (options.isReadonly()) markAsReadOnly(base, start);
        // try to synchronize...
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                decompilationDir.refresh(false, true);
            }
        });
    }

    private void sort(File base, long start) {
        try {
            File[] kids = base.listFiles();
            for (int i = 0; i < kids.length; i++) {
                if (kids[i].isDirectory()) sort(kids[i], start);
                else if (kids[i].isFile() && kids[i].lastModified() >= start && kids[i].canWrite()) {
                    LineSorter.sort(kids[i], false);
                }
            }
        } catch (Exception e) {
            Logger.getInstance(getClass().getName()).error(e);
        }
    }

    private void markAsReadOnly(File base, long start) {
        try {
            File[] kids = base.listFiles();
            for (int i = 0; i < kids.length; i++) {
                if (kids[i].isDirectory())
                    markAsReadOnly(kids[i], start);
                else if (kids[i].isFile() && kids[i].lastModified() >= start) kids[i].setReadOnly();
            }
        } catch (Exception e) {
            Logger.getInstance(getClass().getName()).error(e);
        }
    }

    private void showDecompiledFile(final VirtualFile decompilationDir, final VirtualFile currentlyOpenFile, final Project project, final JadOptions options) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                decompilationDir.refresh(false, true);
                VirtualFile decompiledFile = getDecompiledFile(currentlyOpenFile, decompilationDir, project);
                if (decompiledFile == null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Messages.showMessageDialog(project, localStrings.getString("message.failed_find_open_decomp_file"), "IdeaJad-Plugin", Messages.getErrorIcon());
                        }
                    });
                    return;
                }
                final File file = new File(decompiledFile.getPresentableUrl());
                if (options.isLineNumbersAsComments() && options.isSort()) {
                    try {
                        if (file.canWrite()) LineSorter.sort(file, false);
                    }
                    catch (Exception e) {
                        Logger.getInstance(getClass().getName()).error(e);
                    }
                }
                // try to set readonly. this is a HACK... (hs)
                if (options.isReadonly()) {
                    try {
                        if (file.isFile()) file.setReadOnly();
                    } catch (Exception e) {
                        Logger.getInstance(getClass().getName()).error(e);
                    }
                }
                FileEditorManager fem = FileEditorManager.getInstance(project);
                FileEditor[] editors=fem.getEditors(currentlyOpenFile);
                if (editors != null && editors.length>0) {
                    // close idea stub
                    fem.closeFile(currentlyOpenFile);
                }
                // open decompiled file
                fem.openFile(decompiledFile, true);
                decompiledFile.refresh(false, true);
                Editor editor = fem.getSelectedTextEditor();
                //Editor editor = fem.openTextEditor(new OpenFileDescriptor(decompiledFile), true);
                Document document = editor.getDocument();
                char[] chars = document.getChars();
                int line = findFirstNonEmptyLine(chars);
                LogicalPosition logicalPosition = new LogicalPosition(line, 0);
                editor.getScrollingModel().scrollTo(logicalPosition, ScrollType.CENTER);
                editor.getCaretModel().moveToLogicalPosition(logicalPosition);
            }
        });
    }

    private static int findFirstNonEmptyLine(char[] chars) {
        int line = 0;
        for (int i=0; i<chars.length; i++) {
            if (chars[i] == '\n') line++;
            else if (chars[i] != '\r') break;
        }
        return line;
    }

    private int getPreselectedIndex(String[] dirStrings, JadOptions options) {
        try {
            String dirName = options.getOutputDirectory();
            if (dirName == null) return 0;
            if (dirName.startsWith("\"") && dirName.endsWith("\"")) dirName = dirName.substring(1, dirName.length() - 1);
            File preSetFile = new File(dirName).getCanonicalFile();
            for (int i = 0; i < dirStrings.length; i++) {
                File dir = new File(dirStrings[i]).getCanonicalFile();
                if (dir.equals(preSetFile)) return i;
            }
        } catch (IOException e) {
            Logger.getInstance(getClass().getName()).info(e);
            Messages.showMessageDialog(localStrings.getString("message.out_dir_access_error") + " " + e.getLocalizedMessage(), "IdeaJad-Plugin", Messages.getWarningIcon());
        }
        return 0;
    }

    private JadOptions getOptions(Project project) {
        IdeaJadImpl ideaJad = (IdeaJadImpl) project.getComponent(IdeaJad.class);
        JadOptions options = ideaJad.getJadOptions();
        return options;
    }

    private String getFilesToDecompile(VirtualFile virtualFile, String project) throws IOException {
        String filesToDecompile;
        if (isJar(virtualFile)) {
            File extractDir = getExtractDir(project);
            Logger.getInstance(getClass().getName()).info("Extract dir: " + extractDir);
            extractJar(virtualFile, extractDir, null);
            // this will decompile the whole jar
            filesToDecompile = extractDir + "/**/*.class";
        } else if (isInJar(virtualFile)) {
            String file = getPathWithoutJar(virtualFile);
            File extractDir = getExtractDir(project);
            Logger.getInstance(getClass().getName()).info("Extract dir: " + extractDir);
            extractJar(virtualFile, extractDir, getPrefix(file));
            // this will decompile the whole jar
            //filesToDecompile = "\"" + getExtractDir() + "/**/*.class\"";
            filesToDecompile = extractDir + file;
        } else {
            filesToDecompile = virtualFile.getPresentableUrl();
        }
        return filesToDecompile;
    }

    private String getPrefix(String file) {
        int idx = file.lastIndexOf('.');
        return file.substring(0, idx);
    }

    private String getPathWithoutJar(VirtualFile virtualFile) {
        return virtualFile.getUrl().substring(virtualFile.getUrl().indexOf('!') + 1);
    }

    private static boolean isInJar(VirtualFile virtualFile) {
        return virtualFile.getUrl().startsWith("jar:");
    }

    private static boolean isJar(VirtualFile virtualFile) {
        return "zip".equalsIgnoreCase(virtualFile.getExtension()) || "jar".equalsIgnoreCase(virtualFile.getExtension());
    }

    private VirtualFile getDecompiledFile(VirtualFile classFile, VirtualFile root, Project project) {
        IdeaJadImpl ideaJad = (IdeaJadImpl) project.getComponent(IdeaJad.class);
        JadOptions options = ideaJad.getJadOptions();
        String decompiledFile = stripRoot(classFile, project, ProjectRootType.CLASS);
        String decompiledFileUrl = root.getUrl() + "/" + decompiledFile;
        if (decompiledFileUrl.endsWith(".class")) decompiledFileUrl = decompiledFileUrl.substring(0, decompiledFileUrl.length() - ".class".length()) + "." + options.getFileExtension();
        VirtualFile decompiledVirtualFile = VirtualFileManager.getInstance().findFileByUrl(decompiledFileUrl);
        return decompiledVirtualFile;
    }

    private static URL getClassLocation(Class klass) {
        return klass.getClassLoader().getResource(klass.getName().replace('.', '/') + ".class");
    }

    private static synchronized String getBaseDir() throws IOException {
        if (baseDir == null) {
            URL url = getClassLocation(DecompileAction.class);
            String urlString = url.toString();
            url = new URL(urlString.substring("jar:".length(), urlString.indexOf('!')));
            urlString = URLDecoder.decode(url.getFile(), "UTF-8");
            if (urlString.startsWith("/") && urlString.indexOf(':') == 2) urlString = urlString.substring(1);
            baseDir = new File(urlString).getParentFile().getParent();
        }
        return baseDir;
    }

    private String stripRoot(VirtualFile file, Project project, ProjectRootType type) {
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getRootFiles(type);
        for (int i = 0; i < roots.length; i++) {
            if (file.getUrl().startsWith(roots[i].getUrl())) return file.getUrl().substring(roots[i].getUrl().length());
        }
        return null;
    }

    public static String[] getStringArray(VirtualFile[] decompilationDirs) {
        String[] dirStrings = new String[decompilationDirs.length];
        for (int i = 0; i < decompilationDirs.length; i++) {
            dirStrings[i] = decompilationDirs[i].getPresentableUrl();
        }
        return dirStrings;
    }

    public static VirtualFile[] getDecompilationDirs(Project project) {
        VirtualFile[] sourceRoots = ProjectRootManager.getInstance(project).getRootFiles(ProjectRootType.SOURCE);
        java.util.List list = new ArrayList();
        for (int i = 0; i < sourceRoots.length; i++) {
            if (sourceRoots[i].isDirectory() && sourceRoots[i].isWritable()) {
                list.add(sourceRoots[i]);
            }
        }
        return (VirtualFile[]) list.toArray(new VirtualFile[list.size()]);
    }

    public void update(AnActionEvent event) {
        VirtualFile[] vf = EventHelper.getVirtualFiles(event);
        if (vf != null) {
            boolean setToTrue = false;
            for (int i = 0; i < vf.length; i++) {
                /*
                Logger logger = Logger.getInstance("ideajad");
                logger.info("    " + vf[i].getPresentableUrl());
                logger.info("URL " + vf[i].getUrl());
                logger.info("----");
                */
                if (isJar(vf[i]) || "class".equals(vf[i].getExtension())) {
                    setToTrue = true;
                    break;
                }
            }
            event.getPresentation().setEnabled(setToTrue);
        } else {
            event.getPresentation().setEnabled(false);
        }
    }

    private void extractJar(VirtualFile jarFile, File destDir, String prefix) throws IOException {
        ZipFile zipFile = getZipFile(jarFile);
        ZipHelper.extract(zipFile, destDir, true, prefix);
        zipFile.close();
    }

    private ZipFile getZipFile(VirtualFile vf) throws IOException {
        int idx = vf.getPath().indexOf('!');
        File file = null;
        if (idx == -1) {
            file = new File(vf.getPath());
        } else {
            file = new File(vf.getPath().substring(0, idx));
        }
        Logger.getInstance(getClass().getName()).info("Zip/Jar-file to extract: " + file);
        return new ZipFile(file, ZipFile.OPEN_READ);
    }

    private synchronized static File getExtractDir(String projectName) {
        File extractJarDir = new File(System.getProperty("java.io.tmpdir", System.getProperty("user.home") + "/ideajad-" + projectName + "/") + "/ideajad-" + projectName + "/");
        if (!extractJarDir.exists()) {
            extractJarDir.mkdir();
        }
        extractJarDir.deleteOnExit();
        return extractJarDir;
    }

}
