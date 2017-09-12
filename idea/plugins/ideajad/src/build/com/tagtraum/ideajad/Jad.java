package com.tagtraum.ideajad;

import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.util.Arrays;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version 2169.2,  $Id: Jad.java,v 1.2 2003/11/13 21:45:43 hendriks73 Exp $
 */
public class Jad {
    private String execPath;
    private JadOptions defaultOptions = new JadOptions();

    public Jad(String execPath) {
        setExecPath(execPath);
    }

    public String getExecPath() {
        return execPath;
    }

    public void setExecPath(String execPath) {
        this.execPath = execPath;
    }

    public JadOptions getDefaultOptions() {
        return defaultOptions;
    }

    public void setDefaultOptions(JadOptions defaultOptions) {
        this.defaultOptions = defaultOptions;
    }

    public InputStream decompile(String[] files) throws IOException, InterruptedException {
        return decompile(this.defaultOptions, files);
    }

    public InputStream decompile(JadOptions options, String[] files) throws IOException, InterruptedException {
        //System.out.println(execPath + options.toString() + " " + files);
        String[] optionsArray = options.toStringArray();
        String[] command = new String[files.length + optionsArray.length + 1];
        int i = 0;
        command[i++] = execPath;
        for (int j = 0; j < optionsArray.length; j++) {
            command[i++] = optionsArray[j];
        }
        for (int j = 0; j < files.length; j++) {
            command[i++] = files[j];
        }
        //String _command = execPath + options.toString() + " " + files;
        // Thanks to Edoardo Comar for this workaround for Linux
        //if (!DecompileAction.WINDOWS) _command = _command.replace('"',' ');
        Logger.getInstance(getClass().getName()).info("Jad commandline: " + Arrays.asList(command));
        Process p = Runtime.getRuntime().exec(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        new StreamPumper(p.getErrorStream(), err).start();
        new StreamPumper(p.getInputStream(), out).start();
        p.waitFor();
        if (err.size() > 0) Logger.getInstance(getClass().getName()).info("System.err: " + err.toString());
        if (out.size() > 0) Logger.getInstance(getClass().getName()).info("System.out: " + out.toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(err.toByteArray())));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("Parsing")) throw new JadException(command, err.toString());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}
