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

import java.util.List;
import java.awt.Container;
import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
    private final Container styleContainer; // = Box.createVerticalBox();
    private final ErrorListener errorHandler;
    private final Preferences userStyles;

    public StyleSheetManager(XMLConverter convert, Container styleContainer,
        Collection<StyleSheetController> builtins,
        ErrorListener errorHandler,
        Preferences userStyles){
        this.styleContainer = styleContainer;
        this.errorHandler = errorHandler;
        this.convert = convert;
        this.userStyles = userStyles;

        for(StyleSheetController style : builtins){
            if(style != null){
                addStyleImpl(style);
            }
        }

        try{
        for(StyleSheetController style : StyleSheetController.load(convert, userStyles)){
            addStyleImpl(style);
        }
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    protected boolean hasStyles(){
        return (styles != null) && (!styles.isEmpty());
    }

    protected StyleSheetController[] getStyles(){
        return (hasStyles())?
            styles.toArray(new StyleSheetController[styles.size()]):
            null;
    }

    public boolean addStyle(StyleSheetController style){
        return addStyleImpl(style);
    }

    private StyleSheetController.StyleConfig
            queryStyleConfig(Component comp, StyleSheetController parent){
        StyleSheetController.StyleConfig result = new StyleSheetController.StyleConfig();

        JFileChooser jfc = new JFileChooser(
                Preferences.userNodeForPackage(getClass()).get("styledir",""));
        jfc.setDialogTitle("Choose an XSLT stylesheet");
        jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(comp);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            try{
                result.style = jfc.getSelectedFile().toURI().toURL();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        File dir = jfc.getCurrentDirectory();
        if(dir != null){
            Preferences.userNodeForPackage(getClass()).put("styledir", dir.getAbsolutePath());
        }
        if(result.style == null){
            return null;
        }
        result.name = JOptionPane.showInputDialog(comp, "Please enter a name for the new output format.");
        if(result.name == null){
            return null;
        } else {
            result.name = result.name.replaceAll("[\\./]", " ");
        }
        while((parent == null && nameExists(result.name)) ||
              (parent != null && parent.hasChild(result.name))){
            result.name = JOptionPane.showInputDialog(comp,
                "This name is already in use, please enter another one.",
                result.name);
            if(result.name == null){
                return null;
            } else {
                result.name = result.name.replaceAll("[\\./]", " ");
            }
        }
        result.suffix = JOptionPane.showInputDialog(comp, "Please enter a filename suffix for the new output format.");
        if(result.suffix == null){
            return null;
        }
        while(suffixExists(result.suffix)){
            result.suffix = JOptionPane.showInputDialog(comp,
            "This suffix is already in use, please enter another one.",
                result.suffix);
            if(result.suffix == null){
                return null;
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
            result.customParams = params.isSelected();
            result.customEncoding = enc.isSelected();
            result.windowsLineTerminators = crlf.isSelected();
        } else {
            result = null;
        }
        return result;
    }

    public synchronized boolean addChildStyle(){
        List<StyleSheetController> possibleParents = new LinkedList<StyleSheetController>();
        if(styles != null){
            flatten(styles, possibleParents);
        }
        Iterator<StyleSheetController> it = possibleParents.iterator();
        while(it.hasNext()){
            if(!it.next().allowsChildren()){
                it.remove();
            }
        }
        StyleSheetController parent =
                    (StyleSheetController)
                    JOptionPane.showInputDialog(styleContainer,
                    "Pick parent format",
                    "Pick parent format",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    possibleParents.toArray(new StyleSheetController[
                        possibleParents.size()]),
                    null);
        if(parent == null){
            return false;
        }
        StyleSheetController.StyleConfig config =
                queryStyleConfig(styleContainer, parent);
        StyleSheetController newChild = null;
        if(config != null){
            try{
                newChild = parent.addNewChild(config);
                newChild.setErrorHandler(errorHandler);
            } catch (Exception ex){
                convert.handleException(null, ex);
            }
        }
        System.err.flush();
        return newChild != null;
    }

    private static void flatten(Collection<StyleSheetController> src, List<StyleSheetController> dst){
        for(StyleSheetController element : src){
            dst.add(element);
            if(element.allowsChildren() && element.hasChildren()){
                flatten(Arrays.asList(element.getChildren()), dst);
            }
        }
    }

    public boolean addStyleWithUnknownInput(){
        int result = JOptionPane.showOptionDialog(styleContainer,
            "What kind of input does the stylesheet accept?",
            "Input", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new String[]{"BibTeXML", "Other"}, "BibTeXML");
        if(result == 0){
            return addStyle();
        } else if (result == 1){
            return addChildStyle();
        } else {
            return false;
        }
    }

    public boolean addStyle(){
        StyleSheetController.StyleConfig config = queryStyleConfig(styleContainer, null);
        StyleSheetController ssc = null;
        if(config != null){
            try{
                ssc = new StyleSheetController(convert, config, userStyles);
                addStyle(ssc);
                ssc.setErrorHandler(errorHandler);
            } catch (Exception ex){
                convert.handleException(null, ex);
            }
        }
        System.err.flush();
        return ssc != null;
    }

    public boolean nameExists(String name){
        boolean result = false;
        if(hasStyles()){
            List<StyleSheetController> allStyles = new ArrayList<StyleSheetController>();
            flatten(styles, allStyles);
            for(StyleSheetController ssc : allStyles){
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
            List<StyleSheetController> allStyles = new ArrayList<StyleSheetController>();
            flatten(styles, allStyles);
            for(StyleSheetController ssc : allStyles){
                if(ssc.getSuffix().equals(suffix)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    synchronized private boolean addStyleImpl(StyleSheetController cssc){
        if(cssc == null){
            return false;
        }
        if(styles == null){
            styles = new HashSet<StyleSheetController>();
        }
        boolean result = styles.add(cssc);
        if(result && styleContainer != null){
            styleContainer.add(cssc.getUI());
        }
        return result;
    }

    boolean removeStyle(){
        if(hasStyles()){
            List<StyleSheetController> v =
                new LinkedList<StyleSheetController>();
            flatten(styles, v);
            Iterator<StyleSheetController> it = v.iterator();
            while(it.hasNext()){
                if(it.next().isBuiltin()){
                    it.remove();
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
            cssc.disposeUI();
        } else {
            StyleSheetController parent = null;
            final String name = cssc.getName();
            List<StyleSheetController> allStyles = new ArrayList<StyleSheetController>();
            flatten(styles, allStyles);
            for(StyleSheetController candidate : allStyles){
                if(candidate.hasChild(name)){
                    result = candidate.removeChild(cssc);
                    break;
                }
            }
        }
        return result;
    }

}