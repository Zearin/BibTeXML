<?xml version="1.0"?>
<!-- $Id: bibxml2bib.xsl 391 2007-12-23 17:42:35Z ringler $ -->
<!-- (c) 2008 Moritz Ringler -->
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:bibfunc="http://bibtexml.sf.net/functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:auxparser="java:net.sourceforge.bibtexml.AuxParser">

  <xsl:param name="bibtexml.sf.net.encoding" select="'ISO-8859-1'" />
  <xsl:param name="aux-file" select="'test.aux'" as="xs:string"/>
  <xsl:param name="aux-encoding" select="'ISO-8859-1'" as="xs:string"/>

  <xsl:key match="*" name="idkey" use="@id"/>

  <xsl:output method="text"
      media-type="application/x-bibtex"
      encoding="ISO-8859-1" />

  <xsl:include href="include/bibfunc.xsl"/>

  <xsl:template match="bibtex:file">
    <!--
      Retrieve citations from aux file
    -->
    <xsl:variable name="citations"
      select="auxparser:xslt-parse($aux-file, $aux-encoding)"
      as="xs:string*"/>
    <xsl:if test="exists($citations)">
      <xsl:message>
        <xsl:text>Parsing aux file </xsl:text><xsl:value-of select="$aux-file"/>
        <xsl:text>&#xA;Found the following citations:&#xA;</xsl:text>
        <xsl:value-of select="$citations"/>
      </xsl:message>
    </xsl:if>
    <!--
      FEHLT:
      Preamble
    -->
    <!--
      Have \(no)cite{*} ?
    -->
    <xsl:variable name="star" select="index-of($citations, '*')"/>
      <xsl:choose>
        <xsl:when test="exists($star)">
          <!--
            process entries cited before \(no)cite{*},
            then all others
          -->
          <xsl:variable
            name="explicit-entries"
            select="subsequence($citations, 1, $star[1])"/>
          <xsl:text>\begin{thebibliography}{</xsl:text>
          <xsl:value-of select="count(bibtex:entry)"/>
          <xsl:text>}&#xA;</xsl:text>
          <!-- process explicit citations in cite order -->
          <xsl:apply-templates select="for $id in $explicit-entries return key('idkey', $id)"/>
          <!-- process implicit citations in document order -->
          <xsl:apply-templates select="bibtex:entry except key('idkey', $explicit-entries)"/>
          <xsl:text>&#xA;\end{thebibliography}</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <!--
            process cited entries
          -->
          <xsl:text>\begin{thebibliography}{</xsl:text>
          <xsl:value-of select="count($citations)"/>
          <xsl:text>}&#xA;</xsl:text>
          <xsl:apply-templates select="for $id in $citations return key('idkey', $id)"/>
          <xsl:text>&#xA;\end{thebibliography}</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <xsl:apply-templates select="bibtex:*"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:article">
    <!-- bibitem -->
    <xsl:call-template name="output-bibitem"/>
    <!-- author(s) -->
    <xsl:apply-templates select="bibtex:author">
      <xsl:with-param
        name="author-count"
        select="count(bibtex:author)"
      />
    </xsl:apply-templates>
    <xsl:text>.&#xA;</xsl:text>
    <!-- title -->
    <xsl:text>\newblock </xsl:text>
    <xsl:apply-templates select="bibtex:title"/>
    <!-- journal -->
    <xsl:text>&#xA;\newblock </xsl:text>
    <xsl:apply-templates select="bibtex:journal"/>
    <!-- volume, number, pages -->
    <xsl:if
      test="exists(bibtex:volume) or
            exists(bibtex:number) or
            exists(bibtex:pages)">
      <xsl:text>, </xsl:text>
      <xsl:apply-templates select="bibtex:volume"/>
      <xsl:apply-templates select="bibtex:number"/>
      <xsl:apply-templates select="bibtex:pages">
        <xsl:with-param
          name="has-volume-or-number"
          select="exists(bibtex:volume) or exists(bibtex:number)"
        />
      </xsl:apply-templates>
    </xsl:if>
    <!-- month and year -->
    <xsl:if
      test="exists(bibtex:month) or
            exists(bibtex:year)">
      <xsl:text>,</xsl:text>
      <xsl:apply-templates select="bibtex:month" />
      <xsl:apply-templates select="bibtex:year" />
    </xsl:if>
    <xsl:text>.</xsl:text>
    <!-- note -->
    <xsl:apply-templates select="bibtex:note"/>
  </xsl:template>

  <xsl:template match="bibtex:author">
    <xsl:param name="author-count" as="xs:integer"/>
    <xsl:variable name="pos" select="position()"/>
    <xsl:if test="$pos ne 1">
      <xsl:variable name="is-last" select="($pos eq $author-count)"/>
      <xsl:if test="not($is-last and $pos eq 2)">
        <xsl:text>,</xsl:text>
      </xsl:if>
       <xsl:text> </xsl:text>
      <xsl:if test="$is-last and not(bibtex:others)">
        <xsl:text>and </xsl:text>
      </xsl:if>
    </xsl:if>
    <xsl:choose>
        <xsl:when test="bibtex:others">
            <xsl:text>et~al</xsl:text>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates select="bibfunc:parse-author(text())" />
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibfunc:person">
    <xsl:apply-templates select="bibfunc:first"/>
    <xsl:apply-templates select="bibfunc:last"/>
    <xsl:apply-templates select="bibfunc:junior"/>
  </xsl:template>

  <xsl:template match="bibfunc:first">
    <xsl:variable name="ff" select="replace(normalize-space(text()),' ','~')"/>
    <xsl:if test="(string-length($ff) gt 0) and ($ff ne ' ')">
      <xsl:value-of select="$ff"/>
      <xsl:value-of select="if(string-length($ff) le 2) then '~' else ' '"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibfunc:junior">
    <xsl:text>, </xsl:text>
    <xsl:value-of select="normalize-space(text())"/>
  </xsl:template>

  <xsl:template match="bibfunc:last">
    <xsl:variable name="ll" select="normalize-space(text())"/>
    <xsl:value-of select="replace($ll, '^(\p{L}[^ ]) (.+)', '$1~$2')"/>
  </xsl:template>

  <xsl:template match="bibtex:note">
    <xsl:text>&#xA;\newblock </xsl:text>
      <xsl:value-of select="normalize-space(text())"/>
    <xsl:text>.</xsl:text>
  </xsl:template>

  <xsl:template match="bibfunc:pages" mode="long">
    <xsl:choose>
      <xsl:when test="exists(bibfunc:end-page)">
        <xsl:text>pages </xsl:text>
        <xsl:value-of select="bibfunc:start-page"/>
        <xsl:text>--</xsl:text>
        <xsl:value-of select="bibfunc:end-page"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>page </xsl:text>
        <xsl:value-of select="bibfunc:start-page"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:template>

  <xsl:template match="bibfunc:pages" mode="brief">
    <xsl:text>:</xsl:text>
    <xsl:value-of select="bibfunc:start-page"/>
    <xsl:if test="exists(bibfunc:end-page)">
      <xsl:text>--</xsl:text>
      <xsl:value-of select="bibfunc:end-page"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:pages">
    <xsl:param name="has-volume-or-number" as="xs:boolean"/>
    <xsl:choose>
      <xsl:when test="$has-volume-or-number">
        <xsl:apply-templates
          select="bibfunc:parse-pages(text())"
          mode="brief"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates
          select="bibfunc:parse-pages(text())"
          mode="long"
        />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bibtex:volume">
    <xsl:value-of select="normalize-space(text())"/>
  </xsl:template>

  <xsl:template match="bibtex:number">
    <xsl:text>(</xsl:text>
    <xsl:value-of select="normalize-space(text())"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:title">
    <!-- FEHLT: Gross/Kleinschreibung -->
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:value-of select="$tt"/>
    <xsl:if test="not(matches($tt,'[\.!?]$'))">
      <xsl:text>.</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:journal">
    <xsl:text>{\em </xsl:text>
    <xsl:value-of select="normalize-space(text())"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="bibtex:month">
    <xsl:variable name="mm" select="normalize-space(text())" />
    <xsl:if test="string-length($mm) gt 0">
      <xsl:text> </xsl:text>
      <xsl:value-of select="$mm"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:year">
    <xsl:variable name="yy" select="normalize-space(text())" />
    <xsl:if test="string-length($yy) gt 0">
      <xsl:text> </xsl:text>
      <xsl:value-of select="$yy"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="output-bibitem">
    <xsl:text>&#xA;\bibitem{</xsl:text>
    <xsl:value-of select="../@id" />
    <xsl:text>}&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="*">
  </xsl:template>
</xsl:stylesheet>
