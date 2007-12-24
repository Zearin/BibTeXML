<?xml version="1.0" encoding="UTF-8" ?>
<!-- $Id: bibxml2mods32.xsl 338 2007-08-27 17:27:58Z ringler $
     (c) Moritz Ringler, 2007

      XSLT stylesheet that turn converts bibtexml to bibtexml
      turning dois into urls using the

  Implements the "inherit" feature of bibtex crossref fields

 (c) Moritz Ringler, 2007

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
-->
<xsl:transform version="2.0"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="bibtex xs">
  <xsl:key match="bibtex:entry" name="idkey" use="@id"/>

  <xsl:output
      method="xml"
      indent="yes"
      version="1.0"/>

  <!-- Document root -->
  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:crossref">
    <xsl:call-template name="copy"/>
    <xsl:apply-templates mode="copy-crossref" select="key('idkey', normalize-space(text()))/bibtex:*/bibtex:*">
      <xsl:with-param name="referencing-node" select=".." />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="bibtex:*" mode="copy-crossref">
      <xsl:param name="referencing-node" select="."/>
      <xsl:variable name="qn" select="name()"/>
      <xsl:if test="not(exists($referencing-node/*[name() = $qn]))">
        <xsl:call-template name="copy"/>

      </xsl:if>
  </xsl:template>

  <xsl:template match="*|text()|@*">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template name="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

</xsl:transform>
