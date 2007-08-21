<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) 2007 Moritz Ringler -->
<xsl:transform version="2.0"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2003/11/xpath-functions"
    xmlns:saxon="http://saxon.sf.net/"
    exclude-result-prefixes="bibtex fn saxon">

  <xsl:output
      method="xhtml"
      indent="yes"
      version="1.0"
      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" />

  <xsl:param name="pdfDirURI">./</xsl:param>
  <!-- @param groupby entries will be grouped by this field -->
  <xsl:param name="groupby">keywords</xsl:param>
  <xsl:variable name="local-groupby" select="replace($groupby, 'bibtex:', '')" />

  <xsl:include href="bibxml2html.xsl"/>

  <xsl:template match="bibtex:file">
    <xsl:call-template name="toc"/>
    <xsl:call-template name="groups"/>
    <xsl:call-template name="no-group"/>
  </xsl:template>

  <xsl:template name="toc">
  <!-- Output a TOC -->
    <h2>Contents</h2>
    <ul>
      <xsl:for-each-group select="bibtex:entry" group-by="*/*[local-name()=$groupby]">
        <xsl:sort select="current-grouping-key()" order="ascending"/>
    <!-- Make this more elegant when you have time! -->
        <xsl:if test="current-grouping-key() ne ''">
          <li>
            <a>
              <xsl:attribute name="href">
                <xsl:text>#</xsl:text>
                <xsl:value-of
                    select="replace(current-grouping-key(),'[^a-zA-Z]','_')"/>
              </xsl:attribute>
              <xsl:value-of select="current-grouping-key()"/>
            </a>
          </li>
        </xsl:if>
      </xsl:for-each-group>
      <li>
        <a href="#no-group">
          <xsl:text>No or empty </xsl:text>
          <xsl:value-of
              select="$local-groupby"/>
        </a>
      </li>
    </ul>
  </xsl:template>

  <xsl:template name="groups">
    <xsl:for-each-group select="bibtex:entry" group-by="*/*[local-name()=$groupby]">
      <xsl:sort select="current-grouping-key()" order="ascending"/>
      <xsl:if test="current-grouping-key() ne ''">
        <h2>
          <a>
            <xsl:attribute name="name">
              <xsl:value-of select="replace(current-grouping-key(),'[^a-zA-Z]','_')"/>
            </xsl:attribute>
            <xsl:attribute name="id">
              <xsl:value-of select="replace(current-grouping-key(),'[^a-zA-Z]','_')"/>
            </xsl:attribute>
            <xsl:value-of select="current-grouping-key()"/>
          </a>
        </h2>
        <table class="bibtexml-output" cellspacing="0">
          <tr>
            <th>Key</th>
            <th>Author(s)</th>
            <th>Year</th>
            <th>Title</th>
            <th>Citation</th>
          </tr>
          <xsl:for-each select="current-group()">
            <xsl:sort select="@id"/>
            <xsl:apply-templates select=".">
              <xsl:with-param name="group" select="replace(current-grouping-key(),'[^a-zA-Z]','_')"/>
            </xsl:apply-templates>
          </xsl:for-each>
        </table>
      </xsl:if>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="no-group">
    <h2>
      <a name="no-group" id="no-group">No or empty <xsl:value-of
            select="$local-groupby"/>
      </a>
    </h2>
    <table class="bibtexml-output" cellspacing="0">
      <tr>
        <th>Key</th>
        <th>Author(s)</th>
        <th>Year</th>
        <th>Title</th>
        <th>Citation</th>
      </tr>
      <xsl:apply-templates select="bibtex:entry" mode="no-group">
        <xsl:sort select="@id"/>
      </xsl:apply-templates>
    </table>
  </xsl:template>

  <xsl:template match="bibtex:entry" mode="no-group">
    <xsl:variable name="gb" select="*/*[local-name()=$groupby]"/>
    <xsl:if test="empty($gb) or '' = $gb">
      <xsl:value-of select="bibtex:key"/>
      <xsl:apply-templates select=".">
        <xsl:with-param name="group" select="'no-group'"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

</xsl:transform>
