<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <xsl:template match="bibtex:chapter">
    <xsl:text>`</xsl:text>
      <xsl:apply-templates />
    <xsl:text>' in </xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:year">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates />
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:edition">
    <xsl:text> (</xsl:text>
    <xsl:apply-templates />
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template name="publisher">
    <xsl:apply-templates select="bibtex:edition"/>
    <xsl:text>. </xsl:text>
    <xsl:apply-templates select="bibtex:address"/>
    <xsl:if test="bibtex:address and (bibtex:publisher or
		      bibtex:organization or bibtex:institution)">
      <xsl:text>: </xsl:text>
    </xsl:if>
    <xsl:apply-templates
	select="bibtex:publisher|bibtex:organization|bibtex:institution"/>
  </xsl:template>


  <xsl:template match="bibtex:pages">
    <xsl:choose>
      <xsl:when test="contains(.,'-')">
	<xsl:text>, pp.</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>, p.</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:volume">
    <xsl:text> </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  <xsl:template match="bibtex:number">
    <xsl:text> (</xsl:text>
    <xsl:apply-templates />
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:person">
    <xsl:apply-templates/>
    <xsl:choose>
      <xsl:when test="position()=last()-1">
	<xsl:text> &amp; </xsl:text>
      </xsl:when>
      <xsl:when test="not(position()=last()-1)">
        <!-- Changed to last()-1 as suggested by Andreas Fabri -->
	<xsl:text>, </xsl:text>
      </xsl:when>
    </xsl:choose>
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

  <xsl:template match="bibtex:doi|bibtex:isbn|bibtex:issn|bibtex:lccn">
    <xsl:text>, </xsl:text>
    <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
    <xsl:text>:</xsl:text>
    <xsl:apply-templates />
  </xsl:template>

</xsl:stylesheet>
