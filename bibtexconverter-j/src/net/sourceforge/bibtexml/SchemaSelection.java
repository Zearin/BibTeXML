package net.sourceforge.bibtexml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import de.mospace.swing.SpringUtilities;

/** This class provides dynamic schema generation and schema selection for
 * BibTeXML validation.
*/
public class SchemaSelection{
    /** The name of the parameter for datatype validation. Admissible values
     are strict and loose. */
    public final static String DATATYPES = "datatypes";
    /** The name of the parameter for allowed BibTeX fields. Admissible values
    are core, user, and arbitrary. */
    public final static String FIELDS = "fields";
    /** The name of the parameter for multiple element layout. Admissible values
    are flat, nested, and inline. */
    public final static String STRUCTURE = "structure";

    private final static Font SANS_BOLD_12 = new Font("SansSerif", Font.BOLD, 12);
    private final static Color TITLE_COLOR = new Color(0.8f,0.8f,1.0f);

    private enum DataTypes { strict, loose };
    private enum Fields { core, user, arbitrary };
    private enum Structure {
        flat("Multiple elements",
               "<html><pre>&lt;author&gt;Vidar B. Gundersen&lt;/author&gt;<br>" +
               "&lt;author&gt;Moritz Ringler&lt;/author&gt;</pre></html>"),
        nested("Multiple elements in container",
                "<html><pre>&lt;authors&gt;<br>" +
                "  &lt;author&gt;Vidar B. Gundersen&lt;/author&gt;<br>" +
                "  &lt;author&gt;<br>" +
                "    &lt;givennames&gt;Moritz&lt;/givennames&gt;<br>" +
                "    &lt;surname&gt;Ringler&lt;surname&gt;<br>" +
                "  &lt;/author&gt;<br>" +
                "&lt;authors&gt;</pre></html>"),
        inline("Single element",
                "<html><pre>&lt;author&gt;Vidar B. Gundersen AND" +
                " Moritz Ringler&lt;/author&gt;</pre></html>");

        private final String longname;
        private final String example;

        Structure(String longname, String example){
            this.longname = longname;
            this.example = example;
        }

        public String toString(){
            return longname;
        }

        public String example(){
            return example;
        }
    }

    private final ButtonGroup datatypes = new ButtonGroup();
    private final ButtonGroup fields = new ButtonGroup();
    private final ButtonGroup structure = new ButtonGroup();

    private Container cp;

    private boolean ok = false;
    private final TreeMap<String, String> settings = new TreeMap<String, String>();

    private Transformer t;

    /** Creates a new instance of this class and tries to initialize it
        from user preferences. */
    public SchemaSelection(){
        final Preferences pref = Preferences.userNodeForPackage(getClass()).node("schema");
        settings.put(DATATYPES, valueOf(
            DataTypes.class,
            pref.get(DATATYPES, DataTypes.strict.name()),
            DataTypes.strict).name());
        settings.put(FIELDS, valueOf(
            Fields.class,
            pref.get(FIELDS, Fields.user.name()),
            Fields.user).name());
        settings.put(STRUCTURE, valueOf(
            Structure.class,
            pref.get(STRUCTURE, Structure.flat.name()),
            Structure.flat).name());
    }

    private static <T extends Enum<T>> T valueOf(final Class<T> enumType,
                                            final String name,
                                            final T defaultVal){
        T result = defaultVal;
        try{
            result = Enum.valueOf(enumType, name);
        } catch (Exception ignore){
            //return defaultVal
        }
        return result;
    }

    private Component makeTitle(final String text){
        final Container c = new JPanel(new BorderLayout());
        ((JComponent) c).setBorder(BorderFactory.createEtchedBorder());
        final JLabel label = new JLabel(text, JLabel.LEFT);
        label.setOpaque(true);
        label.setBackground(TITLE_COLOR);
        label.setFont(SANS_BOLD_12);
        label.setForeground(Color.white);
        label.setBorder(BorderFactory.createMatteBorder(5,5,5,5,TITLE_COLOR));
        c.add(label, BorderLayout.CENTER);
        return c;
    }

