<?xml version="1.0"?>
<!--<!DOCTYPE xsl:stylesheet SYSTEM "xslt.dtd">-->
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
      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
   />

  <!-- STYLESHEET PARAMETERS -->
  <!-- @param pdfDirURI pdf links will be formed
       by appending citekeys to this uri -->
  <xsl:param name="pdfDirURI">./</xsl:param>
  <!-- @param groupby entries will be grouped by this field -->
  <xsl:param name="groupby">refgroup</xsl:param>
  <xsl:variable name="local-groupby" select="replace($groupby, 'bibtex:', '')" />
  <xsl:template match="/">
    <html>
      <head>
        <title>References</title>
        <link href="default.css" type="text/css" rel="stylesheet"/>
    <meta name="creator"
      content="Generated using MODIFIED tools from http://bibtexml.sf.net/"/>
      <script type="text/javascript" src="toggle.js"/>
      </head>
      <body>
        <h1>References</h1>
        <p style="font-family:Arial,Helvetica,sans-serif; font-size:8pt; color:#aaaaaa">Generated on <xsl:value-of select="current-dateTime()"/></p>
    <xsl:apply-templates select="bibtex:file" />
<address>
Generated from XML using tools from
<a href="http://bibtexml.sf.net/">bibtexml.sf.net</a>
and <a href="http://www.cs.duke.edu/~sprenkle/bibtex2html/">Sara Sprenkle</a><br/><br/>
(<a href="http://www.gnu.org/copyleft/gpl.html">GPL</a>)
20030714 Vidar Bronken Gundersen, Zeger W. Hendrikse<br />
Style sheet: <xsl:text>$Id: bibxml2htmlg.xsl,v 1.9 2007/02/23 14:18:49 Moritz.Ringler Exp $</xsl:text> by Moritz Ringler, 2003-2006
</address>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="bibtex:file">
   <!-- Output a TOC -->
   <h2>Contents</h2>
   <ul>
   <xsl:for-each-group select="bibtex:entry" group-by="*/*[local-name()=$groupby]">
    <xsl:sort select="current-grouping-key()" order="ascending"/>
    <!-- Make this more elegant when you have time! -->
    <xsl:if test="current-grouping-key() ne ''">
    <li><a>
      <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of
        select="replace(current-grouping-key(),'[^a-zA-Z]','_')"/>
      </xsl:attribute>
      <xsl:value-of select="current-grouping-key()"/>
    </a></li>
    </xsl:if>
   </xsl:for-each-group>
   <li><a href="#novalue">No or empty <xsl:value-of
     select="$local-groupby"/></a></li>
   </ul>
   <xsl:for-each-group select="bibtex:entry" group-by="*/*[local-name()=$groupby]">
    <xsl:sort select="current-grouping-key()" order="ascending"/>
    <xsl:if  test="current-grouping-key() ne ''">
    <h2>
      <a><xsl:attribute name="name">
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
         <xsl:call-template name="bibtex-entry">
          <xsl:with-param name="group" select="replace(current-grouping-key(),'[^a-zA-Z]','_')"/>
         </xsl:call-template>
       </xsl:for-each>
    </table>
    </xsl:if>
   </xsl:for-each-group>
   <xsl:call-template name="novalue"/>
  </xsl:template>

  <xsl:template name="novalue">
  <h2><a name="novalue" id="novalue">No or empty <xsl:value-of
    select="local-groupby"/></a></h2>
        <table class="bibtexml-output" cellspacing="0">
       <tr>
     <th>Key</th>
     <th>Author(s)</th>
     <th>Year</th>
     <th>Title</th>
     <th>Citation</th>
       </tr>
       <xsl:apply-templates select="bibtex:entry" mode="novalue">
         <xsl:sort select="@id"/>
       </xsl:apply-templates>
    </table>
  </xsl:template>

  <xsl:template match="bibtex:entry" mode="novalue">
    <xsl:variable name="gb" select='*/*[local-name()=$groupby]'/>
    <xsl:if test="empty($gb) or '' = $gb">
        <xsl:value-of select="bibtex:key"/> 
        <xsl:call-template name="bibtex-entry">
            <xsl:with-param name="group" select="'novalue'"/>
        </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="bibtex-entry">
    <xsl:param name="group"/>
    <tr>
      <td class="citekey">
    <xsl:choose>
    <xsl:when test="exists(bibtex:article/bibtex:pdf)">
     <a>
    <xsl:attribute name="href"><xsl:value-of select="$pdfDirURI"/>
    <xsl:value-of select="bibtex:article/bibtex:pdf"/></xsl:attribute>
        <xsl:value-of select="@id"/>
    </a>
    </xsl:when>
    <xsl:otherwise>
    <a>
    <xsl:attribute name="href"><xsl:value-of select="$pdfDirURI"/>
    <xsl:value-of select="@id"/>.pdf</xsl:attribute>
        <xsl:value-of select="@id"/>
    </a>
    </xsl:otherwise>
    </xsl:choose>
    </td>
      <td class="author">
    <xsl:apply-templates select="*/bibtex:author"/>
      </td>
      <td class="year">
        <xsl:value-of select="*/bibtex:year"/>
      </td>
      <td class="title">
        <xsl:if test="exists(*/bibtex:abstract) and not(*/bibtex:abstract eq '')">
          <xsl:attribute name="onclick">toggleAbstract('<xsl:value-of
           select="$group"/>_<xsl:value-of
           select="@id"/>')</xsl:attribute>
        </xsl:if>
        <span>
            <xsl:if test="exists(*/bibtex:abstract) and not(*/bibtex:abstract eq '')">
                      <xsl:attribute name="title">
            <xsl:value-of select="substring(*/bibtex:abstract,0.0,100.0)"/>...</xsl:attribute>
            </xsl:if>
          <xsl:apply-templates select="*/bibtex:title"/>
        </span>
        <xsl:if test="exists(*/bibtex:abstract) and not(*/bibtex:abstract eq '')">
        <p class="abstract" style="display:none">
        <xsl:attribute name="id">abstract_<xsl:value-of
          select="$group"/>_<xsl:value-of
          select="@id"/></xsl:attribute>
        <xsl:value-of select="*/bibtex:abstract"/>
        </p>
        </xsl:if>
      </td>
      <td class="ref">
        <xsl:choose>
          <xsl:when test="exists(*/bibtex:url) and not(*/bibtex:url eq '')">
            <a><xsl:attribute name="href">
                <xsl:value-of select="*/bibtex:url"/>
              </xsl:attribute><xsl:call-template name="ref"/></a>
          </xsl:when>
          <xsl:when test="exists(*/bibtex:doi) and not(*/bibtex:doi eq '')">
            <a><xsl:attribute name="href">
                <xsl:text>http://dx.doi.org/</xsl:text><xsl:value-of select="*/bibtex:doi"/>
               </xsl:attribute><xsl:call-template name="ref"/></a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="ref"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="author" match="*/bibtex:author">
    <xsl:value-of select="."/>
    <xsl:text>;</xsl:text><br />
  </xsl:template>

  <xsl:template name="ref">
  <!-- publication specific -->
    <xsl:choose>
      <!-- BOOK -->
      <xsl:when test='local-name(./*)="book"'>
        <xsl:choose>
          <!-- ISBN -->
          <xsl:when test="exists(*/bibtex:isbn)">
          <a>
            <xsl:attribute name="href">
              <xsl:text>http://de.wikipedia.org/w/index.php?title=Spezial:Booksources&amp;isbn=</xsl:text><xsl:value-of select="*/bibtex:isbn"/>
            </xsl:attribute>
            ISBN <xsl:value-of select="*/bibtex:isbn"/>
          </a>
          </xsl:when>
          <!-- Address -->
          <xsl:otherwise>
            <xsl:value-of select="bibtex:book/bibtex:publisher"/>
            <xsl:text>, </xsl:text>
            <xsl:value-of select="bibtex:book/bibtex:address"/>
            <xsl:text>. &#xA;</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!-- ARTICLE -->
      <xsl:when test='local-name(./*)="article"'>
        <xsl:value-of select="bibtex:article/bibtex:journal"/>
        <xsl:text> </xsl:text><span class="vol"><xsl:value-of
          select="bibtex:article/bibtex:volume"/></span>
        <xsl:text> (</xsl:text><xsl:value-of
          select="bibtex:article/bibtex:year"/><xsl:text>), </xsl:text>
        <xsl:value-of select="bibtex:article/bibtex:pages"/>
        <xsl:text>.</xsl:text>
      </xsl:when><!--
        <xsl:if test='local-name(./*)="techreport"'>
      <xsl:value-of select="bibtex:techreport/bibtex:institution"/>
        </xsl:when>
        <xsl:if test='local-name(./*)="manual"'>
      <xsl:value-of select="bibtex:manual/bibtex:organization"/>
      &lt;URL: <xsl:value-of select="*/bibtex:url"/>&gt;
        </xsl:when>
-->   <xsl:otherwise>
        <xsl:text>&#160;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:transform>
