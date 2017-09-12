package com.tagtraum.ideajad;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Element;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version 2169.2,  $Id: JadOptions.java,v 1.4 2003/11/13 21:45:43 hendriks73 Exp $
 */
public class JadOptions implements Cloneable, JDOMExternalizable {

    public static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") != -1;
    //private static final boolean UNIX = System.getProperty("file.separator").equals("/");
    public static final boolean MACOS = System.getProperty("os.name").toLowerCase().indexOf("mac") != -1;
    public static final Integer DEFAULT_MAX_STRING_LENGTH = new Integer(64);
    public static final int MIN_MAX_STRING_LENGTH = 1;
    public static final int MAX_MAX_STRING_LENGTH = 128;

    public static final Integer DEFAULT_LONG_RADIX = new Integer(10);
    public static final Integer DEFAULT_INT_RADIX = new Integer(10);
    public static final int MIN_RADIX = Character.MIN_RADIX;
    public static final int MAX_RADIX = Character.MAX_RADIX;

    public static final Integer DEFAULT_INDENTATION = new Integer(4);
    public static final int MIN_INDENTATION = 0;
    public static final int MAX_INDENTATION = 20;
    public static final String DEFAULT_FILE_EXTENSION = "java";
    public static final Integer DEFAULT_PACK_FIELDS = new Integer(3);
    public static final Integer DEFAULT_PACK_IMPORTS = new Integer(7);

    private boolean annotate;
    private boolean annotateFully;
    private boolean braces;
    private boolean clear;
    private String outputDirectory = "";
    private boolean dead = true;
    private boolean dissassemblerOnly;
    private boolean fullyQualifiedNames;
    private boolean fieldsFirst = true;
    private boolean defaultInitializers;
    private Integer maxStringLength = DEFAULT_MAX_STRING_LENGTH;
    private boolean lineNumbersAsComments = true;
    private Integer longRadix = DEFAULT_LONG_RADIX;
    private boolean splitStringsAtNewline;
    private boolean noconv;
    private boolean nocast;
    private boolean noclass;
    private boolean nocode;
    private boolean noctor;
    private boolean nodos;
    private boolean nofd;
    private boolean noinner;
    private boolean nolvt;
    private boolean nonlb = false;
    private boolean overwrite = true;
    private boolean pipe = false;
    private boolean restorePackages = true;
    private Integer intRadix = DEFAULT_INT_RADIX;
    private String fileExtension = DEFAULT_FILE_EXTENSION;
    private boolean safe;
    private boolean spaceAfterKeyword;
    // should never change
    private static final boolean STATISTICS = false;
    private Integer indentation = DEFAULT_INDENTATION;
    private boolean useTabs;

    // added options as of 6.9.2003
    private String prefixPackages;
    private String prefixNumericalClasses = "_cls";
    private String prefixUnusedExceptions = "_ex";
    private String prefixNumericalFields = "_fld";
    private String prefixNumericalLocals = "_lcl";
    private String prefixNumericalMethods = "_mth";
    private String prefixNumericalParameters = "_prm";
    private Integer packFields = DEFAULT_PACK_FIELDS;
    private Integer packImports = DEFAULT_PACK_IMPORTS;

    private boolean sort = false;

    // should never change
    private static final boolean VERBOSE = false;
    private boolean readonly;
    private boolean confirmNavigationTriggeredDecompile = true;

    private void appendSingleWordOption(List list, String name, boolean on) {
        StringBuffer sb = new StringBuffer();
        sb.append('-');
        sb.append(name);
        if (on) sb.append('+');
        else sb.append('-');
        list.add(sb.toString());
    }

