<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml"
              media-type="text/html"
              encoding="utf-8"/>
  <xsl:include href="include/extended.xsl"/>
  <xsl:include href="include/chicago-helper.xsl"/>
  <xsl:include href="include/html-common.xsl"/>


  <xsl:template match="bibtex:entry" name="bibtex-entry">
    <p>
      <xsl:attribute name="id">
	<xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates select="*/bibtex:author|*/bibtex:editor"/>
      <xsl:text> </xsl:text>
      <xsl:apply-templates />
      <xsl:apply-templates select="*/bibtex:pages"/>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>

  <xsl:template match="bibtex:book|bibtex:inbook|bibtex:booklet|
		       bibtex:proceedings|
		       bibtex:manual|bibtex:techreport|
		       bibtex:mastersthesis|bibtex:phdthesis|
		       bibtex:unpublished|bibtex:misc">
    <xsl:apply-templates select="bibtex:chapter"/>
    <em>
      <xsl:apply-templates select="bibtex:title"/>
    </em>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:article">
      <xsl:text> '</xsl:text>
	<xsl:apply-templates select="bibtex:title"/>
      <xsl:text>,' </xsl:text>
      <em>
	<xsl:apply-templates select="bibtex:journal"/>
      </em>
      <xsl:call-template name="magazinenumber"/>
  </xsl:template>

  <xsl:template match="bibtex:incollection|
		       bibtex:inproceedings|bibtex:conference">
    <xsl:text>`</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>' in </xsl:text>
    <em>
      <xsl:apply-templates select="bibtex:booktitle"/>
    </em>
    <xsl:call-template name="publisher"/>
  </xsl:template>


</xsl:stylesheet>
