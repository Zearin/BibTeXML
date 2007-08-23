/*
 * $Id$
 *
 * This classed is based on BibParser
 * Copyright (c) 2004-2005 Oskar Ojala
 * in this package.
 *
 * Modifications:
 * Copyright (c) 2006 Moritz Ringler
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import net.sourceforge.texlipse.bibparser.lexer.LexerException;
import net.sourceforge.texlipse.bibparser.node.Start;
import net.sourceforge.texlipse.bibparser.parser.Parser;
import net.sourceforge.texlipse.bibparser.parser.ParserException;
import net.sourceforge.texlipse.model.ParseErrorMessage;
import net.sourceforge.texlipse.model.ReferenceEntry;
import org.jdom.Document;

//import org.eclipse.core.resources.IMarker;


/**
 * A BibTeX parser whose parse result can be retrieved as a BibTeXML tree.
 *
 * @author Oskar Ojala, Moritz Ringler
 */
public class Bib2JDomParser {
    private final Reader reader;

    private final ArrayList<ParseErrorMessage> errors = new ArrayList<ParseErrorMessage>();
    private Start ast;
    public static final int SEVERITY_ERROR = 2;

    /**
     * Constructs a new Bib2JDomParser and immediately starts parsing the
     * BibTeX from the supplied reader.
     *
     * @param r A reader to the BibTeX-data to parse
     */
    public Bib2JDomParser(Reader r) throws IOException{
        this.reader = r;
        try {
            BibLexer l;
            l = new BibLexer(new PushbackReader(reader, 1024));

            Parser p = new Parser(l);
            this.ast = p.parse();

        } catch (LexerException le) {
            String msg = le.getMessage();
            int first = msg.indexOf('[');
            int last = msg.indexOf(']');
            String numseq = msg.substring(first + 1, last);
            String[] numbers = numseq.split(",");
            this.errors.add(new ParseErrorMessage(Integer.parseInt(numbers[0]),
                    Integer.parseInt(numbers[1]) - 1,
                    2,
                    msg.substring(last+2),
                    /*IMarker.*/SEVERITY_ERROR));
        } catch (ParserException pe) {
            String msg = pe.getMessage();
            int last = msg.indexOf(']');
            this.errors.add(new ParseErrorMessage(pe.getToken().getLine(),
                    pe.getToken().getPos(),
                    pe.getToken().getText().length(),
                    msg.substring(last+2),
                    /*IMarker.*/SEVERITY_ERROR));
        }
    }


    /**
     * Constructs a list of the entries in the parsed bibtex file and returns
     * them.
     *
     * @return a list of BibTeX entries
     */
    public ArrayList<ReferenceEntry> getEntries() throws IOException, FileNotFoundException {
        EntryRetriever er = new EntryRetriever();
        ast.apply(er);
        return er.getEntries();
    }

    /** Constructs a BibTeXML tree from the parsed bibtex file.
    * The BibTeXML is laid out according to BibTeXConverter's builtin
    * bibtexml-*-*-flat Relax NG schema.
    * @see BibXMLCreator#getResultDocument
    */
    public Document getResultDocument() throws IOException{
        BibXMLCreator xmlmaker = new BibXMLCreator();
        ast.apply(xmlmaker);
        if(xmlmaker.checkError()){
            IOException ex = new IOException("Error creating XML tree.");
            ex.initCause(xmlmaker.getError());
            throw ex;
        } else {
            if(xmlmaker.getEntryCount() == 0){
                throw new IOException("No BibTeX entries found.");
            }
        }
        return xmlmaker.getResultDocument();
    }

    /**
     * @return Returns the abbreviations.
     */
    public ArrayList<ReferenceEntry> getAbbrevs() {
        if (ast != null) {
            AbbrevRetriever ar = new AbbrevRetriever();
            ast.apply(ar);
            return ar.getAbbrevs();
        }
        return null;
    }

    /**
     * @return Returns the errors.
     */
    public ArrayList<ParseErrorMessage> getErrors() {
        return errors;
    }
}
