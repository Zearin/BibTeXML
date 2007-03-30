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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.xml.transform.TransformerException;
import de.mospace.swing.LookAndFeelMenu;
import de.mospace.swing.PathInput;
import de.mospace.swing.text.*;
import de.mospace.xml.ResettableErrorHandler;
import de.mospace.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import net.sourceforge.texlipse.model.ParseErrorMessage;
import net.sourceforge.bibtexml.BibTeXConverter.Parser;

class MessagePanel extends JPanel{
    private final CardLayout layout = new CardLayout();
    private final static String CONSOLE = "Console";
    private final static String ERRORS = "Error List";
    private final JEditorPane console = new JTextPane();
    private final ErrorList errorlist;
    private UniversalErrorHandler errorhandler;
    
    public MessagePanel(){
        super();
        setLayout(layout);
        DocumentOutputStream output = new DocumentOutputStream(console.getDocument());
        System.setOut(new PrintStream(new BufferedOutputStream(output), false));
        output = new DocumentOutputStream(console.getDocument());
        output.setColor(Color.red);
        System.setErr(new PrintStream(new BufferedOutputStream(output), false));
        ActionListener close = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                showConsole();
            }
        };
        errorlist = new ErrorList(close);
        add(new JScrollPane(console), CONSOLE);
        add(errorlist.component(), ERRORS);
        showConsole();
        setPreferredSize(new Dimension(200,200));
    }
    
    public synchronized UniversalErrorHandler getErrorHandler(){
        if(errorhandler == null){
            errorhandler = new JointErrorHandler(
                errorlist.getErrorHandler(),
                new MyErrorHandler());
        }
        return errorhandler;
    }
    
    public void showConsole(){
        layout.show(this, CONSOLE);
    }
    
    public void showErrors(){
        layout.show(this, ERRORS);
    }
    
    protected ErrorList getErrorList(){
        return errorlist;
    }
    
    private class MyErrorHandler implements UniversalErrorHandler{
        public MyErrorHandler(){
        }
        
        public void fatalError( SAXParseException e ) throws SAXException {
            showErrors();
        }
        
        public void error( SAXParseException ex ) throws SAXException {
            showErrors();
        }
        
        public void warning( SAXParseException e ) throws SAXException {
            showErrors();
        }
        
        public void fatalError( TransformerException e ) throws TransformerException {
            showErrors();
        }
        
        public void error( TransformerException ex ) throws TransformerException {
            showErrors();
        }
        
        public void warning( TransformerException e ) throws TransformerException {
            showErrors();
        }
        
        public void error(ParseErrorMessage e) throws IOException {
            showErrors();
        }
        
        public synchronized void reset(){
            errorlist.setAllowDoubleClick(false);
            showConsole();
        }
        
    }
    
}
