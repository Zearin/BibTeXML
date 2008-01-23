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
import java.nio.charset.Charset;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/** Retrieves citations from LaTeX aux files. Dependent aux files referenced
    with \@input are automatically included in the search.**/
public class AuxParser {
    private final List<String> citations = new ArrayList<String>();
    private final Set<URL> urlCache = new HashSet<URL>();
    private final URL baseurl;
    private final Charset charset;

    public AuxParser(URL base, Charset chars){
        baseurl = base;
        charset = chars;
    }

    public void reset(){
        citations.clear();
        urlCache.clear();
    }

    public String[] getCitations(){
        return citations.toArray(new String[citations.size()]);
    }

    public void parse(BufferedReader reader) throws IOException, MalformedURLException{
        final Matcher mCite =
            Pattern.compile("\\s*\\\\citation\\{(.*)\\}\\s*").matcher("");
        final Matcher mInclude =
            Pattern.compile("\\s*\\\\@input\\{(.*)\\}\\s*").matcher("");
        for(String line = reader.readLine();
            line != null;
            line = reader.readLine())
        {
            if(mCite.reset(line).matches()){
                final String[] elements = mCite.group(1).split(",");
                for(String citation : elements){
                    final String ct = citation.trim();
                    if(!citations.contains(ct)){//report only first citation
                        citations.add(ct);
                    }
                }
            } else if (mInclude.reset(line).matches()){
                parse(asURL(baseurl, mInclude.group(1).trim()));
            }
        }
    }

    public void parse(URL url) throws IOException{
        //we try to avoid infinite recursion by checking if
        //we have already accessed the specified url
        if(!urlCache.contains(url)){
            urlCache.add(url);
            final BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), charset));
            try{
                parse(reader);
            } finally {
                reader.close();
            }
        }
    }

    private static URL asURL(URL url, String path) throws MalformedURLException{
        URL result = null;
        try{
            final File f = new File(path);
            if(f != null && f.isFile()){
                result = f.toURI().toURL();
            }
        } catch (Exception ignore){
        }
        if(result == null){
            if(url == null){
                result = new URL(path);
            } else {
                result = new URL(url, path);
            }
        }
        return result;
    }

    public static String[] parse(URL auxfile, Charset encoding)
            throws IOException, MalformedURLException{
        String[] result = null;
        if (auxfile != null){
            final AuxParser parser = new AuxParser(auxfile, encoding);
            parser.parse(auxfile);
            result = parser.getCitations();
        }
        return result;
    }

    /** This method is meant to be used from an XSLT stylesheet. It tries
        to construct an URL from auxfile and a charset from encoding
        and then calls {@link parse(URL, Charset)}. Errors are handled
        by writing a message to the standard error stream. Java applications
        should use {@link parse(URL, Charset)} instead.
        @deprecated This method should not be used by Java applications
     **/
    public static String[] xsltParse(String auxfile, String encoding){
        String[] result = null;
        try{
            result = parse(asURL(null, auxfile), Charset.forName(encoding));
        } catch (MalformedURLException x){
            System.err.println("Cannot open aux file " + x);
        } catch (IOException x){
            System.err.println("Cannot read file");
            System.err.println(x);
        } catch (Exception x){
            System.err.println(x);
        }
        return result;
    }

    public static void main(String[] argv){
        for(String arg : argv){
            System.out.println(Arrays.asList(xsltParse(arg, "iso-8859-1")));
        }
    }
}