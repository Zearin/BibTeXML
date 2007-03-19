<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) Moritz Ringler, 2004                            -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to RIS format.                          -->
<!-- The end-of-line comments avoid unwanted line breaks -->
<!-- in the output. Do not remove them!                  -->
<xsl:stylesheet version="2.0"
 xmlns:bibtex="http://bibtexml.sf.net/"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no" encoding="windows-1252"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/"><xsl:apply-templates select="bibtex:file"/></xsl:template>

<xsl:template match="bibtex:file" name="bibtex-file">
<xsl:apply-templates select="bibtex:entry"/>
</xsl:template>

<xsl:template match="bibtex:entry">
    <xsl:apply-templates />
</xsl:template>

<!-- INCOLLECTION -->
<xsl:template
 match="*/bibtex:incollection"><!--
 NOT IMPLEMENTED
--></xsl:template>

<!-- PHDTHESIS -->
<xsl:template
 match="*/bibtex:phdthesis"><!--
 NOT IMPLEMENTED
--></xsl:template>

<!-- ARTICLE -->
<xsl:template match="*/bibtex:article"><!-- type -->TY  - JOUR
<!-- entry id -->ID  - <xsl:value-of select="../@id"/>
<!-- authors --><xsl:apply-templates select="bibtex:author"/>
<!-- title -->TI  - <xsl:value-of select="bibtex:title"/>
<!-- journal --><xsl:apply-templates select="bibtex:journal"/>
<!-- volume -->VL  - <xsl:value-of select="bibtex:volume"/>
<!-- issue/number -->IS  - <xsl:value-of select="bibtex:number"/>
<!-- pages -->SP  - <xsl:value-of select="tokenize(bibtex:pages,'(-)|(--)')[1]"/>
<xsl:if test="contains(bibtex:pages,'-') or contains(bibtex:pages,'--')">
EP  - <xsl:value-of select="tokenize(bibtex:pages,'(-)|(--)')[2]"/>
</xsl:if>
<!-- year -->Y1  - <xsl:value-of select="bibtex:year"/>//
<!-- url --><xsl:call-template name="bibtex-field-url"/>
<!-- eprint --><!--L1  - <xsl:value-of select="bibtex:eprint"/>--><!-- EndNote doesn't seem to know this tag, so we omit it
--><!-- keywords  --><xsl:apply-templates select="bibtex:keywords"/>
<!-- notes -->N1  - <xsl:value-of select="bibtex:note"/>
<!-- abstract -->N2  - <xsl:value-of select="bibtex:abstract"/>
<!-- end of record -->ER  -<xsl:text>
</xsl:text></xsl:template>

<!-- URL -->
<xsl:template
 name="bibtex-field-url"><!--
--><xsl:choose><!--
--><xsl:when test="exists(bibtex:url) and not(bibtex:url eq '')"><!--
-->UR  - <xsl:value-of select="bibtex:url"/>
</xsl:when><!--
--><xsl:when test="exists(bibtex:doi) and not(bibtex:doi eq '')"><!--
-->UR  - <xsl:text
>http://dx.doi.org/</xsl:text><xsl:value-of select="bibtex:doi"/>
</xsl:when><!--
--><xsl:otherwise><!--
-->UR  - <!--
--></xsl:otherwise><!--
--></xsl:choose><!--
--></xsl:template>

<!-- KEYWORDS -->
<xsl:template
 match="*/bibtex:keywords">
KW  - <xsl:value-of select="."/>
</xsl:template>

<!-- JOURNAL -->
<xsl:template
 match="*/bibtex:journal">
<xsl:choose>
<!-- journal is abbreviated -->
<xsl:when test="contains(.,'.')">
JO  - <xsl:value-of select="."/>
</xsl:when>
<!-- journal is not abbreviated -->
<xsl:otherwise>
JF  - <xsl:value-of select="."/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- AUTHOR -->
<xsl:template
 match="bibtex:author">
<xsl:choose>
<!-- author contains ','
     assume format LASTNAME, FIRST AND MIDDLE NAMES -->
<xsl:when test="contains(.,',')">
AU  - <xsl:value-of select="replace(.,'&#160;',' ')"/>
</xsl:when>
<!-- author contains neither ',' nor ' '
     use unchanged -->
<xsl:when test="not(contains(.,' '))">
AU  - <xsl:value-of select="."/>
</xsl:when>
<!-- author contains ' ' but no ','
     assume format FIRST&#160;AND&#160;MIDDLE&#160;NAMES LASTNAME -->
<xsl:otherwise>
AU  - <!-- part after the last ' ' --><xsl:value-of
select="replace(replace(.,'.* ',''),'&#160;',' ')"/>, <!--
part before the last blank --><xsl:value-of
select="replace(replace(.,'[^ ]+$',''),'&#160;',' ')"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>
