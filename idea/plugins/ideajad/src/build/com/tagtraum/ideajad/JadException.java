package com.tagtraum.ideajad;

import java.io.IOException;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version 2169.2,  $Id: JadException.java,v 1.2 2003/11/13 21:45:43 hendriks73 Exp $
 */
public class JadException extends IOException {

    private String output;

    public JadException(String[] command, String output) {
        super(getCommandLine(command));
        this.output = output;
    }

    private static String getCommandLine(String[] command) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<command.length; i++) {
            sb.append(command[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public String getOutput() {
        return output;
    }
}
