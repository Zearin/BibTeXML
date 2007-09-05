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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JTextPane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.xml.transform.TransformerException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import de.mospace.swing.text.*;
import de.mospace.lang.ProcessRunnable;
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
    private MutableAttributeSet linkStyle;

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
        if(DoubleClickFileLauncher.isKnownOS()){
            console.addMouseListener(new DoubleClickFileLauncher());
        }
        linkStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        //StyleConstants.setUnderline(linkStyle, true);
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


    public void printLink(String s){
        printStyled(s, linkStyle);
    }

    void printStyled(final String s, final MutableAttributeSet style){
        Document doc = console.getDocument();
        if (EventQueue.isDispatchThread()) {
            try{
                doc.insertString(doc.getLength(), s, style);
            } catch (BadLocationException ignore){
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    printStyled(s, style);
                }
            });
        }
    }

    public void printNormal(String s){
        printStyled(s, null);
    }

    public void printFilePath(String s){
        if(DoubleClickFileLauncher.isKnownOS()){
            printLink(s);
        } else {
            printNormal(s);
        }
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

    public void init(){
        console.setText("This is the BibTeXConverter status and output area. " +
            "You can cut and copy from this area as well as write to this area." +
            " To clear previous output just select everything (usually Ctrl+A) "+
            "and hit backspace.\n\n");
        if(DoubleClickFileLauncher.isKnownOS()){
            printNormal("Files and directories that appear on a separate line " +
            "can probably be opened by double click (verified " +
             "only on Windows XP).\n\n");
        }
    }

    /** This class does not have state and could be a Singleton. **/
    private static class DoubleClickFileLauncher extends MouseAdapter{
        private static enum OSY { UNKNOWN, WIN, MAC };
        private static final OSY OS = getOS();

        private static OSY getOS(){
            String sysOsName = System.getProperty("os.name");
            OSY result = OSY.UNKNOWN;
            if(sysOsName != null){
                if(sysOsName.startsWith("Windows")){
                    result = OSY.WIN;
                } else if (sysOsName.startsWith("Mac ")){
                    result = OSY.MAC;
                }
            }
            return result;
        }

        public static boolean isKnownOS(){
            return OS != OSY.UNKNOWN;
        }

        public void mouseClicked(MouseEvent e){
            if(isKnownOS()){
                if(e.getClickCount() == 2 &&
                    e.getButton() == MouseEvent.BUTTON1)
                {
                    doubleClick(e);
                }
            }
        }

        private void doubleClick(MouseEvent e){
            Object src = e.getSource();
            if(src instanceof JTextComponent){
                JTextComponent jtc = (JTextComponent) src;
                try{
                    doubleClickOnText(jtc, e.getPoint());
                } catch (BadLocationException howcouldthishappen){
                    System.err.println(howcouldthishappen);
                    System.err.flush();
                }
            }
        }

        private void doubleClickOnText(JTextComponent comp, Point p) throws BadLocationException{
            int pos = comp.viewToModel(p);
            Document doc = comp.getDocument();
            int linestart = getLineStart(comp.getDocument(), pos);
            int lineend = getLineEnd(comp.getDocument(), pos);
            String line = doc.getText(linestart, lineend-linestart);
            char[] c = line.toCharArray();
            int textstart = 0;
            int textlength = c.length;
            while(textstart < c.length - 1 &&
                Character.isWhitespace(c[textstart])){
                textstart++;
                textlength--;
            }
            int textend = c.length - 1;
            while(
                Character.isWhitespace(c[textend])
                && textlength >= 0){
                textend--;
                textlength--;
            }
            comp.select(linestart + textstart, linestart + textstart + textlength);
            launchFile(line.trim());
        }

        private static int getLineStart(Document doc, int pos) throws BadLocationException{
            int offs = pos;
            String s = "";
            while(!"\n".equals(s) && offs > 0){
                offs --;
                s = doc.getText(offs, 1);
            }
            return offs;
        }

        private static int getLineEnd(Document doc, int pos) throws BadLocationException{
            int offs = pos-1;
            String s = "";
            while(!"\n".equals(s) && offs < doc.getLength()){
                offs ++;
                s = doc.getText(offs, 1);
            }
            return offs;
        }

        private static void launchFile(String path){
            File file = new File(path);
            if(file.exists()){
                launchFile(file);
            }
        }

        private static void launchFile(File file){
            if (OS == OSY.UNKNOWN){
                return;
            }
            List<String> cmdtokens = new ArrayList<String>();
            switch(OS){
            case WIN:
                //tested to work on XP
                cmdtokens.add("rundll32.exe");
                cmdtokens.add("shell32.dll,");
                cmdtokens.add("ShellExec_RunDLL");
                break;
            case MAC:
                cmdtokens.add("open");
            }
            cmdtokens.add(file.getAbsolutePath());
            //using invokeLater here allows the textcomponent to be
            //repainted (i. e. the visible text selection to be
            //updated) before the file is launched.
            SwingUtilities.invokeLater(new ProcessRunnable(
                    cmdtokens.toArray(new String[cmdtokens.size()])
                    ));
        }
    }

}
