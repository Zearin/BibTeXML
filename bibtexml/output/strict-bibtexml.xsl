<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/" >
  <xsl:output method="xml" indent="yes"/>
  <xsl:include href="include/extended.xsl"/>

  <!--
      This style sheet flattens and normalizes BibTeXML extended
      markup to the strict markup scheme of bibtexml.rnc/.dtd
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

  <xsl:template match="*">
    <xsl:element name="{name()}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

</xsl:stylesheet>
