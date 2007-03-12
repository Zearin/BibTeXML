package net.sourceforge.bibtexml;
/*
 * $Id: Bib2BibXML.java,v 1.6 2006/09/28 11:38:41 Moritz.Ringler Exp $
 * This is a Java port of the GPLed python script bibtex2xml.py
 * from http://bibtexml.sf.net
 * (see below)
 * We're incompatible in the case of multiple authors. See javadoc
 * of bibtexAuthor below
 *
 * Copyright for the Java port:
 * (c) Moritz Ringler, 2006
 *
 * Known bugs in addition to those below:
 * might stumble on @ characters in field values
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

 /* ++Python source file header++
 Decoder for bibliographic data, BibTeX
  Usage: python bibtex2xml.py bibfile.bib > bibfile.xml

  (c) Vidar Bronken Gundersen, Sara Sprenkle
  http://bibtexml.sourceforge.net/
  Reuse approved as long as this notification is kept.
  License: http://creativecommons.org/licenses/GPL/2.0/

  Contributions/thanks to:
  Thomas Karl Schwaerzler, read stdin
  Egon Willighagen, http://jreferences.sf.net/
  Richard Mahoney, for providing a test case

  This is Sara Sprenkle's rewrite of our original script, which
  is changed to be more robust and handle more bibtex features:
  3.  Allow spaces between @type and first {
  4.  'author' fields with multiple authors split by ' and '
      are put in separate xml 'bibtex:person' tags.
  5.  Option for Titles: words are capitalized
      only if first letter in title or capitalized inside braces
  6.  Removes braces from within field values
  7.  Ignores comments in bibtex file (including @comment{ or % )
  8.  Replaces some special latex tags, e.g., replaces ~ with '&#160;'
  9.  Handles bibtex @string abbreviations
        --> includes bibtex's default abbreviations for months
        --> does concatenation of abbr # ' more ' and ' more ' # abbr
  10. Handles @type( ... ) or @type{ ... }
  11. The keywords field is split on , or ; and put into
      separate xml 'bibtex:keywords' tags
  12. Ignores @preamble

  replace ':' with '-' for bibtex:entry@id: unique-ids cannot contain ':'

  Known Limitations
  1.  Does not transform Latex encoding like math mode
         and special latex symbols.
  2.  Does not parse author fields into first and last names.
      E.g., It does not do anything special to an author whose name is
      in the form LAST_NAME, FIRST_NAME In'author' tag, will show up as
      <bibtex:author>LAST_NAME, FIRST_NAME</bibtex:author>
  3.  Does not handle 'crossref' fields other than to print
      <bibtex:crossref>...</bibtex:crossref>
  4.  Does not inform user of the input's format errors.
       You just won't be able to transform the file later with XSL
       Create error.log file?

  5.  Special treatment of
      howpublished = '\\url{http://www.cs.duke.edu/ari/crisp/}',

  6. document functions with docstrings

  You will have to manually edit the XML output if you need to handle
  these (and unknown) limitations.
  */

