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
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:my="bibfunc:bar"
    xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <xsl:param name="bibtexml.sf.net.encoding" select="'ISO-8859-1'" />
  <xsl:param name="protectTitleCapitalization" select="false" as="xs:boolean" />
  <xsl:output method="text"
      media-type="application/x-bibtex"
      encoding="ISO-8859-1" />

  <!-- uses only bibtexml-latex-warning from extended.xsl
       when processing bibtexconverter-generated input -->
  <xsl:include href="include/extended.xsl"/>

  <!--
      Be adviced that this converter does no validation or
      error checking of the input BibTeXML data, as this is
      assumed to be a valid BibTeXML document instance.
  -->

  <xsl:template match="bibtex:file" priority="1">
    <xsl:call-template name="bibtexml-latex-warning"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="bibtex:preamble" priority="1">
    <xsl:text>@PREAMBLE{"</xsl:text><xsl:value-of
      select="text()"/><xsl:text>"}&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:entry" priority="1">
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="bibtex:entry/bibtex:*">
    <xsl:text>@</xsl:text>
    <xsl:value-of select="local-name()"/>
    <xsl:text>{</xsl:text>
    <xsl:value-of select="../@id"/>
    <xsl:text>,</xsl:text>
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>}</xsl:text>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:title" priority="0.6">
    <xsl:variable name="text" select="normalize-space(text())"/>
    <xsl:text>   </xsl:text>
    <xsl:value-of select="local-name()"/>
    <xsl:text> = {</xsl:text>
    <xsl:value-of select="if ($protectTitleCapitalization) then replace($text,'(\p{Lu})','{$1}') else $text" />
    <xsl:text>},&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:author|bibtex:editor" priority="0.6">
    <xsl:variable name="me" select="local-name()"/>
    <xsl:variable name="brothers" select="../bibtex:*[local-name() eq $me]"/>

    <xsl:if test="empty(./*) and (. = $brothers[1])"> <!-- no output for containers -->
      <xsl:text>   </xsl:text>
      <xsl:value-of select="$me"/>
      <xsl:text>= {</xsl:text>
      <xsl:value-of select="text()"/>
      <xsl:apply-templates select="$brothers" mode="join-authors"/>
      <xsl:text>},&#xA;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:author|bibtex:editor" mode="join-authors">
    <xsl:if test="position() ne 1">
      <xsl:text> and </xsl:text>
      <xsl:choose>
        <xsl:when test="exists(bibtex:others)">
          <xsl:text>others</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="text()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:entry/*/bibtex:*" priority="0.5">
    <xsl:variable name="myname" select="name()" />
    <xsl:variable name="my-local-name" select="local-name()" />
    <xsl:variable name="brothers" select="../*[name() = $myname]"/>

    <xsl:if test="empty(./*) and (. = $brothers[1])"> <!-- no output for containers -->
      <xsl:text>   </xsl:text>
      <xsl:value-of select="if ($my-local-name eq 'keyword') then 'keywords' else $my-local-name"/>
      <xsl:text> = {</xsl:text>
      <xsl:value-of select="string-join($brothers, ', ')"/>
      <xsl:text>},&#xA;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" priority="0" />

  <!-- Do we need this? Or do we suppose we have escaped text in the xml? -->
  <!--
  <xsl:function name="my:escape">
   <xsl:param name="input" as="xs:string"/>
     <xsl:sequence select="
      replace(
    replace(
      replace(
        replace($input, '\', '\\'}

     "/>
   </xsl:function>
  $ & % # _ { } ~ ^ \
  -->

</xsl:stylesheet>
