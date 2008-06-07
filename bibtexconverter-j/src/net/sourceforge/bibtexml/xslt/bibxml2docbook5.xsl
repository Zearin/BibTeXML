<?xml version="1.0"?>
<!-- $Id: bibxml2docbook.xsl 453 2008-01-27 20:43:18Z ringler $ -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to DocBook 5 bibliography format.         -->
<!--
 * Copyright (c) 2007-2008 Moritz Ringler, Max Berger
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
    xmlns="http://docbook.org/ns/docbook"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bibfunc="http://bibtexml.sf.net/functions"
    exclude-result-prefixes="bibtex xs bibfunc xsl">
  <xsl:import href="bibxml2docbook.xsl"/>
  <xsl:include href="include/bibfunc.xsl"/>

  <xsl:output method="xml" indent="yes" encoding="utf-8" />
  <xsl:param name="title" select="'BibTeXML bibliography'" as="xs:string" />

  <xsl:strip-space elements="*"/>

  <xsl:template match="bibtex:file">
    <bibliography version="5.0">
      <title>
        <xsl:value-of select="$title" />
      </title>
      <xsl:apply-templates select="bibtex:entry"/>
    </bibliography>
  </xsl:template>

<!-- author -->
  <xsl:template match="bibtex:author" mode="author-group">
    <xsl:choose>
      <xsl:when test="exists(surname)">
        <author>
          <personname>
            <xsl:apply-templates />
          </personname>
        </author>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="tokenize(normalize-space(text()), ' and ', 'i')">
          <author>
            <personname>
              <xsl:apply-templates select="bibfunc:parse-author(.)/bibfunc:person/*"/>
            </personname>
          </author>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!-- editor -->
  <xsl:template match="bibtex:editor" mode="author-group">
    <xsl:choose>
      <xsl:when test="exists(surname)">
        <editor>
          <personname>
            <xsl:apply-templates />
          </personname>
        </editor>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="tokenize(normalize-space(text()), ' and ', 'i')">
          <editor>
            <personname>
              <xsl:apply-templates select="bibfunc:parse-author(.)/bibfunc:person/*"/>
            </personname>
          </editor>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
