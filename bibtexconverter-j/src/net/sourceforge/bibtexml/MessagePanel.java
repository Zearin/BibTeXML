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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import javax.swing.*;
import javax.xml.transform.TransformerException;
import de.mospace.swing.text.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import net.sourceforge.texlipse.model.ParseErrorMessage;

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
        final ActionListener close = new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                showConsole();
            }
        };
        errorlist = new ErrorList(close);
        add(new JScrollPane(console), CONSOLE);
        add(errorlist.component(), ERRORS);
        layout.show(this, CONSOLE);
        setPreferredSize(new Dimension(200,200));
    }

    public void makeSystemOut(){
        DocumentOutputStream output = new DocumentOutputStream(console.getDocument());
        System.setOut(new PrintStream(new BufferedOutputStream(output), false));
    }

    public void makeSystemErr(){
        DocumentOutputStream output = new DocumentOutputStream(console.getDocument());
        output.setColor(Color.red);
        System.setErr(new PrintStream(new BufferedOutputStream(output), false));
        final ActionListener close = new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                showConsole();
            }
        };
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
            //sole constructor
        }

        public void fatalError(final SAXParseException e ) throws SAXException {
            showErrors();
        }

        public void error(final SAXParseException ex ) throws SAXException {
            showErrors();
        }

        public void warning(final SAXParseException e ) throws SAXException {
            showErrors();
        }

        public void fatalError(final TransformerException e ) throws TransformerException {
            showErrors();
        }

        public void error(final TransformerException ex ) throws TransformerException {
            showErrors();
        }

        public void warning(final TransformerException e ) throws TransformerException {
            showErrors();
        }

        public void error(final ParseErrorMessage e) throws IOException {
            showErrors();
        }

        public synchronized void reset(){
            errorlist.setAllowDoubleClick(false);
            showConsole();
        }

    }

}
