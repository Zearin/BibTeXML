<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <xsl:template match="bibtex:edition|bibtex:pages">
    <xsl:text>, </xsl:text>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:person">
    <!--
	1 author:                  "Simons, RC"
	2 authors:                 "Robinson, WF and Huxtable, CRR"
	3 authors|editors or more: "Raymond Evans and others"
    -->
    <xsl:choose>
      <xsl:when test="count(../bibtex:person)>2">
	<xsl:if test="position()&lt;2">
	  <xsl:apply-templates />
	  <xsl:text> et al.</xsl:text>
	</xsl:if>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates />
	<xsl:if test="position()=1">
	  <xsl:text> and </xsl:text>
	</xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="editor-add-text"/>
  </xsl:template>

  <xsl:template name="editor-add-text">
    <xsl:if test="name()='bibtex:editor'">
      <xsl:choose>
	<xsl:when test="count(bibtex:person)>1">
	  <xsl:text> (eds)</xsl:text>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:text> (ed.)</xsl:text>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:editor">
    <!--  Conflicts with rule in html-linkify.  -->
    <xsl:apply-templates/>
    <xsl:call-template name="editor-add-text"/>
  </xsl:template>

  <xsl:template match="bibtex:chapter">
    <xsl:text>`</xsl:text>
      <xsl:apply-templates />
    <xsl:text>' in </xsl:text>
  </xsl:template>


  <xsl:template name="publisher">
    <xsl:apply-templates select="bibtex:edition"/>
    <xsl:text> (</xsl:text>
    <xsl:apply-templates select="bibtex:address"/>
    <xsl:if test="bibtex:address and (bibtex:publisher or
		      bibtex:organization or bibtex:institution)">
      <xsl:text>: </xsl:text>
    </xsl:if>
    <xsl:apply-templates
	select="bibtex:publisher|bibtex:organization|bibtex:institution"/>
    <xsl:if test="bibtex:year">
      <xsl:text>, </xsl:text>
      <xsl:apply-templates select="bibtex:year"/>
    </xsl:if>
    <xsl:text>)</xsl:text>
  </xsl:template>


  <xsl:template match="bibtex:volume">
    <xsl:text> </xsl:text>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:number">
    <xsl:text>, no.</xsl:text>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template name="magazinenumber">
    <xsl:apply-templates select="bibtex:volume"/>
    <xsl:apply-templates select="bibtex:number"/>
    <xsl:if test="bibtex:year">
      <xsl:text> (</xsl:text>
      <xsl:apply-templates select="bibtex:year"/>
      <xsl:text>)</xsl:text>
    </xsl:if>
  </xsl:template>


  <xsl:template match="bibtex:doi|bibtex:isbn|bibtex:issn|bibtex:lccn">
    <xsl:text>, </xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:text>:</xsl:text>
    <xsl:apply-templates />
  </xsl:template>


</xsl:stylesheet>
