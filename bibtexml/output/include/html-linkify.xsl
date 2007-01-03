<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:bibtex="http://bibtexml.sf.net/"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!--
      When imported, this stylesheet is intended to make
      author and title text hyperlinked to search amazon.com
      Requires the parent XSLT style sheet to use

         <xsl:apply-templates select="bibtex:author" />

      instead of

         <xsl:value-of select="bibtex:author" />
  -->

  <xsl:template match="bibtex:title|bibtex:booktitle|
		       bibtex:author|bibtex:editor">
    <xsl:variable name="url">
      <xsl:text>http://www.amazon.com/exec/obidos/</xsl:text>
      <xsl:choose>
	<xsl:when test="name()='bibtex:author' or
			name()='bibtex:editor'">
	  <xsl:text>author=</xsl:text>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:text>title=</xsl:text>
	</xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
      <!--
	  <xsl:value-of select='normalize-space(
	                    translate(.," ,:;.&amp;_-"," "))'/>
      -->
    </xsl:variable>
    <a href="{$url}">
      <xsl:apply-templates/>
    </a>
    <xsl:call-template name="editor-add-text"/>
  </xsl:template>


  <xsl:template match="bibtex:keywords|bibtex:category|bibtex:journal">
    <xsl:variable name="url">
      <xsl:text>http://www.google.com/search?q=</xsl:text>
      <xsl:value-of select='normalize-space(
			    translate(.," ,:;.&amp;-_","+"))'/>
    </xsl:variable>
    <a href="{$url}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="bibtex:isbn|bibtex:issn">
    <xsl:variable name="url">
      <xsl:text>http://www.amazon.com/exec/obidos/</xsl:text>
      <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
      <xsl:text>=</xsl:text>
      <xsl:value-of select='normalize-space(
			    translate(.," ,:;.&amp;-_","+"))'/>
    </xsl:variable>
    <xsl:text>, </xsl:text>
    <a href="{$url}">
      <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
      <xsl:text>:</xsl:text>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="bibtex:doi">
    <xsl:variable name="url">
      <xsl:text>http://dx.doi.org/hdl=</xsl:text>
      <xsl:value-of select='.'/>
    </xsl:variable>
    <xsl:text>, </xsl:text>
    <a href="{$url}">
      <xsl:value-of select='substring-after(name(.),"bibtex:")'/>
      <xsl:text>:</xsl:text>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="bibtex:url">
    <xsl:text>, </xsl:text>
    <!-- <xsl:text>[Online] </xsl:text> -->
    <xsl:text>Available at </xsl:text>
    <a href="{.}">
      <xsl:value-of select="."/>
    </a>
    <xsl:text> (accessed 200x.xx.xx)</xsl:text>
  </xsl:template>


</xsl:stylesheet>
