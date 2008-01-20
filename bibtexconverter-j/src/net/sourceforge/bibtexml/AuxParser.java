package net.sourceforge.bibtexml;
/*
 * $Id: BibTeXConverter.java 336 2007-08-27 15:19:48Z ringler $
 * (c) Moritz Ringler, 2006

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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

public class AuxParser {
    private List<String> citations = new ArrayList<String>();

    public AuxParser(){
        //default constructor
    }

    public void reset(){
        citations.clear();
    }

    public void parse(BufferedReader reader) throws IOException{
        Matcher m = Pattern.compile("\\s*\\\\citation\\{(.*)\\}\\s*").matcher("");
        for(String line = reader.readLine();
            line != null;
            line = reader.readLine())
        {
            if(m.reset(line).matches()){
                final String[] elements = m.group(1).split(",");
                for(String citation : elements){
                    final String ct = citation.trim();
                    if(!citations.contains(ct)){//report only first citation
                        citations.add(ct);
                    }
                }
            }
        }
    }

    public String[] getCitations(){
        return citations.toArray(new String[citations.size()]);
    }

    public static String[] xsltParse(String auxfile, String encoding){
        String[] result = null;
        try{
            result = parse(auxfile, encoding);
        } catch (MalformedURLException x){
            System.err.println("Cannot open aux file " + auxfile);
        } catch (IOException x){
            System.err.println("Error reading from  " + auxfile);
            System.err.println(x);
        } catch (Exception x){
            System.err.println(x);
        }
        return result;
    }

    public static String[] parse(String auxfile, String encoding) throws IOException,
            UnsupportedEncodingException, MalformedURLException{
        String[] result = null;
        if (auxfile != null){
            String url = auxfile;
            //if auxfile is a file path, convert to URL
            try{
                File f = new File(auxfile);
                if(f != null && f.isFile()){
                    url = f.toURI().toURL().toString();
                }
            } catch (Exception ignore){
            }
            //open auxfile for reading
            BufferedReader reader = new BufferedReader(
                new InputStreamReader((new URL(url)).openStream(),
                    encoding));
            try{
                AuxParser parser = new AuxParser();
                parser.parse(reader);
                result = parser.getCitations();
            } finally {
                reader.close();
            }
        }
        return result;
    }

    public static void main(String[] argv) throws Exception{
        for(String arg : argv){
            String url = (new File(arg)).toURI().toURL().toString();
            System.out.println(Arrays.asList(parse(url, "iso-8859-1")));
        }
    }
}