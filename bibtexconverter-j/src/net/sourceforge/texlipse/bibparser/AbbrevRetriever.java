/*
 * $Id$
 *
 * Copyright (c) 2004-2005 Oskar Ojala
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

import java.util.*;

import net.sourceforge.texlipse.bibparser.analysis.DepthFirstAdapter;
import net.sourceforge.texlipse.bibparser.node.ABibtex;
import net.sourceforge.texlipse.bibparser.node.AConcat;
import net.sourceforge.texlipse.bibparser.node.AEntryDef;
import net.sourceforge.texlipse.bibparser.node.AEntrybraceEntry;
import net.sourceforge.texlipse.bibparser.node.AEntryparenEntry;
import net.sourceforge.texlipse.bibparser.node.AIdValOrSid;
import net.sourceforge.texlipse.bibparser.node.AKeyvalDecl;
import net.sourceforge.texlipse.bibparser.node.ANumValOrSid;
import net.sourceforge.texlipse.bibparser.node.AStrbraceStringEntry;
import net.sourceforge.texlipse.bibparser.node.AStrparenStringEntry;
import net.sourceforge.texlipse.bibparser.node.AValueValOrSid;
import net.sourceforge.texlipse.model.ReferenceEntry;


/**
 * Retrieves the BibTeX abbreviations (defined with @string{...}) from the AST.
 *
 * This class is a visitor, that is applied on the AST that is a result of parsing a
 * BibTeX-file. See <a href="http://www.sablecc.org">http://www.sablecc.org</a> for
 * more information on the structure of the AST and the visitors.
 *
 * @author Oskar Ojala
 */
public class AbbrevRetriever extends DepthFirstAdapter {

    private SortedMap<String,ReferenceEntry> abbrevs = new TreeMap<String,ReferenceEntry>(); //type: ReferenceEntry
    private ReferenceEntry currEntry;
    private String currEntryInfo;

    /**
     * @return The abbreviations as a list of <code>ReferenceEntry</code>s
     */
    public ArrayList<ReferenceEntry> getAbbrevs() {
        ArrayList<ReferenceEntry> result = new ArrayList<ReferenceEntry>();
        result.addAll(abbrevs.values());
        return result;
    }

    public SortedMap<String, ReferenceEntry> getAbbrevMap(){
        return abbrevs;
    }

    public void inABibtex(ABibtex node) {
    }

    public void outABibtex(ABibtex node) {
    }

    public void inAStrbraceStringEntry(AStrbraceStringEntry node) {
        final String key = node.getIdentifier().getText();
        final String val = node.getStringLiteral().getText();
        abbrevs.put(key, new ReferenceEntry(key, val));
    }

    public void outAStrbraceStringEntry(AStrbraceStringEntry node) {
    }

    public void inAStrparenStringEntry(AStrparenStringEntry node) {
        final String key = node.getIdentifier().getText();
        final String val = node.getStringLiteral().getText();
        abbrevs.put(key, new ReferenceEntry(key, val));
    }

    public void outAStrparenStringEntry(AStrparenStringEntry node) {
    }

    public void inAEntrybraceEntry(AEntrybraceEntry node) {
    }

    public void outAEntrybraceEntry(AEntrybraceEntry node) {
    }

    public void inAEntryparenEntry(AEntryparenEntry node) {
    }

    public void outAEntryparenEntry(AEntryparenEntry node) {
    }

    public void inAEntryDef(AEntryDef node) {
    }

    public void outAEntryDef(AEntryDef node) {
    }

    public void inAKeyvalDecl(AKeyvalDecl node) {
    }

    public void outAKeyvalDecl(AKeyvalDecl node) {
    }

    public void inAConcat(AConcat node) {
    }

    public void outAConcat(AConcat node) {
    }

    public void inAValueValOrSid(AValueValOrSid node) {
    }

    public void outAValueValOrSid(AValueValOrSid node) {
    }

    public void inANumValOrSid(ANumValOrSid node) {
    }

    public void outANumValOrSid(ANumValOrSid node) {
    }

    public void inAIdValOrSid(AIdValOrSid node) {
    }

    public void outAIdValOrSid(AIdValOrSid node) {
    }
}
