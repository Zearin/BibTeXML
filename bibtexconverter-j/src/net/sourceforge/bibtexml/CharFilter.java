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

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.util.*;

public class CharFilter extends FilterReader{
    private final String chars;
    private final List<int[]> forbidden = new ArrayList<int[]>();
    //private final Matcher htmlEscape = Pattern.compile("&#(\\d{1,3});").matcher("");
    private final Set<Integer> illegalsFound = new TreeSet<Integer>();
    private final Map<Character, Character> replaceMap = new TreeMap<Character, Character>();
    private final CharsetEncoder enc;
    private char repl = ' ';

    public CharFilter(Reader in, String charset){
        super(in);
        chars = charset;
        enc = Charset.forName(charset).newEncoder();
    }

    public int read() throws IOException{
        return (int) checkChar((char) in.read());
    }

    private void cleanupArray(final char[] cbuf, final int limit){
        Character cr;
        for(int i=0; i<limit; i++){
            if( isForbidden(cbuf[i]) ){
                cr = replaceMap.get(cbuf[i]);
                cbuf[i] = (cr== null)? repl : cbuf[i];
            }
        }
    }

    public int read(final char[] cbuf, final int off, final int len) throws IOException{
        final int result = in.read(cbuf, off, len);
        cleanupArray(cbuf, result);
        return result;
    }

    public int read(final char[] cbuf) throws IOException{
        final int result = in.read(cbuf);
        cleanupArray(cbuf, result);
        return result;
    }

    /** Not implemented. Always throws an UnsupportedOperationException.
    * @throws UnsupportedOperationException
    */
    public int read(final CharBuffer target) throws IOException{
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int cleanupFile(final File input, final File output){
        int lc = 0;
        BufferedReader br = null;
        PrintWriter bw = null;
        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream(input), chars));
            final File f = (output == null)
                ? new File(input.getAbsoluteFile().getParent(),"cleaned"+input.getName())
                : output;
            bw = new PrintWriter(f, chars);

            String line = br.readLine();
            for(lc=1; line != null; lc++){
                bw.println(cleanupString(line, lc));
                line = br.readLine();
            }

        } catch (Exception ex) {
            System.err.println("Cleanup of file" + input.toString() + " failed.");
            System.err.println(ex.getMessage());

        } finally{

            try{
                br.close();
            } catch(Exception ex) {
                System.err.println(ex);
                System.err.flush();
            }
            try{
                bw.close();
            } catch(Exception ex) {
                System.err.println(ex);
                System.err.flush();
            }
        }
        return lc;
    }

    public String cleanupString(final String line, final int lc){
        char ci;
        Character cr;
        String result = line;
        for(int i=0; i<result.length(); i++){
            ci = result.charAt(i);
            if( isForbidden(ci) ){
                System.out.print("Illegal character:"+(int) ci);
                System.out.println(" in line "+lc+ " column "+i);
                System.out.println(line);
                cr = replaceMap.get(ci);
                result = result.replace(ci, (cr== null)? repl : cr);
            }
        }
        return result;
    }

    public char checkChar(final char ci){
        if( isForbidden(ci) ){
            final Character cr = replaceMap.get(ci);
            return (cr== null)? repl : cr;
        }
        return ci;
    }

    public void addForbiddenRange(final int lower, final int upper){
        if(upper >= lower){
            forbidden.add(new int[]{lower, upper});
        } else {
            throw new IllegalArgumentException("upper < lower!");
        }
    }

    public Integer[] getIllegalsFound(){
        return illegalsFound.toArray(new Integer[illegalsFound.size()]);
    }

    public void clearIllegalsFound(){
        illegalsFound.clear();
    }

    private boolean isForbidden(final char c){
        if(! enc.canEncode(c)){
            return false;
        }
        for(int[] range : forbidden){
            if(c >= range[0] && c <= range[1]){
                illegalsFound.add((int) c);
                return true;
            }
        }
        return false;
    }

    public void setDefaultReplacement(final char c){
        repl = c;
    }

    public void setReplacement(final char replace,final char with){
        replaceMap.put(replace, with);
    }

    public void clearReplacements(){
        replaceMap.clear();
    }

    // private String resolveHtmlEscapes(String line){
        // StringBuffer sb = new StringBuffer();
        // boolean found = false;
        // String result = line;
        // htmlEscape.reset(line);
        // while (htmlEscape.find()) {
            // int i = -1;
            // try{
                // i = Integer.parseInt(htmlEscape.group(1));
            // } catch (NumberFormatException ex){
                // ex.printStackTrace();
            // }
            // if(i != -1){
                // htmlEscape.appendReplacement(sb,String.valueOf((char) i));
                // //System.out.println("replacing html escape "+ htmlEscape.group(0) + " by " + ((char) i));
                // found = true;
            // }
        // }
        // if (found) {
            // htmlEscape.appendTail(sb);
            // result = sb.toString();
        // }
        // return result;
    // }

    /** Convenience method for creating a CharFilter that suppresses all
    * characters that do not belong to the ISO-8859-1  charset.
    */
    public static CharFilter getIsoLatin1Reader(final Reader in){
        final CharFilter ric = new CharFilter(in, "ISO-8859-1");
        ric.addForbiddenRange(127, 159);
        ric.addForbiddenRange(0, 9);
        //10 is linefeed
        ric.addForbiddenRange(11, 31);
        return ric;
    }

    /** Reads the file specified as this methods first argument using an
    * isoLatinReader and prints the result to stdout. */
    public static void main(final String[] argv){
        final CharFilter ric = getIsoLatin1Reader(null);
        final int lc = ric.cleanupFile(new File(argv[0]), null);
        System.out.println("Number of lines processed: " + lc);
        System.out.println("Illegal characters found: " + Arrays.asList(ric.getIllegalsFound()).toString());
    }
}