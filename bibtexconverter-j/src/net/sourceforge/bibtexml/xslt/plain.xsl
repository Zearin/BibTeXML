<?xml version="1.0"?>
<!-- $Id: bibxml2bib.xsl 391 2007-12-23 17:42:35Z ringler $ -->
<!-- This stylesheet produces a latex thebibliography environment
     (bbl file) that is similar to what bibtex produces with the
     standard bibtex style plain.bst.
     Notable differences include:
      * braces are completely ignored (e.g. in author parsing)
      * case is never changed except in edition (always lower)
      * some differences concerning tying with ~ under very rare
        circumstances
      * long lines are not wrapped

      *  unicode-aware sorting. '&auml;' is treated like 'a', not like ''
-->
<!--
 * Copyright (c) 2008 Moritz Ringler
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
    xmlns:bibfunc="http://bibtexml.sf.net/functions"
    xmlns:bibsort="http://bibtexml.sf.net/sort"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:auxparser="java:net.sourceforge.bibtexml.AuxParser"
    xmlns:defcoll="net.sourceforge.bibtexml.DefaultCollator"
    xmlns:my="foo:bar">

  <xsl:import href="unsrt.xsl"/>

  <xsl:param name="bibtexml.sf.net.encoding" select="'ISO-8859-1'" />
  <xsl:param name="aux-file" select="''" as="xs:string"/>
  <xsl:param name="aux-encoding" select="'ISO-8859-1'" as="xs:string"/>
  <xsl:variable name="global-max" select="11"/>

  <xsl:key match="*" name="idkey" use="@id"/>

  <xsl:output method="text"
      media-type="application/x-bibtex"
      encoding="ISO-8859-1" />

  <xsl:template match="bibtex:file">
    <!--
      Retrieve citations from aux file
    -->
    <xsl:variable name="citations" select="my:citations()"
      as="xs:string*"/>

    <xsl:apply-templates select="bibtex:preamble"/>
    <xsl:if test="my:exists(bibtex:preamble)">
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
    <!--
      Have \(no)cite{*} ?
    -->
    <xsl:variable name="has-star" select="exists(index-of($citations, '*'))"/>
    <xsl:text>\begin{thebibliography}{</xsl:text>
    <xsl:value-of select="my:normalize-count(
        if($has-star) then count(bibtex:entry) else count($citations), 1)"/>
    <xsl:text>}&#xA;</xsl:text>
    <xsl:apply-templates select="if($has-star) then bibtex:entry else key('idkey', $citations)">
      <xsl:sort select="xs:string(bibsort:author(.))"
      collation="http://saxon.sf.net/collation?class=net.sourceforge.bibtexml.DefaultCollator"
      use-when="function-available('defcoll:new')"/>
      <xsl:sort select="xs:string(bibsort:author(.))"
      use-when="not(function-available('defcoll:new'))"/>
      <xsl:sort select="xs:integer(bibsort:year(.))"/>
      <xsl:sort select="xs:string(bibsort:title(.))"
      collation="http://saxon.sf.net/collation?class=net.sourceforge.bibtexml.DefaultCollator"
      use-when="function-available('defcoll:new')" />
      <xsl:sort select="xs:string(bibsort:title(.))"
      use-when="not(function-available('defcoll:new'))" />
    </xsl:apply-templates>
    <xsl:text>&#xA;\end{thebibliography}</xsl:text>
  </xsl:template>

  <xsl:function name="bibsort:year">
    <xsl:param name="me"/>
    <xsl:sequence select="if (my:exists($me/bibtex:*/bibtex:year))
                          then xs:integer($me/bibtex:*/bibtex:year[1]/text())
                          else xs:integer('0')"/>
  </xsl:function>

  <xsl:function name="bibsort:title">
    <xsl:param name="me"/>
    <xsl:apply-templates select="$me/bibtex:*/bibtex:title" mode="sort"/>
  </xsl:function>

  <xsl:function name="bibsort:author">
    <xsl:param name="me"/>
    <xsl:variable name="result">
      <xsl:apply-templates select="$me/bibtex:*" mode="sort-author"/>
    </xsl:variable>
    <xsl:sequence select="xs:string($result)" />
  </xsl:function>

  <xsl:template match="bibtex:book|bibtex:inbook" mode="sort-author">
    <xsl:choose>
      <xsl:when test="my:exists(bibtex:author)">
        <xsl:call-template name="author-sort-key"/>
      </xsl:when>
      <xsl:when test="my:exists(bibtex:editor)">
        <xsl:call-template name="editor-sort-key"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="bibtex:key" mode="sort"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:proceedings" mode="sort-author">
    <xsl:choose>
      <xsl:when test="my:exists(bibtex:editor)">
        <xsl:call-template name="editor-sort-key"/>
      </xsl:when>
      <xsl:when test="my:exists(bibtex:organization)">
        <xsl:apply-templates select="bibtex:organization" mode="sort"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="bibtex:key" mode="sort"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:manual" mode="sort-author">
    <xsl:choose>
      <xsl:when test="my:exists(bibtex:author)">
        <xsl:call-template name="author-sort-key"/>
      </xsl:when>
      <xsl:when test="my:exists(bibtex:organization)">
        <xsl:apply-templates select="bibtex:organization" mode="sort"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="bibtex:key" mode="sort"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:*" mode="sort-author">
    <xsl:call-template name="author-sort-key"/>
    <xsl:if test="my:empty(bibtex:author)">
      <xsl:apply-templates select="bibtex:key" mode="sort"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:key" mode="sort">
    <xsl:value-of select="my:sortify(bibtex:key[1]/text())"/>
  </xsl:template>

  <xsl:template match="bibtex:title" mode="sort">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:choose>
      <xsl:when test="starts-with($tt, 'A ')">
        <xsl:sequence select="substring(
            my:sortify(substring($tt, 3)),
            1, $global-max)"/>
      </xsl:when>
      <xsl:when test="starts-with($tt, 'An ')">
        <xsl:sequence select="substring(
            my:sortify(substring($tt, 4)),
            1, $global-max)"/>
      </xsl:when>
      <xsl:when test="starts-with($tt, 'The ')">
        <xsl:sequence select="substring(
            my:sortify(substring($tt, 5)),
            1, $global-max)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="substring(
            my:sortify($tt),
            1, $global-max)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:organization" mode="sort">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:choose>
      <xsl:when test="starts-with($tt, 'The ')">
        <xsl:sequence select="my:sortify(substring($tt, 5))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="my:sortify($tt)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="editor-sort-key">
    <xsl:if test="my:exists(bibtex:editor)">
      <xsl:variable name="raw">
      <xsl:apply-templates select="bibtex:editor" mode="sort">
        <xsl:with-param name="person-count" select="count(bibtex:editor)"/>
      </xsl:apply-templates>
      </xsl:variable>
      <xsl:value-of select="my:sortify($raw)"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="author-sort-key">
    <xsl:if test="my:exists(bibtex:author)">
      <xsl:variable name="raw">
      <xsl:apply-templates select="bibtex:author" mode="sort">
        <xsl:with-param name="person-count" select="count(bibtex:author)"/>
      </xsl:apply-templates>
      </xsl:variable>
      <xsl:value-of select="my:sortify($raw)"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="bibtex:author|bibtex:editor" mode="sort">
    <xsl:param name="person-count" as="xs:integer" required="yes"/>
    <xsl:variable name="pos" select="position()"/>
    <xsl:choose>
        <xsl:when test="bibtex:others and ($pos eq $person-count)">
            <xsl:text> et al</xsl:text>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates select="bibfunc:parse-author(text())" mode="sort"/>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibfunc:person" mode="sort">
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="bibfunc:last" mode="sort"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="bibfunc:first" mode="sort"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="bibfunc:junior" mode="sort"/>
  </xsl:template>

  <xsl:template match="bibfunc:first|bibfunc:last|bibfunc:junior" mode="sort">
    <xsl:value-of select="text()"/>
  </xsl:template>

  <xsl:function name="my:sortify">
    <xsl:param as="xs:string" name="txt"/>
    <xsl:variable name="lower" select="lower-case(normalize-space($txt))"/>
    <xsl:variable name="sep-char-to-space" select="replace($lower, '[~-]',' ')"/>
    <xsl:sequence select="replace($sep-char-to-space, '[\P{L}-[ 0-9]]', '')"/>
  </xsl:function>

</xsl:stylesheet>
