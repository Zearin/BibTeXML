<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns="http://www.loc.gov/mods/v3" >
  <xsl:output method="xml" indent="yes" />

  <xsl:strip-space elements="*"/>
  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <xsl:template match="/">
    <modsCollection xmlns="http://www.loc.gov/mods/v3">
<!--
	xmlns:xlink="http://www.w3.org/TR/xlink"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/standards/mods/mods.xsd"
-->
      <xsl:apply-templates />
    </modsCollection>
    <xsl:comment>
      <xsl:text>
	Processed </xsl:text>
	<xsl:value-of select="count(bibtex:file/bibtex:entry/bibtex:*)"/>
      <xsl:text> references.</xsl:text>
      <xsl:text>
	Unfinished output filter: Manual review
        and cleanup may be required.
      </xsl:text>
    </xsl:comment>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <mods ID="{@id}">
      <xsl:apply-templates />
    </mods>
  </xsl:template>

<xsl:template match="bibtex:book|bibtex:booklet">
  <!-- chapter -->
  <xsl:apply-templates select="bibtex:title"/>
  <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
  <xsl:apply-templates select="bibtex:publisher"/>
  <typeOfResource>text</typeOfResource>
  <genre authority="bibx">book</genre>
  <xsl:call-template name="commonfields"/>
</xsl:template>

<xsl:template match="bibtex:inbook">
  <!-- chapter -->
  <xsl:choose>
    <xsl:when test="bibtex:booktitle">
      <xsl:apply-templates select="bibtex:title"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="bibtex:chapter"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:choose>
    <xsl:when test="bibtex:chapter/bibtex:author">
      <xsl:apply-templates select="bibtex:chapter/bibtex:author"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
    </xsl:otherwise>
  </xsl:choose>
  <typeOfResource>text</typeOfResource>
  <genre authority="xbib">chapter</genre>
  <relatedItem type="host">
    <xsl:apply-templates select="bibtex:title|bibtex:booktitle"/>
    <xsl:if test="bibtex:chapter/bibtex:author">
      <xsl:apply-templates select="bibtex:editor|bibtex:author"/>
    </xsl:if>
    <typeOfResource>text</typeOfResource>
    <genre authority="xbib">book</genre>
    <xsl:apply-templates select="bibtex:publisher"/>
    <xsl:apply-templates select="bibtex:pages"/>
  </relatedItem>
  <xsl:call-template name="commonfields"/>
</xsl:template>

<xsl:template match="bibtex:article">
  <!-- journal -->
  <xsl:apply-templates select="bibtex:title"/>
  <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
  <xsl:apply-templates select="bibtex:journal"/>
  <xsl:call-template name="commonfields"/>
</xsl:template>

<xsl:template match="bibtex:incollection">
  <!-- serial -->
  <xsl:apply-templates select="bibtex:title"/>
  <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
  <typeOfResource>text</typeOfResource>
  <relatedItem type="host">
    <xsl:apply-templates select="bibtex:booktitle|bibtex:series"/>
    <xsl:apply-templates select="bibtex:publisher"/>
    <genre authority="xbib">book</genre>
  </relatedItem>
  <xsl:call-template name="commonfields"/>
</xsl:template>

<xsl:template match="bibtex:proceedings|bibtex:inproceedings|bibtex:conference">
  <!-- conference publication -->
  <xsl:apply-templates select="bibtex:title"/>
  <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
  <typeOfResource>text</typeOfResource>
  <genre authority="xbib">conference paper</genre>
  <relatedItem type="host">
    <xsl:apply-templates select="bibtex:booktitle|bibtex:series|
				 bibtex:organization|
				 bibtex:institution|bibtex:school"/>
    <genre authority="xbib">conference publication</genre>
    <originInfo>
      <xsl:apply-templates select="bibtex:address"/>
      <xsl:if test="bibtex:publisher">
	<publisher>
	  <xsl:value-of select="bibtex:publisher"/>
	</publisher>
      </xsl:if>
      <xsl:apply-templates select="bibtex:year"/>
      <xsl:apply-templates select="bibtex:edition"/>
    </originInfo>
    <xsl:apply-templates select="bibtex:pages"/>
  </relatedItem>
  <xsl:call-template name="commonfields"/>
</xsl:template>

