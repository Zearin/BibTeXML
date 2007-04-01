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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.xml.transform.TransformerException;
import de.mospace.swing.LookAndFeelMenu;
import de.mospace.swing.PathInput;
import de.mospace.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class BibTeXConverterController extends JFrame implements ActionListener{
    private static final Preferences PREF =
            Preferences.userNodeForPackage(BibTeXConverterController.class);
    private static final ImageIcon logo = new ImageIcon((URL) BibTeXConverterController.class.getResource("icon/ledgreen2.png"));
    private final static String INPUT_PREFIX = InputType.class.getName()+":";
    final static String ENCODING_PREFIX = Charset.class.getName()+":";
    private final static String ENCODING_XML = ENCODING_PREFIX + "XML";
    private final static String JABREF_ENC = "Look for JabRef encoding";
    private final static String START_CONVERSION = "Start conversion";
    
    private final Container styleContainer = Box.createVerticalBox();    
    private final StyleSheetManager styleManager;
    private InputType input = InputType.BIBTEX;
    BibTeXConverter convert = new BibTeXConverter();

    private final SchemaSelection schemaSelection = new SchemaSelection();
    

    final static Object[] allEncodings = Charset.availableCharsets().keySet().toArray();

    private PathInput inputFile;
    private JButton startbutton;
    private PathInput outputDir;
    private Map<JToggleButton, Set<Component>> dependencies = new HashMap<JToggleButton, Set<Component>>();
    private JRadioButton bibTeXInput;
    private JComboBox encodings;
    private String groupingKey = "keywords";
    
    protected MessagePanel msgPane = new MessagePanel();
    private final ErrorCounter ecount = new ErrorCounter();
    private final UniversalErrorHandler errorHandler = new JointErrorHandler(ecount, msgPane.getErrorHandler());

    public BibTeXConverterController(){
        super("BibTeXConverter");
        Object tf = convert.tryToGetTransformerFactory(); 
        if(tf == null){
            tf = convert.loadTransformerFactory(this);
        }
        init(tf != null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        if(tf == null){
            System.err.println("Saxon not found!");
            System.err.println("Only XML output is possible.");
            styleManager = null;
        } else {
            styleManager = new StyleSheetManager(convert, styleContainer, builtInStyles(), errorHandler);
        }
        pack();
        
        convert.setValidationErrorHandler(errorHandler);
        convert.setBibTeXErrorHandler(errorHandler);
        try{
            convert.setXMLEncoding(Charset.forName(
                PREF.get(ENCODING_XML, BibTeXConverter.DEFAULT_ENC.name())));
        } catch (Exception ex){
            System.err.println("Error setting XML encoding.");
            System.err.println(ex);
        }
        
        System.err.flush();
        System.out.flush();
    }
    
    private Collection<StyleSheetController> builtInStyles(){
        List<StyleSheetController> builtins = new ArrayList<StyleSheetController>();
        builtins.add(
            StyleSheetController.newInstance(convert, "BibTeX", "-new.bib",
                    getClass().getResource("xslt/bibxml2bib.xsl"),
                    false, true, true));
        builtins.add(
            StyleSheetController.newInstance(convert, "RIS (Reference Manager & Endnote)", ".ris",
                    getClass().getResource("xslt/bibxml2ris.xsl"),
                    false, false, true));
        builtins.add(
            StyleSheetController.newInstance(convert, "HTML (flat)", ".html",
                    getClass().getResource("xslt/bibxml2html.xsl"),
                    true, true, false));
        builtins.add(
            StyleSheetController.newInstance(convert, "HTML (grouped)", "g.html",
                        getClass().getResource("xslt/bibxml2htmlg.xsl"),
                        true, true, false));
        return builtins;
    }
    
    public boolean addStyle(StyleSheetController cssc){
        return styleManager.addStyle(cssc);
    }
    
    public boolean removeStyle(StyleSheetController cssc){
        return styleManager.removeStyle(cssc);
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

        startbutton = new JButton(new ImageIcon(BibTeXConverterController.class.getResource("icon/ledgreen.png")));
        startbutton.setToolTipText(START_CONVERSION);
        startbutton.setActionCommand(START_CONVERSION);
        startbutton.setBorderPainted(false);
        startbutton.setContentAreaFilled(false);
        startbutton.addActionListener(this);

        cp.add(createOutputPanel(), gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        
        cp.add(msgPane, gbc);

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
        fm.add(new ValidationMenu(convert));
        
        JMenu menu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                (new About(BibTeXConverterController.this, 
                    new ImageIcon((URL) BibTeXConverterController.class.getResource("icon/ledgreen.png")),
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
        button.setActionCommand(INPUT_PREFIX + InputType.BIBXML.name());
        button.addActionListener(this);
        bgroup.add(button);
        input.add(button, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;

        bibTeXInput = new JRadioButton("BibTeX");
        bibTeXInput.setActionCommand(INPUT_PREFIX + InputType.BIBTEX.name());
        bibTeXInput.addActionListener(this);
        bgroup.add(bibTeXInput);
        Set<Component> bibtexComps = new HashSet<Component>();
        dependencies.put(bibTeXInput, bibtexComps);
        input.add(bibTeXInput, gbc);

        String prefval = PREF.get(INPUT_PREFIX, InputType.BIBTEX.name());
        if(prefval.equals(InputType.BIBXML.name())){
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
        String key = ENCODING_PREFIX + InputType.class.getName();
        prefval = PREF.get(key, BibTeXConverter.DEFAULT_ENC.name());
        encodings = new JComboBox(allEncodings);
        encodings.setActionCommand(key);
        encodings.setEditable(true);
        encodings.addActionListener(this);
        if(Charset.isSupported(prefval)){
            encodings.setSelectedItem(prefval);
        }
        label = new JLabel("BibTeX Encoding");
        label.setLabelFor(encodings);
        ImageIcon ic = new ImageIcon((URL) getClass().getResource("icon/jabref.png"));
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

        return input;
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
        label.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
        label.setLabelFor(outputDir);
        result.add(label, gbc);
        gbc.gridx = 1;
        result.add(outputDir, gbc);

        gbc.fill = GridBagConstraints.VERTICAL;

        /* Section BibXML */
        JCheckBox bibxmlCB = new JCheckBox("BibXML");
        bibxmlCB.setSelected(true);
        bibxmlCB.setEnabled(false);
        Set<Component> bibTeXComps = dependencies.get(bibTeXInput);

        JButton bconfig = new JButton(StyleSheetController.config);
        bconfig.setToolTipText("Configure...");
        bconfig.setBorderPainted(false);
        bconfig.setContentAreaFilled(false);
        bconfig.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    configureBibXML();
                    outputDir.requestFocusInWindow();
            }
        });
        Container p2 = Box.createHorizontalBox();
        p2.add(bibxmlCB);
        p2.add(Box.createHorizontalStrut(5));
        p2.add(bconfig);
        
        gbc.gridy++;
        gbc.insets = new Insets(10,0,0,0);
        gbc.gridx = 0;
        result.add(p2, gbc);
        
        bibTeXComps.add(p2);
        
        /* styles */
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(styleContainer, BorderLayout.CENTER);
        panel.add(startbutton, BorderLayout.EAST);
        
        gbc.gridy++;
        result.add(panel, gbc);
        
        return result;
    }
    
    private void configureBibXML(){
        /* - BibXML encoding */
        String prefVal = PREF.get(ENCODING_XML, 
                BibTeXConverter.DEFAULT_ENC.name());
        String result = (String) JOptionPane.showInputDialog(this,
                                     "Please select the encoding for generated BibXML.",
                                     "BibXML encoding",
                                     JOptionPane.QUESTION_MESSAGE,
                                     StyleSheetController.config,
                                     allEncodings,
                                     prefVal);
         if(result != null){
             try{
                 Charset cs = Charset.forName(result);
                 convert.setXMLEncoding(cs);
                 PREF.put(ENCODING_XML, cs.name());
             } catch (Exception ex){
                 System.err.println("Error setting BibXML encoding");
                 System.err.println(ex);
                 System.err.flush();
             }
         }
    }

    private void doConversion(){
        msgPane.showConsole();
        
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
        try{
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
                        " does not exist.\n Do you want to create it?",
                        "Create output dir?",
                        JOptionPane.OK_CANCEL_OPTION)
                    == JOptionPane.OK_OPTION
                ){
                    dir.mkdirs();
                } else {
                    System.out.println("ABORTED");
                    System.out.flush();
                    startbutton.setEnabled(true);
                    return;
                }
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
            int parseErrors  = 0;
            
            int i = 0;
            FILELOOP: for(File inputf : inf){
                if(i++ != 0){
                    System.out.println();
                }
                System.out.println("CONVERTING " + inputf.getPath());
                System.out.flush();
                String basename = inputf.getName();
                int lastdot = basename.lastIndexOf(".");
                String extension = "";
                if(lastdot >= 0){
                    extension = basename.substring(lastdot);
                    basename = basename.substring(0, lastdot);
                }
                
                File xml = inputf;
                
                Charset xmlencoding = null;
                if(input == InputType.BIBTEX){
                    /* bibtex to bibxml */
                    xmlencoding = convert.getXMLEncoding();
                    xml = new File(dir, basename + ".xml");
                    System.out.println("Creating XML in " + xml.getPath());
                    System.out.flush();
                    try{
                        errorHandler.reset();
                        convert.bibTexToXml(inputf, xml);
                        parseErrors  = ecount.getErrorCount();
                    } catch (IOException ex){
                        convert.handleException("*** ERROR TRANSFORMING BIBTEX TO XML ***", ex);
                        parseErrors = 0;
                        break FILELOOP;
                    }
                    if(parseErrors  > 0){
                        ErrorList el = msgPane.getErrorList(); 
                        el.setFile(new XFile(inputf, InputType.BIBTEX, convert.getBibTeXEncoding()));
                        el.setTitle("Errors parsing " + inputf.getName());
                        el.setAllowDoubleClick(true);
                        if(parseErrors != 1){
                            System.err.println(parseErrors  + " errors parsing " +  inputf.getName());
                        }
                        System.err.flush();
                        break FILELOOP;
                    }
                } else {
                    /* read xml encoding */
                    InputStream is = null;
                    try{
                        is = new FileInputStream(xml);
                        is = new BufferedInputStream(is); 
                        String enc = XMLUtils.getXMLDeclarationEncoding(new InputStreamReader(is, "utf-8"), "utf-8");
                        xmlencoding = Charset.forName(enc);
                        System.out.println("XML Encoding: " + xmlencoding.name());
                    } catch (IOException ex){
                        convert.handleException("*** ERROR READING XML ***", ex);
                        parseErrors = 0;
                        break FILELOOP;
                    } finally {
                        if(is != null){
                            try{
                                is.close();
                            } catch (Exception ignore){
                            }
                        }
                    }
                }
                System.err.flush();
                System.out.flush();
                
                /* xml validation */
                if(convert.hasSchema()){
                    String schemaID = convert.getXMLSchemaID();
                    if(schemaID == null){
                        schemaID = "";
                    } else {
                        schemaID = convert.getXMLSchemaID();
                        int lastslash = schemaID.lastIndexOf('/');
                        if(lastslash >= 0 && ++lastslash < schemaID.length()){
                            schemaID = schemaID.substring(lastslash);
                        }
                        schemaID = " against " + schemaID;
                    }
                    try{
                        System.out.println("Validating "+ xml.getPath() + schemaID);
                        System.out.flush();
                        errorHandler.reset();
                        convert.validate(xml);
                        parseErrors  = ecount.getErrorCount();
                    } catch (SAXParseException ex){
                        System.err.println("*** FATAL ERROR VALIDATING BIBXML ***");
                        try{
                            //add the error to the error list
                            //never mind if it has already been added
                            //the SortedSetListModel of the errorlist will take
                            //care of eliminating duplicates
                            msgPane.getErrorHandler().fatalError(ex); 
                        } catch (SAXException ignore){
                        }
                        parseErrors  = 1;
                    } catch (SAXException ex){
                        System.err.println("*** FATAL ERROR VALIDATING BIBXML ***");
                        System.err.println(ex.getMessage());
                        parseErrors = 1;
                    } catch (IOException ex){
                        convert.handleException("*** FATAL ERROR VALIDATING BIBXML ***", ex);
                        parseErrors  = 0;
                        break FILELOOP;
                    }
                    if(parseErrors  > 0){
                        ErrorList el = msgPane.getErrorList(); 
                        el.setFile(new XFile(xml, InputType.BIBXML, xmlencoding));
                        el.setTitle("Errors validating " + xml.getName() + schemaID);
                        el.setAllowDoubleClick(true);
                        if(parseErrors != 1){
                            System.err.println(parseErrors  + " errors validating " +  xml.getName());
                        }
                        System.err.println("Document is not valid.");
                        System.err.flush();
                        break FILELOOP;
                    } else {
                        System.out.println("Document is valid.");
                    }
                }
                System.err.flush();
                System.out.flush();
                
                /* xslt transformation */
                if(styleManager.hasStyles()){
                    for(StyleSheetController cssc : styleManager.getStyles()){
                        if(cssc.isActive()){
                            try{
                                errorHandler.reset();
                                cssc.transform(xml, dir, basename);
                                parseErrors = ecount.getErrorCount();
                                if( cssc.getName().equals("HTML (flat)") ||
                                    cssc.getName().equals("HTML (grouped)") )
                                {
                                    html = true;
                                }
                            } catch (TransformerException ex){
                                System.err.println("*** FATAL ERROR TRANSFORMING BIBXML ***");
                                try{
                                    //add the error to the error list
                                    //never mind if it has already been added
                                    //the SortedSetListModel of the errorlist will take
                                    //care of eliminating duplicates
                                    msgPane.getErrorHandler().fatalError(ex); 
                                } catch (TransformerException ignore){
                                }
                                parseErrors  = 1;
                            } catch (IOException ex){
                                convert.handleException("*** FATAL ERROR TRANSFORMING BIBXML ***", ex);
                                parseErrors  = 0;
                                break FILELOOP;
                            }
                            if(parseErrors  > 0){
                                ErrorList el = msgPane.getErrorList(); 
                                el.setFile(new XFile(xml, InputType.BIBXML, convert.getXMLEncoding()));
                                el.setTitle("Errors transforming " + xml.getName() + " to " + cssc.getName());
                                el.setAllowDoubleClick(true);
                                if(parseErrors != 1){
                                    System.err.println(parseErrors  + " errors transforming " +  xml.getName());
                                }
                                break FILELOOP;
                            }
                            System.err.flush();
                            System.out.flush();
                        } 
                    }
                }
            }
            System.err.flush();
            System.out.flush();
            if(html){
                try{
                    /* Creates CSS and JavaScript used by the HTML output. */
                    convert.copyResourceToFile("xslt/default.css", dir);
                    convert.copyResourceToFile("xslt/toggle.js", dir);
                }  catch (IOException ex){
                    convert.handleException("Cannot generate javascript or css.", ex);
                }
            }
            System.err.flush();
            System.out.println("FINISHED\n");
            System.out.flush();
            if(parseErrors  > 0){
                msgPane.showErrors();
            }
        } catch (RuntimeException ex){
            msgPane.showConsole();
            ex.printStackTrace();
            System.err.flush();
            throw ex;
        } finally {
            startbutton.setEnabled(true);
        }
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
            if(styleManager.addStyle()){
                pack();
            }
            
        } else if(cmd.equals("rmXSLT")){
            if(styleManager.removeStyle()){
                pack();
            }
        } else if(cmd.equals(JABREF_ENC)){
            //read first 5 lines and look for ENCODING: xxx
            jabrefEncoding();

        } else if(cmd.startsWith(INPUT_PREFIX)){
            cmd = cmd.substring(INPUT_PREFIX.length());
            input = Enum.valueOf(InputType.class, cmd);
            PREF.put(INPUT_PREFIX, cmd);

        }
        if(c instanceof JToggleButton){
            updateDependentComponents();
        }
    }
    
    private void handleComboBox(JComboBox c) throws Exception{
        String cmd = c.getActionCommand();
        Object item = c.getSelectedItem();
        String sitem = item.toString();

        if(cmd.equals(ENCODING_PREFIX + InputType.class.getName())){
            convert.setBibTeXEncoding(Charset.forName(sitem));
            PREF.put(cmd, sitem);
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
                    jcb.setSelectedItem(BibTeXConverter.DEFAULT_ENC.name());
                } else {
                    jcb.setSelectedIndex(0);
                }
            }
        }
    }
    
   
}