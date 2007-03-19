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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import de.mospace.swing.LookAndFeelMenu;
import de.mospace.swing.PathInput;
import de.mospace.swing.text.DocumentOutputStream;
import net.sourceforge.bibtexml.BibTeXConverter.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

//ToDo:
//insert doctype declaration into xml output
//* Think about output encoding:
//  - Which encoding(s) can you use for RIS?

class BibTeXConverterController extends JFrame implements ActionListener{
    private static final Preferences PREF =
            Preferences.userNodeForPackage(BibTeXConverterController.class);
    private static final ImageIcon logo = new ImageIcon((URL) BibTeXConverterController.class.getResource("bibxml.png"));

    private Input input = Input.BIBTEX;
    BibTeXConverter convert = new BibTeXConverter();

    private final static String INPUT_PREFIX = Input.class.getName()+":";
    final static String ENCODING_PREFIX = Charset.class.getName()+":";
    final static String VALIDATION_PREFIX = "javax.xml.validation:";
    private final static String JABREF_ENC = "Look for JabRef encoding";
    private final static String START_CONVERSION = "Start conversion";
    private final static String RIS = "RIS (Reference Manager & Endnote)";
    private final static String HTMLFLAT = "HTML (flat)";
    private final static String HTMLGROUPED = "HTML (grouped)";
    private final static String BIBTEX = "BibTeX";
    private final static String[] BUILTIN = new String[]{
        BIBTEX, RIS, HTMLFLAT, HTMLGROUPED};
    private String schemaLanguageExtension;

    final static String DEFAULT_ENC = BibTeXConverter.DEFAULT_ENC;

    final static Object[] allEncodings = Charset.availableCharsets().keySet().toArray();

    private PathInput inputFile;
    private JButton startbutton;
    private PathInput outputDir;
    private Map<JToggleButton, Set<Component>> dependencies = new HashMap<JToggleButton, Set<Component>>();
    private Collection<StyleSheetController> styles;
    private JRadioButton bibTeXInput;
    private JComboBox encodings;
    private String groupingKey = "keywords";
    private Container styleContainer = Box.createVerticalBox();
    private String xmlschema = "bibtexmlExtended";
    protected File styledir;

