<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" media-type="text/x-tex"/>
  <xsl:include href="include/extended.xsl"/>
  <xsl:include href="include/chicago-helper.xsl"/>


  <xsl:template match="bibtex:doi|bibtex:isbn|bibtex:issn|bibtex:lccn">
    <xsl:text>, \textsc{</xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:text>}:</xsl:text>
    <xsl:apply-templates />
  </xsl:template>


  <xsl:template match="/">
    <xsl:call-template name="bibtexml-latex-warning"/>
    <xsl:text>\footnote{&#xA;</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>}&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:entry" name="bibtex-entry">
       <xsl:apply-templates select="*/bibtex:author|*/bibtex:editor"/>
       <xsl:text> </xsl:text>
       <xsl:apply-templates />
       <xsl:apply-templates select="*/bibtex:pages"/>
       <xsl:if test="not(position()=last())">
	 <xsl:text>;</xsl:text>
       </xsl:if>
       <xsl:text>&#xA;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:book|bibtex:inbook|
		       bibtex:booklet|bibtex:proceedings">
    <xsl:apply-templates select="bibtex:chapter"/>
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:article">
    <xsl:text>`</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>,' </xsl:text>
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:journal"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="magazinenumber"/>
  </xsl:template>

  <xsl:template match="bibtex:incollection|
		       bibtex:inproceedings|bibtex:conference">
    <xsl:text>`</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>' in </xsl:text>
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:booktitle"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:manual|bibtex:techreport|
		       bibtex:mastersthesis|bibtex:phdthesis|
		       bibtex:unpublished|bibtex:misc">
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates select="bibtex:edition"/>
    <xsl:text> (</xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:text>, </xsl:text>
    <xsl:apply-templates
	select="bibtex:publisher|bibtex:organization|bibtex:institution"/>
    <xsl:apply-templates select="bibtex:year"/>
    <xsl:text>)</xsl:text>
  </xsl:template>


</xsl:stylesheet>
