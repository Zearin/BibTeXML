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
import java.util.regex.Matcher;
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
public final class BibXMLCreator extends AbbrevRetriever {
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
    private transient StringBuilder value;
    private transient String entryType;
    private int entryCount = 0;
    private BibTeXDecoder decoder = new BibTeXDecoder();

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
        value = new StringBuilder();
    }

    public void outAKeyvalDecl(AKeyvalDecl node) {
        makeKeyvalNodes(node.getIdentifier().getText().toLowerCase(),
                  value.toString());
        value = null;
    }

    private void makeKeyvalNodes(String key, String val){
        String[] values;
        boolean etal = false;
        if(key.equals("author")){
            values = AUTHOR_REX.split(val);
            for(int i=0; i<values.length; i++){
                if("others".equals(values[i].trim().toLowerCase())){
                    etal = true;
                    continue;
                }
                try{
                    values[i] = new Author(values[i]).toString();
                } catch (java.text.ParseException ignore){
                    System.err.println("Cannot parse author "+values[i]);
                    System.err.println(ignore);
                    System.err.flush();
                }
            }
        } else if (key.equals("keywords") || key.equals("refgroup")){
            values = KEYWORDS_REX.split(val);
        } else if (key.equals("pages")){
            values = new String[]{WHITESPACE_REX.matcher(val).replaceAll("")};
        }  else {
            values = new String[]{val};
        }
        for(int i=0, stop = values.length; i< stop; i++){
            Element bibnode = new Element(key, BIB_NAMESPACE);
            bibnode.setText(decoder.decode(values[i]));
            entrysub.addContent(bibnode);
        }
        if(etal){
            Element bibnode = new Element(key, BIB_NAMESPACE);
            bibnode.addContent(new Element("others", BIB_NAMESPACE));
            entrysub.addContent(bibnode);
        }
    }

    public void inAPrebracePreambleEntry(APrebracePreambleEntry node) {
        value = new StringBuilder();
    }

    public void outAPrebracePreambleEntry(APrebracePreambleEntry node) {
        makePreambleNode(value.toString());
        value = null;
    }

    public void inAPreparenPreambleEntry(APreparenPreambleEntry node) {
        value = new StringBuilder();
    }

    public void outAPreparenPreambleEntry(APreparenPreambleEntry node) {
        makePreambleNode(value.toString());
        value = null;
    }

    private void makePreambleNode(String text){
        Element preamble = new Element("preamble", BIB_NAMESPACE);
        preamble.setText(text);
        root.addContent(preamble);
    }


    public void outAValueValOrSid(AValueValOrSid node) {
        value.append(node.getStringLiteral().getText());
    }

    public void outANumValOrSid(ANumValOrSid node) {
        value.append(node.getNumber().getText());
    }

    public void outAIdValOrSid(AIdValOrSid node) {
        String nodeText = node.getIdentifier().getText();
        if(getAbbrevMap().containsKey(nodeText)){
            nodeText = getAbbrevMap().get(nodeText).info;
        }
        value.append(nodeText);
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
