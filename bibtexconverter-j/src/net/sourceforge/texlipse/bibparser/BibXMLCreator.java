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
 * Builds a BibTeXML tree from the AST of BibTeX entries. Instances
 * of this class are re-usable and can be
 * applied to several ASTs sequentially. A new Document will be started
 * whenever a new AST root node is encountered.<br>
 * This class is not thread-safe and each thread must create its own instance.
 * <p>
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
    /** The BibTeXML namespace. **/
    public static final String BIB_NAMESPACE = "http://bibtexml.sf.net/";
    /** A regular expression describing the separator in author lists.**/
    final static Pattern AUTHOR_REX = Pattern.compile("\\s+and\\s+");
    /** A regular expression describing the separator in keyword lists.**/
    final static Pattern KEYWORDS_REX = Pattern.compile("\\s*[,;]\\s*");
    /** A regular expression describing one or more whitespace characters.**/
    final static Pattern WHITESPACE_REX = Pattern.compile("\\s+");
    private transient String key = "";
    private transient String entryType;
    private int entryCount = 0;

    /** Normalizes whitespace in the string argument; encodes the XML
        special characters &, >, and <; decodes LaTeX ~, and --, and removes
        all braces.
        @Return the processed string
    **/
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

    /** Checks whether an exception occurred when building the
        XML tree. **/
    public boolean checkError(){
        return error != null;
    }

    /** Returns the last exception that occurred when building the XML tree.
    * @return an exception or <code>null</code> if the XML tree was built without
    * errors.
    **/
    public Throwable getError(){
        return error;
    }

    /** Constructs a new isntance of this class. */
    public BibXMLCreator(){
        //sole constructor
    }

    /** Returns the BibTeXML document that resulted from the last
    * application of this visitor to a BibTeX AST.
    * @return the BibTeXML document or <code>null</code> if this visitor
    * has not been applied to an AST yet
    */
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

    public void inAEntrybraceEntry(AEntrybraceEntry node) {
        entry = new Element("entry", BIB_NAMESPACE);
        entry.setAttribute("id", node.getIdentifier().getText());
        root.addContent(entry);
        entryCount++;
    }

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

    /** Returns the number of entries that have been converted to BibTeXML
    * when this visitor was last applied to a BibTeX AST.
    * @return the entry count or 0 if this visitor has not been applied to an
    * AST yet
    **/
    public int getEntryCount(){
        return entryCount;
    }
}
