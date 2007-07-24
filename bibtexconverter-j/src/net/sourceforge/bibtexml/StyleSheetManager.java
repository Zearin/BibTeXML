package net.sourceforge.bibtexml;
/*
 * $Id: BibTeXConverterController.java 167 2007-03-23 19:16:11Z ringler $
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

import java.util.List;
import java.awt.Container;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.util.Collection;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JCheckBox;
import javax.swing.Box;
import javax.xml.transform.ErrorListener;

class StyleSheetManager {
    private Collection<StyleSheetController> styles;
    private final XMLConverter convert;
    private final Collection<StyleSheetController> builtins;
    private final Container styleContainer; // = Box.createVerticalBox();
    private final ErrorListener errorHandler;
    protected File styledir;

    public StyleSheetManager(XMLConverter convert, Container styleContainer,
        Collection<StyleSheetController> builtins, ErrorListener errorHandler){
        this.styleContainer = styleContainer;
        this.errorHandler = errorHandler;
        this.convert = convert;

        String[] builtInNames = new String[builtins.size()];
        int i = 0;
        for(StyleSheetController style : builtins){
            builtInNames[i++] = style.getName();
            addStyle(style);
        }
        this.builtins = builtins;

        Preferences pref = Preferences.userNodeForPackage(getClass());
        String styledirpath = pref.get("styledir", null);
        styledir = (styledirpath == null)? null : new File(styledirpath);

        try{
        for(StyleSheetController style : StyleSheetController.load(convert, builtInNames)){
            addStyle(style);
        }
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    protected String[] getBuiltinStyleNames(){
        if(builtins == null){
            return new String[0];
        }
        List<String> result = new ArrayList<String>();
        for(StyleSheetController style : builtins){
            result.add(style.getName());
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    protected boolean hasStyles(){
        return (styles != null) && (!styles.isEmpty());
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
        int returnVal = jfc.showOpenDialog(styleContainer);
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
            Preferences.userNodeForPackage(getClass()).put("styledir", styledir.getAbsolutePath());
        }
        if(style == null){
            return false;
        }
        String name = null;
        name = JOptionPane.showInputDialog(styleContainer, "Please enter a name for the new output format.");
        if(name == null){
            return false;
        } else {
            name = name.replaceAll("[\\./]", " ");
        }
        while(nameExists(name)){
            name = JOptionPane.showInputDialog(styleContainer,
            "This name is already in use, please enter another one.", name);
            if(name == null){
                return false;
            } else {
                name = name.replaceAll("[\\./]", " ");
            }
        }
        String suffix = null;
        suffix = JOptionPane.showInputDialog(styleContainer, "Please enter a filename suffix for the new output format.");
        if(suffix == null){
            return false;
        }
        while(suffixExists(suffix)){
            suffix = JOptionPane.showInputDialog(styleContainer,
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
            styleContainer,
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
                ssc.setErrorHandler(errorHandler);
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
        if(cssc == null){
            return false;
        }
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
            if(!v.isEmpty()){
                StyleSheetController result =
                    (StyleSheetController)
                    JOptionPane.showInputDialog(styleContainer,
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
        JOptionPane.showMessageDialog(styleContainer,"There are currently no removable output styles!");
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

}