<xsl:template match="bibtex:manual|bibtex:techreport|
		     bibtex:mastersthesis|bibtex:phdthesis|
		     bibtex:unpublished|bibtex:misc">
  <xsl:apply-templates select="bibtex:title"/>
  <xsl:apply-templates select="bibtex:author|bibtex:editor"/>
  <xsl:apply-templates select="bibtex:organization|bibtex:institution|bibtex:school"/>
  <xsl:choose>
    <xsl:when test="name()='bibtex:unpublished'">
      <typeOfResource manuscript="yes">text</typeOfResource>
    </xsl:when>
    <xsl:otherwise>
      <typeOfResource>text</typeOfResource>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:choose>
    <xsl:when test="name()='bibtex:manual'">
      <genre authority="xbib">instruction</genre>
    </xsl:when>
    <xsl:when test="contains(name(),'thesis')">
      <genre authority="xbib">theses</genre>
    </xsl:when>
    <xsl:when test="name()='bibtex:techreport'">
      <genre authority="xbib">technical report</genre>
    </xsl:when>
  </xsl:choose>
  <xsl:if test="bibtex:address|bibtex:publisher|bibtex:year|bibtex:edition">
    <originInfo>
      <xsl:apply-templates select="bibtex:year"/>
      <xsl:if test="bibtex:publisher">
	<publisher>
	  <xsl:value-of select="bibtex:publisher"/>
	</publisher>
      </xsl:if>
      <xsl:apply-templates select="bibtex:address"/>
      <xsl:apply-templates select="bibtex:edition"/>
    </originInfo>
  </xsl:if>
  <xsl:call-template name="commonfields"/>
</xsl:template>


<xsl:template match="bibtex:title|bibtex:chapter">
  <xsl:choose>
    <xsl:when test="bibtex:subtitle">
      <titleInfo>
	<title>
	  <xsl:value-of select="normalize-space(bibtex:title)"/>
	</title>
	<subTitle>
	  <xsl:value-of select="normalize-space(bibtex:subtitle)"/>
	</subTitle>
      </titleInfo>
    </xsl:when>
    <xsl:otherwise>
      <titleInfo>
	<title>
	  <xsl:value-of select="normalize-space(.)"/>
	</title>
      </titleInfo>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="bibtex:author|bibtex:editor">
  <xsl:choose>
<!--
      <namePart type="given">Jane</namePart>
      <namePart type="family">Doe</namePart>
-->
    <xsl:when test="bibtex:person">
      <xsl:for-each select="bibtex:person">
	<name type="personal">
	  <namePart>
	    <xsl:value-of select="normalize-space(.)"/>
	  </namePart>
	  <role>
	    <roleTerm authority="marc" type="text">
	      <xsl:value-of select="substring-after(name(..),'bibtex:')"/>
	    </roleTerm>
	  </role>
	</name>
      </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      <name type="personal">
	<namePart>
	  <xsl:value-of select="normalize-space(.)"/>
	</namePart>
	<role>
	  <roleTerm authority="marc" type="text">
	    <xsl:value-of select="substring-after(name(),'bibtex:')"/>
	  </roleTerm>
	</role>
      </name>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="bibtex:journal">
  <originInfo>
    <xsl:apply-templates select="../bibtex:year"/>
  </originInfo>
  <typeOfResource>text</typeOfResource>
  <genre authority="xbib">article</genre>
  <relatedItem type="host">
    <titleInfo>
      <title>
	<xsl:value-of select="../bibtex:journal"/>
      </title>
    </titleInfo>
    <originInfo>
      <issuance>continuing</issuance>
    </originInfo>
    <genre authority="marc">periodical</genre>
    <genre authority="xbib">academic journal</genre><!-- newspaper|magazine -->
    <part>
      <date>
	<xsl:value-of select="normalize-space(../bibtex:year)"/>
      </date>
      <detail type="volume">
	<number>
	  <xsl:value-of select="../bibtex:volume"/>
	</number>
      </detail>
      <detail type="number"><!-- number|issue -->
	<number>
	  <xsl:value-of select="../bibtex:number"/>
	</number>
      </detail>
      <xsl:apply-templates select="../bibtex:pages"/>
    </part>
  </relatedItem>
</xsl:template>

<xsl:template match="bibtex:publisher">
  <originInfo>
    <xsl:apply-templates select="../bibtex:year"/>
    <publisher>
      <xsl:value-of select="normalize-space(.)"/>
    </publisher>
    <xsl:apply-templates select="../bibtex:address"/>
    <xsl:apply-templates select="../bibtex:edition"/>
  </originInfo>
</xsl:template>

<xsl:template match="bibtex:organization|bibtex:institution|bibtex:school">
  <name type="corporate">
    <namePart>
      <xsl:value-of select="normalize-space(.)"/>
    </namePart>
  </name>
  <originInfo>
    <xsl:apply-templates select="../bibtex:address"/>
    <xsl:apply-templates select="../bibtex:publisher"/>
    <xsl:apply-templates select="../bibtex:year"/>
    <xsl:apply-templates select="../bibtex:edition"/>
  </originInfo>
</xsl:template>

<xsl:template match="bibtex:year">
  <dateIssued encoding="w3cdtf">
    <xsl:value-of select="normalize-space(.)"/>
  </dateIssued>
</xsl:template>

