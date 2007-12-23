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

    /** Normalizes whitespace in the string argument; encodes the XML
        special characters &, >, and <; decodes LaTeX ~ and --,
        decodes LaTeX accented characters, and removes
        remaining braces (except in entries that enclosed in double braces).
        @Return the processed string
    **/
    private static String replacements(String txt){
        String text = txt.replaceAll("\\s+", " ");
        text = replaceAccents(text);
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll("~", "\u00A0");
        text = text.replaceAll("--", "-");
        if(!text.startsWith("{")){
            text = text.replaceAll("[\\{\\}]","");
        }
        return text;
    }

    private static final char[][] ACCENTED = new char[][]{
        "\u00e4\u00eb\u00ef\u00f6\u00fc\u00c4\u00cb\u00cf\u00d6\u00dc\u00ff".toCharArray(),
        "\u00e1\u00e9\u00ed\u00f3\u00fa\u00c1\u00c9\u00cd\u00d3\u00da\u00fd\u00dd".toCharArray(),
        "\u00e0\u00e8\u00ec\u00f2\u00f9\u00c0\u00c8\u00cc\u00d2\u00d9".toCharArray(),
        "\u00e2\u00ea\u00ee\u00f4\u00fb\u00c2\u00ca\u00ce\u00d4\u00db".toCharArray(),
        "\u00e3\u00f1\u00f5\u00c3\u00d1\u00d5".toCharArray()};
    private static final char[] ACCENTS = "\"'`^".toCharArray();
    private static final char[] VOWELS = "aeiouAEIOUyY".toCharArray();
    private static final char[] TILDE_CHARS = "anoANO".toCharArray();
    private static final int indexOf(char[] cc, char c){
        int i = cc.length - 1;
        for(; i != -1; i--){
            if(cc[i] == c){
                break;
            }
        }
        return i;
    }
    private static final Pattern PACCENTS =
        Pattern.compile("\\\\([\"'`^])\\{([aeiouAEIOUyY])\\}");
    private static final Pattern PTILDE =
        Pattern.compile("\\\\~\\{([anoANO])\\}");
    private static String replaceAccents(String txt){
        String text = txt;

        //Accents \u00e4\u00e1\u00e0\u00e2
        Matcher m = PACCENTS.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final int vowel = indexOf(VOWELS, m.group(2).charAt(0));
            final int accent = indexOf(ACCENTS, m.group(1).charAt(0));
            final String repl =
                (vowel == -1 || accent == -1 || ACCENTED[accent].length <= vowel )
                ? m.group(0)
                : String.valueOf(ACCENTED[accent][vowel]);
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        text = sb.toString();

        //Tilde
        m = PTILDE.matcher(text);
        sb.setLength(0);
        while (m.find()) {
            final int tchar = indexOf(TILDE_CHARS, m.group(1).charAt(0));
            final String repl =
                (tchar == -1)
                ? m.group(0)
                : String.valueOf(ACCENTED[ACCENTS.length + 1][tchar]);
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        text = sb.toString();

        //Cedille
        text = text.replaceAll("\\\\c\\{(c)\\}","\u00e7");
        text = text.replaceAll("\\\\c\\{(C)\\}","\u00c7");

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
        makeNodes(node.getIdentifier().getText().toLowerCase(),
                  value.toString());
        value = null;
    }

    private void makeNodes(String key, String val){
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
            bibnode.setText(replacements(values[i]));
            entrysub.addContent(bibnode);
        }
        if(etal){
            Element bibnode = new Element(key, BIB_NAMESPACE);
            bibnode.addContent(new Element("others", BIB_NAMESPACE));
            entrysub.addContent(bibnode);
        }
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
        value.append(node.getStringLiteral().getText());
    }

    public void inANumValOrSid(ANumValOrSid node) {
        //do nothing
    }

    public void outANumValOrSid(ANumValOrSid node) {
        value.append(node.getNumber().getText());
    }

    public void inAIdValOrSid(AIdValOrSid node) {
        //do nothing
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
