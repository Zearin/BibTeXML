<?xml version="1.0"?>
<!-- $Id$ -->
<!--
 * Copyright (c) 2007 Moritz Ringler
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
-->
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