import java.io.IOException;
import java.io.*;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Bib2BibXML extends AbstractBibTeXParser{
    /** set of valid name characters **/
    protected final static String VALID_NAME_CHARS = "[\\w\\-:]";

    /* define global regular expression variables */
    protected final static Pattern AUTHOR_REX = Pattern.compile("\\s+and\\s+");
    protected final static Pattern REMBRACES_REX = Pattern.compile("[\\{\\}]");
    protected final static Pattern CAPITALIZE_REX = Pattern.compile("\\{\\w*\\}");

    /** used by bibtexkeywords(data) **/
    protected final static Pattern KEYWORDS_REX = Pattern.compile("[,;]");

    /* used by concat_line(line) */
    protected final static Pattern CONCATSPLIT_REX = Pattern.compile("\\s*#\\s*");

    /* split on {, }, or " in verify_out_of_braces */
    protected final static Pattern DELIMITER_REX = Pattern.compile("(?i)[\\{\\}\"]");

    protected final static Pattern FIELD_REX = Pattern.compile("\\s*(\\w*)\\s*=\\s*(.*)");
    protected final static Pattern DATA_REX = Pattern.compile("\\s*(\\w*)\\s*=\\s*([^,]*),?");

    public Bib2BibXML(String charset)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(charset);
    }

    public Bib2BibXML(String inputChars, String outputChars)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(inputChars, outputChars);
    }

    public Bib2BibXML(String inputChars, String outputChars, boolean cleanInput)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(inputChars, outputChars, cleanInput);
    }

    /** Returns the string parameter without braces **/
    private String removeBraces(String str){
        return REMBRACES_REX.matcher(str).replaceAll("");
    }

    private static String lstrip(String in){
        return in.replaceFirst("^\\s+","");
    }

    private String makeXMLList(String data, String tag, Pattern p){
        StringBuilder bibtex = new StringBuilder(data.length() + 50);
        String[] list = p.split(data);
        for(String element : list){
            bibtex.append("<bibtex:").append(tag).append(">");
            bibtex.append(removeBraces(element.trim()).trim());
            bibtex.append("</bibtex:").append(tag).append(">\n");
        }
        return bibtex.toString().trim();
    }

    private String makeXMLTag(String data, String tag){
        return "<bibtex:" + tag + ">" +
               removeBraces(data).trim() +
               "</bibtex:" + tag + ">";
    }

    /** Fixes author so that it creates multiple authors, split by "and"
    * the implementation here is different from that in bibtex2xml.py
    * the python script wraps multiple (but not single!) authors in
    * person tags. We stick with Sara Sprenkle's (in my eyes wiser)
    * choice of simply creating multiple author tags.
    * We output lastname, firstname. An xsl stylesheet can easily
    * convert this to firstname lastname using tokenize().
    **/
    private String bibtexAuthor(String data){
        String[] list = AUTHOR_REX.split(data);
        StringBuilder bibtex = new StringBuilder();
        String[] authorparts;
        for(String element : list){
            bibtex.append("<bibtex:author>");
            element = removeBraces(element.trim()).trim();

            if(element.indexOf(',') == -1){
                authorparts = element.split("\\s+");

                if(authorparts.length < 2){
                    bibtex.append(element);

                } else {
                    final int n = authorparts.length - 1;
                    bibtex.append(authorparts[n]).append(", ");
                    for(int i=0; i< n - 1; i++){
                        bibtex.append(authorparts[i]).append(" ");
                    }
                    bibtex.append(authorparts[n - 1]);
                }
            } else {
                bibtex.append(element);
            }
            bibtex.append("</bibtex:author>\n");
        }
        return bibtex.toString().trim();
    }

    /** Creates title xml snippet. Braces are removed.
     * @param data the title string input
     * @return the bibtex for the title
     */
    private String bibtexTitle(String data){
        return makeXMLTag(data, "title");
    }

    /** Creates keywords xml snippet.
     * Keywords are assumed to be delimited by , or ;
     * @return the bibtex for the keyword
     */
    private String bibtexKeyword(String data){
        return makeXMLList(data, "keywords", KEYWORDS_REX);
    }

    /**
    * data = title string
    * @return the capitalized title (first letter is capitalized), rest are capitalized
    * only if capitalized inside braces
    **/
    private String capitalizeTitle(String data){
        StringBuilder title = new StringBuilder(data.length() + 50);
        String[] titleList = splitWithSeparators(data, CAPITALIZE_REX);

        int count = 0;
        for(String phrase : titleList){
            String check = lstrip(phrase);
            //keep phrase's capitalization the same
            if(check.indexOf('{') == 0){
                title.append(removeBraces(phrase));
            } else if(check.length() > 0){
                //first word --> capitalize first letter (after spaces)
                if (count == 0){
                    title.append(Character.toUpperCase(check.charAt(0)));
                    if(check.length() > 1){
                        title.append(check.substring(1));
                    }
                } else {
                    title.append(phrase.toLowerCase());
                }
            }
            count++;
        }

        return title.toString();
    }

    /**
     * Creates the XML for the transformed "filecontents"
     * @param filecontents will be edited in place to save memory
     **/
    public String[] bibtexDecoder(String[] filecontents){
        String endEntry = null;

        // want @<alphanumeric chars><spaces>{<spaces><any chars>,
        Pattern pubTypeRex = Pattern.compile("@(\\w*)\\s*\\{\\s*(.*),");
        Pattern endTypeRex = Pattern.compile("\\}\\s*$");
        Pattern endTagRex = Pattern.compile("^\\s*\\}\\s*$");

        //contains pairs of data and field patterns
        Pattern[][] patterns = new Pattern[3][2];

        final int _DATA = 0;
        final int _FIELD = 1;
        //Pattern braceFieldRex = Pattern.compile("\\s*(\\w*)\\s*=\\s*(.*)");
        //Pattern braceDataRex = Pattern.compile("\\s*(\\w*)\\s*=\\s*\\{(.*)\\},?");
        Pattern braceFieldRex = Pattern.compile("\\s*([^=\\s]*)\\s*=\\s*(.*)");
        Pattern braceDataRex = Pattern.compile("\\s*([^=\\s]*)\\s*=\\s*\\{(.*)\\},?");

        patterns[0][_DATA] = braceDataRex;
        patterns[0][_FIELD] = braceFieldRex;

        Pattern quoteFieldRex = Pattern.compile("\\s*(\\w*)\\s*=\\s*(.*)");
        Pattern quoteDataRex = Pattern.compile("\\s*(\\w*)\\s*=\\s*\"(.*)\",?");
        patterns[1][_DATA] = quoteDataRex;
        patterns[1][_FIELD] = quoteFieldRex;

        patterns[2][_DATA] = DATA_REX;
        patterns[2][_FIELD] = FIELD_REX;

        for (int i = 0; i<filecontents.length; i++){
            String line = filecontents[i];
            //why do we need this?
            line = line.substring(0, line.length() - 1);

            // encode character entities
            line = line.replaceAll("&", "&amp;");
            line = line.replaceAll("<", "&lt;");
            line = line.replaceAll(">", "&gt;");

            // start item: publication type (store for later use)
            Matcher m = pubTypeRex.matcher(line);
            if (m.lookingAt()){
                // want @<alphanumeric chars><spaces>{<spaces><any chars>,
                String arttype = m.group(1).toLowerCase();
                String artid   = m.group(2).replace(":","-");
                endEntry = "</bibtex:" + arttype + ">" + "\n</bibtex:entry>\n";
                line = "<bibtex:entry id=\"" + artid + "\">\n" +
                       "<bibtex:" + arttype + ">";
            }

            m = endTypeRex.matcher(line);
            if(m.lookingAt()){
                // end entry if just a }
                line = endTagRex.matcher(line).replaceAll(endEntry);
                endEntry = null;
                //this will cause a NullPointerException if the endTypeRex
                //matches again before the pubTypeRex
            }

            String field = "";
            String data = "";
            for(Pattern[] rex : patterns){
                m = rex[_DATA].matcher(line);
                if(m.lookingAt()){
                    data =  m.group(2);

                    //match the field rex
                    m = rex[_FIELD].matcher(line);
                    if(m.find()){
                        field = m.group(1).toLowerCase();
                    }
                    break;
                }
            }


            if (field.equals("title")){
                line = bibtexTitle(data);
            } else if(field.equals("author")){
                line = bibtexAuthor(data);
            } else if (field.equals("keywords")){
                line = bibtexKeyword(data);
            } else if (field.equals("refgroup")){
                line = makeXMLList(data, "refgroup", KEYWORDS_REX);
            } else if (!field.equals("")){
                data = removeBraces(data).trim();
                if (!data.equals("")){
                    line = makeXMLTag(data, field);
                } else {
                    // get rid of the field={} type stuff
                    line = "";
                }
            }

            if (!line.equals("")){
                // latex-specific replacements
                // do this now after braces were removed
                line = line.replaceAll("~", "&#160;");
                line = line.replaceAll("\\'a", "&#225;"); // matches \'a
                line = line.replaceAll("\\\\\"a", "&#228;"); //matches  \"a
                line = line.replaceAll("\\\\\"c", "&#263;"); //matches \"c
                line = line.replaceAll("\\\\\"o", "&#246;"); //matches \"o
                line = line.replaceAll("\\\\o", "&#248;"); //matches \"o
                line = line.replaceAll("\\\\\"u", "&#252;"); //matches \"u
                /* The following line will cause trouble when an XSL transform
                 * wants to write ISO-8859-1 plain text */
                //line = line.replaceAll("---", "&#x2014;");
                line = line.replaceAll("--", "-");
            }

            filecontents[i] = line;
        }

        return filecontents;
    }

    /** return true iff abbr is in line but not inside braces or quotes
     * assumes that abbr appears only once on the line (out of braces and quotes)
     */
    private boolean verifyOutOfBraces(String line, String abbr){
        String[] phraseSplit = splitWithSeparators(line, DELIMITER_REX);

        Pattern abbrRex = Pattern.compile( "(?i)\\b" + abbr + "\\b");
        int openBrace = 0;
        int openQuote = 0;

        for (String phrase : phraseSplit){
            if (phrase.equals("{")){
                openBrace++;
            } else if (phrase.equals("}")){
                openBrace--;
            } else if (phrase.equals("\"")){
                openQuote = (openQuote == 1)? 0 : 1;
            } else if (abbrRex.matcher(phrase).find()){
                if(openBrace == 0 && openQuote == 0){
                    return true;
                }
            }
        }
        return false;
    }

    /**
    # a line in the form phrase1 # phrase2 # ... # phrasen
    # is returned as phrase1 phrase2 ... phrasen
    # with the correct punctuation
    # Bug: Doesn't always work with multiple abbreviations plugged in
    **/
    private String concatLine(String line){
        // only look at part after equals
        String field = "";
        String rest = "";
        Matcher m = FIELD_REX.matcher(line);
        if(m.find()){
            field = m.group(1);
            rest = m.group(2);
        }

        StringBuilder concatLine = new StringBuilder(line.length() * 2);
        concatLine.append(field).append(" =");

        String[] poundSplit = CONCATSPLIT_REX.split(rest);

        int phraseCount = 0;
        int length = poundSplit.length;

        for (String phrase : poundSplit){
            phrase = phrase.trim();

            if (phraseCount != 0){
                if(phrase.startsWith("\"") || phrase.startsWith("{")){
                    phrase = (phrase.length() > 1)? phrase.substring(1) : "";
                }
            } else if (phrase.startsWith("\"")){
                phrase = phrase.replaceFirst("\"","{");
            }

            if (phraseCount == length - 1){
                if (phrase.endsWith("\"") || phrase.endsWith("{")){
                    phrase = phrase.substring(0, phrase.length() - 1);
                }
            } else {
                if (phrase.endsWith("\"")){
                    phrase = phrase.substring(0, phrase.length() - 1) + "}";
                } else if (phrase.endsWith("\",")){
                    phrase = phrase.substring(0, phrase.length() - 2) + "}";
                }
            }

            // if phrase did have \#, add the \# back
            if (phrase.endsWith("\\")){
                phrase = phrase + "#";
            }
            concatLine.append(' ').append(phrase);
            phraseCount++;
        }

        return concatLine.toString();
    }

    //bibtexReplaceAbbreviations
    /**
    # substitute abbreviations into filecontents
    # @param filecontents - lines of data from file, will be modified by this method
    **/
    public String bibtexReplaceAbbreviations(String[] filecontents){
        Map<String, String> abbrList = new TreeMap<String, String>();
        Map<String, Pattern> abbrRex = new TreeMap<String, Pattern>();
        abbrList.put("jan", "January");
        abbrList.put("feb", "February");
        abbrList.put("mar", "March");
        abbrList.put("apr", "April");
        abbrList.put("may", "May");
        abbrList.put("jun", "June");
        abbrList.put("jul", "July");
        abbrList.put("aug", "August");
        abbrList.put("sep", "September");
        abbrList.put("oct", "October");
        abbrList.put("nov", "November");
        abbrList.put("dec", "December");

        String front = "(?i)\\b";
        String back = "(,?)\\b";

        for (String x : abbrList.keySet()){
            abbrRex.put(x, Pattern.compile( front + x + back) );
        }

        Pattern abbrDefRex = Pattern.compile(
                "(?i)\\s*@string\\s*\\{\\s*(" +
                VALID_NAME_CHARS +
                "*)\\s*=(.*)");

        Pattern commentRex = Pattern.compile("(?i)@comment\\s*\\{");
        Pattern preambleRex = Pattern.compile("(?i)@preamble\\s*\\{");

        Matcher m = null;

        boolean waitingForEndString = false;

        StringBuilder fc2 = new StringBuilder(filecontents.length * 80);
        for (String line : filecontents){
            if (line.equals(" ") || line.equals("")){
                continue;
            }

            if (waitingForEndString){
                if ( line.indexOf('}') >= 0 ){
                    waitingForEndString = false;
                    continue;
                }
            }

            m = abbrDefRex.matcher(line);
            if (m.find()){
                String abbr = "";
                abbr = m.group(1);
                if (!abbrList.keySet().contains(abbr)){
                    abbrList.put(abbr, m.group(2).trim());
                    abbrRex.put(abbr, Pattern.compile( front + abbr + back));
                }
                waitingForEndString = true;
                continue;
            }

            if ( (commentRex.matcher(line).find()) ||
                 (preambleRex.matcher(line).find()) ){
                waitingForEndString = true;
                continue;
            }


            //# replace subsequent abbreviations with the value
            for (String abbr : abbrList.keySet()){
                m = abbrRex.get(abbr).matcher(line);
                if (m.find()){
                    if (verifyOutOfBraces(line, abbr)){
                        line = abbrList.get(abbr) + m.group(1);
                        // Check for # concatenations
                        if (CONCATSPLIT_REX.matcher(line).find()){
                            line = concatLine(line);
                        }
                    }
                }
            }

            //# make sure that didn"t end up with {" or }" after the substitution
            line = line.replaceAll("\\{\"","{{");
            line = line.replaceAll("\"}","}}");

            fc2.append(line).append("\n");
        }
        String filecontents2 = fc2.toString();

        //# Do one final pass over file
        Pattern afterQuoteValueRex = Pattern.compile("\"\\s*,\\s*");
        Pattern afterBraceRex = Pattern.compile("\"\\s*\\}");
        Pattern afterBraceValueRex = Pattern.compile("(=\\s*\\{[^=]*)\\},\\s*");

        // add new lines to data that changed because of abbreviation substitutions
        filecontents2 = afterQuoteValueRex.matcher(filecontents2).replaceAll("\",\n");
        filecontents2 = afterBraceRex.matcher(filecontents2).replaceAll("\"\n\\}");
        filecontents2 = afterBraceValueRex.matcher(filecontents2).replaceAll("$1\\},\n");
        return filecontents2;
    }

    private String[] splitWithSeparators(String input, Pattern regex){
        Matcher m = regex.matcher(input);
        int start = 0;
        int end = 0;
        String element;
        List<String> result = new Vector<String>(50);
        while(m.find()){
            //add non-matching part
            end = m.start();
            if(end > start){
                result.add(input.substring(start, end));
            }
            //add match
            start = end;
            end = m.end();
            if(end > start){
                result.add(input.substring(start, end));
            }
            start = end;
        }
        //add tail
        end = input.length();
        if(end > start){
            result.add(input.substring(start, end));
        }
        return result.toArray(new String[0]);
    }

    private String[] splitWithSeparators(String input, String regex){
        return splitWithSeparators(input, Pattern.compile(regex));
    }

    //noOuterParens
    //
    // convert @type( ... ) to @type{ ... }
    //
    private String noOuterParens(String input){
        // do checking for open parens
        // will convert to braces
        String[] parenSplit = splitWithSeparators(input, "[()\\{\\}]");
        int openParenCount = 0;
        int openType = 0;
        int lookNext = 0;

        // rebuild filecontents
        StringBuilder fc = new StringBuilder(input.length());

        Matcher atRex = Pattern.compile("@\\w*").matcher("");

        for (String phrase : parenSplit){
            if (lookNext == 1){
                if (phrase.equals("(")){
                    phrase = "{";
                    openParenCount++;
                } else {
                    openType = 0;
                    lookNext = 0;
                }
            }

            if (phrase.equals("(")){
                openParenCount++;
            } else if (phrase.equals(")")){
                openParenCount--;
                if (openType == 1 && openParenCount == 0){
                    phrase = "}";
                    openType = 0;
                }
            } else if (atRex.reset( phrase ).find()){
                openType = 1;
                lookNext = 1;
            }
            fc.append(phrase);
        }
        return fc.toString();
    }


    //bibtexWasher
    /** make all whitespace into just one space
    * format the bibtex file into a usable form.
    */
    private String[] bibtexWasher(List<String> filecontentsSource){

        Pattern spaceRex = Pattern.compile("\\s+");
        Pattern commentRex = Pattern.compile("\\s*%");

        StringBuilder fc = new StringBuilder(1023);

        // remove trailing and excessive whitespace
        // ignore comments
        for (String line : filecontentsSource){
            line = line.trim();
            line = spaceRex.matcher(line).replaceAll(" ");
            // ignore comments
            if (!commentRex.matcher(line).lookingAt()){
                fc.append(" ").append(line);
            }
        }

        String filecontents = fc.toString();
        fc = null;
        // the file is in one long string

        filecontents = this.noOuterParens(filecontents);
        //if(filecontents != null){
          //  return filecontents.split("\n");
        //}


        //
        // split lines according to preferred syntax scheme
        //
        //when an '=' sign is found we look for the following brace
        //everything that is not another '='
        //and ends with '},'. After matches add a newline.
        /* BUG: This get's us into trouble when the field value contains a = sign
         * instead we should count braces
         */
        filecontents = filecontents.replaceAll("(=\\s*\\{[^=]*)\\},", "$0\n");

        // add new lines after commas that are after values
        // '",' is transformed to '",\n'
        filecontents = filecontents.replaceAll("\"\\s*,", "\",\n");
        // '= numsandwordchars', is transformed to ' = numsandwordchars,\n'
        filecontents = filecontents.replaceAll("=\\s*([\\w\\d]+)\\s*,", "= $1,\n");
        // '@ wordchars { nocommaorspace' , is transformed to '\n\n@wordchars{ nocommaorspace,\n'
        filecontents = filecontents.replaceAll("(@\\w*)\\s*(\\{(\\s*)[^,\\s]*)\\s*,", "\n\n$1$2,\n");

        // add new lines after }
        filecontents = filecontents.replaceAll("\"\\s*\\}", "\"\n}\n");
        filecontents = filecontents.replaceAll("\\}\\s*,", "},\n");


        filecontents = filecontents.replaceAll("@(\\w*)", "\n@$1");

        // character encoding, reserved latex characters
        filecontents = filecontents.replaceAll("\\{\\&\\}", "&"); // matches {\&}
        filecontents = filecontents.replaceAll("\\&", "&"); // matches \&


        // do checking for open braces to get format correct
        int openBraceCount = 0;
        String[] split = splitWithSeparators(filecontents, "[\\{\\}]");
        filecontents = null;
        // rebuild filecontents
        fc = new StringBuilder(1024);
        for (String phrase : split){
            //System.out.println(phrase);
            if (phrase.equals("{")){
                openBraceCount++;
            } else if (phrase.equals("}")){
                openBraceCount--;
                if (openBraceCount == 0){
                    fc.append("\n");
                }
            }
            fc.append(phrase);
        }
        //System.out.println(fc);

        Pattern lineEndRex = Pattern.compile("\n");
        split = lineEndRex.split(fc);
        fc = null;

        split = lineEndRex.split(bibtexReplaceAbbreviations(split));
        //for (String a : split){
            //System.out.println(a);
        //}

        // gather

        String line = null;
        int istore = 0;
        for (int iread=0; iread<split.length; iread++){
            line = split[iread];
            // ignore blank lines
            if (!(line.equals("") || line.equals(" "))){
                split[istore++] = line + "\n";
            }
         }

         /*
        # get rid of the extra stuff at the end of the array
        # (The extra stuff are duplicates that are in the array because
        # blank lines were removed.)
        */
        String[] result = new String[istore];
        System.arraycopy(split,0,result,0,istore);

        return result;
    }

    //processFile


    /** does not close stream */
    protected String[] translateBibTeXStream(BufferedReader reader) throws IOException{
        List<String> lines = new Vector<String>(500);
        String line = reader.readLine();
        while(line != null){
            lines.add(line);
            line = reader.readLine();
        }
        String[] data = this.bibtexWasher(lines);
        //for(String lne : data){
            //System.out.println(lne);
        //}
        return this.bibtexDecoder(data);
    }

}