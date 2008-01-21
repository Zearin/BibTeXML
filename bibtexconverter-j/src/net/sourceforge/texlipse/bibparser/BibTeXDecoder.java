/*
 * $Id: BibXMLCreator.java 389 2007-12-23 12:22:48Z ringler $
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

public class BibTeXDecoder{
    private static final char[][] ACCENTED = new char[][]{
        "\u00e4\u00eb\u00ef\u00f6\u00fc\u00c4\u00cb\u00cf\u00d6\u00dc\u00ff".toCharArray(),
        "\u00e1\u00e9\u00ed\u00f3\u00fa\u00c1\u00c9\u00cd\u00d3\u00da\u00fd\u00dd".toCharArray(),
        "\u00e0\u00e8\u00ec\u00f2\u00f9\u00c0\u00c8\u00cc\u00d2\u00d9".toCharArray(),
        "\u00e2\u00ea\u00ee\u00f4\u00fb\u00c2\u00ca\u00ce\u00d4\u00db".toCharArray(),
        "\u00e3\u00f1\u00f5\u00c3\u00d1\u00d5".toCharArray()};

    private static final char[] ACCENTS = "\"'`^".toCharArray();

    private static final char[] VOWELS = "aeiouAEIOUyY".toCharArray();

    private static final char[] TILDE_CHARS = "anoANO".toCharArray();

    private static final Pattern PACCENTS =
        Pattern.compile("\\\\([\"'`^])" +
            "(" +
                "(?:\\{[aeiouAEIOUyY]\\})" +
                "|" +
                "[aeiouAEIOUyY]" +
            ")"
            );

    private static final Pattern PTILDE =
        Pattern.compile("\\\\~" +
            "(" +
                "(?:\\{[anoANO]\\})" +
                "|" +
                "[anoANO]" +
            ")");

    public String decode(String latex){
        String result = replaceAccents(latex);
        result = replacements(result);
        return result;
    }

    /** Replaces LaTeX accented characters in the input String with
        their unicode equivalents.
        @param txt the input string
        @return the decoded input string
     **/
    protected String replaceAccents(String txt){
        String text = txt;

        //Accents \u00e4\u00e1\u00e0\u00e2
        Matcher m = PACCENTS.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final String g2 = m.group(2);
            final char vwc = (g2.length() == 1)? g2.charAt(0) : g2.charAt(1);
            final int vowel = indexOf(VOWELS, vwc);
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
            final String g2 = m.group(1);
            final char tc = (g2.length() == 1)? g2.charAt(0) : g2.charAt(1);
            final int tchar = indexOf(TILDE_CHARS, tc);
            final String repl =
                (tchar == -1)
                ? m.group(0)
                : String.valueOf(ACCENTED[ACCENTS.length][tchar]);
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        text = sb.toString();

        //Cedille
        text = text.replaceAll("\\\\c\\{(c)\\}","\u00e7");
        text = text.replaceAll("\\\\c\\{(C)\\}","\u00c7");

        return text;
    }

    /** Normalizes whitespace in the string argument;
    decodes LaTeX ~ and --,
    and removes remaining braces (except in entries that enclosed in
    double braces).
    @Return the processed string
    **/
    private String replacements(String txt){
        String text = txt.replaceAll("\\s+", " ");
        text = text.replaceAll("~", "\u00A0");
        text = text.replaceAll("--", "-");
        if(!text.startsWith("{")){
            text = text.replaceAll("[\\{\\}]","");
        }
        return text;
    }

    private static final int indexOf(char[] cc, char c){
        int i = cc.length - 1;
        for(; i != -1; i--){
            if(cc[i] == c){
                break;
            }
        }
        return i;
    }
}
