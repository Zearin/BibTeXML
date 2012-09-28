# BibTeXML: BibTeX as XML markup
[http://bibtexml.sourceforge.net]

A bibliography schema for XML that expresses the content model
in BibTeX, a bibliography format specified for LaTeX.

DTD and RELAX NG schemas are included in this distribution,
along with tools to uptranslate native TeX-syntax BibTeX
bibliographies to XML, and translating this into other markup
schemes.

The `bibtexml-ext.rnc` schema should be used for manual editing,
and is backward compatible with the strict `bibtexml.rnc` syntax.

Updates and new links are announced on our hand-rolled RSS feed.
[http://bibtexml.sourceforge.net/rss.xml]


You can use `bibutils` (by Chris Putnam) or `bib2xml` (by Johannes Henkel)
to convert `.bib` files. They are more robust than the included
`bibtex2xml.py`, because `bib2xml` is based on the btparse library.

After the conversion, you’ll need to convert from LaTeX character
encoding to UTF-8 (or numeric XML character entities).

If you encounter BibTeX instances that fails please post these
on the bibtexml-users mailing list, or open a bug report. That way
we can put these in our test case `.bib` file.


## License
This program is free software; you can redistribute it and/or
modify it under the terms of the
[GNU General Public License](http://creativecommons.org/licenses/GPL/2.0/)


## Recommended software:

* [bibutils](http://www.scripps.edu/~cdputnam/software/bibutils/)
* [bib2xml](http://www.cs.colorado.edu/~henkel/stuff/bib2xml/)
* Any XSLT engine (such as Sablotron, xsltproc or Saxon)
* [rnv: Relax-NG XML validator](http://davidashen.net/rnv.html)
* [Trang: Convert between Relax-NG, DTD and W3C XML Schema notations](http://www.thaiopensource.com/relaxng/trang.html)
* [emacs-nxml-mode: Relax-NG based XML editng mode for Emacs](http://www.thaiopensource.com/download/)

For nxml-mode, put this in your `schemas.xml`:

```xml
   <namespace ns="http://bibtexml.sf.net/" typeId="bibtexml"/>
   <documentElement prefix="bibtex" typeId="bibtexml"/>
   <typeId id="bibtexml" uri="bibtexml-ext.rnc"/>
```

## Usage examples:

```bash
    $ xsltproc output/starter.xsl examples/example.bibtex.xml
```

Read or edit `starter.xsl` for more information. The different output stylesheets can also be called directly:

```bash
    $ xsltproc output/html-harvard.xsl examples/example.bibtex.xml
    $ xsltproc output/dublincore.xsl examples/example.bibtex.xml
    $ xsltproc output/bibtex.xsl examples/example.bibtex.xml
    $ xsltproc output/latex-biblist-harvard.xsl examples/example.bibtex.xml

    $ rnv schema/bibtexml-ext.rnc examples/example.bibtex.xml

    $ java -jar %java_site_lib_path%/trang.jar bibtexml-ext.rnc bibtexml-ext.xsd
```