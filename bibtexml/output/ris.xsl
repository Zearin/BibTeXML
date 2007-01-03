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

<xsl:template match="bibtex:entry">
  <xsl:apply-templates/>
  <xsl:text>ID  - </xsl:text>
  <xsl:value-of select='@id'/>
  <xsl:text>&#xA;</xsl:text>
  <xsl:text>ER  -</xsl:text>
  <xsl:text>&#xA;</xsl:text>
</xsl:template>

<xsl:template match="bibtex:book|bibtex:booklet|bibtex:incollection">
  <xsl:text>TY  - BOOK</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:article">
  <xsl:text>TY  - JOUR</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:inbook">
  <xsl:text>TY  - CHAP</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:proceedings|bibtex:inproceedings|bibtex:conference">
  <xsl:text>TY  - CONF</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:mastersthesis|bibtex:phdthesis">
  <xsl:text>TY  - THES</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:techreport">
  <xsl:text>TY  - RPRT</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:unpublished">
  <xsl:text>TY  - UNPB</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:manual|bibtex:unpublished|bibtex:misc">
  <xsl:text>TY  - STD</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

  <xsl:template match="bibtex:title">
    <xsl:text>TI  - </xsl:text>
    <xsl:choose>
      <xsl:when test="bibtex:title|bibtex:subtitle">
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

  <xsl:template match="bibtex:author">
    <xsl:choose>
      <xsl:when test="bibtex:person">
	<xsl:for-each select="bibtex:person">
	  <xsl:text>AU  - </xsl:text>
	  <xsl:value-of select="normalize-space(.)"/>
	  <xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>AU  - </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:editor">
    <xsl:choose>
      <xsl:when test="bibtex:person">
	<xsl:for-each select="bibtex:person">
	  <xsl:text>ED  - </xsl:text>
	  <xsl:value-of select="normalize-space(.)"/>
	  <xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>ED  - </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:year">
    <xsl:text>PY  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:address">
    <xsl:text>CY  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:publisher|bibtex:organization|
		       bibtex:institution|bibtex:school">
    <xsl:text>PB  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:journal">
    <xsl:text>JO  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:volume">
    <xsl:text>VL  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:if test="../bibtex:number"><!-- IS -->
      <xsl:text>(</xsl:text>
      <xsl:value-of select="normalize-space(../bibtex:number)"/>
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:pages">
    <xsl:choose>
      <xsl:when test="contains(.,'-')">
	<xsl:text>SP  - </xsl:text>
	<xsl:value-of select="substring-before(.,'-')"/>
	<xsl:text>&#xA;</xsl:text>
	<xsl:text>EP  - </xsl:text>
	<xsl:value-of select="substring-after(.,'-')"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>SP  - </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:isbn|bibtex:issn|bibtex:doi|
		       bibtex:lccn|bibtex:mrnumber|bibtex:url">
    <xsl:text>SN  - </xsl:text>
    <xsl:value-of select='substring-after(name(),"bibtex:")'/>
    <xsl:text>:</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:url">
    <xsl:text>UR  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:howpublished">
    <!--
	special case for the often used
	howpublished = "\url{http://www.example.com/}",
    -->
    <xsl:if test="contains(.,'\url')">
      <xsl:text>UR  - </xsl:text>
      <xsl:value-of select="substring-after(.,'\url')"/>
    </xsl:if>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:language"/>
  <xsl:template match="bibtex:copyright"/>

  <xsl:template match="bibtex:booktitle">
    <xsl:text>BT  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
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

  <xsl:template match="bibtex:keywords|bibtex:category">
    <xsl:choose>
      <xsl:when test="bibtex:keyword">
	<xsl:for-each select="*">
	  <xsl:text>KW  - </xsl:text>
	  <xsl:value-of select="normalize-space(.)"/>
	  <xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>KW  - </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:abstract">
    <xsl:text>AB  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:notes">
    <xsl:text>N1  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:affiliation">
    <xsl:text>AF  - </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="text()" />


</xsl:stylesheet>
