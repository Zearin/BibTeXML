<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		xmlns:dc="http://purl.org/dc/elements/1.1/" >
  <xsl:output method="text" encoding="UTF-8" />

<!--

Reference Types

%0 Generic
%0 Journal Article
%0 Book
%0 Book Section
%0 Manuscript
%0 Edited Book
%0 Magazine Article
%0 Newspaper Article
%0 Conference Proceedings
%0 Thesis
%0 Report 
%0 Personal Communication
%0 Computer Program
%0 Electronic Source
%0 Audiovisual Material
%0 Film or Broadcast
%0 Artwork
%0 Map
%0 Patent 
%0 Hearing
%0 Bill 
%0 Statute
%0 Case


EndNote Tags and Corresponding Fields

%A Author
%B Secondary Title (of a Book or Conference Name)
%C Place Published
%D Year
%E Editor /Secondary Author
%F Label
%I Publisher
%J Journal Name
%K Keywords
%L Call Number
%M Accession Number
%N Number (Issue)
%P Pages
%S Tertiary Title
%T Title
%U URL
%V Volume
%X Abstract
%Y Tertiary Author / Translator
%Z Notes
%0 Reference Type
%1 Custom 1
%2 Custom 2
%3 Custom 3
%4 Custom 4
%6 Number of Volumes
%7 Edition
%8 Date
%9 Type of Work
%? Subsidiary Author
%@ ISBN/ISSN
%! Short Title
%# Custom 5
%$ Custom 6
%& Section
%( Original Publication
%) Reprint Edition
%* Reviewed Item
%+ Author Address
-->

<xsl:template match="bibtex:entry">
  <xsl:apply-templates/>
  <xsl:text>%F </xsl:text>
  <xsl:value-of select='@id'/>
  <xsl:text>&#xA;</xsl:text>
  <xsl:text>&#xA;</xsl:text>
</xsl:template>

<xsl:template match="bibtex:book|bibtex:booklet|bibtex:incollection">
  <xsl:text>%0 Book</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:article">
  <xsl:text>%0 Journal Article</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:inbook">
  <xsl:text>%0 Book Section</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:proceedings|bibtex:inproceedings|bibtex:conference">
  <xsl:text>%0 Conference Proceedings</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:techreport">
  <xsl:text>%0 Report</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:mastersthesis|bibtex:phdthesis">
  <xsl:text>%0 Thesis</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="bibtex:manual|bibtex:unpublished|bibtex:misc">
  <xsl:text>%0 Generic</xsl:text>
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

  <xsl:template match="bibtex:title">
    <xsl:text>%T </xsl:text>
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
	  <xsl:text>%A </xsl:text>
	  <xsl:value-of select="normalize-space(.)"/>
	  <xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>%A </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:editor">
    <xsl:choose>
      <xsl:when test="bibtex:person">
	<xsl:for-each select="bibtex:person">
	  <xsl:text>%E </xsl:text>
	  <xsl:value-of select="normalize-space(.)"/>
	  <xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>%E </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:year">
    <xsl:text>%D </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:month">
    <xsl:text>%8 </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:address">
    <xsl:text>%C </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:publisher|bibtex:organization|
		       bibtex:institution|bibtex:school">
    <xsl:text>%I </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:journal">
    <xsl:text>%J </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:volume">
    <xsl:text>%V </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:number">
    <xsl:text>%N </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:edition">
    <xsl:text>%7 </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:pages">
    <xsl:text>%P </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:isbn|bibtex:issn|bibtex:doi">
    <xsl:text>%@ </xsl:text>
    <xsl:value-of select='substring-after(name(),"bibtex:")'/>
    <xsl:text>:</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:lccn|bibtex:mrnumber">
    <xsl:text>%L </xsl:text>
    <xsl:value-of select='substring-after(name(),"bibtex:")'/>
    <xsl:text>:</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:url">
    <xsl:text>%U </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:howpublished">
    <!--
	special case for the often used
	howpublished = "\url{http://www.example.com/}",
    -->
    <xsl:if test="contains(.,'\url')">
      <xsl:text>%U </xsl:text>
      <xsl:value-of select="substring-after(.,'\url')"/>
    </xsl:if>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:language"/>
  <xsl:template match="bibtex:copyright"/>

  <xsl:template match="bibtex:booktitle">
    <xsl:text>%B </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:inbook/bibtex:chapter">
    <xsl:text>%T </xsl:text>
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
    <xsl:text>%B </xsl:text>
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
	  <xsl:text>%K </xsl:text>
	  <xsl:value-of select="normalize-space(.)"/>
	  <xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>%K </xsl:text>
	<xsl:value-of select="normalize-space(.)"/>
	<xsl:text>&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:abstract">
    <xsl:text>%X </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:note">
    <xsl:text>%Z </xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="text()" />


</xsl:stylesheet>
