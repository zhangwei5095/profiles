package com.tagtraum.ideajad;

import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;

/**
 * Re-arranges a decompiled file according to its line numbers.
 * Requires jad option -lnc (lineNumbersAsComments).
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version 2169.2,  $Id: LineSorter.java,v 1.6 2004/07/07 03:42:53 hendriks73 Exp $
 */
public class LineSorter {

    private static Logger LOG = Logger.getInstance("LineSorter");
    private static final Pattern LINE_NUMBER_PATTERN = Pattern.compile("^/\\*\\s*(\\d+)\\*/");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int LINE_NUMBER_MARKER_LENGTH = 8;

    public static void sort(File file, boolean backup) throws IOException {
        if (!file.canWrite()) {
            throw new IOException("File is readonly and can not be sorted: " + file);
        }
        //LOG.setLevel(Level.DEBUG);
        LOG.debug("Trying to sort " + file + ", size: " + file.length());
        if (backup) {
            File backupFile = new File(file.toString() + ".bak");
            copy(file, backupFile);
        }
        File tempFile = File.createTempFile("linesorter", ".java");
        LOG.debug("Temp file: " + tempFile);
        // TODO: Take care of charencodings
        LineNumberReader in = null;
        BufferedWriter out = null;
        try {
            in = new LineNumberReader(new FileReader(file), 64 * 1024); // let's have a big buffer
            List lines = reformat(in);
            String lineString;
            Block currentBlock = new Block();
            currentBlock.add(new Line("/* Decompiled by IdeaJad 2169.2 ~ tagtraum industries incorporated ~ http://www.tagtraum.com/ */", 1));
            out = new BufferedWriter(new FileWriter(tempFile));
            for (int i=0; i<lines.size(); i++) {
                lineString = (String)lines.get(i);
                LOG.debug("Line: " + lineString);
                // skip comment lines, as they waste valuable space
                if (lineString.trim().startsWith("//")) continue;
                Matcher lineNumberMatcher = LINE_NUMBER_PATTERN.matcher(lineString);
                if (lineNumberMatcher.find()) {
                    // we have a line  number
                    String lineNumberString = lineNumberMatcher.group(1);
                    LOG.debug("Linenumber: " + lineNumberString);
                    int lineNumber = Integer.parseInt(lineNumberString);
                    //String lineStringWithoutNumber = lineString.substring(lineNumberMatcher.end());
                    String lineStringWithoutNumber = lineString;
                    if (currentBlock.getLastLine() != null && currentBlock.getLastLine().getNumber() == lineNumber) {
                        // if the last line exists and has the same number, add this line to it without number
                        currentBlock.getLastLine().add(lineStringWithoutNumber);
                    } else {
                        // if this line is a new line, i.e. the last line has a different number, add a new line to the block
                        currentBlock.add(new Line(lineStringWithoutNumber, lineNumber));
                    }
                    String lastLine = currentBlock.getLastLine().getContent();
                    if (lastLine.endsWith("{")) {
                        LOG.debug("Block start," + in.getLineNumber() + ": " + currentBlock.getLastLine().getContent());
                        currentBlock = new Block(currentBlock, currentBlock.removeLastLine());
                    }
                } else {
                    // no line number
                    String trimmedLineString = lineString.trim();
                    LOG.debug("trimmedLineString," + in.getLineNumber() + ": '" + trimmedLineString + "'");
                    if ("{".equals(trimmedLineString)) {
                        currentBlock.getLastLine().add("{");
                        // this means we are starting a new block
                        LOG.debug("Block start," + in.getLineNumber() + ": " + currentBlock.getLastLine().getContent());
                        currentBlock = new Block(currentBlock, currentBlock.removeLastLine());
                    } else if (trimmedLineString.endsWith("{")) {
                        if (trimmedLineString.startsWith("}")) {
                            int closingBrace = lineString.indexOf('}');
                            // get rid of whatever is coming after '}'
                            currentBlock.add(new Line(lineString.substring(0, closingBrace + 1)));
                            LOG.debug("Block end," + in.getLineNumber() + ": " + currentBlock.getLastLine().getContent());
                            currentBlock = currentBlock.getParent();
                            // get rid if '}'
                            lineString = lineString.substring(0, closingBrace) + lineString.substring(closingBrace + 1).trim();
                        }
                        // this means we are starting a new block
                        LOG.debug("Block start," + in.getLineNumber() + ": " + lineString);
                        currentBlock = new Block(currentBlock, new Line(lineString));
                    } else if ("}".equals(trimmedLineString)) {
                        currentBlock.add(new Line(lineString));
                        LOG.debug("Block end," + in.getLineNumber() + ": " + currentBlock.getLastLine().getContent());
                        currentBlock = currentBlock.getParent();
                    } else if (trimmedLineString.startsWith("}")) {
                        int closingBrace = lineString.indexOf('}');
                        // get rid of whatever is coming after '}'
                        currentBlock.add(new Line(lineString.substring(0, closingBrace + 1)));
                        LOG.debug("Block end," + in.getLineNumber() + ": " + currentBlock.getLastLine().getContent());
                        currentBlock = currentBlock.getParent();
                        // get rid if '}'
                        lineString = lineString.substring(0, closingBrace) + lineString.substring(closingBrace + 1).trim();
                        currentBlock.add(new Line(lineString));
                    } else {
                        currentBlock.add(new Line(lineString));
                    }
                }
            }
            // try to sort things
            currentBlock.sort();
            // let's print it out
            currentBlock.write(out);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("Failed to close src reader", e);
                }
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("Failed to close tempfile writer", e);
                }
        }
        if (file.canWrite()) {
            LOG.debug("File " + file + " is read only. Trying to delete it...");
            file.delete();
        }
        copy(tempFile, file);
        tempFile.delete();
    }

    /**
     * Makes sure that multiple lines without linenumbers that belong into one line
     * (like method declarations) are really in one line. To detect them, we
     * count the indentation - if there is a step of two indents, its a hit.
     *
     * @return list of lines
     */
    private static List reformat(LineNumberReader in) throws IOException {
        String lineString = null;
        int lastIndent = 0;
        int indent = -1;
        boolean lastLineHadNoNumber = false;
        List lines = new ArrayList();
        while ((lineString = in.readLine()) != null) {
            LOG.debug("Line: " + lineString);
            if (lineString.length() > LINE_NUMBER_MARKER_LENGTH && !lineString.startsWith("/*")) {
                LOG.debug("no number");
                int thisIndent = 0;
                // count spaces or tabs
                for (; thisIndent<lineString.length() && (lineString.charAt(thisIndent) == ' ' || lineString.charAt(thisIndent) == '\t'); thisIndent++) ;
                thisIndent = thisIndent - LINE_NUMBER_MARKER_LENGTH;
                if (indent == -1 && thisIndent > 0) indent = thisIndent;
                LOG.debug("indent: " + indent);
                LOG.debug("lastIndent: " + lastIndent);
                LOG.debug("thisIndent: " + thisIndent);
                LOG.debug("indent step: " + ((thisIndent - lastIndent) / indent));
                LOG.debug("lastLineHadNoNumber: " + lastLineHadNoNumber);
                if ((thisIndent - lastIndent) / indent == 2 && lastLineHadNoNumber) {
                    LOG.debug("attached");
                    // add this line to the last line
                    String l = ((String)lines.get(lines.size()-1)) + " " + lineString.trim();
                    lines.set(lines.size()-1, l);
                }
                else {
                    LOG.debug("new line");
                    lastIndent = thisIndent;
                    lines.add(lineString);
                }
                lastLineHadNoNumber = true;
            }
            else {
                // strip case statement comments, which have the form "case 2: // '\002'"
                int casePos = lineString.indexOf("case");
                int colon;
                if (casePos != -1 && (colon = lineString.indexOf(": //", casePos)) != -1) lineString = lineString.substring(0, colon + 2);
                lines.add(lineString);
                lastLineHadNoNumber = false;
            }
        }
        return lines;
    }

    private static void copy(File src, File dst) throws IOException {
        LOG.debug("Trying to copy " + src + " to " + dst);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            byte[] buf = new byte[1024 * 64];
            int justRead = 0;
            while ((justRead = in.read(buf)) != -1) {
                if (justRead > 0) out.write(buf, 0, justRead);
            }
        } catch (IOException ioe) {
            LOG.error("Failed to create backup for " + src, ioe);
            throw new IOException("Failed to create backup for " + src);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("Failed to close copy inputstream", e);
                }
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("Failed to close copy outputstream", e);
                }
        }
    }

    private static abstract class Element {

        public abstract boolean hasNumber();

        public abstract int getNumber();
    }

    private static class Line extends Element {

        private int number = -1;
        private String content;

        public Line(String content) {
            this(content, -1);
        }

        public Line(String content, int number) {
            this.number = number;
            this.content = content;
        }

        public void add(String content) {
            this.content = this.content + " " + content.trim();
        }

        public int getNumber() {
            return number;
        }

        public boolean hasNumber() {
            return number != -1;
        }

        public String getContent() {
            return content;
        }
    }

    private static class Block extends Element {

        private Block parent;
        private List elements;

        public Block() {
            elements = new ArrayList();
        }

        public Block(Block parent, Line firstLine) {
            this();
            this.parent = parent;
            if (parent != null) parent.add(this);
            add(firstLine);
        }

        public void sort() {
            if (isSorted()) {
                // naive sorting algo
                List sortedElements = new ArrayList();
                int insertionPos = 0;
                Element lastElement = (Element) elements.get(elements.size() - 1);
                int size = elements.size();
                if (lastElement instanceof Line) size--;
                for (int i = 0; i < size; i++) {
                    Element element = (Element) elements.get(i);
                    if (element instanceof Block) ((Block) element).sort();
                    if (!element.hasNumber()) {
                        sortedElements.add(insertionPos, element);
                    } else {
                        for (insertionPos = 0; insertionPos < sortedElements.size(); insertionPos++) {
                            Element sortedElement = (Element) sortedElements.get(insertionPos);
                            if (sortedElement.hasNumber() && sortedElement.getNumber() > element.getNumber()) break;
                        }
                        sortedElements.add(insertionPos, element);
                    }
                    insertionPos++;
                }
                if (lastElement instanceof Line) sortedElements.add(lastElement);
                elements = sortedElements;
            }
        }

        public Block getParent() {
            return parent;
        }

        public Line getLastLine() {
            Object lastElement = elements.get(elements.size() - 1);
            if (lastElement instanceof Line) return (Line) lastElement;
            return null;
        }

        public Line removeLastLine() {
            Object lastElement = elements.get(elements.size() - 1);
            if (lastElement instanceof Line) {
                elements.remove(elements.size() - 1);
                return (Line) lastElement;
            }
            return null;
        }

        public void add(Element element) {
            elements.add(element);
        }

        public boolean hasNumber() {
            return getNumber() != -1;
        }

        public int getNumber() {
            int lowestNumber = -1;
            for (int i = 0; i < elements.size(); i++) {
                if (((Element) elements.get(i)).hasNumber()) {
                    if (lowestNumber == -1 || ((Element) elements.get(i)).getNumber() < lowestNumber) lowestNumber = ((Element) elements.get(i)).getNumber();
                }
            }
            return lowestNumber;
        }

        /**
         * Checks whether the lines in this block are sorted.
         */
        public boolean isSorted() {
            int currentLineNumber = 0;
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i) instanceof Line) {
                    Line line = (Line) elements.get(i);
                    if (line.hasNumber() && line.getNumber() < currentLineNumber) return false;
                    if (line.hasNumber()) currentLineNumber = line.getNumber();
                }
            }
            return true;
        }

        public void write(Writer out) throws IOException {
            List lines = getLines();
            Map offLines = findOffLines(lines);
            int currentLine = 1;
            boolean lastLineHadNumber = false;
            for (int i = 0; i < lines.size(); i++) {
                Line line = (Line) lines.get(i);
                if (line.hasNumber()) {
                    while (currentLine < line.getNumber()) {
                        out.write(LINE_SEPARATOR);
                        currentLine++;
                        checkOffLines(offLines, line, currentLine, out);
                    }
                    lastLineHadNumber = true;
                    if (currentLine != line.getNumber()) {
                        out.write("/*off*/");
                    }
                } else {
                    Line nextLineWithNumber = null;
                    int linesInbetween = 0;
                    for (int j = i + 1; j < lines.size(); j++) {
                        Line l = (Line) lines.get(j);
                        if (l.hasNumber()) {
                            nextLineWithNumber = l;
                            linesInbetween = j - i;
                            break;
                        }
                    }
                    if (nextLineWithNumber != null) {
                        int linesToSkip = nextLineWithNumber.getNumber() - currentLine - linesInbetween;
                        if (lastLineHadNumber) linesToSkip = Math.min(1, linesToSkip);
                        for (int k = 0, max = linesToSkip; k < max; k++) {
                            out.write(LINE_SEPARATOR);
                            currentLine++;
                            checkOffLines(offLines, line, currentLine, out);
                        }
                    } else {
                        out.write(LINE_SEPARATOR);
                        currentLine++;
                        checkOffLines(offLines, line, currentLine, out);
                    }
                    lastLineHadNumber = false;
                }
                out.write(line.getContent());
            }
        }

        private void checkOffLines(Map offLines, Line line, int currentLine, Writer out) throws IOException {
            final Integer key = new Integer(currentLine);
            if (offLines.containsKey(key)) {
                Line offLine = (Line) offLines.get(key);
                if (!offLine.getContent().equals(line.getContent()))
                    out.write("// off: " + offLine.getContent().substring(8));
            }
        }

        private Map findOffLines(List lines) {
            Map offLines = new HashMap();
            int currentLine = 1;
            for (int i = 0; i < lines.size(); i++) {
                Line line = (Line) lines.get(i);
                if (line.hasNumber()) {
                    if (line.getNumber() < currentLine)
                        offLines.put(new Integer(line.getNumber()), line);
                    else
                        currentLine = line.getNumber();
                }
            }
            return offLines;
        }

        private List getLines() {
            List lines = new ArrayList();
            for (int i = 0; i < elements.size(); i++) {
                Element element = (Element) elements.get(i);
                if (element instanceof Line) {
                    lines.add(element);
                } else {
                    Block block = (Block) element;
                    lines.addAll(block.getLines());
                }
            }
            return lines;
        }

        public String toString() {
            return toString(0);
        }

        private String toString(int indent) {
            StringBuffer sb = new StringBuffer();
            sb.append("Block " + getNumber() + "\r\n");
            for (int i = 0; i < elements.size(); i++) {
                Element element = (Element) elements.get(i);
                if (element instanceof Line) {
                    for (int j = 0; j < indent; j++) sb.append("+");
                    sb.append("|" + element.getNumber() + " " + ((Line) element).getContent() + "\r\n");
                } else {
                    sb.append(((Block) element).toString(indent + 1));
                }
            }
            return sb.toString();
        }
    }
}
