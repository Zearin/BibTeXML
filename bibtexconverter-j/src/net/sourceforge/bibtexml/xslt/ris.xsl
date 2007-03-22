<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		xmlns:dc="http://purl.org/dc/elements/1.1/" >
  <xsl:output method="text" encoding="UTF-8" />

<!--

ABST (abstract reference)
ADVS (audiovisual material)
ART (art work)
BILL (bill/resolution)
BOOK (whole book reference)
CASE (case)
CHAP (book chapter reference)
COMP (computer program)
CONF (conference proceeding)
CTLG (catalog)
DATA (data file)
ELEC (electronic citation)
GEN (generic)
ICOMM (internet communication)
INPR (in press reference)
JFULL (journal - full)
JOUR (journal reference)
MAP (map)
MGZN (magazine article)
MPCT (motion picture)
MUSIC (music score)
NEWS (newspaper)
PAMP (pamphlet)
PAT (patent)
PCOMM (personal communication)
RPRT (report)
SER (serial - book, monograph)
SLIDE (slide)
SOUND (sound recording)
STAT (statute)
THES (thesis/dissertation)
UNBILL (unenacted bill/resolution)
UNPB (unpublished work reference)
VIDEO (video recording)

-->
<!-- affiliation -->
  <xsl:template match="bibtex:affiliation">
        <xsl:call-template name="field">
            <xsl:param name="risid" select="'AF'" />
        </xsl:call-template>
  </xsl:template>

  <xsl:template match="bibtex:inbook/bibtex:chapter">
    <xsl:text>TI  - </xsl:text>
    <xsl:choose>
      <xsl:when test="bibtex:title">
	<xsl:value-of select="normalize-space(bibtex:title)"/>
	<xsl:text>: </xsl:text>
	<xsl:value-of select="normalize-space(bibtex:subtitle)"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="normalize-space(.)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:inbook/bibtex:title">
    <xsl:text>BT  - </xsl:text>
    <xsl:choose>
      <xsl:when test="bibtex:title">
	<xsl:value-of select="normalize-space(bibtex:title)"/>
	<xsl:text>: </xsl:text>
	<xsl:value-of select="normalize-space(bibtex:subtitle)"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="normalize-space(.)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>



  <xsl:template match="text()" />


</xsl:stylesheet>
