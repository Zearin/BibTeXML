<?xml version="1.0"?>
<!-- $Id: bibxml2html.xsl 133 2007-03-19 17:38:30Z ringler $ -->
<xsl:transform version="2.0"
        xmlns:bibtex="http://bibtexml.sf.net/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2003/11/xpath-functions"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    exclude-result-prefixes="bibtex fn dc">
  <xsl:output
      method="xhtml"
      indent="yes"
      version="1.0"
      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
   />
   <xsl:strip-space elements="meta head"/>
  <xsl:param name="pdfDirURI">./</xsl:param>

  <xsl:include href="bibxml2html.xsl"/>

  <xsl:template match="bibtex:file">
    <table class="bibtexml-output" cellspacing="0">
       <tr>
     <th><xsl:text>Key</xsl:text></th>
     <th><xsl:text>Author(s)</xsl:text></th>
     <th><xsl:text>Year</xsl:text></th>
     <th><xsl:text>Title</xsl:text></th>
     <th><xsl:text>Citation</xsl:text></th>
       </tr>
    <xsl:apply-templates select="bibtex:entry">
           <xsl:sort select="@id"/>
    </xsl:apply-templates>
    </table>
  </xsl:template>

</xsl:transform>
