/*
 * $Id: BibXMLCreator.java,v 1.6 2006/10/26 13:32:57 Moritz.Ringler Exp $
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


/**
 * Retrieves the BibTeX entries from the AST and prints them as BibXML.
 *
 * This class is a visitor, that is applied on the AST that is a result of parsing a
 * BibTeX-file. See <a href="http://www.sablecc.org">http://www.sablecc.org</a> for
 * more information on the structure of the AST and the visitors.
 * @deprecated use BibXMLWriter instead
 *
 * @author Moritz Ringler
 */
public final class BibXMLCreator extends DepthFirstAdapter {
    PrintWriter writer;
    private final static class XMLAttribute{
        public final String name;
        public final String value;
        public XMLAttribute(String name, String value){
            this.name = name;
            this.value = value;
        }
    }
    protected final static Pattern AUTHOR_REX = Pattern.compile("\\s+and\\s+");
    protected final static Pattern KEYWORDS_REX = Pattern.compile("\\s*[,;]\\s*");
    private String key = "";
    private String entryType;

    private enum XMLNodeType{
        TEXT, ELEMENT
    }

    private XMLNodeType lastClosed = XMLNodeType.ELEMENT;

    private void open(String tag){
        writer.print("\n<bibtex:"+tag.toLowerCase()+">");
    }

    private void open(String tag, XMLAttribute att){
        writer.print("\n<bibtex:"+tag.toLowerCase()+" "+
            att.name + "=\"" +
            att.value.replaceAll("\"", "'") +
            "\">");
    }

    private void open(String tag, XMLAttribute[] att){
        writer.print("\n<bibtex:"+tag.toLowerCase());
        for(XMLAttribute eatt : att){
            writer.print(" "+
            eatt.name + "=\"" +
            eatt.value.replaceAll("\"", "'"));
        }
        writer.print("\">");
    }

    private void close(String tag){
        if(lastClosed == XMLNodeType.ELEMENT){
            writer.print("\n");
        }
        writer.print("</bibtex:"+tag.toLowerCase()+">");
        lastClosed = XMLNodeType.ELEMENT;
    }

    private String replacements(String txt){
        String text = txt.replaceAll("\\s+", " ");
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll("~", "&#160;");
        text = text.replaceAll("--", "-");
        text = text.replaceAll("[\\{\\}]","");
        return text;
    }

    private void textNode(String text){
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
        writer.print(wrappedText);
        lastClosed = XMLNodeType.TEXT;
    }

    private void shortTextNode(String text){
        writer.print(text.replaceAll("[\\{\\}]",""));
    }

    BibXMLCreator(PrintWriter writer){
        this.writer = writer;
    }

    public void inABibtex(ABibtex node) {
        open("file", new XMLAttribute("xmlns:bibtex", "http://bibtexml.sf.net/"));
    }

    public void outABibtex(ABibtex node) {
        close("file");
        writer.println();
        writer.flush();
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
        open("entry", new XMLAttribute("id", node.getIdentifier().getText()));
    }

    /**
     * Called when exiting a bibliography entry, adds the formed
     * entry into the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void outAEntrybraceEntry(AEntrybraceEntry node) {
        close(entryType);
        close("entry");
        //close(node.getIdentifier().getText());
        writer.println();
    }

    public void inAEntryparenEntry(AEntryparenEntry node) {
        open("entry", new XMLAttribute("id", node.getIdentifier().getText()));
    }

    public void outAEntryparenEntry(AEntryparenEntry node) {
        close(entryType);
        close("entry");
        //close(node.getIdentifier().getText());
        writer.println();
    }

    public void inAEntryDef(AEntryDef node) {
    }

    /**
     * Handles the type of the bibliography entry
     *
     * @param node an <code>AEntryDef</code> value
     */
    public void outAEntryDef(AEntryDef node) {
        entryType = node.getEntryName().getText().substring(1);
        open(entryType);
    }

    public void inAKeyvalDecl(AKeyvalDecl node) {
        key = node.getIdentifier().getText().toLowerCase();
        open(key);
    }

    public void outAKeyvalDecl(AKeyvalDecl node) {
        close(node.getIdentifier().getText());
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
        } else {
            values = new String[]{value};
        }
        textNode(values[0]);
        for(int i=1, stop = values.length; i< stop; i++){
            close(key);
            open(key);
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
