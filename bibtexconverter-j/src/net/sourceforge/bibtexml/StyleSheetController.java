package net.sourceforge.bibtexml;
/*
 * $Id$
 * (c) Moritz Ringler, 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFormattedTextField;
import javax.swing.SpringLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import de.mospace.swing.SpringUtilities;
import de.mospace.xml.XSLParamHandler;
import org.xml.sax.SAXException;
import net.sourceforge.bibtexml.util.GUIUtils;

public class StyleSheetController {
    static Preferences PREF =
            Preferences.userNodeForPackage(StyleSheetController.class).node("styles");
    private final Preferences pref;
    private final XMLConverter conv;
    private final String name;
    private final String ext;
    private final URL style;
    private final boolean crlf;

    private final static String P_NODE_PARAM = "param";
    private final static String P_KEY_STYLE = "stylesheet";
    private final static String P_KEY_NEWLINE = "newline";
    private final static String P_KEY_SUFFIX = "suffix";
    private final static String P_KEY_ENCODING = "encoding";
    private final static String P_KEY_ACTIVE = "active";

    private final JPanel panel = new JPanel(new BorderLayout());
    private final Container custom = new JPanel(new SpringLayout());
    private JButton expcoll = null;
    private JDialog dialog;
    private boolean active = true;
    static final ImageIcon config = new ImageIcon(StyleSheetController.class.getResource("icon/configure.png"));

    private String enc;
    protected Map<String, Object> params = null;
    private Transformer t;
    private final static String PARAM_PREFIX = "{http://www.w3.org/1999/XSL/Transform}param=";

    private final ActionListener updater = new ActionListener(){
        public void actionPerformed(final ActionEvent e){
            final String command = e.getActionCommand();
            final Object source = e.getSource();
            if(command.startsWith(PARAM_PREFIX)){
                final String param = command.substring(PARAM_PREFIX.length());
                Object val = null;
                if(source instanceof JFormattedTextField){
                    val = ((JFormattedTextField) source).getValue();
                } else if (source instanceof JCheckBox){
                    val = Boolean.valueOf(((JCheckBox) source).isSelected());
                } else if (source instanceof JTextField){
                    val = ((JTextField) source).getText();
                }
                if(val != null){
                    params.put(param, val);
                    pref.node(P_NODE_PARAM).put(param, val.toString());
                }
            } else if (command.equals(P_KEY_ENCODING)){
                final JComboBox cb = (JComboBox) source;
                final String cs = (String) cb.getSelectedItem();
                if(Charset.isSupported(cs)){
                    enc = cs;
                    pref.put(P_KEY_ENCODING,cs);
                } else {
                    cb.setSelectedItem(enc);
                }
            }
        }
    };

    private final FocusListener fireActionOnFocusLost = new FocusAdapter(){
        public void focusLost(final FocusEvent e){
            final JTextField tf = (JTextField) e.getSource();
            tf.postActionEvent();
        }
    };

    private StyleSheetController(XMLConverter conv, String name)
             throws SAXException, IOException
    {
        this.conv = conv;
        this.name = name;
        this.pref = PREF.node(name);
        this.ext = pref.get(P_KEY_SUFFIX, "." + name.toLowerCase().replaceAll("\\s",""));
        this.style = new URL(pref.get(P_KEY_STYLE, ""));
        this.crlf = pref.getBoolean(P_KEY_NEWLINE, false);
        boolean customParams = false;
        try{
            customParams = pref.nodeExists(P_NODE_PARAM);
        } catch (Exception ex){
            final IOException ex2 = new IOException(ex.getMessage());
            ex2.initCause(ex);
            ex2.setStackTrace(ex.getStackTrace());
            throw ex2;
        }
        init(customParams,  pref.get(P_KEY_ENCODING, null) != null);
    }

    public StyleSheetController(final XMLConverter conv,
            final String outputname,
            final String outputSuffix,
            final URL stylesheet,
            final boolean customParams,
            final boolean customEncoding,
            final boolean newlineConversion)
            throws SAXException, IOException
    {
        this.conv = conv;
        this.name = outputname;
        this.ext = outputSuffix;
        this.style = stylesheet;
        this.crlf = newlineConversion;
        pref = PREF.node(name);
        pref.put(P_KEY_SUFFIX, ext);
        pref.put(P_KEY_STYLE, style.toString());
        pref.putBoolean(P_KEY_NEWLINE, crlf);
        init(customParams, customEncoding);
    }

    /** Doesn't throw any exceptions, prints errors to stderr. **/
    public static StyleSheetController newInstance(final XMLConverter conv,
            final String outputname,
            final String outputSuffix,
            final URL stylesheet,
            final boolean customParams,
            final boolean customEncoding,
            final boolean newlineConversion){
        StyleSheetController ssc = null;
        try{
            ssc = new StyleSheetController(conv, outputname, outputSuffix,
                   stylesheet, customParams, customEncoding, newlineConversion);
        } catch (SAXException ex){
            System.err.println("Error compiling stylesheet for " + outputname);
            System.err.println((ex.getCause() == null)? ex : ex.getCause());
        } catch (IOException ex){
            System.err.println("Error loading stylesheet from " + stylesheet);
            ex.printStackTrace();
        }
        return ssc;
    }

    private synchronized void setConfigVisible(final boolean b){
        if(dialog != null && dialog.isVisible()){
            if(b){
                custom.requestFocus();
            } else {
                dialog.setVisible(false);
            }
        } else if (b){
            final Window w = SwingUtilities.windowForComponent(expcoll);
            if(dialog == null){
                dialog = (w instanceof Dialog)
                ? new JDialog((Dialog) w, name, false)
                : new JDialog((Frame)
                ((w instanceof Frame)? w : null), name, false);
                dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
                dialog.setContentPane(custom);
                dialog.pack();
            }
            GUIUtils.getInstance().placeWindow(dialog, w);
            dialog.setVisible(true);
        }
    }



    public String toString(){
        return getName();
    }

    public String getName(){
        return name;
    }

    public String getSuffix(){
        return ext;
    }

    private void init(boolean customParams, boolean customEncoding) throws SAXException, IOException{
        InputStream in = null;
        try{
            in = new BufferedInputStream(style.openStream());
            t = conv.loadStyleSheet(in, style);
            if(t == null){
                throw new IOException("Cannot compile style sheet.");
            }
            if(customParams){
                in.close();
                in = new BufferedInputStream(style.openStream());
                params = XSLParamHandler.getStyleSheetParameters(in);
                for(String key : params.keySet().toArray(new String[0])){
                    if(key.startsWith(XMLConverter.INTERNAL_PARAMETER_PREFIX)){
                        params.remove(key);
                    }
                }
                customParams = (!params.isEmpty());
            }
        } catch (TransformerConfigurationException ex){
            throw new SAXException(ex);
        } finally {
            if (in != null){
                in.close();
            }
        }
        final boolean customizable = customParams || customEncoding;
        if(customizable){
            expcoll = new JButton(config);
            expcoll.setToolTipText("Edit Parameters...");
            expcoll.setBorderPainted(false);
            expcoll.setContentAreaFilled(false);
            expcoll.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    setConfigVisible(true);
                }
            });
        }
        final JCheckBox typeCheckBox = new JCheckBox(name);
        typeCheckBox.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent e){
                active = ((JCheckBox) e.getSource()).isSelected();
                if(customizable){
                    expcoll.setEnabled(active);
                    if(!active){
                        setConfigVisible(false);
                    }
                }
                pref.putBoolean(P_KEY_ACTIVE, active);
            }
        });
        if(customizable){
            final Container p2 = Box.createHorizontalBox();
            p2.add(typeCheckBox);
            p2.add(Box.createHorizontalStrut(5));
            p2.add(expcoll);
            panel.add(p2, BorderLayout.CENTER);
        } else {
            panel.add(typeCheckBox, BorderLayout.CENTER);
        }

        /* encoding */
        JLabel label;
        int rowcount = 0;
        if(customEncoding){
            final String prefval = pref.get(P_KEY_ENCODING, XMLConverter.DEFAULT_ENC.name());
            final JComboBox outpEnc = new JComboBox(BibTeXConverterController.allEncodings);
            outpEnc.setSelectedItem(XMLConverter.DEFAULT_ENC.name());
            outpEnc.setEditable(true);
            if(Charset.isSupported(prefval)){
                outpEnc.setSelectedItem(prefval);
            }
            enc = (String) outpEnc.getSelectedItem();
            pref.put(P_KEY_ENCODING, enc);
            outpEnc.setActionCommand(P_KEY_ENCODING);
            outpEnc.addActionListener(updater);
            outpEnc.getEditor().getEditorComponent().addFocusListener(new FocusAdapter(){
                    public void focusLost(FocusEvent e){
                        System.err.flush();
                        outpEnc.setSelectedItem(outpEnc.getEditor().getItem());
                    }
            });
            label = new JLabel("encoding");
            label.setLabelFor(outpEnc);
            custom.add(label);
            custom.add(outpEnc);
            rowcount ++;
        }
        if(customParams){
            final Preferences pref2 = pref.node(P_NODE_PARAM);
            String[] keys = null;
            try{
                keys = pref2.keys();
            } catch (Exception ex){
                ex.printStackTrace();
            }
            if(keys != null){
                Arrays.sort(keys);
            }
            for(String param : params.keySet()){
                Object o = params.get(param);
                if(keys != null && Arrays.binarySearch(keys, param) >= 0){
                    final Object o2 = getSavedParam(pref2, param, o);
                    if(
                        !(
                            o2 == o ||
                            (o != null && o.equals(o2))
                        )
                    ){
                        o = o2;
                        params.put(param, o);
                    }
                }
                label = new JLabel(param);
                custom.add(label);
                JTextField tf = null;
                if(o instanceof String){
                    tf = new JTextField((String) o);
                } else if (o instanceof Boolean){
                    final JCheckBox jcb = new JCheckBox((String) null,((Boolean) o).booleanValue());
                    jcb.setActionCommand(PARAM_PREFIX + param);
                    jcb.addActionListener(updater);
                    custom.add(jcb);
                } else {
                    final JFormattedTextField ftf = new JFormattedTextField(o);
                    GUIUtils.getInstance().installInputVerifier(ftf);
                    tf = ftf;
                }
                if(tf != null){
                    tf.setActionCommand(PARAM_PREFIX + param);
                    tf.addActionListener(updater);
                    tf.addFocusListener(fireActionOnFocusLost);
                    label.setLabelFor(tf);
                    custom.add(tf);
                }
                rowcount++;
            }
        }
        SpringUtilities.makeCompactGrid(custom,
                                        rowcount, 2, //rows, cols
                                        2, 2,        //initX, initY
                                        5, 2);       //xPad, yPad
        active = pref.getBoolean(P_KEY_ACTIVE, true);
        typeCheckBox.setSelected(active);
        if(expcoll != null){
            expcoll.setEnabled(active);
        }
    }

    private static Object getSavedParam(final Preferences p, final String key, final Object o){
        Object result = o;
        final Class c = o.getClass();
        if(c.equals(String.class)){
            result = p.get(key, (String) o);
        } else if(c.equals(Boolean.class)){
            result = Boolean.valueOf(p.getBoolean(key, ((Boolean) o).booleanValue()));
        } else if(c.equals(Long.class)){
            result = Long.valueOf(p.getLong(key, ((Long) o).longValue()));
        } else if(c.equals(Float.class)){
            result = Float.valueOf(p.getFloat(key, ((Float) o).floatValue()));
        } else if(c.equals(Double.class)){
            result = Double.valueOf(p.getDouble(key, ((Double) o).doubleValue()));
        }
        p.put(key, result.toString());
        return result;
    }

    public static StyleSheetController load(final XMLConverter cv, final String name)
    throws SAXException, IOException
    {
        return new StyleSheetController(cv, name);
    }

    public static StyleSheetController[] load(final XMLConverter cv, final String[] excludeNames)
            throws BackingStoreException{
        final Collection<StyleSheetController> result = new ArrayList<StyleSheetController>();
        StyleSheetController cssc;
        final boolean noexclude = (excludeNames == null);
        if(!noexclude){
            Arrays.sort(excludeNames);
        }
        for(String name : PREF.childrenNames()){
            if(noexclude || Arrays.binarySearch(excludeNames, name) < 0){
                try{
                    cssc = load(cv, name);
                } catch (Exception ex){
                    cv.handleException("Cannot load output format " + name, ex);
                    PREF.node(name).removeNode();
                    continue;
                }
                result.add(cssc);
            }
        }
        return result.toArray(new StyleSheetController[result.size()]);
    }

    public static StyleSheetController[] load(final XMLConverter cv)
            throws BackingStoreException{
        return load(cv, (String[]) null);
    }

    public void destroyPrefNode() throws BackingStoreException{
        pref.removeNode();
    }

    public boolean isActive(){
        return active;
    }

    public void transform(final File xml,
                           final File dir,
                           final String basename)
    throws TransformerException, IOException
    {
        final File xslout = new File(dir, basename + ext);
        System.out.printf("Creating %s in %s\n", name, xslout.toString());
        System.out.flush();
        transformImpl(xml, xslout);
    }

    protected void transformImpl(final File xml, final File xslout)
    throws TransformerException, IOException
    {
        conv.transform(t, xml, xslout, params, enc, crlf);
    }

    public void setErrorHandler(final ErrorListener handler){
        t.setErrorListener(handler);
    }

    public synchronized void dispose(){
        if(dialog != null){
            dialog.setVisible(false);
            dialog.dispose();
        }
        dialog = null;
    }

    public Component getUI(){
        return panel;
    }

    public boolean equals(final Object o){
        if(!(o instanceof StyleSheetController)){
            return false;
        }
        if(o == this){
            return true;
        }
        final StyleSheetController other =
            (StyleSheetController) o;
        return name.equals(other.name);
    }

    public int hashCode(){
        int result = 43;
        result = 37 * 43 + name.hashCode();
        return result;
    }
}