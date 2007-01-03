<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" media-type="text/x-tex"/>
  <xsl:include href="include/extended.xsl"/>
  <xsl:include href="include/harvard-helper.xsl"/>

  <xsl:template match="bibtex:doi|bibtex:isbn|bibtex:issn|bibtex:lccn">
    <xsl:text>, \textsc{</xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:text>}:</xsl:text>
    <xsl:apply-templates />
  </xsl:template>


  <xsl:template match="/">
    <xsl:call-template name="bibtexml-latex-warning"/>
    <xsl:text>\begin{biblist}&#xA;&#xA;</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>\end{biblist}&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:entry" name="bibtex-entry">
    <xsl:text>\bibitem[</xsl:text>
    <xsl:value-of select="substring(@id,0,4)"/>
    <xsl:text>]&#xA;</xsl:text>
    
       <xsl:apply-templates />
       <xsl:apply-templates select="*/bibtex:pages"/>
       <xsl:apply-templates select="*/bibtex:doi|*/bibtex:isbn|*/bibtex:issn|
	                            */bibtex:lccn|*/bibtex:url" />
    <xsl:text>.</xsl:text>
    <xsl:text>&#xA;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:book|bibtex:booklet|bibtex:proceedings">
    <xsl:call-template name="author"/>
    <xsl:apply-templates select="bibtex:chapter"/>
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:inbook">
    <xsl:choose>
      <xsl:when test="bibtex:chapter/bibtex:author">
	<xsl:apply-templates select="bibtex:chapter/bibtex:author"/>
	<xsl:text> </xsl:text>
	<xsl:apply-templates select="bibtex:year"/>
	<xsl:text>, </xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="author"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="bibtex:chapter"/>
    <em>
      <xsl:apply-templates select="bibtex:title"/>
    </em>
    <xsl:if test="bibtex:chapter/bibtex:author">
      <xsl:text>, </xsl:text>
      <xsl:apply-templates select="bibtex:editor|bibtex:author"/>
    </xsl:if>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:article">
    <xsl:call-template name="author"/>
    <xsl:text>`</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>' in </xsl:text>
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:journal"/>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates select="bibtex:volume"/>
    <xsl:apply-templates select="bibtex:number"/>
  </xsl:template>

  <xsl:template match="bibtex:incollection|
		       bibtex:inproceedings|bibtex:conference">
    <xsl:call-template name="author"/>
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
    <xsl:call-template name="author"/>
    <xsl:text>\textit{</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates select="bibtex:edition"/>
    <xsl:text>, </xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:apply-templates
	select="bibtex:publisher|bibtex:organization|bibtex:institution"/>
  </xsl:template>


</xsl:stylesheet>
