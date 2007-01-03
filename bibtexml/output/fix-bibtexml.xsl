<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/" >
  <xsl:output method="xml" indent="yes"/>

  <!--
      This style sheet reorder and fixes BibTeXML into
      the extended markup scheme of bibtexml-ext.rnc/.dtd
  -->

  <xsl:strip-space elements="*"/>
  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="{name()}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="bibtex:file">
    <xsl:for-each select="bibtex:entry">
      <xsl:sort select="@id"/>
      <xsl:call-template name="bibtex-entry"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="bibtex:entry" name="bibtex-entry">
    <xsl:apply-templates/>
  </xsl:template>

<!--
  <xsl:template match="bibtex:entry/*">
    <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
    <xsl:apply-templates select="bibtex:title"/>
    <xsl:apply-templates select="bibtex:publisher|bibtex:organization|
				 bibtex:institution|bibtex:school"/>
    <xsl:apply-templates select="bibtex:address"/>
    <xsl:apply-templates select="bibtex:chapter"/>
    <xsl:apply-templates select="bibtex:pages"/>
    <xsl:apply-templates select="bibtex:isbn|bibtex:issn|bibtex:doi|
				 bibtex:lccn|bibtex:mrnumber|bibtex:url|
				 bibtex:howpublished|bibtex:key"/>
    <xsl:apply-templates select="bibtex:language"/>
    <xsl:apply-templates select="bibtex:abstract"/>
    <xsl:apply-templates select="bibtex:keywords"/>
    <xsl:apply-templates select="bibtex:category"/>
    <xsl:apply-templates select="bibtex:note|bibtex:annote|bibtex:type"/>
  </xsl:template>
-->

  <xsl:template match="bibtex:howpublished">
    <!--
	special case for the often used
	howpublished = "\url{http://www.example.com/}",
    -->
    <xsl:if test="contains(.,'\url')">
      <bibtex:url>
	<xsl:value-of select="substring-after(.,'\url')"/>
      </bibtex:url>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
