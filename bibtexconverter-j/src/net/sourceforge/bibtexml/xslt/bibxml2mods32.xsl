<?xml version="1.0" encoding="UTF-8" ?>
<!-- $Id: bibxml2mods32.xsl 338 2007-08-27 17:27:58Z ringler $
      XSLT stylesheet that converts bibliographic data
      from BibXML to MODS v3.2.

      Sources:
      http://www.loc.gov/standards/mods/v3/mods-3-2.xsd
      http://www.loc.gov/standards/mods/v3/mods-userguide.html
      http://www.loc.gov/standards/mods/v3/mods-mapping.html
      http://www.loc.gov/standards/mods/#examples
      http://www.loc.gov/standards/mods/v3/mods-userguide-examples.html
-->
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
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:bibfunc="http://bibtexml.sf.net/functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="bibtex fn bibfunc">

  <xsl:include href="include/bibfunc.xsl"/>
  <xsl:include href="include/mods-classification.xsl"/>

  <xsl:output
      method="xml"
      indent="yes"
      version="1.0"/>

  <!-- Document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="bibtex:file" />
  </xsl:template>

  <!-- root element bibtex:file -->
  <xsl:template match="bibtex:file">
    <mods:modsCollection xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-2.xsd">
      <xsl:apply-templates select="bibtex:entry" />
    </mods:modsCollection>
  </xsl:template>

  <!-- bibtex:entry -->
  <xsl:template match="bibtex:entry">
    <xsl:apply-templates select="bibtex:*"/>
  </xsl:template>

  <!-- bibtex @entries -->

  <xsl:template match="bibtex:article">
    <mods:mods version="3.2">
      <xsl:attribute name="ID" select="../@id"/>
      <xsl:apply-templates select="bibtex:title"/>
      <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
      <xsl:call-template name="text-resource"/>
      <mods:genre>journal article</mods:genre>
      <mods:genre authority="bibtex">
        <xsl:text>article</xsl:text>
      </mods:genre>
      <xsl:apply-templates select="bibtex:category"/>
      <mods:originInfo>
        <mods:issuance>
          <xsl:text>monographic</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <xsl:call-template name="abstract-note-keywords-classification"/>

      <!-- Journal -->
      <xsl:comment>
        <xsl:text>Journal</xsl:text>
      </xsl:comment>
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
        </mods:part>
        <xsl:apply-templates select="bibtex:issn"/>
        <mods:genre authority="marcgt">
          <xsl:text>periodical</xsl:text>
        </mods:genre>
      </mods:relatedItem>

      <xsl:call-template name="identifier-location-access"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="bibtex:book|bibtex:booklet|bibtex:manual|bibtex:techreport|bibtex:unpublished|bibtex:misc">
    <mods:mods xmlns:xlink="http://www.w3.org/1999/xlink" version="3.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.loc.gov/mods/v3" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-0.xsd">
      <xsl:attribute name="ID" select="../@id"/>
      <xsl:apply-templates select="bibtex:title"/>
      <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
      <xsl:apply-templates select="bibtex:editor|bibtex:editors"/>
      <xsl:apply-templates select="bibtex:organization" mode="default"/>
      <xsl:apply-templates select="bibtex:organization|bibtex:institution" mode="default"/>
      <xsl:call-template name="text-resource"/>
      <mods:genre authority="bibtex">
        <xsl:value-of select="local-name()"/>
      </mods:genre>
      <xsl:apply-templates select="bibtex:category"/>
      <mods:originInfo>
        <xsl:apply-templates select="bibtex:address"/>
        <xsl:apply-templates select="bibtex:publisher"/>
        <xsl:apply-templates select="bibtex:year"/>
        <xsl:apply-templates select="bibtex:edition" />
        <xsl:apply-templates select="bibtex:howpublished"/>
        <mods:issuance>
          <xsl:text>monographic</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <mods:physicalDescription>
        <mods:form authority="marcform">
          <xsl:text>print</xsl:text>
          <xsl:apply-templates select="bibtex:size"/>
        </mods:form>
      </mods:physicalDescription>
      <xsl:call-template name="abstract-note-keywords-classification"/>
      <xsl:apply-templates select="bibtex:series" />
      <xsl:call-template name="identifier-location-access"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="bibtex:proceedings">
    <mods:mods xmlns:xlink="http://www.w3.org/1999/xlink" version="3.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.loc.gov/mods/v3" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-0.xsd">
      <xsl:attribute name="ID" select="../@id"/>
      <xsl:apply-templates select="bibtex:title"/>
      <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
      <xsl:apply-templates select="bibtex:editor|bibtex:editors"/>
      <xsl:apply-templates select="bibtex:organization" mode="conference"/>
      <xsl:call-template name="text-resource"/>
      <mods:genre authority="bibtex">
        <xsl:value-of select="local-name()"/>
      </mods:genre>
      <mods:genre authority="marcgt">conference publication</mods:genre>
      <xsl:apply-templates select="bibtex:category"/>
      <mods:originInfo>
        <xsl:apply-templates select="bibtex:address"/>
        <xsl:apply-templates select="bibtex:publisher"/>
        <xsl:apply-templates select="bibtex:year"/>
        <xsl:apply-templates select="bibtex:edition" />
        <xsl:apply-templates select="bibtex:howpublished"/>
        <mods:issuance>
          <xsl:text>monographic</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <mods:physicalDescription>
        <mods:form authority="marcform">
          <xsl:text>print</xsl:text>
          <xsl:apply-templates select="bibtex:size"/>
        </mods:form>
      </mods:physicalDescription>
      <xsl:call-template name="abstract-note-keywords-classification"/>
      <xsl:apply-templates select="bibtex:series" />
      <xsl:call-template name="identifier-location-access"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="bibtex:inbook|bibtex:incollection">
    <mods:mods version="3.2">
      <xsl:attribute name="ID" select="../@id"/>
      <xsl:if test="exists(bibtex:booktitle)">
        <xsl:apply-templates select="bibtex:title"/>
      </xsl:if>
      <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
      <xsl:call-template name="text-resource"/>
      <mods:genre authority="bibtex">
        <xsl:value-of select="local-name()"/>
      </mods:genre>
      <xsl:apply-templates select="bibtex:category"/>
      <mods:originInfo>
        <mods:issuance>
          <xsl:text>monographic</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <xsl:call-template name="abstract-note-keywords-classification"/>

      <mods:relatedItem type="host">
        <xsl:choose>
          <xsl:when test="exists(bibtex:booktitle)">
            <xsl:apply-templates select="bibtex:booktitle" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="title" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="bibtex:editor|bibtex:editors"/>
        <xsl:call-template name="text-resource"/>
        <mods:genre>book</mods:genre>
        <mods:originInfo>
          <xsl:apply-templates select="bibtex:address"/>
          <xsl:apply-templates select="bibtex:publisher"/>
          <xsl:apply-templates select="bibtex:year"/>
          <xsl:apply-templates select="bibtex:edition" />
          <xsl:apply-templates select="bibtex:howpublished"/>
          <mods:issuance>
            <xsl:text>monographic</xsl:text>
          </mods:issuance>
        </mods:originInfo>
        <mods:part>
          <xsl:apply-templates select="bibtex:chapter"/>
          <xsl:apply-templates select="bibtex:pages"/>
        </mods:part>
        <mods:physicalDescription>
          <mods:form authority="marcform">
            <xsl:text>print</xsl:text>
            <xsl:apply-templates select="bibtex:size"/>
          </mods:form>
        </mods:physicalDescription>
        <xsl:apply-templates select="bibtex:series" />
      </mods:relatedItem>

      <xsl:call-template name="identifier-location-access"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="bibtex:inproceedings|bibtex:conference">
    <mods:mods version="3.2">
      <xsl:attribute name="ID" select="../@id"/>
      <xsl:if test="exists(bibtex:booktitle)">
        <xsl:apply-templates select="bibtex:title"/>
      </xsl:if>
      <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
      <xsl:call-template name="text-resource"/>
      <mods:genre authority="bibtex">
        <xsl:value-of select="local-name()"/>
      </mods:genre>
      <mods:genre authority="marcgt">conference publication</mods:genre>
      <xsl:apply-templates select="bibtex:category"/>
      <mods:originInfo>
        <mods:issuance>
          <xsl:text>monographic</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <xsl:call-template name="abstract-note-keywords-classification"/>

      <mods:relatedItem type="host">
        <xsl:choose>
          <xsl:when test="exists(bibtex:booktitle)">
            <xsl:apply-templates select="bibtex:booktitle" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="title" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="bibtex:editor|bibtex:editors"/>
        <xsl:call-template name="text-resource"/>
        <mods:genre authority="marcgt">conference publication</mods:genre>
        <mods:originInfo>
          <xsl:apply-templates select="bibtex:address"/>
          <xsl:apply-templates select="bibtex:publisher"/>
          <xsl:apply-templates select="bibtex:year"/>
          <xsl:apply-templates select="bibtex:edition" />
          <xsl:apply-templates select="bibtex:howpublished"/>
          <mods:issuance>
            <xsl:text>monographic</xsl:text>
          </mods:issuance>
        </mods:originInfo>
        <mods:part>
          <xsl:apply-templates select="bibtex:chapter"/>
          <xsl:apply-templates select="bibtex:pages"/>
        </mods:part>
        <mods:physicalDescription>
          <mods:form authority="marcform">
            <xsl:text>print</xsl:text>
            <xsl:apply-templates select="bibtex:size"/>
          </mods:form>
        </mods:physicalDescription>
        <xsl:apply-templates select="bibtex:series" />
      </mods:relatedItem>

      <xsl:call-template name="identifier-location-access"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="bibtex:phdthesis|bibtex:masterthesis">
    <mods:mods xmlns:xlink="http://www.w3.org/1999/xlink" version="3.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.loc.gov/mods/v3" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-0.xsd">
      <xsl:attribute name="ID" select="../@id"/>
      <xsl:apply-templates select="bibtex:title"/>
      <xsl:apply-templates select="bibtex:author|bibtex:authors"/>
      <xsl:apply-templates select="bibtex:editor|bibtex:editors"/>
      <xsl:apply-templates select="bibtex:school" mode="name"/>
      <xsl:call-template name="text-resource"/>
      <mods:genre authority="bibtex">
        <xsl:value-of select="local-name()"/>
      </mods:genre>
      <!-- 'theses' is listed at http://www.loc.gov/marc/sourcecode/genre/genrelist.html
           but this may be a typo -->
      <mods:genre authority="marcgt">theses</mods:genre>
      <mods:genre authority="marcgt">thesis</mods:genre>
      <xsl:apply-templates select="bibtex:category"/>
      <mods:originInfo>
        <xsl:apply-templates select="bibtex:address"/>
        <xsl:apply-templates select="bibtex:publisher"/>
        <xsl:apply-templates select="bibtex:school" mode="publisher"/>
        <xsl:apply-templates select="bibtex:year"/>
        <mods:issuance>
          <xsl:text>monographic</xsl:text>
        </mods:issuance>
      </mods:originInfo>
      <mods:physicalDescription>
        <mods:form authority="marcform">
          <xsl:text>print</xsl:text>
          <xsl:apply-templates select="bibtex:size"/>
        </mods:form>
      </mods:physicalDescription>
      <xsl:call-template name="abstract-note-keywords-classification"/>
      <xsl:call-template name="identifier-location-access"/>
    </mods:mods>
  </xsl:template>

  <!-- bibtex fields -->
  <xsl:template match="bibtex:school" mode="name">
    <xsl:call-template name="organization-name">
      <xsl:with-param name="role" select="'Originator'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bibtex:school" mode="affiliation">
    <mods:affiliation>
      <xsl:apply-templates />
    </mods:affiliation>
  </xsl:template>

  <xsl:template match="bibtex:school" mode="publisher">
    <mods:publisher>
      <xsl:apply-templates />
    </mods:publisher>
  </xsl:template>

  <xsl:template match="bibtex:organization|bibtex:institution" mode="default">
    <xsl:call-template name="organization-name" />
  </xsl:template>

  <xsl:template match="bibtex:organization" mode="conference">
    <xsl:call-template name="organization-name">
      <xsl:with-param name="role" select="'Organizer of meeting'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bibtex:howpublished" />

  <xsl:template match="bibtex:series">
    <mods:relatedItem type="series">
      <mods:titleInfo>
        <mods:title>
          <xsl:apply-templates />
        </mods:title>
      </mods:titleInfo>
      <mods:part>
        <xsl:apply-templates select="../bibtex:volume"/>
        <xsl:apply-templates select="../bibtex:number"/>
      </mods:part>
    </mods:relatedItem>
  </xsl:template>

  <xsl:template match="bibtex:edition">
    <mods:edition>
      <xsl:apply-templates />
    </mods:edition>
  </xsl:template>

  <xsl:template match="bibtex:category">
    <mods:genre>
      <xsl:apply-templates />
    </mods:genre>
  </xsl:template>

  <xsl:template match="bibtex:size">
    <mods:extent>
      <xsl:apply-templates />
    </mods:extent>
  </xsl:template>

  <xsl:template match="bibtex:doi" mode="url">
    <mods:url>
      <xsl:value-of select="bibfunc:doi-to-url(text())"/>
    </mods:url>
  </xsl:template>

  <xsl:template match="bibtex:url">
    <mods:url>
      <xsl:apply-templates/>
    </mods:url>
  </xsl:template>

 <xsl:template match="bibtex:keywords">
  <xsl:if test="empty(./*)">
    <mods:topic>
      <xsl:apply-templates />
    </mods:topic>
  </xsl:if>
  <xsl:apply-templates select="bibtex:keyword"/>
 </xsl:template>

  <xsl:template match="bibtex:keyword">
    <mods:topic>
      <xsl:apply-templates />
    </mods:topic>
  </xsl:template>

  <xsl:template match="bibtex:address">
    <mods:place>
      <mods:placeTerm type="text">
        <xsl:apply-templates />
      </mods:placeTerm>
    </mods:place>
  </xsl:template>

  <xsl:template match="bibtex:publisher">
    <mods:publisher>
      <xsl:apply-templates />
    </mods:publisher>
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

  <xsl:template match="bibtex:isbn">
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
    <mods:dateIssued encoding="marc">
      <xsl:apply-templates />
    </mods:dateIssued>
  </xsl:template>

  <xsl:template match="bibtex:pages">
    <mods:extent unit="pages">
      <xsl:apply-templates select="bibfunc:parse-pages(text())/bibfunc:pages/*"/>
    </mods:extent>
  </xsl:template>

  <xsl:template match="bibfunc:start-page">
    <mods:start>
      <xsl:apply-templates />
    </mods:start>
  </xsl:template>

  <xsl:template match="bibfunc:end-page">
    <mods:end>
      <xsl:apply-templates />
    </mods:end>
  </xsl:template>

  <xsl:template match="bibtex:number">
    <mods:detail type="issue">
      <mods:number>
        <xsl:apply-templates />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="bibtex:volume|bibtex:chapter">
    <mods:detail>
      <xsl:attribute name="type" select="local-name()"/>
      <mods:number>
        <xsl:apply-templates />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="bibtex:authors|bibtex:editors">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="bibtex:author|bibtex:editor">
    <mods:name type="personal">
      <xsl:apply-templates select="bibfunc:parse-author(.)/bibfunc:person/*"/>
      <mods:role>
        <mods:roleTerm type="text">
          <xsl:value-of select="local-name()"/>
        </mods:roleTerm>
      </mods:role>
      <xsl:apply-templates select="../bibtex:affiliation"/>
      <xsl:apply-templates select="../bibtex:school" mode="affiliation"/>
    </mods:name>
  </xsl:template>

  <xsl:template match="bibtex:affiliation">
    <mods:affiliation>
      <xsl:apply-templates />
    </mods:affiliation>
  </xsl:template>

  <xsl:template match="bibfunc:last">
    <mods:namePart type="family">
      <xsl:apply-templates />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="bibfunc:junior">
    <mods:namePart type="termsOfAddress">
      <xsl:apply-templates />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="bibfunc:first">
    <mods:namePart type="given">
      <xsl:apply-templates />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="bibtex:title|bibtex:booktitle|bibtex:journal">
    <mods:titleInfo>
      <mods:title>
        <xsl:apply-templates />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="bibtex:copyright">
    <mods:accessCondition type="useAndReproduction">
      <xsl:apply-templates />
    </mods:accessCondition>
  </xsl:template>

  <!-- text nodes -->
  <xsl:template match="text()">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- unknown elements -->
  <xsl:template match="*" />

  <!-- named templates -->
  <xsl:template name="text-resource">
    <mods:typeOfResource>
      <xsl:text>text</xsl:text>
    </mods:typeOfResource>
  </xsl:template>

  <xsl:template name="abstract-note-keywords-classification">
    <xsl:apply-templates select="bibtex:abstract" />
    <xsl:apply-templates select="bibtex:note" />
    <xsl:if test="exists(bibtex:keywords) or exists(bibtex:keyword)">
      <mods:subject>
        <xsl:apply-templates select="bibtex:keywords|bibtex:keyword"/>
      </mods:subject>
      <xsl:call-template name="classification"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="identifier-location-access">
    <mods:identifier>
      <xsl:value-of select="../@id" />
    </mods:identifier>
    <xsl:apply-templates select="bibtex:isbn" />
    <xsl:apply-templates select="bibtex:doi" mode="identifier"/>
    <xsl:if test="exists(bibtex:url) or exists(bibtex:doi)">
      <mods:location>
        <xsl:apply-templates select="bibtex:url"/>
        <xsl:apply-templates select="bibtex:doi" mode="url"/>
      </mods:location>
    </xsl:if>
    <xsl:apply-templates select="bibtex:copyright" />
  </xsl:template>

  <xsl:template name="organization-name">
    <xsl:param name="role" as="xs:string" select="'creator'"/>
    <mods:name type="corporate">
      <mods:displayForm>
        <xsl:apply-templates />
      </mods:displayForm>
      <mods:role>
        <mods:roleTerm type="text">
          <xsl:value-of select="$role"/>
        </mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:template>

</xsl:transform>

