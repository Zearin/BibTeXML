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
import java.util.regex.Pattern;
import org.xml.sax.SAXException;
import org.jdom.*;


/**
 * Retrieves the BibTeX entries from the AST and prints them as BibXML.
 *
 * This class is a visitor, that is applied on the AST that is a result of parsing a
 * BibTeX-file. See <a href="http://www.sablecc.org">http://www.sablecc.org</a> for
 * more information on the structure of the AST and the visitors.
 *
 * @author Moritz Ringler
 */
public final class BibXMLCreator extends DepthFirstAdapter {
    private Document doc;
    private transient Element root;
    private transient Element entry;
    private transient Element entrysub;
    public static final String BIB_NAMESPACE = "http://bibtexml.sf.net/";
    final static Pattern AUTHOR_REX = Pattern.compile("\\s+and\\s+");
    final static Pattern KEYWORDS_REX = Pattern.compile("\\s*[,;]\\s*");
    final static Pattern WHITESPACE_REX = Pattern.compile("\\s+");
    private transient String key = "";
    private transient String entryType;
    private int entryCount = 0;

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

    public BibXMLCreator(){
        //sole constructor
    }
    
    public Document getResultDocument(){
        return doc;
    }

    public void inABibtex(ABibtex node) {
        root = new Element("file", BIB_NAMESPACE);
        doc = new Document(root);
    }

    public void outABibtex(ABibtex node) {
        root = null;
    }

    public void inAStrbraceStringEntry(AStrbraceStringEntry node) {
        //do nothing
    }

    public void outAStrbraceStringEntry(AStrbraceStringEntry node) {
        //do nothing
    }

    public void inAStrparenStringEntry(AStrparenStringEntry node) {
        //do nothing
    }

    public void outAStrparenStringEntry(AStrparenStringEntry node) {
        //do nothing
    }

    /**
     * Called when entering a bibliography entry, starts
     * forming an entry for the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void inAEntrybraceEntry(AEntrybraceEntry node) {
        entry = new Element("entry", BIB_NAMESPACE);
        entry.setAttribute("id", node.getIdentifier().getText());
        root.addContent(entry);
        entryCount++;
    }

    /**
     * Called when exiting a bibliography entry, adds the formed
     * entry into the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void outAEntrybraceEntry(AEntrybraceEntry node) {
        entry = null;
    }

    public void inAEntryparenEntry(AEntryparenEntry node) {
        entry = new Element("entry", BIB_NAMESPACE);
        entry.setAttribute("id", node.getIdentifier().getText());
        root.addContent(entry);
        entryCount++;
    }

    public void outAEntryparenEntry(AEntryparenEntry node) {
        entry = null;
    }

    public void inAEntryDef(AEntryDef node) {
        //do nothing
    }

    /**
     * Handles the type of the bibliography entry
     *
     * @param node an <code>AEntryDef</code> value
     */
    public void outAEntryDef(AEntryDef node) {
        entryType = node.getEntryName().getText().substring(1).toLowerCase();
        entrysub = new Element(entryType, BIB_NAMESPACE);
        entry.addContent(entrysub);
    }

    public void inAKeyvalDecl(AKeyvalDecl node) {
        key = node.getIdentifier().getText().toLowerCase();
    }

    public void outAKeyvalDecl(AKeyvalDecl node) {
        key = null;
    }

    public void inAConcat(AConcat node) {
        //do nothing
    }

    public void outAConcat(AConcat node) {
        //do nothing
    }

    public void inAValueValOrSid(AValueValOrSid node) {
        //do nothing
    }

    public void outAValueValOrSid(AValueValOrSid node) {
        String value = node.getStringLiteral().getText();
        String[] values;
        if(key.equals("author")){
            values = AUTHOR_REX.split(value);
            for(int i=0; i<values.length; i++){
                try{
                    values[i] = new Author(values[i]).toString();
                } catch (java.text.ParseException ignore){
                    System.err.println("Cannot parse author "+values[i]);
                    System.err.println(ignore);
                    System.err.flush();
                }
            }
        } else if (key.equals("keywords") || key.equals("refgroup")){
            values = KEYWORDS_REX.split(value);
        } else if (key.equals("pages")){
            values = new String[]{WHITESPACE_REX.matcher(value).replaceAll("")};
        }  else {
            values = new String[]{value};
        }
        for(int i=0, stop = values.length; i< stop; i++){
            Element bibnode = new Element(key, BIB_NAMESPACE);
            bibnode.setText(replacements(values[i]));
            entrysub.addContent(bibnode);
        }
    }

    public void inANumValOrSid(ANumValOrSid node) {
        //do nothing
    }

    public void outANumValOrSid(ANumValOrSid node) {
        Element bibnode = new Element(key, BIB_NAMESPACE);
        bibnode.setText(replacements(node.getNumber().getText()));
        entrysub.addContent(bibnode);
    }

    public void inAIdValOrSid(AIdValOrSid node) {
        //do nothing
    }

    public void outAIdValOrSid(AIdValOrSid node) {
        Element bibnode = new Element(key, BIB_NAMESPACE);
        bibnode.setText(replacements(node.getIdentifier().getText()));
        entrysub.addContent(bibnode);
    }

    public int getEntryCount(){
        return entryCount;
    }
}
