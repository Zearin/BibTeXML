/* This file was generated by SableCC (http://www.sablecc.org/). */

package net.sourceforge.texlipse.bibparser.analysis;

import java.util.*;
import net.sourceforge.texlipse.bibparser.node.*;

public class AnalysisAdapter implements Analysis
{
    private Hashtable<Node,Object> in;
    private Hashtable<Node,Object> out;

    public Object getIn(Node node)
    {
        if(this.in == null)
        {
            return null;
        }

        return this.in.get(node);
    }

    public void setIn(Node node, Object o)
    {
        if(this.in == null)
        {
            this.in = new Hashtable<Node,Object>(1);
        }

        if(o != null)
        {
            this.in.put(node, o);
        }
        else
        {
            this.in.remove(node);
        }
    }

    public Object getOut(Node node)
    {
        if(this.out == null)
        {
            return null;
        }

        return this.out.get(node);
    }

    public void setOut(Node node, Object o)
    {
        if(this.out == null)
        {
            this.out = new Hashtable<Node,Object>(1);
        }

        if(o != null)
        {
            this.out.put(node, o);
        }
        else
        {
            this.out.remove(node);
        }
    }

    public void caseStart(Start node)
    {
        defaultCase(node);
    }

    public void caseABibtex(ABibtex node)
    {
        defaultCase(node);
    }

    public void caseAStrContent(AStrContent node)
    {
        defaultCase(node);
    }

    public void caseAPreContent(APreContent node)
    {
        defaultCase(node);
    }

    public void caseAEntContent(AEntContent node)
    {
        defaultCase(node);
    }

    public void caseAStrbraceStringEntry(AStrbraceStringEntry node)
    {
        defaultCase(node);
    }

    public void caseAStrparenStringEntry(AStrparenStringEntry node)
    {
        defaultCase(node);
    }

    public void caseAPrebracePreambleEntry(APrebracePreambleEntry node)
    {
        defaultCase(node);
    }

    public void caseAPreparenPreambleEntry(APreparenPreambleEntry node)
    {
        defaultCase(node);
    }

    public void caseAEntrybraceEntry(AEntrybraceEntry node)
    {
        defaultCase(node);
    }

    public void caseAEntryparenEntry(AEntryparenEntry node)
    {
        defaultCase(node);
    }

    public void caseAEntryDef(AEntryDef node)
    {
        defaultCase(node);
    }

    public void caseAKeyvalDecl(AKeyvalDecl node)
    {
        defaultCase(node);
    }

    public void caseAConcat(AConcat node)
    {
        defaultCase(node);
    }

    public void caseAValueValOrSid(AValueValOrSid node)
    {
        defaultCase(node);
    }

    public void caseANumValOrSid(ANumValOrSid node)
    {
        defaultCase(node);
    }

    public void caseAIdValOrSid(AIdValOrSid node)
    {
        defaultCase(node);
    }

    public void caseAEmptyValOrSid(AEmptyValOrSid node)
    {
        defaultCase(node);
    }

    public void caseTWhitespace(TWhitespace node)
    {
        defaultCase(node);
    }

    public void caseTEstring(TEstring node)
    {
        defaultCase(node);
    }

    public void caseTScribeComment(TScribeComment node)
    {
        defaultCase(node);
    }

    public void caseTPreamble(TPreamble node)
    {
        defaultCase(node);
    }

    public void caseTEntryName(TEntryName node)
    {
        defaultCase(node);
    }

    public void caseTComment(TComment node)
    {
        defaultCase(node);
    }

    public void caseTLBrace(TLBrace node)
    {
        defaultCase(node);
    }

    public void caseTRBrace(TRBrace node)
    {
        defaultCase(node);
    }

    public void caseTBString(TBString node)
    {
        defaultCase(node);
    }

    public void caseTLParen(TLParen node)
    {
        defaultCase(node);
    }

    public void caseTRParen(TRParen node)
    {
        defaultCase(node);
    }

    public void caseTComma(TComma node)
    {
        defaultCase(node);
    }

    public void caseTEquals(TEquals node)
    {
        defaultCase(node);
    }

    public void caseTSharp(TSharp node)
    {
        defaultCase(node);
    }

    public void caseTNumber(TNumber node)
    {
        defaultCase(node);
    }

    public void caseTIdentifier(TIdentifier node)
    {
        defaultCase(node);
    }

    public void caseTQuotec(TQuotec node)
    {
        defaultCase(node);
    }

    public void caseTStringLiteral(TStringLiteral node)
    {
        defaultCase(node);
    }

    public void caseEOF(EOF node)
    {
        defaultCase(node);
    }

    public void defaultCase(@SuppressWarnings("unused") Node node)
    {
        // do nothing
    }
}
