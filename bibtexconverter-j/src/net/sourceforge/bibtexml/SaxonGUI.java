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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import de.mospace.swing.LookAndFeelMenu;
import de.mospace.swing.PathInput;
import de.mospace.swing.text.DocumentOutputStream;
import net.sourceforge.bibtexml.BibTeXConverter.*;
import net.sourceforge.bibtexml.util.XSLTUtils;
import org.xml.sax.SAXException;

/**
THIS CLASS IS NOT ACTIVELY MAINTAINED and may break at any time (if it not
already is broken).<p>
When I wrote BibTeXConverterController I realized it can be used as
a rather general saxon GUI. This is (an older version of)
BibTeXConverterController minus the BibTeX to BibXML functionality with slightly
different GUI labels plus some hacks to avoid that SaxonGUI and
BibTeXConverterController use the same preference nodes. For
this reason this class cannot be instantiated
other than by invoking its main method.
**/
public class SaxonGUI extends JFrame implements ActionListener{
    private static final Preferences PREF =
            Preferences.userNodeForPackage(SaxonGUI.class).node("saxongui");
    private static final ImageIcon logo = new ImageIcon((URL) BibTeXConverterController.class.getResource("icon/configure.png"));

    BibTeXConverter convert = new BibTeXConverter();

    private final static String INPUT_PREFIX = InputType.class.getName()+":";
    final static String ENCODING_PREFIX = Charset.class.getName()+":";
    private final static String START_CONVERSION = "Start conversion";

    final static Charset DEFAULT_ENC = BibTeXConverter.DEFAULT_ENC;

    final static Object[] allEncodings = Charset.availableCharsets().keySet().toArray();

    private PathInput inputFile;
    private PathInput outputDir;
    private Collection<StyleSheetController> styles;
    private final Container styleContainer = Box.createVerticalBox();
    protected File styledir;

