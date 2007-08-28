<?xml version="1.0"?>
<!-- $Id: bibxml2mods32.xsl 338 2007-08-27 17:27:58Z ringler $ -->
<!-- (c) Moritz Ringler, 2004-2007                       -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to MODS v3.2.                          -->
<xsl:transform version="2.0"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2003/11/xpath-functions"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:foo="http://foo/bar/baz"
    exclude-result-prefixes="bibtex fn foo">

  <xsl:include href="include/fn-parse-author.xsl"/>

  <xsl:output
      method="xml"
      indent="yes"
      version="1.0"/>

  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:file">
    <mods:modsCollection xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-2.xsd">
      <xsl:apply-templates />
    </mods:modsCollection>
  </xsl:template>

  <xsl:template match="bibtex:entry">
      <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:article">
  <mods:mods version="3.2">
    <xsl:apply-templates select="bibtex:title"/>
    <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
    <mods:typeOfResource>
      <xsl:text>text</xsl:text>
    </mods:typeOfResource>
    <mods:originInfo>
      <mods:issuance>
        <xsl:text>monographic</xsl:text>
      </mods:issuance>
    </mods:originInfo>
    <xsl:apply-templates select="bibtex:abstract" />
    <xsl:apply-templates select="bibtex:note" />
    <mods:identifier>
      <xsl:value-of select="../@id" />
    </mods:identifier>
    <xsl:apply-templates select="bibtex:doi" mode="identifier"/>
    <mods:relatedItem type="host">
      <xsl:apply-templates select="bibtex:journal" />
      <mods:originInfo>
        <mods:issuance>
          <xsl:text>continuing</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <mods:part>
        <xsl:apply-templates select="bibtex:volume"/>
        <xsl:apply-templates select="bibtex:number"/>
        <xsl:apply-templates select="bibtex:pages"/>
        <mods:date>
          <xsl:apply-templates select="bibtex:year" />
        </mods:date>
      </mods:part>
      <xsl:apply-templates select="bibtex:issn"/>
    </mods:relatedItem>
    </mods:mods>
  </xsl:template>

  <xsl:template match="bibtex:abstract">
    <mods:abstract>
      <xsl:apply-templates />
    </mods:abstract>
  </xsl:template>

  <xsl:template match="bibtex:note">
    <mods:note>
      <xsl:apply-templates />
    </mods:note>
  </xsl:template>

  <xsl:template match="bibtex:issn">
    <mods:identifier type="issn">
      <xsl:apply-templates />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="bibtex:doi" mode="identifier">
    <mods:identifier type="doi">
      <xsl:apply-templates />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="bibtex:year">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:pages">
    <mods:extent unit="pages">
      <xsl:apply-templates select="foo:parse-pages(text())/foo:pages/*"/>
    </mods:extent>
  </xsl:template>

  <xsl:template match="foo:start-page">
    <mods:start>
      <xsl:apply-templates />
    </mods:start>
  </xsl:template>

  <xsl:template match="foo:end-page">
    <mods:end>
      <xsl:apply-templates />
    </mods:end>
  </xsl:template>

  <xsl:template match="bibtex:number">
    <mods:detail type="issue">
      <mods:number>2</mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="bibtex:volume">
    <mods:detail type="volume">
      <mods:number>
        <xsl:apply-templates />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="bibtex:journal">
    <mods:titleInfo>
      <mods:title>
        <xsl:apply-templates />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="bibtex:authors">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:author">
    <mods:name type="personal">
      <xsl:apply-templates select="foo:parse-author(.)/foo:person/*"/>
      <mods:role>
        <mods:roleTerm type="text"><xsl:text>author</xsl:text></mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:template>

  <xsl:template match="foo:last">
    <mods:namePart type="family">
      <xsl:apply-templates />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="foo:first">
    <mods:namePart type="given">
      <xsl:apply-templates />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:copy-of select="."/>
  </xsl:template>


  <xsl:template match="bibtex:title">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()"/>
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="*" />

</xsl:transform>
