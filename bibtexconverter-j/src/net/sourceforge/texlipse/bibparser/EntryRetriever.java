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

import java.util.ArrayList;

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
 * Retrieves the BibTeX entries from the AST.
 *
 * This class is a visitor, that is applied on the AST that is a result of parsing a
 * BibTeX-file. See <a href="http://www.sablecc.org">http://www.sablecc.org</a> for
 * more information on the structure of the AST and the visitors.
 *
 * @author Oskar Ojala
 */
public final class EntryRetriever extends DepthFirstAdapter {

    private ArrayList entries = new ArrayList(); //type: ReferenceEntry
    private ReferenceEntry currEntry;
    private StringBuffer currEntryInfo;

    /**
     * @return The entries as a list of <code>ReferenceEntry</code>s
     */
    public ArrayList getEntries() {
        return entries;
    }

    public void inABibtex(ABibtex node) {
    }

    public void outABibtex(ABibtex node) {
    }

    public void inAStrbraceStringEntry(AStrbraceStringEntry node) {
    }

    public void outAStrbraceStringEntry(AStrbraceStringEntry node) {
    }

    public void inAStrparenStringEntry(AStrparenStringEntry node) {
    }

    public void outAStrparenStringEntry(AStrparenStringEntry node) {
    }

    /**
     * Called when entering a bibliography entry, starts
     * forming an entry for the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void inAEntrybraceEntry(AEntrybraceEntry node) {
        currEntry = new ReferenceEntry(node.getIdentifier().getText());
        currEntry.startLine = node.getIdentifier().getLine();
        currEntryInfo = new StringBuffer();
    }

    /**
     * Called when exiting a bibliography entry, adds the formed
     * entry into the entry list
     *
     * @param node an <code>AEntry</code> value
     */
    public void outAEntrybraceEntry(AEntrybraceEntry node) {
        currEntry.info = currEntryInfo.toString();
        entries.add(currEntry);
    }

    public void inAEntryparenEntry(AEntryparenEntry node) {
        currEntry = new ReferenceEntry(node.getIdentifier().getText());
        currEntry.startLine = node.getIdentifier().getLine();
        currEntryInfo = new StringBuffer();
    }

    public void outAEntryparenEntry(AEntryparenEntry node) {
        currEntry.info = currEntryInfo.toString();
        entries.add(currEntry);
    }

    public void inAEntryDef(AEntryDef node) {
    }

    /**
     * Handles the type of the bibliography entry
     *
     * @param node an <code>AEntryDef</code> value
     */
    public void outAEntryDef(AEntryDef node) {
        //currEntryInfo.append(node.getEntryName().getText().substring(1).toLowerCase());
        currEntryInfo.append(node.getEntryName().getText().substring(1));
        currEntryInfo.append('\n');
    }

    public void inAKeyvalDecl(AKeyvalDecl node) {
        currEntryInfo.append(node.getIdentifier().getText().toLowerCase());
        currEntryInfo.append(": ");
    }

    public void outAKeyvalDecl(AKeyvalDecl node) {
        currEntryInfo.append('\n');
    }

    public void inAConcat(AConcat node) {
    }

    public void outAConcat(AConcat node) {
    }

    public void inAValueValOrSid(AValueValOrSid node) {
    }

    public void outAValueValOrSid(AValueValOrSid node) {
        currEntryInfo.append(node.getStringLiteral().getText().replaceAll("\\s+", " "));
    }

    public void inANumValOrSid(ANumValOrSid node) {
    }

    public void outANumValOrSid(ANumValOrSid node) {
        currEntryInfo.append(node.getNumber().getText());
    }

    public void inAIdValOrSid(AIdValOrSid node) {
    }

    public void outAIdValOrSid(AIdValOrSid node) {
        currEntryInfo.append(node.getIdentifier().getText());
    }
}
