/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Oct 26, 2002
 * Time: 3:23:58 PM
 * To change this template use Options | File Templates.
 */
package com.tagtraum.ideajad;

public class Util {
    public static String stripQuotes(String dir) {
        if (dir != null && dir.startsWith("\"") && dir.endsWith("\"")) return dir.substring(1, dir.length() - 1);
        return dir;
    }
}
