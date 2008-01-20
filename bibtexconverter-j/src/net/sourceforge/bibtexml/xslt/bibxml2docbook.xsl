<?xml version="1.0"?>
<!-- $Id$ -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to DocBook 4.5 bibliography format.         -->
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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bibfunc="http://bibtexml.sf.net/functions"
    exclude-result-prefixes="bibtex xs bibfunc xsl">
    <!-- We could put xmlns="http://docbook.org/ns/docbook".
     But then the result does not validate any more against the dtd -->
  <xsl:output method="xml" indent="yes" encoding="utf-8"
      doctype-system="http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd"
      doctype-public="-//OASIS//DTD DocBook XML V4.5//EN"/>
  <xsl:param name="title" select="'BibTeXML bibliography'" as="xs:string" />

  <xsl:strip-space elements="*"/>

  <xsl:include href="include/bibfunc.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates select="bibtex:file"/>
  </xsl:template>

  <xsl:template match="bibtex:file">
    <bibliography>
      <title>
        <xsl:value-of select="$title" />
      </title>
      <xsl:apply-templates select="bibtex:entry"/>
    </bibliography>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <biblioentry>
      <xsl:attribute name="xreflabel" select="@id"/>
      <xsl:attribute name="id" select="@id"/>
      <xsl:call-template name="authors"/>
      <xsl:call-template name="date"/>
      <xsl:apply-templates select="*" />
      <xsl:apply-templates select="*/*"/>
    </biblioentry>
  </xsl:template>

<!-- authors -->
  <xsl:template name="authors">
    <authorgroup>
      <xsl:apply-templates select="descendant::element(bibtex:author)" mode="author-group"/>
      <xsl:apply-templates select="descendant::element(bibtex:editor)" mode="author-group" />
    </authorgroup>
  </xsl:template>

<!-- author -->
  <xsl:template match="bibtex:author" mode="author-group">
    <xsl:choose>
      <xsl:when test="exists(surname)">
        <author>
          <xsl:apply-templates />
        </author>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="tokenize(normalize-space(text()), ' and ', 'i')">
          <author>
            <xsl:apply-templates select="bibfunc:parse-author(.)/bibfunc:person/*"/>
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
          <xsl:apply-templates />
        </editor>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="tokenize(normalize-space(text()), ' and ', 'i')">
          <editor>
            <xsl:apply-templates select="bibfunc:parse-author(.)/bibfunc:person/*"/>
          </editor>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:givennames|bibfunc:first">
    <xsl:variable name="tokens" select="tokenize(., '\s+')"/>
    <firstname>
      <xsl:value-of select="$tokens[1]"/>
    </firstname>
    <xsl:for-each select="$tokens[position() = 2 to count($tokens)]">
      <othername role="middlename">
        <xsl:value-of select="."/>
      </othername>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="bibtex:surname|bibfunc:last">
    <surname>
      <xsl:value-of select="text()"/>
    </surname>
  </xsl:template>

  <xsl:template match="bibtex:jr|bibfunc:junior">
    <lineage>
      <xsl:value-of select="text()"/>
    </lineage>
  </xsl:template>

<!-- abstract -->
  <xsl:template
      match="*/bibtex:abstract">
    <abstract>
      <para>
        <xsl:value-of select="text()"/>
      </para>
    </abstract>
  </xsl:template>

<!-- address -->
  <xsl:template
      match="*/bibtex:address">
    <address>
      <xsl:value-of select="text()"/>
    </address>
  </xsl:template>

<!-- date/pubdate -->
  <xsl:template
      name="date">
    <xsl:if test="exists(bibtex:year)">
      <xsl:variable name="date">
        <xsl:if test="exists(bibtex:month)">
          <xsl:value-of select="bibtex:month" />
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:value-of select="bibtex:year" />
      </xsl:variable>
      <date>
        <xsl:copy-of select="$date"/>
      </date>
      <pubdate>
        <xsl:copy-of select="$date"/>
      </pubdate>
    </xsl:if>
  </xsl:template>

<!-- edition -->
  <xsl:template
      match="*/bibtex:edition">
    <edition>
      <xsl:value-of select="text()"/>
    </edition>
  </xsl:template>

