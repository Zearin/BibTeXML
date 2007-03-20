package net.sourceforge.jeditsyntax;
/*
 * EiffelTokenMarker.java - Eiffel token marker
 * Copyright (C) 1999 Slava Pestov
 * Copyright (C) 1999 Artur Biesiadowski
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

import javax.swing.text.Segment;

/**
 * BibTeX token Marker. This is very primitive but better than nothing...
 *
 * @author Moritz Ringler
 */
public class BibTeXTokenMarker extends TokenMarker
{

	public BibTeXTokenMarker()
	{
		this.keywords = getKeywords();
	}

	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		lastOffset = offset;
		lastKeyword = offset;
		int length = line.count + offset;
		boolean backslash = false;

loop:		for(int i = offset; i < length; i++)
		{
			int i1 = (i+1);

			char c = array[i];
			if(c == '\\')
			{
				backslash = !backslash;
				continue;
			}

			switch(token)
			{
			case Token.NULL:
				switch(c)
				{
                case '%':
                    if(backslash){
                        backslash = false;
                    } else{
                        addToken(i - lastOffset,token);
                        addToken(length - i,Token.COMMENT1);
                        lastOffset = lastKeyword = length;
                        break loop;
                    }
                    break;
				default:
					backslash = false;
					if(!Character.isLetter(c) && c != '@'){
						doKeyword(line,i,c);
                    }
					break;
				}
				break;
            case Token.KEYWORD3:
			case Token.KEYWORD2:
			default:
				throw new InternalError("Invalid state: "
					+ token);
			}
		}

		if(token == Token.NULL){
			doKeyword(line,length,'\0');
        }

     	addToken(length - lastOffset,token);

		return token;
	}

	public static KeywordMap getKeywords()
	{
		if(bibtexKeywords == null)
		{
			bibtexKeywords = new KeywordMap(true);
			bibtexKeywords.add("@article", Token.KEYWORD3);
			bibtexKeywords.add("@book", Token.KEYWORD3);
			bibtexKeywords.add("@booklet", Token.KEYWORD3);
			bibtexKeywords.add("@manual", Token.KEYWORD3);
			bibtexKeywords.add("@techreport", Token.KEYWORD3);
			bibtexKeywords.add("@masterthesis", Token.KEYWORD3);
			bibtexKeywords.add("@phdthesis", Token.KEYWORD3);
			bibtexKeywords.add("@inbook", Token.KEYWORD3);
			bibtexKeywords.add("@incollection", Token.KEYWORD3);
			bibtexKeywords.add("@proceedings", Token.KEYWORD3);
			bibtexKeywords.add("@inproceedings",Token.KEYWORD3);
			bibtexKeywords.add("@conference", Token.KEYWORD3);
			bibtexKeywords.add("@unpublished", Token.KEYWORD3);
			bibtexKeywords.add("@misc", Token.KEYWORD3);
            
           bibtexKeywords.add("address", Token.KEYWORD2);
            bibtexKeywords.add("annote", Token.KEYWORD2);
            bibtexKeywords.add("author", Token.KEYWORD2);
            bibtexKeywords.add("booktitle", Token.KEYWORD2);
            bibtexKeywords.add("chapter", Token.KEYWORD2);
            bibtexKeywords.add("crossref", Token.KEYWORD2);
            bibtexKeywords.add("edition", Token.KEYWORD2);
            bibtexKeywords.add("editor", Token.KEYWORD2);
            bibtexKeywords.add("howpublished", Token.KEYWORD2);
            bibtexKeywords.add("institution", Token.KEYWORD2);
            bibtexKeywords.add("journal", Token.KEYWORD2);
            bibtexKeywords.add("key", Token.KEYWORD2);
            bibtexKeywords.add("month", Token.KEYWORD2);
            bibtexKeywords.add("note", Token.KEYWORD2);
            bibtexKeywords.add("number", Token.KEYWORD2);
            bibtexKeywords.add("organization", Token.KEYWORD2);
            bibtexKeywords.add("pages", Token.KEYWORD2);
            bibtexKeywords.add("publisher", Token.KEYWORD2);
            bibtexKeywords.add("school", Token.KEYWORD2);
            bibtexKeywords.add("series", Token.KEYWORD2);
            bibtexKeywords.add("title", Token.KEYWORD2);
            bibtexKeywords.add("type", Token.KEYWORD2);
            bibtexKeywords.add("volume", Token.KEYWORD2);
            bibtexKeywords.add("year", Token.KEYWORD2);
		}
		return bibtexKeywords;
	}

	// private members
	private static KeywordMap bibtexKeywords;

	private boolean cpp;
	private KeywordMap keywords;
	private int lastOffset;
	private int lastKeyword;

	private boolean doKeyword(Segment line, int i, char c)
	{
		int i1 = i+1;

		int len = i - lastKeyword;
		byte id = keywords.lookup(line,lastKeyword,len);
		if(id != Token.NULL)
		{
			if(lastKeyword != lastOffset)
				addToken(lastKeyword - lastOffset,Token.NULL);
			addToken(len,id);
			lastOffset = i;
		}
		lastKeyword = i1;
		return false;
	}
}
