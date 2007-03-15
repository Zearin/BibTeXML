<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/">
  <xsl:param name="bibtexml.sf.net.encoding" select="'ISO-8859-1'" />
  <xsl:output method="text"
	      media-type="application/x-bibtex"
        encoding="ISO-8859-1" />
  <xsl:include href="include/extended.xsl"/>

  <!--
      Be adviced that this converter does no validation or
      error checking of the input BibTeXML data, as this is
      assumed to be a valid BibTeXML document instance.
  -->

  <xsl:template match="bibtex:file">
    <xsl:call-template name="bibtexml-latex-warning"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="bibtex:entry/bibtex:*">
    <xsl:text>@</xsl:text>
    <xsl:value-of select='substring-after(name(),"bibtex:")'/>
    <xsl:text>{</xsl:text>
    <xsl:value-of select="../@id"/>
    <xsl:text>,</xsl:text>
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>}</xsl:text>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:entry/*/bibtex:*">
    <xsl:text>   </xsl:text>
    <xsl:value-of select='substring-after(name(),"bibtex:")'/>
    <xsl:text> = {</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>},</xsl:text>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

</xsl:stylesheet>