    private synchronized Container contentPane(){
        if(cp == null){
        cp = Box.createVerticalBox();
        ((Box) cp).setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        Container c;
        JPanel section;
        JLabel label;

        JRadioButton button;
        final Border cellBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(0,4,0,4)
        );

        section = new JPanel(new BorderLayout());
        section.add(makeTitle("BibTeX Fields"), BorderLayout.NORTH);

        c = new JPanel(new SpringLayout());
        c.add(new JPanel());
        label = new JLabel("Required", JLabel.CENTER);
        label.setBorder(cellBorder);
        c.add(label);
        label = new JLabel("Optional", JLabel.CENTER);
        label.setBorder(cellBorder);
        c.add(label);
        label = new JLabel("User-Defined", JLabel.CENTER);
        label.setBorder(cellBorder);
        c.add(label);

        button = new JRadioButton("Core");
        button.setActionCommand(Fields.core.name());
        fields.add(button);
        c.add(button);
        label = new JLabel("!", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);
        label = new JLabel("?", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);
        label = new JLabel("-", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);

        button = new JRadioButton("User");
        button.setActionCommand(Fields.user.name());
        fields.add(button);
        c.add(button);
        label = new JLabel("!", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);
        label = new JLabel("?", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);
        label = new JLabel("?", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);

        button = new JRadioButton("Arbitrary");
        button.setActionCommand(Fields.arbitrary.name());
        fields.add(button);
        c.add(button);
        label = new JLabel("?", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);
        label = new JLabel("?", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);
        label = new JLabel("?", JLabel.CENTER);
        label.setFont(SANS_BOLD_12);
        c.add(label);

        SpringUtilities.makeCompactGrid(c,
        4, 4, //rows, cols
        0, 0,        //initX, initY
        0, 0);       //xPad, yPad

        Box b = Box.createHorizontalBox();
        b.add(c);
        b.add(Box.createHorizontalGlue());
        b.setBorder(BorderFactory.createEtchedBorder());
        b.setOpaque(true);
        section.add(b, BorderLayout.CENTER);
        cp.add(section);

        c = Box.createHorizontalBox();
        label = new JLabel("<html>"+
            "<b>!</b> : must be present<br>"+
            "<b>?</b> : can be present<br>"+
            "<b>-</b> : must not be present"+
            "</html>", JLabel.LEFT);
        Font f = label.getFont();
        f = f.deriveFont(Font.PLAIN, f.getSize() * 0.75f);
        label.setFont(f);
        c.add(label);
        c.add(Box.createHorizontalGlue());
        cp.add(c);

        cp.add(Box.createVerticalStrut(5));

        section = new JPanel(new BorderLayout());
        section.add(makeTitle("Type checking"), BorderLayout.NORTH);

        c = Box.createHorizontalBox();
        ((JComponent) c).setOpaque(false);
        ((JComponent) c).setBorder(BorderFactory.createEtchedBorder());
        for(DataTypes dt : DataTypes.values()){
            final String name = dt.name();
            button = new JRadioButton(upperFirst(name));
            button.setActionCommand(name);
            c.add(button);
            datatypes.add(button);
            c.add(Box.createHorizontalStrut(5));
        }
        c.add(Box.createHorizontalGlue());
        section.add(c, BorderLayout.CENTER);
        cp.add(section);

        cp.add(Box.createVerticalStrut(5));

        section = new JPanel(new BorderLayout());
        c = Box.createVerticalBox();
        ((JComponent) c).setOpaque(false);
        section.add(makeTitle("Author and editor lists"), BorderLayout.NORTH);

        for(Structure struct : Structure.values()){
            final String name = struct.name();
            button = new JRadioButton(upperFirst(struct.toString()));
            button.setToolTipText(struct.example());
            button.setActionCommand(name);
            c.add(button);
            structure.add(button);
        }
        c.add(Box.createVerticalGlue());
        b = Box.createHorizontalBox();
        b.add(c);
        b.setBorder(BorderFactory.createEtchedBorder());
        section.add(b, BorderLayout.CENTER);
        cp.add(section);

        cp.add(Box.createVerticalStrut(5));

        c  = new JPanel();
        ((JComponent) c).setOpaque(false);
        final JButton buttonOK     = new JButton();
        final JButton buttonCancel = new JButton();
        buttonOK.setText(UIManager.getString("OptionPane.okButtonText"));
        buttonCancel.setText(UIManager.getString("OptionPane.cancelButtonText"));
        buttonOK.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                ok = true;
                final Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
                w.setVisible(false);
                w.dispose();
            }
        });
        buttonCancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ok = false;
                final Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
                w.setVisible(false);
                w.dispose();
            }
        });
        c.add(buttonCancel);
        c.add(buttonOK);
        cp.add(c);
        }
        return cp;
    }

    private static String upperFirst(final String str){
        return (new StringBuilder()).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).toString();
    }

    /** Creates and shows a modal dialog that allows to choose a BibTeXML schema. This method
      should be called from the AWT event thread. */
    public boolean showDialog(final Component parent){
        final Preferences pref = Preferences.userNodeForPackage(getClass()).node("schema");
        JDialog dialog;
        Window w = null;
        if(parent instanceof Window){
            w = (Window) parent;
        } else if (parent != null) {
            w = SwingUtilities.getWindowAncestor(parent);
        }
        if(w instanceof Frame){
            dialog = new JDialog((Frame) w, "Validation options");
        } else if (w instanceof Dialog){
            dialog = new JDialog((Dialog) w, "Validation options");
        } else {
            dialog = new JDialog((Frame) null, "Validation options");
        }
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(contentPane());
        dialog.pack();
        dialog.setResizable(false);

        select(datatypes, settings.get(DATATYPES));
        select(fields, settings.get(FIELDS));
        select(structure, settings.get(STRUCTURE));

        ok = false;
        dialog.setVisible(true);
        if(ok){
            ButtonModel model;
            model = fields.getSelection();
            String selected;
            if(model != null){
                selected = model.getActionCommand();
                settings.put(FIELDS, selected);
                pref.put(FIELDS, selected);
            }
            model = datatypes.getSelection();
            if(model != null){
                selected = model.getActionCommand();
                settings.put(DATATYPES, selected);
                pref.put(DATATYPES, selected);
            }
            model = structure.getSelection();
            if(model != null){
                selected = model.getActionCommand();
                settings.put(STRUCTURE, selected);
                pref.put(STRUCTURE, selected);
            }
        }
        return ok;
    }

    /** Returns a copy of the current schema configuration. */
    public Map<String, String> getSelection(){
        final Map<String, String> result = new HashMap<String, String>();
        result.putAll(settings);
        return result;
    }

    /** Allows to programmatically set the schema configuration.
    * @throws IllegalArgumentException if parameter is not one of
    * DATATYPES, FIELDS, or STRUCTURE or choice is an illegal value*/
    public void select(String parameter, String choice){
        if(FIELDS.equals(parameter)){
            Fields field = Enum.valueOf(Fields.class, choice);
            settings.put(FIELDS, choice);
            select(fields, choice);
        } else if (DATATYPES.equals(parameter)){
            DataTypes type = Enum.valueOf(DataTypes.class, choice);
            settings.put(DATATYPES, choice);
            select(datatypes, choice);
        } else if (STRUCTURE.equals(parameter)){
            Structure struc = Enum.valueOf(Structure.class, choice);
            settings.put(STRUCTURE, choice);
            select(structure, choice);
        } else {
            throw new IllegalArgumentException("No such parameter " + parameter);
        }
        Preferences.userNodeForPackage(getClass()).node("schema").put(parameter, choice);
    }

    private void select(final ButtonGroup group, final String actionCommand){
        final Enumeration<AbstractButton> e = group.getElements();
        for(AbstractButton b; e.hasMoreElements(); ){
            b = e.nextElement();
            if(b.getActionCommand().equals(actionCommand)){
                b.setSelected(true);
                return;
            }
        }
    }

    /** Returns whether the specified schema language is supported by
     *  {@link #getSchemaSource}. */
    public boolean isSchemaLanguageSupported(final String schemaLanguage){
        return schemaLanguage.equals(XMLConstants.RELAXNG_NS_URI);
    }

    /** Uses the current selection to generate a Source that can be
     * compiled to a schema by a SchemaFactory. Currently only RELAX NG
     * is supported.
     * @param schemaLanguage the schema language URI for the new schema,
     * currently only XMLConstants.RELAXNG_NS_URI is accepted
     * @return a source that can be compiled to a schema
     * @throws IllegalArgumentException if the schemaLanguage is not supported
     * @throws TransformerException if an error occurs during schema generation
     * @throws IOException if an IOError occurs
     */
    public synchronized Source getSchemaSource(final String schemaLanguage,
                                               final XMLConverter conv)
            throws TransformerException, IOException{
        if(!isSchemaLanguageSupported(schemaLanguage)){
            throw new IllegalArgumentException("Currently only RELAX NG is supported.");
        }
        if(t == null){
            t = conv.loadStyleSheet(null, "schema/schema.xsl");
        }
        /* transform generic parent schema to specific child schema */
        final URL parentSchema = getClass().getResource("schema/bibtexml-generic.rng");
        if(parentSchema == null){
            throw new FileNotFoundException("schema/bibtexml-generic.rng");
        }
        String systemID = parentSchema.toString();
        final InputStream is = parentSchema.openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(settings);
        try{
            XMLConverter.transform(t, is, systemID, os, params,  null, false);
        } finally {
            if(is != null){
                is.close();
            }
            //bytearrayoutputstream doesn't need to be closed
        }

        /* build a systemID for the new schema */
        final StringBuilder sb = new StringBuilder(
            systemID.substring(0, systemID.lastIndexOf('/') + 1));
        sb.append("bibtexml");
        for(String key : settings.keySet()){
            sb.append('-').append(settings.get(key));
        }
        sb.append(".rng");
        systemID = sb.toString();

        /* wrap the new schema as a source object */
        final ByteArrayInputStream schema = new ByteArrayInputStream(os.toByteArray());
        return new StreamSource(schema, systemID);
    }

}
