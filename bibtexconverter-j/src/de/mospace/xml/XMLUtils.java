package de.mospace.xml;

import java.io.*;

/** Useful stuff for xml handling. From 
 * derived from org.apache.batik.xml.XMLUtilities
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 */
public class XMLUtils{
    /**
    * The bit array representing the valid XML version characters.
    */
    public static final int[] VERSION_CHARACTER = {
        0,134176768,-2013265922,134217726,
        };

    
    private XMLUtils(){
    }
    
    /**
     * Tests whether the given character is a valid space.
     */
    public static boolean isXMLSpace(char c) {
      return (c <= 0x0020) &&
             (((((1L << 0x0009) |
                 (1L << 0x000A) |
                 (1L << 0x000D) |
                 (1L << 0x0020)) >> c) & 1L) != 0);
    }
    
    /**
     * Tests whether the given character is a valid XML version character.
     */
    public static boolean isXMLVersionCharacter(char c) {
        return (c < 128) &&
            (VERSION_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    
    /**
     * Reads an XML declaration to get the encoding declaration value.
     * @param r a reader positioned just after '&lt;?xm'.
     * @param r a reader positioned at the start of an xml document
     * @param e the encoding to return by default or on error.
     */
    public static String getXMLDeclarationEncoding(Reader r, String e)
        throws IOException {
        int c;
        
        while (isXMLSpace((char)(c = r.read())));
        
        if (c != '<') {
            return e;
        }
        if ((c = r.read()) != '?') {
            return e;
        }
        if ((c = r.read()) != 'x') {
            return e;
        }
        if ((c = r.read()) != 'm') {
            return e;
        }
        if ((c = r.read()) != 'l') {
            return e;
        }

        if (!isXMLSpace((char)(c = r.read()))) {
            return e;
        }

        while (isXMLSpace((char)(c = r.read())));
            
        if (c != 'v') {
            return e;
        }
        if ((c = r.read()) != 'e') {
            return e;
        }
        if ((c = r.read()) != 'r') {
            return e;
        }
        if ((c = r.read()) != 's') {
            return e;
        }
        if ((c = r.read()) != 'i') {
            return e;
        }
        if ((c = r.read()) != 'o') {
            return e;
        }
        if ((c = r.read()) != 'n') {
            return e;
        }
             
        c = r.read();
        while (isXMLSpace((char)c)) {
            c = r.read();
        }

        if (c != '=') {
            return e;
        }

        while (isXMLSpace((char)(c = r.read())));
            
        if (c != '"' && c != '\'') {
            return e;
        }
        char sc = (char)c;

        for (;;) {
            c = r.read();
            if (c == sc) {
                break;
            }
            if (!isXMLVersionCharacter((char)c)) {
                return e;
            }
        }

        if (!isXMLSpace((char)(c = r.read()))) {
            return e;
        }
        while (isXMLSpace((char)(c = r.read())));

        if (c != 'e') {
            return e;
        }
        if ((c = r.read()) != 'n') {
            return e;
        }
        if ((c = r.read()) != 'c') {
            return e;
        }
        if ((c = r.read()) != 'o') {
            return e;
        }
        if ((c = r.read()) != 'd') {
            return e;
        }
        if ((c = r.read()) != 'i') {
            return e;
        }
        if ((c = r.read()) != 'n') {
            return e;
        }
        if ((c = r.read()) != 'g') {
            return e;
        }

        c = r.read();
        while (isXMLSpace((char)c)) {
            c = r.read();
        }

        if (c != '=') {
            return e;
        }

        while (isXMLSpace((char)(c = r.read())));
            
        if (c != '"' && c != '\'') {
            return e;
        }
        sc = (char)c;

        StringBuffer enc = new StringBuffer();
        for (;;) {
            c = r.read();
            if (c == -1) {
                return e;
            }
            if (c == sc) {
                return enc.toString();
            }
            enc.append((char)c);
        }
    }
}
