# BibTeXML

This project is a revival of the excellent [BibTeXML project from SourceForge](http://bibtexml.sf.net).


##	Project Overview

###	(Original Overview)
> A bibliography DTD and schema for XML that expresses the content model in BibTeX — the bibliographic system for use with LaTeX, which is widely adopted by the scientific community.
> BibTeXML is shipped with tools to translate native TeX-syntax BibTeX bibliographies to XML and translate this into any markup scheme. Hence one is able to profit from both the existing BibTeX system and bibliographies as well as the transformation and presentation facilities offered by XML.
> Our goal is to maintain a strict BibTeX schema and develop (and collect!) conversion tools that will help you tag your bibliographic data in XML and save typing time, or export it to XML based bibliographic formats such as MODS, as well as HTML, DocBook, LaTeX or native BibTeX syntax.


##	Motivation


###	Similar tools didn’t meet my needs
There’s certainly no shortage of existing tools for XML developers who want to work with BibTeX.  Probably the most well-known include:

* [bibtex2html](http://www.lri.fr/~filliatr/bibtex2html/)
* [Bibutils](http://www.scripps.edu/~cdputnam/software/bibutils/)

…But these didn’t quite do what I wanted.  Bibutils uses MODS XML for everything, and bibtex2html produces HTML that I find offensive to even look at.


###	What were my needs?
I needed something that converted BibTeX into a flavor of XML that *preserved the structure of the input BibTeX file*.  The only project I found that could do this was [BibTeXML on SourceForge](http://bibtexml.sf.net).


###	Reviving the project on GitHub
BibTeXML stopped being developed [several years ago](http://sourceforge.net/projects/bibtexml/files/).  For a while, it fulfilled my needs perfectly.  But eventually my BibTeX source became more complicated, and the conversion started failing.  I started tweaking a few problems, but the more I looked at the code, the more I wanted to fix—specifically, the Relax-NG Compact schemas and `bibtex2xml` Python script.

I prefer Relax-NG Compact over other XML schema languages, and I appreciate Python’s readability compared to Java. (Nothing against Java. It’s just more
difficult for me to understand than Python.)

So I hooked up some electrodes to the original SVN repository and resurrected the project as a zombified Git repo.  I’m planning to improve the mentioned schemas and Python script, which I hope will be helpful.

The rest of the project may also help as-is.  Maybe it will get noticed more on GitHub.  Or if you know Java, maybe you can help there.  :-)