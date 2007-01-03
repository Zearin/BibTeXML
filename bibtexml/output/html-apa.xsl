<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" media-type="text/html"/>
  <xsl:include href="include/extended.xsl"/>
  <xsl:include href="include/apa-helper.xsl"/>
  <xsl:include href="include/html-common.xsl"/>


  <xsl:template match="bibtex:entry" name="bibtex-entry">
    <p>
      <xsl:attribute name="id">
	<xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates select="*/bibtex:author|*/bibtex:editor"/>
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="*/bibtex:year"/>
      <xsl:text>. </xsl:text>
      <xsl:apply-templates />
      <xsl:apply-templates select="*/bibtex:doi|*/bibtex:isbn|*/bibtex:issn|
		  */bibtex:lccn|*/bibtex:url" />
      <xsl:apply-templates select="*/bibtex:pages"/>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>

  <xsl:template match="bibtex:book|bibtex:inbook|
		       bibtex:booklet|bibtex:proceedings">
    <xsl:apply-templates select="bibtex:chapter"/>
    <em>
      <xsl:apply-templates select="bibtex:title"/>
    </em>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:article">
    <xsl:text>'</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>' in </xsl:text>
    <em>
      <xsl:apply-templates select="bibtex:journal"/>
    </em>
    <xsl:apply-templates select="bibtex:volume"/>
    <xsl:apply-templates select="bibtex:number"/>
  </xsl:template>

  <xsl:template match="bibtex:incollection|
		       bibtex:inproceedings|bibtex:conference">
    <xsl:text>'</xsl:text>
      <xsl:apply-templates select="bibtex:title"/>
    <xsl:text>' in </xsl:text>
    <em>
      <xsl:apply-templates select="bibtex:booktitle"/>
    </em>
    <xsl:call-template name="publisher"/>
  </xsl:template>

  <xsl:template match="bibtex:manual|bibtex:techreport|
		       bibtex:mastersthesis|bibtex:phdthesis|
		       bibtex:unpublished|bibtex:misc">
    <em>
      <xsl:apply-templates select="bibtex:title"/>
    </em>
    <xsl:apply-templates select="bibtex:edition"/>
    <xsl:text>, </xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:apply-templates
	select="bibtex:publisher|bibtex:organization|bibtex:institution"/>
  </xsl:template>


</xsl:stylesheet>