<!-- biblioid -->
  <xsl:template
      match="*/bibtex:isbn | */bibtex:issn | */bibtex:doi">
    <biblioid>
      <xsl:attribute name="class" select="local-name()"/>
      <xsl:value-of select="text()"/>
    </biblioid>
  </xsl:template>

  <xsl:template
      match="*/bibtex:url">
    <biblioid class="uri">
      <xsl:value-of select="text()"/>
    </biblioid>
  </xsl:template>

<!-- issuenum -->
  <xsl:template match="*/bibtex:number">
    <issuenum>
      <xsl:value-of select="text()"/>
    </issuenum>
  </xsl:template>

<!-- orgname -->
  <xsl:template match="*/bibtex:organization|bibtex:institution|bibtex:school">
    <orgname>
      <xsl:value-of select="text()"/>
    </orgname>
  </xsl:template>

<!-- pagenums -->
  <xsl:template
      match="*/bibtex:pages">
    <pagenums>
      <xsl:value-of select="text()"/>
    </pagenums>
  </xsl:template>

<!-- publishername -->
  <xsl:template match="*/bibtex:publisher">
    <publishername>
      <xsl:value-of select="text()"/>
    </publishername>
  </xsl:template>

<!-- volumenum -->
  <xsl:template match="*/bibtex:volume">
    <volumenum>
      <xsl:value-of select="text()"/>
    </volumenum>
  </xsl:template>

<!-- citetitle journal -->
  <xsl:template match="*/bibtex:journal">
    <citetitle pubwork="journal">
      <xsl:value-of select="text()"/>
    </citetitle>
  </xsl:template>


<!-- chapter  -->
  <xsl:template match="*/bibtex:chapter">
    <citetitle pubwork="chapter">
      <xsl:value-of select="text()"/>
    </citetitle>
  </xsl:template>

<!-- booktitle -->
  <xsl:template match="*/bibtex:booktitle">
    <citetitle pubwork="book">
      <xsl:value-of select="text()"/>
    </citetitle>
  </xsl:template>



<!-- article -->
  <xsl:template match="bibtex:article|bibtex:manual|bibtex:inbook|bibtex:incollection|bibtex:unpublished|bibtex:misc|bibtex:techreport|bibtex:mastersthesis|bibtex:phdthesis|bibtex:unpublished">
    <citetitle pubwork="article">
      <xsl:value-of select="bibtex:title"/>
    </citetitle>
    <artpagenums>
      <xsl:value-of select="bibtex:pages"/>
    </artpagenums>
  </xsl:template>


<!-- book -->
  <xsl:template match="bibtex:book|bibtex:booklet">
    <citetitle pubwork="book">
      <xsl:value-of select="bibtex:title"/>
    </citetitle>
  </xsl:template>


<!-- conference proceedings -->
  <xsl:template match="bibtex:proceedings">
    <confgroup>
      <confdates>
        <xsl:value-of select="bibtex:month"/>
        <xsl:value-of select="bibtex:year"/>
      </confdates>
      <conftitle>
        <xsl:value-of select="bibtex:title"/>
      </conftitle>
      <xsl:choose>
        <xsl:when test="exists(bibtex:volume)">
          <confnum>
            <xsl:value-of select="bibtex:volume"/>
          </confnum>
        </xsl:when>
        <xsl:when test="exists(bibtex:number)">
          <confnum>
            <xsl:value-of select="bibtex:number"/>
          </confnum>
        </xsl:when>
      </xsl:choose>
    </confgroup>
    <citetitle pubwork="book">
      <xsl:value-of select="bibtex:title"/>
    </citetitle>
  </xsl:template>

  <xsl:template match="bibtex:proceedings|bibtex:inproceedings|bibtex:conference">
    <confgroup>
      <confdates>
        <xsl:value-of select="bibtex:month" />
        <xsl:value-of select="bibtex:year"/>
      </confdates>
      <conftitle>
        <xsl:value-of select="bibtex:booktitle"/>
      </conftitle>
      <xsl:choose>
        <xsl:when test="exists(bibtex:volume)">
          <confnum>
            <xsl:value-of select="bibtex:volume"/>
          </confnum>
        </xsl:when>
        <xsl:when test="exists(bibtex:number)">
          <confnum>
            <xsl:value-of select="bibtex:number"/>
          </confnum>
        </xsl:when>
      </xsl:choose>
    </confgroup>
    <citetitle pubwork="article">
      <xsl:value-of select="bibtex:title"/>
    </citetitle>
  </xsl:template>

  <xsl:template match="text()" priority="0.5" />

</xsl:stylesheet>
