/*
 * $Id$
 *
 * Copyright (c) 2006 Moritz Ringler
 * This class is derived from EntryRetriever by Oskar Ojala
 * (also in this package)
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
package net.sourceforge.texlipse.bibparser;


import net.sourceforge.texlipse.bibparser.analysis.DepthFirstAdapter;
import net.sourceforge.texlipse.bibparser.node.*;
import java.io.PrintWriter;
import java.util.regex.*;
import de.mospace.xml.SaxXMLWriter;
import org.xml.sax.SAXException;


/**
 * Retrieves the BibTeX entries from the AST and prints them as BibXML.
 *
 * This class is a visitor, that is applied on the AST that is a result of parsing a
 * BibTeX-file. See <a href="http://www.sablecc.org">http://www.sablecc.org</a> for
 * more information on the structure of the AST and the visitors.
 *
 * @author Moritz Ringler
 */
public final class BibXMLWriter extends DepthFirstAdapter {
    private final SaxXMLWriter xw;

    protected final static Pattern AUTHOR_REX = Pattern.compile("\\s+and\\s+");
    protected final static Pattern KEYWORDS_REX = Pattern.compile("\\s*[,;]\\s*");
    protected final static Pattern WHITESPACE_REX = Pattern.compile("\\s+");
    private String key = "";
    private String entryType;

    private static String replacements(String txt){
        String text = txt.replaceAll("\\s+", " ");
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll("~", "\u00A0");
        text = text.replaceAll("--", "-");
        text = text.replaceAll("[\\{\\}]","");
        return text;
    }

    private Throwable error = null;

    private void setError(Throwable ex){
        error = ex;
    }

    public boolean checkError(){
        return error != null;
    }

    public Throwable getError(){
        return error;
    }

    private void textNode(String text){
        if(error == null){
            final int MAXLINE = 80;
            int offset = MAXLINE;
            StringBuffer wrappedText = new StringBuffer(replacements(text));
            while(wrappedText.length() > offset){
                int k = wrappedText.lastIndexOf(" ", offset);
                if(k < offset - MAXLINE){
                    k = wrappedText.indexOf(" ", offset);
                }
                if(k == -1){
                    break;
                } else {
                    wrappedText.setCharAt(k, '\n');
                    offset += 80;
                }
            }
            try{
                xw.text(wrappedText.toString());
            } catch (SAXException ex){
                setError(ex);
            }
        }
    }

    private void shortTextNode(String text){
        if(error == null){
            try{
                xw.text(replacements(text));
            } catch (SAXException ex){
                setError(ex);
            }
        }
    }

    private void startElement(String element){
        if(error == null){
             try{
                xw.startElement(element);
            } catch (SAXException ex){
                setError(ex);
            }
        }
    }

    private void endElement(String element){
        if(error == null){
             try{
                xw.endElement(element);
            } catch (SAXException ex){
                setError(ex);
            }
        }
    }

    private void attribute(String name, String value){
        if(error == null){
             try{
                xw.attribute(name, value);
            } catch (SAXException ex){
                setError(ex);
            }
        }
    }

    BibXMLWriter(SaxXMLWriter writer){
        xw = writer;
    }

    public void inABibtex(ABibtex node) {
        startElement("file");
    }

    public void outABibtex(ABibtex node) {
        endElement("file");
    }

    public void inAStrbraceStringEntry(AStrbraceStringEntry node) {
    }

    public void outAStrbraceStringEntry(AStrbraceStringEntry node) {
    }

    public void inAStrparenStringEntry(AStrparenStringEntry node) {
    }

    public void outAStrparenStringEntry(AStrparenStringEntry node) {
    }

    /**
     * Called when entering a bibliography entry, starts
     * forming an entry for the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void inAEntrybraceEntry(AEntrybraceEntry node) {
        attribute("id", node.getIdentifier().getText());
        startElement("entry");
    }

    /**
     * Called when exiting a bibliography entry, adds the formed
     * entry into the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void outAEntrybraceEntry(AEntrybraceEntry node) {
        endElement(entryType);
        endElement("entry");
    }

    public void inAEntryparenEntry(AEntryparenEntry node) {
        attribute("id", node.getIdentifier().getText());
        startElement("entry");
    }

    public void outAEntryparenEntry(AEntryparenEntry node) {
        endElement(entryType);
        endElement("entry");
    }

    public void inAEntryDef(AEntryDef node) {
    }

    /**
     * Handles the type of the bibliography entry
     *
     * @param node an <code>AEntryDef</code> value
     */
    public void outAEntryDef(AEntryDef node) {
        entryType = node.getEntryName().getText().substring(1).toLowerCase();
        startElement(entryType);
    }

    public void inAKeyvalDecl(AKeyvalDecl node) {
        key = node.getIdentifier().getText().toLowerCase();
        startElement(key);
    }

    public void outAKeyvalDecl(AKeyvalDecl node) {
        endElement(node.getIdentifier().getText().toLowerCase());
    }

    public void inAConcat(AConcat node) {
    }

    public void outAConcat(AConcat node) {
    }

    public void inAValueValOrSid(AValueValOrSid node) {
    }

    public void outAValueValOrSid(AValueValOrSid node) {
        String value = node.getStringLiteral().getText();
        String[] values;
        if(key.equals("author")){
            values = AUTHOR_REX.split(value);
            String[] authorparts;
            StringBuilder builder;
            for(int i=0; i<values.length; i++){
                values[i] = values[i].trim();
                if(values[i].indexOf(',') == -1){
                    authorparts = values[i].split("\\s+");
                    if(authorparts.length > 1){
                        final int n = authorparts.length - 1;
                        builder = new StringBuilder();
                        builder.append(authorparts[n]).append(", ");
                        for(int j=0; j< n - 1; j++){
                            builder.append(authorparts[j]).append(" ");
                        }
                        builder.append(authorparts[n - 1]);
                        values[i] = builder.toString();
                    }
                }
            }
        } else if (key.equals("keywords") || key.equals("refgroup")){
            values = KEYWORDS_REX.split(value);
        } else if (key.equals("pages")){
            values = new String[]{WHITESPACE_REX.matcher(value).replaceAll("")};
        }  else {
            values = new String[]{value};
        }
        textNode(values[0]);
        for(int i=1, stop = values.length; i< stop; i++){
            endElement(key);
            startElement(key);
            textNode(values[i]);
        }
    }

    public void inANumValOrSid(ANumValOrSid node) {
    }

    public void outANumValOrSid(ANumValOrSid node) {
        shortTextNode(node.getNumber().getText());
    }

    public void inAIdValOrSid(AIdValOrSid node) {
    }

    public void outAIdValOrSid(AIdValOrSid node) {
        shortTextNode(node.getIdentifier().getText());
    }
}