    public BibTeXConverterController() throws SAXException, IOException{
        super("BibTeXConverter");
        Object tf = convert.tryToGetTransformerFactory(); 
        if(tf == null){
            tf = convert.loadTransformerFactory(this);
        }
        init(tf != null);
        String styledirpath = PREF.get("styledir", null);
        styledir = (styledirpath == null)? null : new File(styledirpath);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        if(tf == null){
            System.err.println("Saxon not found!");
            System.err.println("Only XML output is possible.");
        } else {
            /* load built-in styles */
            addStyle(
                new StyleSheetController(convert, BIBTEX, "-new.bib",
                        getClass().getResource("xslt/bibxml2bib.xsl"),
                        false, true, true));
            addStyle(
                new StyleSheetController(convert, RIS, ".ris",
                        getClass().getResource("xslt/bibxml2ris.xsl"),
                        false, false, true));
            addStyle(
                new StyleSheetController(convert, HTMLFLAT, ".html",
                        getClass().getResource("xslt/bibxml2html.xsl"),
                        true, true, false){
                    public boolean transformImpl(File a, File b){
                        return checkPdfDirURI(params) && super.transformImpl(a, b);
                    }
                });
            addStyle(
                new StyleSheetController(convert, HTMLGROUPED, "g.html",
                        getClass().getResource("xslt/bibxml2htmlg.xsl"),
                        true, true, false){
                    public boolean transformImpl(File a, File b){
                        return checkPdfDirURI(params) && super.transformImpl(a, b);
                    }
                });
                try{
                    for(StyleSheetController style : 
                            StyleSheetController.load(convert, BUILTIN)){
                        addStyle(style);
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
        }
        pack();
        System.err.flush();
        System.out.flush();
    }
    
    private void setValidationSchema(String cmd){
        String xmlschema2 = "";
        try{
            if(cmd.equals("None")){
                convert.setXMLSchema(null);
                xmlschema = "";
            } else {
                xmlschema = cmd;
                xmlschema2 = cmd + schemaLanguageExtension;
                URL schema = getClass().getResource("schema/"+xmlschema2);
                if(schema == null){
                    System.err.println("Warning: cannot load schema " + xmlschema2);
                    System.err.println("Resource not found.");
                }
                convert.setXMLSchema(schema);
            }
        } catch (SAXParseException ex){
            convert.handleException("Warning: error in schema " + xmlschema2 
            + "\n" + ex.getSystemId() + " line " + ex.getLineNumber(), ex);
        } catch (Exception ex){
            convert.handleException("Warning: cannot load schema " + xmlschema2, ex);
            if(ex instanceof NullPointerException){
                ex.printStackTrace();
            }
        } finally {
            System.out.flush();
            System.err.flush();
        }
    }
    
    private boolean checkPdfDirURI(Map<String, Object> params){
        Object baseURI = params.get("pdfDirURI");
        if(baseURI == null){
            return true;
        }
        String uri = (String) baseURI;
        try{
            new URI(uri);
        } catch (URISyntaxException ex){
            System.err.println("*** ERROR CREATING HTML OUTPUT ***");
            System.err.println("Malformed PDF directory URI");
            System.err.println(ex.getMessage());
            System.err.flush();
            return false;
        }
        if(!uri.endsWith("/")){
            params.put("pdfDirURI", uri + "/");
        }
        return true;
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
        startbutton = new JButton(START_CONVERSION);
        startbutton.setActionCommand(START_CONVERSION);
        startbutton.addActionListener(this);
        cp.add(startbutton, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1;
        JEditorPane console = new JTextPane();

        JScrollPane x = new JScrollPane(console);
        x.setPreferredSize(new Dimension(200,200));
        cp.add(x, gbc);
        DocumentOutputStream output = new DocumentOutputStream(console.getDocument());
        System.setOut(new PrintStream(new BufferedOutputStream(output), false));
        output = new DocumentOutputStream(console.getDocument());
        output.setColor(Color.red);
        System.setErr(new PrintStream(new BufferedOutputStream(output), false));

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
        
        fm = new JMenu("Options");
        mb.add(fm);
        fm.add(new LookAndFeelMenu(PREF,this));
        JMenu menu = new JMenu("BibXML Validation");
        fm.add(menu);
        
        JMenu menu2 = new JMenu("Schema Language");
        ButtonGroup group = new ButtonGroup();
        schemaLanguageExtension = PREF.get("Schema Language", ".rng");
        JRadioButtonMenuItem mi;
        
        Map<String, String> lang = new TreeMap<String, String>();
        lang.put("RELAX NG", ".rng");
        lang.put("W3C Schema", ".xsd");
        for(String name : lang.keySet()){
            mi = new JRadioButtonMenuItem(name.equals("RELAX NG")? 
                "<html>" + name + " <i>(recommended)</i></html>" :
                name);
            final String extension = lang.get(name);
            mi.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    String oldext = schemaLanguageExtension;
                    schemaLanguageExtension = extension;
                    PREF.put("Schema Language", extension);
                    if(xmlschema.length() > 0){
                        setValidationSchema(xmlschema);
                    }
                }
            });
            group.add(mi);
            menu2.add(mi);
            if(extension.equals(schemaLanguageExtension)){
                mi.setSelected(true);
            }
        }
        menu.add(menu2);        
        
        group = new ButtonGroup();
        String prefval = PREF.get(VALIDATION_PREFIX, "None");
        for(String item : 
        new String[]{"None", "bibtexmlFlat", "bibtexmlExtended", "bibtexml"}){
            mi = new JRadioButtonMenuItem(
                item.equals("bibtexmlFlat") ?
                "<html>" + item + " <i>(recommended)</i></html>" :
                item);
            mi.addActionListener(this);
            mi.setActionCommand(VALIDATION_PREFIX + item);
            menu.add(mi);
            group.add(mi);
            if(item.equals(prefval)){
                mi.setSelected(true);
                mi.doClick();
            }
        }

        menu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                (new About(BibTeXConverterController.this, 
                    logo,
                    convert.getSaxonVersion())).setVisible(true);
            }
        });
        menu.add(about);
        mb.add(menu);
        
        setJMenuBar(mb);

        setIconImage(logo.getImage());

        updateDependentComponents();
    }

    private void updateDependentComponents(){
        Set<JToggleButton> toggles = dependencies.keySet();
        Set<Component> comps;
        for(JToggleButton toggle : toggles){
            comps = dependencies.get(toggle);
            if(comps != null){
                boolean b = toggle.isSelected();
                for(Component comp : comps){
                    comp.setVisible(b);
                }
            }
        }
        pack();
    }
    
    protected String[] getBuiltinStyleNames(){
        return BUILTIN;
    }

    private JComponent createInputPanel(){
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel input = new JPanel(new GridBagLayout());
        input.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(),"Input"));
        JLabel label;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx= 0;
        gbc.gridheight = 1;
        gbc.insets = new Insets(2,2,2,2);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;

        /* Input type: BibXML or BibTeX */
        ButtonGroup bgroup = new ButtonGroup();
        JRadioButton button = new JRadioButton("BibXML");
        button.setActionCommand(INPUT_PREFIX + Input.BIBXML.name());
        button.addActionListener(this);
        bgroup.add(button);
        input.add(button, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;

        bibTeXInput = new JRadioButton("BibTeX");
        bibTeXInput.setActionCommand(INPUT_PREFIX + Input.BIBTEX.name());
        bibTeXInput.addActionListener(this);
        bgroup.add(bibTeXInput);
        Set<Component> bibtexComps = new HashSet<Component>();
        dependencies.put(bibTeXInput, bibtexComps);
        input.add(bibTeXInput, gbc);

        String prefval = PREF.get(INPUT_PREFIX, Input.BIBTEX.name());
        if(prefval.equals(Input.BIBXML.name())){
            button.doClick();
        } else {
            bibTeXInput.doClick();
        }

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1;

        /* Input file */
        prefval = PREF.get("InputFile", "");
        inputFile = new PathInput(prefval, JFileChooser.FILES_AND_DIRECTORIES);
        input.add(inputFile, gbc);

        /* BibTeX input encodings */
        String key = ENCODING_PREFIX + Input.class.getName();
        prefval = PREF.get(key, DEFAULT_ENC);
        encodings = new JComboBox(allEncodings);
        encodings.setActionCommand(key);
        encodings.setEditable(true);
        encodings.addActionListener(this);
        if(Charset.isSupported(prefval)){
            encodings.setSelectedItem(prefval);
        }
        label = new JLabel("BibTeX Encoding");
        label.setLabelFor(encodings);
        ImageIcon ic = new ImageIcon((URL) getClass().getResource("jabref.png"));
        JButton jb = new JButton(ic);
        jb.setToolTipText(JABREF_ENC);
        jb.setActionCommand(JABREF_ENC);
        jb.addActionListener(this);

        bibtexComps.add(label);
        bibtexComps.add(jb);
        bibtexComps.add(encodings);

        gbc.weightx = 0;
        gbc.gridy= 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        input.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        input.add(encodings, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        input.add(jb, gbc);

        
        /* BibTeX parser */
        /* //Disabled parser selection because texlipse parser is much superior
        key = Parser.class.getName();
        prefval = PREF.get(key, Parser.TEXLIPSE.name());
        JComboBox parser = new JComboBox(Parser.values());
        parser.addActionListener(this);
        parser.setActionCommand(key);
        try {
            parser.setSelectedItem(Enum.valueOf(Parser.class, prefval));
        } catch (Exception ignore){
            ignore.printStackTrace();
        }
        parser.setEditable(false);
        label = new JLabel("BibTeX Parser");
        label.setLabelFor(parser);
        bibtexComps.add(label);
        bibtexComps.add(parser);

        gbc.weightx = 0;
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        input.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        input.add(parser, gbc);
        */

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
        boolean bprefval;

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

        /* Section BibXML */
        JCheckBox typeCheckBox = new JCheckBox("BibXML");
        typeCheckBox.setSelected(true);
        typeCheckBox.setEnabled(false);
        Set<Component> bibTeXComps = dependencies.get(bibTeXInput);
        bibTeXComps.add(typeCheckBox);
        addRow(result, null, typeCheckBox, gbc);


        /* - BibXML encoding */
        key = ENCODING_PREFIX + "XML";
        prefval = PREF.get(key, DEFAULT_ENC);
        JComboBox outpEnc = new JComboBox(allEncodings);
        outpEnc.setEditable(true);
        outpEnc.setActionCommand(key);
        outpEnc.addActionListener(this);
        if(Charset.isSupported(prefval)){
            outpEnc.setSelectedItem(prefval);
        }
        label = new JLabel("XML Encoding");
        bibTeXComps.add(label);
        bibTeXComps.add(outpEnc);
        addRow(result, label, outpEnc, gbc);
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
            System.err.flush();
            return;
        }
        
        startbutton.setEnabled(false);

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
                System.out.flush();
            }
            startbutton.setEnabled(true);
            return;
        }
        PREF.put("OutputDir", dir.getPath());

        File[] inf = null;
        inf = inp.isDirectory()?
            inp.listFiles(
                new FileFilter(){
                    public boolean accept(File file){
                        return file.isFile() && file.getName().endsWith(input.extension());
                    }
                }
            )
            : new File[]{inp};

        boolean html = false;
        int i = 0;
        FILELOOP: for(File inputf : inf){
            if(i++ != 0){
                System.out.println();
            }
            System.out.println("CONVERTING "+inputf.getPath());
            System.out.flush();
            String basename = inputf.getName();
            int lastdot = basename.lastIndexOf(".");
            String extension = "";
            if(lastdot >= 0){
                extension = basename.substring(lastdot);
                basename = basename.substring(0, lastdot);
            }

            File xml = inputf;
            if(input == Input.BIBTEX){
                xml = new File(dir, basename + ".xml");
                try{
                    convert.bibTexToXml(inputf, xml);
                } catch (Exception ex){
                    convert.handleException("*** ERROR TRANSFORMING BIBTEX TO XML ***", ex);
                    continue;
                }
                /*
                try{
                    convert.createBibXmlDTD(dir);
                } catch (IOException ex){
                    convert.handleException("*** ERROR GENERATING BIBXML DTD ***", ex);
                    return;
                }
                */
            }
            
            /* xml validation */
            boolean isValid;
            if(convert.hasSchema()){
                try{
                    System.out.println("Validating "+ xml.getPath() + 
                        " using schema " + xmlschema + schemaLanguageExtension);
                    System.out.flush();
                    isValid = convert.validate(xml);
                    if(isValid){
                        System.out.println("Document is valid.");
                    } else {
                        System.err.println("Document is not valid.");
                    }
                } catch (SAXParseException ex){
                    System.err.println("*** FATAL ERROR VALIDATING BIBXML ***");
                    System.err.println(ex.getSystemId() + " line " + ex.getLineNumber());
                    System.err.println(ex.getLocalizedMessage());
                    continue;
                } catch (Exception ex){
                    convert.handleException("*** FATAL ERROR VALIDATING BIBXML ***", ex);
                    continue;
                }
            }
            System.out.flush();
            System.err.flush();
            
            /* xslt transformation */
            if(hasStyles()){
                for(StyleSheetController cssc : getStyles()){
                    if(cssc.isActive()){
                        if(cssc.transform(xml, dir, basename)){
                            if( cssc.getName().equals("HTML (flat)") ||
                                cssc.getName().equals("HTML (grouped)") )
                            {
                                html = true;
                            }
                        } else {
                            break FILELOOP;
                        }
                        System.out.flush();
                        System.err.flush();
                    } 
                }
            }
        }
        if(html){
            try{
                /* Creates CSS and JavaScript used by the HTML output. */
                convert.copyResourceToFile("xslt/default.css", dir);
                convert.copyResourceToFile("xslt/toggle.js", dir);
            }  catch (IOException ex){
                convert.handleException("Cannot generate javascript or css.", ex);
            }
        }
        System.out.println("FINISHED\n");
        System.out.flush();
        System.err.flush();
        startbutton.setEnabled(true);
    }

    private void jabrefEncoding(){
        String path = inputFile.getPath();
        if(path.equals("")){
            System.err.println("No input file specified.");
            return;
        }

        File inp = new File(path);
        if(inp.isDirectory()){
            convert.handleException("No input", new FileNotFoundException("Input path "+path+" denotes a directory."));
            return;
        }
        if(!inp.exists()){
            convert.handleException("No input", new FileNotFoundException("Input file "+path+" does not exist."));
            return;
        }

        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(inp));
            String line;
            Matcher m = Pattern.compile("(?i)Encoding:?\\s*([\\w_-]+)").matcher("");
            for(int i=0; i<5; i++){
                line = reader.readLine();
                if(line == null){
                    break;
                }
                if(m.reset(line).find()){
                    String match = m.group(1);
                    String charset = Charset.forName(match).name();
                    if(!charset.equals(match)){
                        match = match + " (" + charset + ")";
                    }
                    encodings.setSelectedItem(charset);
                    System.out.println("JabRef encoding found: " + match + "\n");
                    return;
                }
            }
            System.err.println("JabRef encoding not found.");
        } catch (IOException ex){
            convert.handleException(null, ex);
        } catch (UnsupportedCharsetException ex){
            convert.handleException("Charset not supported", ex);
        } catch (IllegalCharsetNameException ex){
            convert.handleException("Illegal charset name", ex);
        }
        finally {
            if(reader != null){
                try{
                    reader.close();
                } catch (IOException ignore){
                }
            }
            System.out.flush();
            System.err.flush();
        }

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
                        crlf.isSelected());
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
            cssc.dispose();
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
        } else if(cmd.equals(JABREF_ENC)){
            //read first 5 lines and look for ENCODING: xxx
            jabrefEncoding();

        } else if(cmd.startsWith(INPUT_PREFIX)){
            cmd = cmd.substring(INPUT_PREFIX.length());
            input = Enum.valueOf(Input.class, cmd);
            PREF.put(INPUT_PREFIX, cmd);

        } else if(cmd.startsWith(VALIDATION_PREFIX)){
            cmd = cmd.substring(VALIDATION_PREFIX.length());
            PREF.put(VALIDATION_PREFIX, cmd);
            setValidationSchema(cmd);
        }
        if(c instanceof JToggleButton){
            updateDependentComponents();
        }
    }

    private void handleComboBox(JComboBox c) throws Exception{
        String cmd = c.getActionCommand();
        Object item = c.getSelectedItem();
        String sitem = item.toString();

        if(cmd.equals(Parser.class.getName())){
            convert.setBibTeXParser((Parser) item);
            PREF.put(cmd, ((Parser) item).name());

        } else if(cmd.equals(ENCODING_PREFIX + Input.class.getName())){
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
                    jcb.setSelectedItem(DEFAULT_ENC);
                } else {
                    jcb.setSelectedIndex(0);
                }
            }
        }
    }
}