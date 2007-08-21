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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class ValidationMenu extends JMenu implements ActionListener{
    private static final Preferences PREF =
    Preferences.userNodeForPackage(BibTeXConverterController.class).node("schema");
    final static String VALIDATION_PREFIX = "javax.xml.validation:";
    private final static String SF = "javax.xml.validation.SchemaFactory";
    private final static String VALIDATION_DISABLED = VALIDATION_PREFIX + "disabled";
    private final static String VALIDATION_BUILTIN = VALIDATION_PREFIX + "builtin";
    private final static String VALIDATION_USER = VALIDATION_PREFIX + "user";

    private final SchemaSelection schemaSelection = new SchemaSelection();
    private final XMLConverter xmlconv;
    private final AbstractButton disabled = new JRadioButtonMenuItem("Disabled", true);
    private final AbstractButton userSchema = new JRadioButtonMenuItem("Custom schema...");
    private final AbstractButton builtin = new JRadioButtonMenuItem("Built-in schema...");
    private URL userSchemaURL;

    public ValidationMenu(XMLConverter converter){
        super("BibXML Validation");
        xmlconv = converter;
        init();
    }

    private void init(){
        JMenu menu = this;

        ButtonGroup schema = new ButtonGroup();
        menu.add(disabled);
        schema.add(disabled);
        disabled.addActionListener(this);

        menu.add(builtin);
        schema.add(builtin);
        builtin.addActionListener(this);

        menu.add(userSchema);
        schema.add(userSchema);
        userSchema.addActionListener(this);

        String prefval = PREF.get("userSchemaURL", null);
        if(prefval != null){
            try{
                userSchemaURL = new URL(prefval);
            } catch (Exception ex){
                System.err.println(ex);
                System.err.flush();
            }
        }

        prefval = PREF.get(VALIDATION_PREFIX, VALIDATION_DISABLED);
        if(prefval.equals(VALIDATION_BUILTIN)){
            builtin.setSelected(true);
            setValidationEnabled(true);
        } else if(prefval.equals(VALIDATION_USER) && userSchema != null){
            userSchema.setSelected(true);
            setValidationEnabled(true);
        } else {
            disabled.setSelected(true);
            setValidationEnabled(false);
        }

        List<JMenu> submenus = new Vector<JMenu>();
        menu = engineSelection("Relax NG", XMLConstants.RELAXNG_NS_URI);
        if(menu != null){
            submenus.add(menu);
        }
        menu = engineSelection("W3C Schema", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if(menu != null){
            submenus.add(menu);
        }
        switch (submenus.size()){
            case 0:
                break;
            case 1:
                menu = submenus.get(0);
                menu.setText("Engine");
                addSeparator();
                add(menu);
                break;
            default:
                menu = new JMenu("Engines");
                for(JMenu submenu : submenus){
                    menu.add(submenu);
                }
                addSeparator();
                add(menu);
        }

    }

    private JMenu engineSelection(String name, final String schemaLanguage){
        String prefVal = PREF.get(SF + ":" + schemaLanguage, null);
        List<JMenuItem> items = new Vector<JMenuItem>();
        ButtonGroup bg = new ButtonGroup();
        final ActionListener factorySwitcher = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                setPreferredEngine(schemaLanguage, e.getActionCommand());
            }
        };
        for(String provider : providers(schemaLanguage)){
            if("org.iso_relax.verifier.jaxp.validation.RELAXNGSchemaFactoryImpl".equals(provider)){
                /* Skip generic RELAXNGSchemaFactory */
                continue;
            }
            String displayName = provider;
            int lastdot = provider.lastIndexOf('.');
            if(lastdot > 0 && ++lastdot < provider.length()){
                displayName = provider.substring(lastdot);
            }
            displayName = displayName.replaceAll("SchemaFactoryImpl","");
            JMenuItem jmi = new JRadioButtonMenuItem(displayName);
            jmi.setActionCommand(provider);
            jmi.addActionListener(factorySwitcher);
            bg.add(jmi);
            items.add(jmi);
            if(provider.equals(prefVal)){
                jmi.doClick();
            }
        }

        JMenu result = null;
        if(items.size() > 1){
            result = new JMenu(name);
            for(JMenuItem item : items){
                result.add(item);
            }
            add(result);
        }
        return result;
    }


    // K.K: the following providers method is copied from Apache Batik project.

    /*****************************************************************************
     * Copyright (C) The Apache Software Foundation. All rights reserved.        *
     * ------------------------------------------------------------------------- *
     * This software is published under the terms of the Apache Software License *
     * version 1.1, a copy of which has been included with this distribution in  *
     * the LICENSE file.                                                         *
     *****************************************************************************/
    /*
     * version  Service.java,v 1.1 2001/04/27 19:55:44 deweese Exp
     */
    private final Map<String, List<String>> providerMap = new HashMap<String, List<String>>();
    private synchronized Iterable<String> providers(String schemaLanguage) {
        String serviceFile = "META-INF/services/"+SF;

        // System.out.println("File: " + serviceFile);

        List<String> v = providerMap.get(schemaLanguage);
        if (v != null){
            return v;
        }

        v = new Vector<String>();
        providerMap.put(schemaLanguage, v);

        ClassLoader cl = xmlconv.getClassLoader();
        Enumeration<URL> e;
        try {
            e = cl.getResources(serviceFile);
        } catch (IOException ioe) {
            return v;
        }

        while (e.hasMoreElements()) {
            try {
                URL u = e.nextElement();
//                System.out.println("URL: " + u);

                InputStream    is = u.openStream();
                Reader         r  = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();
                while (line != null) {
                    try {
                        // First strip any comment...
                        int idx = line.indexOf('#');
                        if (idx != -1){
                            line = line.substring(0, idx);
                        }

                        // Trim whitespace.
                        line = line.trim();

                        // If nothing left then loop around...
                        if (line.length() == 0) {
                            line = br.readLine();
                            continue;
                        }
                        // System.out.println("Line: " + line);

                        // Try and load the class
                        SchemaFactory sf = (SchemaFactory) Class.forName(line, true, cl).newInstance();
                        // stick it into our vector...
                        if(
                            sf.isSchemaLanguageSupported(schemaLanguage) &&
                            !v.contains(line)
                        ){
                            v.add(line);
                        }
                    } catch (Exception ex) {
                        // Just try the next line
                        ex.printStackTrace();
                    }
                    line = br.readLine();
                }
            } catch (Exception ex) {
                // Just try the next file...
            }
        }
        return v;
    }


    public void actionPerformed(ActionEvent e){
        Object c = e.getSource();
        if(c == disabled){
            setValidationEnabled(false);
        } else if(c == builtin){
            configureBuiltInValidation();
        } else if(c == userSchema){
            setSchemaFile();
        }
    }

    private void configureBuiltInValidation(){
        PREF.put(VALIDATION_PREFIX, VALIDATION_BUILTIN);
        builtin.setSelected(true);
        if(schemaSelection.showDialog(this)){
            setValidationEnabled(true);
        } else {
            setValidationEnabled(false);
        }
    }

    private void setSchemaFile(){
        PREF.put(VALIDATION_PREFIX, VALIDATION_USER);
        userSchema.setSelected(true);

        /* configure file chooser */
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new SchemaFileFilter());
        if(userSchemaURL != null){
            try{
                File f = new File(userSchemaURL.toURI());
                chooser.setSelectedFile(f);
            } catch (Exception ignore){
                System.err.println(ignore);
                System.err.flush();
            }
        }
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Choose a Relax NG or W3C Schema file");

        //open file selection dialog
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            File f = chooser.getSelectedFile();
            if(f != null){
                try{
                    userSchemaURL = f.toURI().toURL();
                    setValidationEnabled(true);
                    return;
                } catch (MalformedURLException ex){
                    System.err.println("Warning: cannot load schema " + f.getName());
                    System.err.println(ex);
                    System.err.flush();
                }
            }
        }
        setValidationEnabled(false);
    }

    public void setPreferredEngine(String schemaLanguage, String schemaFactoryClass){
        boolean ok = true;
        String systemProperty = SF + ":" +schemaLanguage;
        String current = System.getProperty(systemProperty);
        if(!schemaFactoryClass.equals(current)){
            /* test if the schema factory works */
            /*
            try{
                SchemaFactory sf = (SchemaFacotry) Class.forName(schemaFactoryClass, true, xmlconv.getClassLoader()).newInstance();
                if(!sf.isSchemaLanguageSupported(schemaLanguage)){
                    System.err.println(System.err.println(schemaFactoryClass + " does not support " + schemaLanguage));
                }
                ok = false;
            } catch (Exception ex){
                System.err.println("Error loading class " + schemaFactoryClass);
                System.err.println(ex);
                ok = false;
            }
            */

            if(ok){
                /* set the system property */
                System.setProperty(systemProperty, schemaFactoryClass);

                /* set the Preferences */
                PREF.put(systemProperty, schemaFactoryClass);

                /* force recompilation of the schema by xmlconv */
                if(!disabled.isSelected()){
                    setValidationEnabled(true);
                }
            } else {
                System.err.flush();
            }
        }
    }

    public String getPreferredEngine(String schemaLanguage){
        String systemProperty = SF + ":" +schemaLanguage;
        return(System.getProperty(systemProperty));
    }

    public boolean setValidationEnabled(boolean b){
        disabled.setSelected(!b);
        if(b){
            if(!enableValidation()){
                setValidationEnabled(false);
            }
        } else {
            try{
                xmlconv.setXMLSchema(null);
            } catch (SAXException ex){
                /* should never happen */
                throw new RuntimeException(ex);
            }
            PREF.put(VALIDATION_PREFIX, VALIDATION_DISABLED);
        }
        return b;
    }

    private boolean enableValidation(){
        boolean ok = false;
        try{
            if(userSchema.isSelected()){
                if(userSchemaURL != null){
                    xmlconv.setXMLSchema(userSchemaURL);
                    ok = true;
                }
            } else if(builtin.isSelected()){
                xmlconv.setXMLSchema(
                schemaSelection.getSchemaSource(XMLConstants.RELAXNG_NS_URI, xmlconv),
                XMLConstants.RELAXNG_NS_URI);
                ok = true;
            }
        } catch (SAXParseException ex){
            System.err.println("Warning: error in schema");
            System.err.println(ex.getSystemId() + " line " + ex.getLineNumber());
            System.err.println(ex);
        } catch (Exception ex){
            System.err.println("Error activating validation.");
            System.err.println(ex);
        } finally {
            System.out.flush();
            System.err.flush();
        }
        return ok;
    }

    private static class SchemaFileFilter extends javax.swing.filechooser.FileFilter{
        public boolean accept(File f){
            return f.isDirectory() || f.getName().endsWith(".xsd") || f.getName().endsWith(".rng");
        }

        public String getDescription(){
            return "*.rng, *.xsd";
        }
    }

}