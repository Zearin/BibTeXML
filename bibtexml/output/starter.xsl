<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!--
      Select output style sheet from the following:

      HTML using Harvard, Chicago 15th A, APA citation style,
         or with titles first as seen used on the W3C web site.
      <xsl:include href="html-harvard.xsl"/>
      <xsl:include href="html-chicago.xsl"/>
      <xsl:include href="html-apa.xsl"/>
      <xsl:include href="html-w3c.xsl"/>

      Add links to the HTML output
      <xsl:include href="include/html-linkify.xsl"/>

      LaTeX notation BibTeX .bib file
      <xsl:include href="bibtex.xsl"/>

      LaTeX biblist environment, Harvard citation style
      <xsl:include href="latex-biblist-harvard.xsl"/>

      LaTeX, Chicago citation style (for footnotes)
      <xsl:include href="latex-footnotetext-chicago.xsl"/>

      Flatten to strict BibTeXML
      <xsl:include href="strict-bibtexml.xsl"/>

      XML based bibliographic formats
      Dublin Core/RDF   <xsl:include href="dublincore.xsl"/>
      MODS              <xsl:include href="mods.xsl"/>
  -->
  <xsl:include href="html-harvard.xsl"/>
  <xsl:include href="include/html-linkify.xsl"/>

  <!--
      Sort output by:
      <xsl:sort select="*/bibtex:author|*/bibtex:editor"/>
      <xsl:sort select="*/bibtex:title"/>
      <xsl:sort select="*/bibtex:year"/>
      <xsl:sort select="@id"/>
  -->
  <xsl:template match="bibtex:file">
    <xsl:for-each select="bibtex:entry">
      <xsl:sort select="@id"/>
      <xsl:call-template name="bibtex-entry"/>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>