    private SaxonGUI() throws SAXException, IOException{
        super("Saxon GUI");
        Object tf = XSLTUtils.getInstance().tryToGetTransformerFactory();
        if(tf == null){
            tf = XSLTUtils.getInstance().loadTransformerFactory(this);
        }
        init(tf != null);
        String styledirpath = PREF.get("styledir", null);
        styledir = (styledirpath == null)? null : new File(styledirpath);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        if(tf == null){
            System.err.println("Saxon not found!");
        } else {
            /* load styles */
                try{
                    for(StyleSheetController style :
                            StyleSheetController.load(convert, PREF.node("styles"))){
                        addStyle(style);
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
        }
        pack();
    }

    private void init(boolean hasSaxon){
        JPanel cp = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty= 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;

        cp.add(createInputPanel(), gbc);

        gbc.gridy = 1;

        cp.add(createOutputPanel(), gbc);

        gbc.gridy = 2;
        JButton button = new JButton(START_CONVERSION);
        button.setActionCommand(START_CONVERSION);
        button.addActionListener(this);
        cp.add(button, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1;
        JEditorPane console = new JTextPane();

        JScrollPane x = new JScrollPane(console);
        x.setPreferredSize(new Dimension(200,200));
        cp.add(x, gbc);
        DocumentOutputStream output = new DocumentOutputStream(console.getDocument());
        System.setOut(new PrintStream(output));
        output = new DocumentOutputStream(console.getDocument());
        output.setColor(Color.red);
        System.setErr(new PrintStream(output));

        setContentPane(cp);
        JMenuBar mb = new JMenuBar();
        JMenu fm = new JMenu("File");
        if(hasSaxon){
            JMenuItem mi = new JMenuItem("Add output style");
            mi.setActionCommand("addXSLT");
            mi.addActionListener(this);
            fm.add(mi);

            mi = new JMenuItem("Remove output style");
            mi.setActionCommand("rmXSLT");
            mi.addActionListener(this);
            fm.add(mi);
        }
        JMenuItem exit = new JMenuItem("Exit");
        exit.setActionCommand("exit");
        exit.addActionListener(this);
        fm.add(exit);
        mb.add(fm);
        mb.add(new LookAndFeelMenu(PREF,this));

        setJMenuBar(mb);

        setIconImage(logo.getImage());

    }

    protected String[] getBuiltinStyleNames(){
        return new String[0];
    }

    private JComponent createInputPanel(){
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel input = new JPanel(new GridBagLayout());
        input.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(),"Input"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx= 0;
        gbc.gridheight = 1;
        gbc.insets = new Insets(2,2,2,2);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;

        /* Input type: BibXML or BibTeX */

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1;

        /* Input file */
        String prefval = PREF.get("InputFile", "");
        inputFile = new PathInput(prefval, JFileChooser.FILES_AND_DIRECTORIES);
        input.add(inputFile, gbc);

        return input;
    }

    private final static Insets tab =  new Insets(0,5,0,0);
    private final static Insets defInsets = new Insets(10,0,0,0);

    private void addRow(Container c, JLabel label, Component cp, GridBagConstraints gbc){
        if(label == null){
            gbc.gridy++;
            gbc.insets = defInsets;
            gbc.gridx = 0;
            c.add(cp, gbc);
        } else {
            label.setLabelFor(cp);
            gbc.gridy++;
            gbc.insets = tab;
            gbc.gridx = 0;
            c.add(label, gbc);
            gbc.gridx = 1;
            c.add(cp, gbc);
        }
    }

    private JComponent createOutputPanel(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel result = new JPanel(new GridBagLayout());
        result.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(),"Output"));
        String key, prefval;

        /* Output directory */
        key = "OutputDir";
        prefval = PREF.get(key, "");
        outputDir = new PathInput(prefval, JFileChooser.DIRECTORIES_ONLY);
        JLabel label = new JLabel("Output directory");
        label.setLabelFor(outputDir);
        result.add(label, gbc);
        gbc.gridx = 1;
        result.add(outputDir, gbc);

        gbc.fill = GridBagConstraints.VERTICAL;

        /* styles */
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        addRow(result, null, styleContainer, gbc);

        return result;
    }

    private void doConversion(){
        String path = inputFile.getPath();

        if(path.equals("")){
            return;
        }

        File inp = new File(path);
        if(!inp.exists()){
            convert.handleException("No input", new FileNotFoundException("Input file "+path+" does not exist."));
            return;
        }

        File dir = inp.isDirectory()
                ? inp
                : inp.getAbsoluteFile().getParentFile();
        PREF.put("InputFile", inp.getAbsolutePath());

        path = outputDir.getPath();
        if(!path.equals("")){
            File x = new File(path);
            if(x.isAbsolute()){
                dir = x;
            } else {
                dir = new File(dir, path);
            }
        }
        if (!dir.exists()){
            if(JOptionPane.showConfirmDialog(this,
                "Output directory " + dir +
                " does not exist.\n Do you want to create it?", "Create output dir?",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
                    dir.mkdirs();
            } else {
                System.out.println("FINISHED");
            }
            return;
        }
        PREF.put("OutputDir", dir.getPath());

        File[] inf = null;
        inf = inp.isDirectory()?
            inp.listFiles(
                new FileFilter(){
                    public boolean accept(File file){
                        return file.isFile();
                    }
                }
            )
            : new File[]{inp};

        int i = 0;
        FILELOOP: for(File inputf : inf){
            if(i++ != 0){
                System.out.println();
            }
            System.out.println("CONVERTING "+inputf.getPath());
            String basename = inputf.getName();
            int lastdot = basename.lastIndexOf(".");
            if(lastdot >= 0){
                basename = basename.substring(0, lastdot);
            }

            File xml = inputf;
            if(hasStyles()){
                for(StyleSheetController cssc : getStyles()){
                    if(cssc.isActive()){
                        try{
                            cssc.transform(xml, dir, basename);
                        } catch (Exception ex){
                            System.out.println(ex);
                            break FILELOOP;
                        }
                    }
                }
            }
        }
        System.out.println("FINISHED\n");
    }

    protected boolean hasStyles(){
        return (styles != null) && (styles.size() != 0);
    }

    protected StyleSheetController[] getStyles(){
        return (hasStyles())?
            styles.toArray(new StyleSheetController[styles.size()]):
            null;
    }

    public boolean addStyle(){
        JFileChooser jfc = new JFileChooser(styledir);
        jfc.setDialogTitle("Choose an XSLT stylesheet");
        jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        URL style = null;
        if(returnVal == JFileChooser.APPROVE_OPTION){
            try{
                style = jfc.getSelectedFile().toURI().toURL();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        File dir = jfc.getCurrentDirectory();
        if(dir != null){
            styledir = dir;
            PREF.put("styledir", styledir.getAbsolutePath());
        }
        if(style == null){
            return false;
        }
        String name = null;
        name = JOptionPane.showInputDialog(this, "Please enter a name for the new output format.");
        if(name == null){
            return false;
        } else {
            name = name.replaceAll("[\\./]", " ");
        }
        while(nameExists(name)){
            name = JOptionPane.showInputDialog(this,
            "This name is already in use, please enter another one.", name);
            if(name == null){
                return false;
            } else {
                name = name.replaceAll("[\\./]", " ");
            }
        }
        String suffix = null;
        suffix = JOptionPane.showInputDialog(this, "Please enter a filename suffix for the new output format.");
        if(suffix == null){
            return false;
        }
        while(suffixExists(suffix)){
            suffix = JOptionPane.showInputDialog(this,
            "This suffix is already in use, please enter another one.", suffix);
            if(suffix == null){
                return false;
            }
        }
        Box b = Box.createVerticalBox();
        JCheckBox enc = new JCheckBox("Enable custom output encoding?");
        b.add(enc);
        JCheckBox params = new JCheckBox("Enable custom stylesheet parameters?");
        b.add(params);
        JCheckBox crlf = new JCheckBox("Force windows line terminators (CRLF)?");
        b.add(crlf);
        returnVal = JOptionPane.showConfirmDialog(
            this,
            b,
            "XSLT options",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        StyleSheetController ssc = null;
        if(returnVal == JOptionPane.OK_OPTION){
            try{
                ssc = new StyleSheetController(
                        convert,
                        name,
                        suffix,
                        style,
                        params.isSelected(),
                        enc.isSelected(),
                        crlf.isSelected(),
                        PREF.node("styles"));
                addStyle(ssc);
            } catch (Exception ex){
                convert.handleException(null, ex);
            }
        }
        return ssc != null;
    }

    private boolean nameExists(String name){
        boolean result = false;
        if(hasStyles()){
            for(StyleSheetController ssc : getStyles()){
                if(ssc.getName().equals(name)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean suffixExists(String suffix){
        boolean result = false;
        if(hasStyles()){
            for(StyleSheetController ssc : getStyles()){
                if(ssc.getSuffix().equals(suffix)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    synchronized public boolean addStyle(StyleSheetController cssc){
        if(styles == null){
            styles = new HashSet<StyleSheetController>();
        }
        boolean result = styles.add(cssc);
        if(result){
            styleContainer.add(cssc.getUI());
        }
        return result;
    }

    boolean removeStyle(){
        if(hasStyles()){
            Collection<StyleSheetController> v =
                new Vector<StyleSheetController>();
            Collection<String> c = Arrays.asList(getBuiltinStyleNames());
            for(StyleSheetController style : getStyles()){
                if(!c.contains(style.getName())){
                    v.add(style);
                }
            }
            if(v.size() != 0){
                StyleSheetController result =
                    (StyleSheetController)
                    JOptionPane.showInputDialog(this,
                    "Pick output style to remove",
                    "Remove stylesheet",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    v.toArray(new StyleSheetController[v.size()]),
                    null);
                if((result != null) && removeStyle(result)){
                    try{
                        result.destroyPrefNode();
                    } catch (Exception ex){
                        convert.handleException(null, ex);
                    }
                    return true;
                }
                return false;
            }
        }
        JOptionPane.showMessageDialog(this,"There are currently no removable output styles!");
        return false;
    }

    synchronized public boolean removeStyle(StyleSheetController cssc){
        if(!hasStyles()){
            return false;
        }
        boolean result = styles.remove(cssc);
        if(result){
            styleContainer.remove(cssc.getUI());
            cssc.disposeUI();
        }
        return result;
    }

    private void handleButton(AbstractButton c){
        boolean selected = c.isSelected();
        String cmd = c.getActionCommand();

        if (cmd.equals(START_CONVERSION)){
            (new Thread(){
                public void run(){
                    doConversion();
                }
            }).start();

        } else if (cmd.equals("exit")){
            System.exit(0);

        } else if(cmd.equals("addXSLT")){
            if(addStyle()){
                pack();
            }

        } else if(cmd.equals("rmXSLT")){
            if(removeStyle()){
                pack();
            }

        }
    }

    private void handleComboBox(JComboBox c) throws Exception{
        String cmd = c.getActionCommand();
        Object item = c.getSelectedItem();
        String sitem = item.toString();

        if(cmd.equals(Parser.class.getName())){
            convert.setBibTeXParser((Parser) item);
            PREF.put(cmd, ((Parser) item).name());

        } else if(cmd.equals(ENCODING_PREFIX + InputType.class.getName())){
            convert.setBibTeXEncoding(Charset.forName(sitem));
            PREF.put(cmd, sitem);
            //System.out.println("Input encoding is " + sitem);

        } else if(cmd.startsWith(ENCODING_PREFIX)){
            convert.setXMLEncoding(Charset.forName(sitem));
            PREF.put(cmd, sitem);
            //System.out.println(outp.toString() + " encoding is " + sitem);
        }
    }

    public void actionPerformed(ActionEvent e){
        Object c = e.getSource();

        if(c instanceof AbstractButton){
            handleButton((AbstractButton) c);

        } else if(c instanceof JComboBox){
            JComboBox jcb = (JComboBox) c;
            try{
                handleComboBox(jcb);
            } catch (Exception ex){
                convert.handleException("Invalid selection", ex);
                if(jcb.getActionCommand().startsWith(ENCODING_PREFIX)){
                    jcb.setSelectedItem(DEFAULT_ENC.name());
                } else {
                    jcb.setSelectedIndex(0);
                }
            }
        }
    }

    /** The method that starts up the SaxonGUI Application. **/
    public static void main(String[] argv) throws Exception{
        LookAndFeelMenu.setLookAndFeel(PREF, null);
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                java.io.PrintStream syserr = System.err;
                try{
                SaxonGUI btcc =
                        new SaxonGUI();
                btcc.setVisible(true);
                } catch (Exception ex){
                    System.setErr(syserr);
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }
}