<xsl:template match="bibtex:edition">
  <edition>
    <xsl:value-of select="normalize-space(.)"/>
  </edition>
</xsl:template>

<xsl:template match="bibtex:address">
  <place>
    <placeTerm>
      <xsl:value-of select="normalize-space(.)"/>
    </placeTerm>
  </place>
</xsl:template>

<xsl:template match="bibtex:pages">
  <extent unit="page">
    <xsl:choose>
      <xsl:when test="contains(.,'-')">
	<start>
	  <xsl:value-of select="substring-before(.,'-')"/>
	</start>
	<end>
	  <xsl:value-of select="substring-after(.,'-')"/>
	</end>
      </xsl:when>
      <xsl:otherwise>
	<start>
	  <xsl:value-of select="substring-before(.,'-')"/>
	</start>
      </xsl:otherwise>
    </xsl:choose>
  </extent>
</xsl:template>

<xsl:template match="bibtex:inbook/bibtex:pages|
		     bibtex:inproceedings/bibtex:pages">
  <part>
    <extent unit="page">
      <list>
	<xsl:value-of select="normalize-space(.)"/>
      </list>
    </extent>
  </part>
</xsl:template>

<xsl:template match="bibtex:booktitle|bibtex:series">
  <name type="conference">
    <namePart>
      <xsl:value-of select="normalize-space(.)"/>
    </namePart>
  </name>
</xsl:template>

<xsl:template match="bibtex:inbook/bibtex:booktitle|
		     bibtex:inbook/bibtex:series|
		     bibtex:incollection/bibtex:booktitle|
		     bibtex:incollection/bibtex:series">
  <titleInfo>
    <title>
      <xsl:value-of select="normalize-space(.)"/>
    </title>
  </titleInfo>
</xsl:template>

<xsl:template name="commonfields">
  <xsl:apply-templates select="bibtex:isbn|bibtex:issn|bibtex:doi|
			       bibtex:lccn|bibtex:mrnumber|bibtex:url|
			       bibtex:howpublished|bibtex:key"/>
  <xsl:apply-templates select="bibtex:language"/>
  <xsl:apply-templates select="bibtex:abstract"/>
  <xsl:apply-templates select="bibtex:keywords"/>
  <xsl:apply-templates select="bibtex:category"/>
  <xsl:apply-templates select="bibtex:note|bibtex:annote|bibtex:type"/>
  <!--<xsl:apply-templates select="bibtex:copyright"/>-->
  <identifier type="citekey">
    <xsl:value-of select="../@id"/>
  </identifier>
</xsl:template>

<xsl:template match="bibtex:isbn|bibtex:issn|bibtex:doi|bibtex:lccn
		     |bibtex:mrnumber">
  <identifier type="{substring-after(name(),'bibtex:')}">
    <xsl:value-of select="normalize-space(.)"/>
  </identifier>
</xsl:template>

<xsl:template match="bibtex:url">
  <identifier type="uri">
    <xsl:value-of select="normalize-space(.)"/>
  </identifier>
</xsl:template>

<xsl:template match="bibtex:howpublished">
  <!--
      special case for the often used
      howpublished = "\url{http://www.example.com/}",
  -->
  <xsl:if test="contains(.,'\url')">
    <identifier type="uri">
      <xsl:value-of select="substring-after(.,'\url')"/>
    </identifier>
  </xsl:if>
</xsl:template>

<xsl:template match="bibtex:key">
  <identifier type="citekey">
    <xsl:value-of select="normalize-space(.)"/>
  </identifier>
</xsl:template>

<xsl:template match="bibtex:language">
  <language>
    <languageTerm authority="iso639-2b" type="code">
      <xsl:value-of select="normalize-space(.)"/>
    </languageTerm>
  </language>
</xsl:template>

<xsl:template match="bibtex:note|bibtex:annote|bibtex:type">
  <note>
    <xsl:value-of select="normalize-space(.)"/>
  </note>
</xsl:template>

<xsl:template match="bibtex:abstract">
  <abstract>
    <xsl:value-of select="normalize-space(.)"/>
  </abstract>
</xsl:template>

<xsl:template match="bibtex:keywords">
  <subject>
    <xsl:choose>
      <xsl:when test="bibtex:keyword">
	<xsl:for-each select="bibtex:keyword">
	  <topic>
	    <xsl:value-of select="normalize-space(.)"/>
	  </topic>
	</xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<topic>
	  <xsl:value-of select="normalize-space(.)"/>
	</topic>
      </xsl:otherwise>
    </xsl:choose>
  </subject>
</xsl:template>

<xsl:template match="bibtex:category">
  <classification>
    <xsl:value-of select="normalize-space(.)"/>
  </classification>
</xsl:template>

<xsl:template match="bibtex:copyright" />


</xsl:stylesheet>
