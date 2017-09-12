package com.tagtraum.ideajad;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectRootType;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version 2169.2,  $Id: OptionsPanel.java,v 1.8 2004/07/07 03:42:53 hendriks73 Exp $
 */
public class OptionsPanel extends JPanel {

    private JadOptions options;
    private Project project;
    private static HashMap properties;
    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.ideajad.localStrings");
    private static ResourceBundle propertiesInfo = ResourceBundle.getBundle("com.tagtraum.ideajad.propertiesInfo");
    private static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static Logger LOG = Logger.getInstance("OptionsPanel");

    static {
        properties = new HashMap();
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(JadOptions.class, Object.class).getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                properties.put(propertyDescriptors[i].getName(), propertyDescriptors[i]);
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public OptionsPanel(Project project, JadOptions options) {
        super();
        try {
            this.options = (JadOptions) options.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        this.project = project;
        setLayout(new GridBagLayout());
        //setBorder(LineBorder.createGrayLineBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Basic", createBasicJadOptionsPanel());
        tabbedPane.addTab("Advanced", createAdvancedJadOptionsPanel());
        add(tabbedPane, gbc);

        gbc.gridy = 1;
        gbc.weighty = 2;
        gbc.anchor = GridBagConstraints.SOUTHEAST;

        Container copyrightPanel = createCopyrightPanel();
        add(copyrightPanel, gbc);
    }

    private Container createCopyrightPanel() {
        Container copyrightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        copyrightPanel.add(new JLabel("IdeaJad 2169.2  © 2002-2004 tagtraum industries incorporated", JLabel.RIGHT));
        if (JadOptions.WINDOWS) copyrightPanel.add(new JLink("http://www.tagtraum.com/"));
        else copyrightPanel.add(new JLabel("http://www.tagtraum.com/", JLabel.RIGHT));
        return copyrightPanel;
    }

    private Container createBasicJadOptionsPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 0, 2);

        JPanel jadOptions = new JPanel(new GridBagLayout());
        //jadOptions.setBorder(new TitledBorder("Jad Options"));
        addGroup(jadOptions, "jad.boolean", gbc);
        addGroup(jadOptions, "format.jad.boolean", gbc);
        Component sizeComponent = jadOptions.getComponents()[1];
        Dimension p = sizeComponent.getPreferredSize();
        addGroup(jadOptions, "jad.integer", gbc);
        addGroup(jadOptions, "jad.string", gbc);
        addGroup(jadOptions, "plugin.boolean", gbc);

        IntegerControl indentation = (IntegerControl)getCustomControl("indentation", jadOptions);
        BooleanControl useTabs = (BooleanControl)getCustomControl("useTabs", jadOptions);
        useTabs.addItemListener(indentation);

        BooleanControl sort = (BooleanControl)getCustomControl("sort", jadOptions);
        BooleanControl lineNumbersAsComments = (BooleanControl)getCustomControl("lineNumbersAsComments", jadOptions);
        lineNumbersAsComments.addItemListener(sort);

        // make sure all have the same height.
        Component[] components = jadOptions.getComponents();
        for (int i = 0; i < components.length; i++) {
            JComponent component = (JComponent) components[i];
            component.setPreferredSize(new Dimension(component.getPreferredSize().width, p.height));
            component.setMinimumSize(new Dimension(component.getMinimumSize().width, p.height));
        }
        gbc.gridy = gbc.gridy + 1;
        gbc.weighty = 2;
        jadOptions.add(Box.createVerticalGlue(), gbc);
        gbc.gridx = 2;
        gbc.weightx = 1;
        jadOptions.add(Box.createHorizontalGlue(), gbc);
        return jadOptions;
    }

    private Container createAdvancedJadOptionsPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 2, 0, 2);

        JPanel jadOptions = new JPanel(new GridBagLayout());
        //jadOptions.setBorder(new TitledBorder("Advanced Options"));
        addGroup(jadOptions, "advanced.jad.boolean", gbc);
        Component sizeComponent = jadOptions.getComponents()[1];
        Dimension p = sizeComponent.getPreferredSize();
        addGroup(jadOptions, "advanced.jad.integer", gbc);
        addGroup(jadOptions, "advanced.jad.string", gbc);
        // make sure all have the same height.
        Component[] components = jadOptions.getComponents();
        for (int i = 0; i < components.length; i++) {
            JComponent component = (JComponent) components[i];
            component.setPreferredSize(new Dimension(component.getPreferredSize().width, p.height));
            component.setMinimumSize(new Dimension(component.getMinimumSize().width, p.height));
        }
        gbc.gridy = gbc.gridy + 1;
        gbc.weighty = 2;
        jadOptions.add(Box.createVerticalGlue(), gbc);
        gbc.gridx = 2;
        gbc.weightx = 1;
        jadOptions.add(Box.createHorizontalGlue(), gbc);
        return jadOptions;
    }

    private void addGroup(Container container, String groupName, GridBagConstraints gbc) {
        gbc.weighty = 0;
        if (gbc.gridx == 1) incConstraints(gbc);
        for (Iterator i = properties.values().iterator(); i.hasNext();) {
            PropertyDescriptor pd = (PropertyDescriptor) i.next();
            if (gbc.gridy == (properties.size() - 1) / 2) {
                gbc.weighty = 1;
            }
            String propertyGroup = propertiesInfo.getString(pd.getName() + ".group");

            if (pd.getPropertyType() == Boolean.TYPE && groupName.equals(propertyGroup)) {
                BooleanControl booleanControl = new BooleanControl(pd);
                container.add(booleanControl, gbc);
                if (JadOptions.MACOS && pd.getName().equals("nofd")) booleanControl.setEnabled(false);
                incConstraints(gbc);
            } else if (pd.getPropertyType() == Integer.class && groupName.equals(propertyGroup)) {
                IntegerControl ic = new IntegerControl(pd, getSpinnerModel(pd.getName()));
                container.add(ic, gbc);
                incConstraints(gbc);
            } else if (pd.getPropertyType() == String.class && groupName.equals(propertyGroup)) {
                if (pd.getName().equals("outputDirectory")) {
                    if (gbc.gridx == 1) incConstraints(gbc);
                    gbc.gridwidth = 2;
                    container.add(new OutputDirectoryControl(pd), gbc);
                    gbc.gridwidth = 1;
                } else {
                    container.add(new TextControl(pd, 6), gbc);
                }
                incConstraints(gbc);
            }
        }
    }

    private Component getCustomControl(String propertyName, Container container) {
        Component[] components = container.getComponents();
        Component comp = null;
        for (int j = 0; j < components.length; j++) {
            if (components[j] instanceof CustomControl && ((CustomControl) components[j]).getPropertyDescriptor().getName().equals(propertyName)) {
                comp = components[j];
                break;
            }
        }
        return comp;
    }

    private void incConstraints(GridBagConstraints gbc) {
        gbc.gridx = (gbc.gridx + 1) % 2;
        if (gbc.gridx == 0) {
            gbc.gridy = gbc.gridy + 1;
            gbc.weightx = 0;
        } else {
            //gbc.weightx = 1;
        }
    }

    private SpinnerModel getSpinnerModel(String name) {
        if (!name.endsWith("Radix")) {
            return new SpinnerNumberModel(getDefault(name), getMin(name), getMax(name), 1);
        }
        SpinnerModel model = new SpinnerListModel(new Object[]{new Integer(8), JadOptions.DEFAULT_INT_RADIX, new Integer(16)});
        model.setValue(JadOptions.DEFAULT_INT_RADIX);
        return model;
    }

    public boolean isModified(JadOptions options) {
        return !this.options.equals(options);
    }

    public void setJadOptions(JadOptions options) {
        try {
            this.options = (JadOptions) options.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Component[] components = this.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof CustomControl) ((CustomControl) components[i]).init();
        }

    }

    public JadOptions getJadOptions() {
        try {
            return (JadOptions) options.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getMin(String name) {
        if (name.equals("maxStringLength")) {
            return JadOptions.MIN_MAX_STRING_LENGTH;
        } else if (name.equals("indentation")) {
            return JadOptions.MIN_INDENTATION;
        } else if (name.equals("packFields")) {
            return 1;
        } else if (name.equals("packImports")) {
            return 1;
        }
        LOG.debug("No match getMin: " + name);
        return 0;
    }

    private int getMax(String name) {
        if (name.equals("maxStringLength")) {
            return JadOptions.MAX_MAX_STRING_LENGTH;
        } else if (name.equals("indentation")) {
            return JadOptions.MAX_INDENTATION;
        } else if (name.equals("packFields")) {
            return 256; // arbitrary value
        } else if (name.equals("packImports")) {
            return 256; // arbitrary value
        }
        LOG.debug("No match getMax: " + name);
        return 0;
    }

    private int getDefault(String name) {
        if (name.equals("maxStringLength")) {
            return JadOptions.DEFAULT_MAX_STRING_LENGTH.intValue();
        } else if (name.equals("indentation")) {
            return JadOptions.DEFAULT_INDENTATION.intValue();
        } else if (name.equals("packFields")) {
            return JadOptions.DEFAULT_PACK_FIELDS.intValue();
        } else if (name.equals("packImports")) {
            return JadOptions.DEFAULT_PACK_IMPORTS.intValue();
        }
        LOG.debug("No match getDefault: " + name);
        return 0;
    }

    private static interface CustomControl {
        public void init();
        public PropertyDescriptor getPropertyDescriptor();
    }

    private class BooleanControl extends JPanel implements CustomControl, ItemListener {

        private final PropertyDescriptor pd;
        private JCheckBox box;

        public void setEnabled(boolean enable) {
            super.setEnabled(enable);
            box.setEnabled(enable);
        }

        public BooleanControl(final PropertyDescriptor pd) {
            this.pd = pd;
            setLayout(new FlowLayout(FlowLayout.LEFT));
            //setBorder(new BevelBorder(BevelBorder.LOWERED));
            box = new JCheckBox(localStrings.getString("property." + pd.getName() + ".label"));
            box.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    try {
                        pd.getWriteMethod().invoke(options, new Object[]{new Boolean(e.getStateChange() == ItemEvent.SELECTED)});
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            );
            init();
            add(box);
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public PropertyDescriptor getPropertyDescriptor() {
            return pd;
        }

        public void itemStateChanged(ItemEvent e) {
            box.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        }

        public void init() {
            try {
                box.setSelected(((Boolean) pd.getReadMethod().invoke(options, EMPTY_OBJECT_ARRAY)).booleanValue());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public void addItemListener(ItemListener il) {
            box.addItemListener(il);
        }
    }

    private class IntegerControl extends JPanel implements CustomControl, ItemListener {

        private final PropertyDescriptor pd;
        private JSpinner spinner;

// --Recycle Bin START (11/27/02 8:48 PM):
//        public IntegerControl(PropertyDescriptor pd, int min, int max, int defaultValue) {
//            this(pd, new SpinnerNumberModel(defaultValue, min, max, 1));
//        }
// --Recycle Bin STOP (11/27/02 8:48 PM)

        public IntegerControl(final PropertyDescriptor pd, SpinnerModel model) {
            //setBorder(new BevelBorder(BevelBorder.LOWERED));
            this.pd = pd;
            spinner = new JSpinner(model);
            spinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    try {
                        pd.getWriteMethod().invoke(options, new Object[]{((JSpinner) e.getSource()).getValue()});
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            );
            init();
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            add(Box.createHorizontalStrut(5), gbc);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 1;
            add(new JLabel(localStrings.getString("property." + pd.getName() + ".label")), gbc);
            gbc.gridx = 2;
            add(Box.createHorizontalStrut(5), gbc);
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridx = 3;
            gbc.weightx = 1;
            add(spinner, gbc);
        }

        public void init() {
            try {
                Integer i = (Integer) pd.getReadMethod().invoke(options, EMPTY_OBJECT_ARRAY);
                if (i != null) spinner.setValue(i);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public PropertyDescriptor getPropertyDescriptor() {
            return pd;
        }

        public void itemStateChanged(ItemEvent e) {
            spinner.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
        }
    }

    private class TextControl extends JPanel implements CustomControl {

        private final PropertyDescriptor pd;
        private JTextField field;

        public TextControl(final PropertyDescriptor pd, int cols) {
            //setBorder(new BevelBorder(BevelBorder.LOWERED));
            this.pd = pd;
            field = new JTextField(cols);
            field.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                }
                public void focusLost(FocusEvent e) {
                    try {
                        pd.getWriteMethod().invoke(options, new Object[]{((JTextField) e.getSource()).getText()});
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            init();
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 0;
            add(Box.createHorizontalStrut(5), gbc);
            gbc.anchor = GridBagConstraints.WEST;
            add(new JLabel(localStrings.getString("property." + pd.getName() + ".label")), gbc);
            add(Box.createHorizontalStrut(5), gbc);
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill= GridBagConstraints.HORIZONTAL;
            gbc.weightx = 2;
            add(field, gbc);
        }

        public void init() {
            try {
                field.setText((String) pd.getReadMethod().invoke(options, EMPTY_OBJECT_ARRAY));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public PropertyDescriptor getPropertyDescriptor() {
            return pd;
        }

    }

    private class OutputDirectoryControl extends JPanel implements CustomControl, ModuleRootListener {

        private final PropertyDescriptor pd;
        private JComboBox box;

        public OutputDirectoryControl(final PropertyDescriptor pd) {
            //setBorder(new BevelBorder(BevelBorder.LOWERED));
            ProjectRootManager.getInstance(project).addModuleRootListener(this);
            this.pd = pd;
            box = new JComboBox(DecompileAction.getStringArray(DecompileAction.getDecompilationDirs(project)));
            box.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        pd.getWriteMethod().invoke(options, new Object[]{"\"" + ((JComboBox) e.getSource()).getSelectedItem() + "\""});
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            init();
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            add(Box.createHorizontalStrut(5), gbc);
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            add(new JLabel(localStrings.getString("property." + pd.getName() + ".label")), gbc);
            add(Box.createHorizontalStrut(5), gbc);
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill= GridBagConstraints.HORIZONTAL;
            gbc.weightx = 2;
            add(box, gbc);
        }

        public void init() {
            try {
                String dir = (String) pd.getReadMethod().invoke(options, EMPTY_OBJECT_ARRAY);
                String strippedDir;
                strippedDir = Util.stripQuotes(dir);
                for (int i = 0; i < box.getItemCount(); i++) {
                    String s = (String) box.getItemAt(i);
                    if (s.equals(strippedDir) || s.equals(dir)) {
                        box.setSelectedIndex(i);
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public PropertyDescriptor getPropertyDescriptor() {
            return pd;
        }

        public void beforeRootsChange(ModuleRootEvent event) {
            // do nothing
        }

        public void rootsChanged(ModuleRootEvent event) {
            VirtualFile[] files = ProjectRootManager.getInstance(project).getRootFiles(ProjectRootType.SOURCE);
            box.removeAllItems();
            String[] stringFiles = DecompileAction.getStringArray(files);
            for (int i = 0; i < stringFiles.length; i++) {
                box.addItem(stringFiles[i]);
            }
            init();
        }

    }

}