    public String[] toStringArray() {
        List list = new ArrayList();
        appendSingleWordOption(list, "a", annotate);
        appendSingleWordOption(list, "af", annotateFully);
        appendSingleWordOption(list, "b", braces);
        // clear does not seem to support the +/- syntax (hs)
        if (clear) list.add("-clear");
        if (outputDirectory != null && outputDirectory.length() > 0) {
            list.add("-d");
            list.add(outputDirectory);
        }
        appendSingleWordOption(list, "dead", dead);
        appendSingleWordOption(list, "dis", dissassemblerOnly);
        appendSingleWordOption(list, "f", fullyQualifiedNames);
        appendSingleWordOption(list, "ff", fieldsFirst);
        appendSingleWordOption(list, "i", defaultInitializers);
        if (maxStringLength != null) list.add("-l" + maxStringLength);
        appendSingleWordOption(list, "lnc", lineNumbersAsComments);
        if (longRadix != null) list.add("-lradix" + longRadix);
        appendSingleWordOption(list, "nl", splitStringsAtNewline);
        appendSingleWordOption(list, "noconv", noconv);
        appendSingleWordOption(list, "nocast", nocast);
        appendSingleWordOption(list, "noclass", noclass);
        appendSingleWordOption(list, "nocode", nocode);
        appendSingleWordOption(list, "noctor", noctor);
        appendSingleWordOption(list, "nodos", nodos);
        if (!MACOS) appendSingleWordOption(list, "nofd", nofd);
        // noinner seems to work just the other way around (hs)
        appendSingleWordOption(list, "noinner", !noinner);
        appendSingleWordOption(list, "nolvt", nolvt);
        appendSingleWordOption(list, "nonlb", nonlb);
        appendSingleWordOption(list, "o", overwrite);
        appendSingleWordOption(list, "p", pipe);

        if (prefixPackages != null && prefixPackages.length()>0) list.add("-pa" + prefixPackages);
        if (prefixNumericalClasses != null && prefixNumericalClasses.length()>0) list.add("-pc" + prefixNumericalClasses);
        if (prefixUnusedExceptions != null && prefixUnusedExceptions.length()>0) list.add("-pe" + prefixUnusedExceptions);
        if (prefixNumericalFields != null && prefixNumericalFields.length()>0) list.add("-pf" + prefixNumericalFields);
        if (prefixNumericalLocals != null && prefixNumericalLocals.length()>0) list.add("-pl" + prefixNumericalLocals);
        if (prefixNumericalMethods != null && prefixNumericalMethods.length()>0) list.add("-pm" + prefixNumericalMethods);
        if (prefixNumericalParameters != null && prefixNumericalParameters.length()>0) list.add("-pp" + prefixNumericalParameters);

        if (packFields != null && packFields.intValue()>1) list.add("-pv" + packFields);
        if (packImports != null) list.add("-pi" + packImports);

        appendSingleWordOption(list, "r", restorePackages);
        if (intRadix != null) list.add("-radix" + intRadix);
        if (fileExtension != null) list.add("-s" + fileExtension);
        appendSingleWordOption(list, "safe", safe);
        appendSingleWordOption(list, "space", spaceAfterKeyword);
        appendSingleWordOption(list, "stat", STATISTICS);
        if (indentation != null && !useTabs) list.add("-t" + indentation);
        appendSingleWordOption(list, "t", useTabs);
        appendSingleWordOption(list, "v", VERBOSE);
        return (String[])list.toArray(new String[list.size()]);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = Arrays.asList(toStringArray()).iterator(); i.hasNext(); ) {
            sb.append(' ');
            sb.append(i.next());
        }
        return sb.toString();
    }

    public boolean isConfirmNavigationTriggeredDecompile() {
        return confirmNavigationTriggeredDecompile;
    }

    public void setConfirmNavigationTriggeredDecompile(boolean confirmNavigationTriggeredDecompile) {
        this.confirmNavigationTriggeredDecompile = confirmNavigationTriggeredDecompile;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isAnnotate() {
        return annotate;
    }

    public void setAnnotate(boolean annotate) {
        this.annotate = annotate;
    }

    public boolean isAnnotateFully() {
        return annotateFully;
    }

    public void setAnnotateFully(boolean annotateFully) {
        this.annotateFully = annotateFully;
    }

    public boolean isBraces() {
        return braces;
    }

    public void setBraces(boolean braces) {
        this.braces = braces;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isDissassemblerOnly() {
        return dissassemblerOnly;
    }

    public void setDissassemblerOnly(boolean dissassemblerOnly) {
        this.dissassemblerOnly = dissassemblerOnly;
    }

    public boolean isFullyQualifiedNames() {
        return fullyQualifiedNames;
    }

    public void setFullyQualifiedNames(boolean fullyQualifiedNames) {
        this.fullyQualifiedNames = fullyQualifiedNames;
    }

    public boolean isFieldsFirst() {
        return fieldsFirst;
    }

    public void setFieldsFirst(boolean fieldsFirst) {
        this.fieldsFirst = fieldsFirst;
    }

    public boolean isDefaultInitializers() {
        return defaultInitializers;
    }

    public void setDefaultInitializers(boolean defaultInitializers) {
        this.defaultInitializers = defaultInitializers;
    }

    public Integer getMaxStringLength() {
        return maxStringLength;
    }

    public void setMaxStringLength(Integer maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    public boolean isLineNumbersAsComments() {
        return lineNumbersAsComments;
    }

    public void setLineNumbersAsComments(boolean lineNumbersAsComments) {
        this.lineNumbersAsComments = lineNumbersAsComments;
    }

    public Integer getLongRadix() {
        return longRadix;
    }

    public void setLongRadix(Integer longRadix) {
        this.longRadix = longRadix;
    }

    public boolean isSplitStringsAtNewline() {
        return splitStringsAtNewline;
    }

    public void setSplitStringsAtNewline(boolean splitStringsAtNewline) {
        this.splitStringsAtNewline = splitStringsAtNewline;
    }

    public boolean isNoconv() {
        return noconv;
    }

    public void setNoconv(boolean noconv) {
        this.noconv = noconv;
    }

    public boolean isNocast() {
        return nocast;
    }

    public void setNocast(boolean nocast) {
        this.nocast = nocast;
    }

    public boolean isNoclass() {
        return noclass;
    }

    public void setNoclass(boolean noclass) {
        this.noclass = noclass;
    }

    public boolean isNocode() {
        return nocode;
    }

    public void setNocode(boolean nocode) {
        this.nocode = nocode;
    }

    public boolean isNoctor() {
        return noctor;
    }

    public void setNoctor(boolean noctor) {
        this.noctor = noctor;
    }

    public boolean isNodos() {
        return nodos;
    }

    public void setNodos(boolean nodos) {
        this.nodos = nodos;
    }

    public boolean isNofd() {
        return nofd;
    }

    public void setNofd(boolean nofd) {
        this.nofd = nofd;
    }

    public boolean isNoinner() {
        return noinner;
    }

    public void setNoinner(boolean noinner) {
        this.noinner = noinner;
    }

    public boolean isNolvt() {
        return nolvt;
    }

    public void setNolvt(boolean nolvt) {
        this.nolvt = nolvt;
    }

    public boolean isNonlb() {
        return nonlb;
    }

    public void setNonlb(boolean nonlb) {
        this.nonlb = nonlb;
    }

    public Integer getIntRadix() {
        return intRadix;
    }

    public void setIntRadix(Integer intRadix) {
        this.intRadix = intRadix;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        if (fileExtension == null) fileExtension = DEFAULT_FILE_EXTENSION;
        this.fileExtension = fileExtension;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public boolean isSpaceAfterKeyword() {
        return spaceAfterKeyword;
    }

    public void setSpaceAfterKeyword(boolean spaceAfterKeyword) {
        this.spaceAfterKeyword = spaceAfterKeyword;
    }

    public Integer getIndentation() {
        return indentation;
    }

    public void setIndentation(Integer indentation) {
        this.indentation = indentation;
    }

    public boolean isUseTabs() {
        return useTabs;
    }

    public void setUseTabs(boolean useTabs) {
        this.useTabs = useTabs;
    }

    public String getPrefixPackages() {
        return prefixPackages;
    }

    public void setPrefixPackages(String prefixPackages) {
        this.prefixPackages = prefixPackages;
    }

    public String getPrefixNumericalClasses() {
        return prefixNumericalClasses;
    }

    public void setPrefixNumericalClasses(String prefixNumericalClasses) {
        this.prefixNumericalClasses = prefixNumericalClasses;
    }

    public String getPrefixUnusedExceptions() {
        return prefixUnusedExceptions;
    }

    public void setPrefixUnusedExceptions(String prefixUnusedExceptions) {
        this.prefixUnusedExceptions = prefixUnusedExceptions;
    }

    public String getPrefixNumericalFields() {
        return prefixNumericalFields;
    }

    public void setPrefixNumericalFields(String prefixNumericalFields) {
        this.prefixNumericalFields = prefixNumericalFields;
    }

    public String getPrefixNumericalLocals() {
        return prefixNumericalLocals;
    }

    public void setPrefixNumericalLocals(String prefixNumericalLocals) {
        this.prefixNumericalLocals = prefixNumericalLocals;
    }

    public String getPrefixNumericalMethods() {
        return prefixNumericalMethods;
    }

    public void setPrefixNumericalMethods(String prefixNumericalMethods) {
        this.prefixNumericalMethods = prefixNumericalMethods;
    }

    public String getPrefixNumericalParameters() {
        return prefixNumericalParameters;
    }

    public void setPrefixNumericalParameters(String prefixNumericalParameters) {
        this.prefixNumericalParameters = prefixNumericalParameters;
    }

    public Integer getPackFields() {
        return packFields;
    }

    public void setPackFields(Integer packFields) {
        this.packFields = packFields;
    }

    public Integer getPackImports() {
        return packImports;
    }

    public void setPackImports(Integer packImports) {
        this.packImports = packImports;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public boolean equals(Object obj) {
        if ((!(obj instanceof JadOptions)) || obj == null) return false;
        JadOptions that = (JadOptions) obj;
        return this.annotate == that.annotate
                && this.annotateFully == that.annotateFully
                && this.braces == that.braces
                && this.clear == that.clear
                && this.dead == that.dead
                && this.defaultInitializers == that.defaultInitializers
                && this.dissassemblerOnly == that.dissassemblerOnly
                && this.fieldsFirst == that.fieldsFirst
                && this.fileExtension.equals(that.fileExtension)
                && this.fullyQualifiedNames == that.fullyQualifiedNames
                && this.indentation.equals(that.indentation)
                && this.intRadix.equals(that.intRadix)
                && this.lineNumbersAsComments == that.lineNumbersAsComments
                && this.longRadix.equals(that.longRadix)
                && this.maxStringLength.equals(that.maxStringLength)
                && this.nocast == that.nocast
                && this.noclass == that.noclass
                && this.nocode == that.nocode
                && this.noconv == that.noconv
                && this.noctor == that.noctor
                && this.nodos == that.nodos
                && this.nofd == that.nofd
                && this.noinner == that.noinner
                && this.nolvt == that.nolvt
                && this.nonlb == that.nonlb
                && this.outputDirectory.equals(that.outputDirectory)
                && this.overwrite == that.overwrite
                && this.pipe == that.pipe
                && this.restorePackages == that.restorePackages
                && this.safe == that.safe
                && this.spaceAfterKeyword == that.spaceAfterKeyword
                && this.splitStringsAtNewline == that.splitStringsAtNewline
                && this.STATISTICS == that.STATISTICS
                && this.useTabs == that.useTabs
                && this.VERBOSE == that.VERBOSE
                && this.readonly == that.readonly
                && this.confirmNavigationTriggeredDecompile == that.confirmNavigationTriggeredDecompile
                && equals(this.prefixPackages, that.prefixPackages)
                && equals(this.prefixNumericalClasses, that.prefixNumericalClasses)
                && equals(this.prefixUnusedExceptions, that.prefixUnusedExceptions)
                && equals(this.prefixNumericalFields, that.prefixNumericalFields)
                && equals(this.prefixNumericalLocals, that.prefixNumericalLocals)
                && equals(this.prefixNumericalMethods, that.prefixNumericalMethods)
                && equals(this.prefixNumericalParameters, that.prefixNumericalParameters)
                && equals(this.packFields, that.packFields)
                && equals(this.packImports, that.packImports)
                && this.sort == that.sort;
    }

    private static boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void readExternal(Element element) throws InvalidDataException {
        try {
            for (Iterator p = element.getChildren().iterator(); p.hasNext();) {
                Element property = (Element) p.next();
                String name = property.getAttributeValue("name");
                String value = property.getAttributeValue("value");
                PropertyDescriptor pd = getPropertyDescriptor(name);
                Object o = getType(pd.getReadMethod().getReturnType()).getConstructor(new Class[]{String.class}).newInstance(new Object[]{value});
                pd.getWriteMethod().invoke(this, new Object[]{o});
            }
        } catch (Exception e) {
            throw new InvalidDataException(e.toString());
        }
    }

    // make sure we have non-primitive types - for now we only have boolean
    private static Class getType(Class klass) {
        if (!klass.isPrimitive()) return klass;
        return Boolean.class;
    }

    private PropertyDescriptor getPropertyDescriptor(String name) throws IntrospectionException {
        // TODO: move this
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(JadOptions.class, Object.class).getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            if (propertyDescriptors[i].getName().equals(name)) return propertyDescriptors[i];
        }
        return null;
    }

    public void writeExternal(Element element) throws WriteExternalException {
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(JadOptions.class, Object.class).getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                Element property = new Element("property");
                property.setAttribute("name", propertyDescriptors[i].getName());
                Object value = propertyDescriptors[i].getReadMethod().invoke(this, new Object[]{});
                if (value != null) {
                    property.setAttribute("value", value.toString());
                    element.addContent(property);
                }
            }
        } catch (Exception e) {
            Logger.getInstance("JadOptions").error("writeExternal(Element element)", e);
            throw new WriteExternalException(e.toString());
        }
    }
}
