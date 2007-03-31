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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import de.mospace.swing.SpringUtilities;

public class SchemaSelection{
    public final static String DATATYPES = "datatypes";
    public final static String FIELDS = "fields";
    public final static String STRUCTURE = "structure";

    private final static Font SANS_BOLD_12 = new Font("SansSerif", Font.BOLD, 12);
    private final static Color TITLE_COLOR = new Color(0.8f,0.8f,1.0f);
    
    private enum DataTypes { strict, lax };
    private enum Fields { core, user, arbitrary };
    private enum Structure { flat, container };
    
    private final ButtonGroup datatypes = new ButtonGroup();
    private final ButtonGroup fields = new ButtonGroup();
    private final ButtonGroup structure = new ButtonGroup();
    
    private Container cp;
    
    private boolean ok = false;
    private TreeMap<String, String> settings = new TreeMap<String, String>();
    
    private Transformer t;
    
    public SchemaSelection(){
        Preferences pref = Preferences.userNodeForPackage(getClass()).node("schema");
        settings.put(DATATYPES, pref.get(DATATYPES, DataTypes.strict.name()));
        settings.put(FIELDS, pref.get(FIELDS, Fields.user.name()));
        settings.put(STRUCTURE,  pref.get(STRUCTURE, Structure.flat.name()));
    }
    
    private Component makeTitle(String text){
        Container c = new JPanel(new BorderLayout()); 
        ((JComponent) c).setBorder(BorderFactory.createEtchedBorder());
        JLabel label;
        label = new JLabel(text, JLabel.LEFT);
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
        JLabel label;
        cp.add(makeTitle("BibTeX Fields"));
        
        c = new JPanel(new SpringLayout());
        ((JComponent) c).setBorder(BorderFactory.createEtchedBorder());
        ((JComponent) c).setOpaque(true);
        JRadioButton button;
        Border cellBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(0,4,0,4)
        );
        
        
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
        
        cp.add(c);
        
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
        
        c = Box.createHorizontalBox();
        ((JComponent) c).setOpaque(true);
        ((JComponent) c).setBorder(BorderFactory.createEtchedBorder());
        cp.add(makeTitle("Datatypes"));
        button = new JRadioButton("Strict");
        button.setActionCommand(DataTypes.strict.name());
        c.add(button);
        datatypes.add(button);
        c.add(Box.createHorizontalStrut(5));
        button = new JRadioButton("Lax");
        button.setActionCommand(DataTypes.lax.name());
        c.add(button);
        datatypes.add(button);
        c.add(Box.createHorizontalGlue());
        ((JComponent) c).setBackground(button.getBackground());
        cp.add(c);
        
        cp.add(Box.createVerticalStrut(5));
        
        c = Box.createHorizontalBox();
        ((JComponent) c).setBorder(BorderFactory.createEtchedBorder());
        ((JComponent) c).setOpaque(true);
        cp.add(makeTitle("Structure"));
        button = new JRadioButton("Flat");
        button.setActionCommand(Structure.flat.name());
        c.add(button);
        structure.add(button);
        c.add(Box.createHorizontalStrut(5));
        button = new JRadioButton("Nested");
        button.setActionCommand(Structure.container.name());
        c.add(button);
        structure.add(button);
        c.add(Box.createHorizontalGlue());
        ((JComponent) c).setBackground(button.getBackground());
        cp.add(c);
        
        cp.add(Box.createVerticalStrut(5));
        
        c  = new JPanel();
        ((JComponent) c).setOpaque(false);
        JButton buttonOK     = new JButton();
        JButton buttonCancel = new JButton();
        buttonOK.setText(UIManager.getString("OptionPane.okButtonText"));
        buttonCancel.setText(UIManager.getString("OptionPane.cancelButtonText"));
        buttonOK.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ok = true;
                Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
                w.setVisible(false);
                w.dispose();
            }
        });
        buttonCancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ok = false;
                Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
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
    
    public boolean showDialog(Component parent){
        Preferences pref = Preferences.userNodeForPackage(getClass()).node("schema");
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
    
    public Map<String, String> getSelection(){
        return (Map<String, String>) settings.clone();
    }
    
    private void select(ButtonGroup group, String actionCommand){
        Enumeration<AbstractButton> e = group.getElements();
        for(AbstractButton b; e.hasMoreElements(); ){
            b = e.nextElement();                    
            if(b.getActionCommand().equals(actionCommand)){
                b.setSelected(true);
                return;
            }
        }
    }
    
    public boolean isSchemaLanguageSupported(String schemaLanguage){
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
    public synchronized Source getSchemaSource(String schemaLanguage, 
                                               XMLConverter conv) 
            throws TransformerException, IOException{
        if(!isSchemaLanguageSupported(schemaLanguage)){
            throw new IllegalArgumentException("Currently only RELAX NG is supported.");
        }
        if(t == null){
            t = conv.loadStyleSheet(null, "schema/schema.xsl");
        }
        /* transform generic parent schema to specific child schema */ 
        URL parentSchema = getClass().getResource("schema/bibtexml-groups2.rng");
        if(parentSchema == null){
            throw new FileNotFoundException("schema/bibtexml-groups2.rng");
        }
        String systemID = parentSchema.toString();
        InputStream is = parentSchema.openStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Map<String, Object> params = new HashMap<String, Object>();
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
        StringBuilder sb = new StringBuilder(
            systemID.substring(0, systemID.lastIndexOf('/') + 1));
        sb.append("bibtexml");
        for(String key : settings.keySet()){
            sb.append('-').append(settings.get(key));
        }
        sb.append(".rng");
        systemID = sb.toString();
        
        /* wrap the new schema as a source object */
        ByteArrayInputStream schema = new ByteArrayInputStream(os.toByteArray());
        StreamSource source = new StreamSource(schema, systemID);
        return source;
    }
    
    public static void main(String[] argv) throws Exception{
        SchemaSelection s = new SchemaSelection();
        if(s.showDialog(null)){
            System.out.println(s.getSelection());
        }
    }
    
}
