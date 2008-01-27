<?xml version="1.0"?>
<!-- $Id: bibxml2bib.xsl 391 2007-12-23 17:42:35Z ringler $ -->
<!-- This stylesheet produces a latex thebibliography environment
     (bbl file) that is similar to what bibtex produces with the
     standard bibtex style unsrt.bst.
     Notable differences include:
      * braces are completely ignored (e.g. in author parsing)
      * case is never changed except in edition (always lower)
      * some differences concerning tying with ~ under very rare
        circumstances
      * long lines are not wrapped
      * no cross-referencing, you can achieve a similar (but not
        exactly the same) cross-ref behaviour if you precede
        the bibtexml-to-latex transformation with
        the bibtexml-to-bitexml transformation resolve-crossref.xsl
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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:auxparser="java:net.sourceforge.bibtexml.AuxParser"
    xmlns:my="foo:bar">

  <xsl:param name="bibtexml.sf.net.encoding" select="'ISO-8859-1'" />
  <xsl:param name="aux-file" select="''" as="xs:string"/>
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
    <xsl:variable name="citations" select="my:citations()"
      as="xs:string*"/>

    <xsl:apply-templates select="bibtex:preamble"/>
    <xsl:if test="my:exists(bibtex:preamble)">
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
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
          <xsl:value-of select="my:normalize-count(count(bibtex:entry), 1)"/>
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

  <xsl:template match="bibtex:preamble">
    <xsl:value-of select="normalize-space(text())"/>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <!--<xsl:message>Processing <xsl:value-of select="@id"/>&#xA;</xsl:message>-->
    <xsl:apply-templates select="bibtex:*"/>
  </xsl:template>

  <!-- @ARTICLE -->
  <xsl:template match="bibtex:article">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- author(s) -->
        <xsl:call-template name="author-block"/>
        <!-- block: title -->
        <xsl:call-template name="title-block"/>
        <!-- block: journal, volume, number, pages, month, year -->
        <xsl:call-template name="journal-block"/>
        <!-- block: note -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @BOOK -->
  <xsl:template match="bibtex:book">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- author or editor -->
        <xsl:call-template name="author-or-editor-block"/>
        <!-- block: title, volume(series) -->
        <xsl:call-template name="title-block-in-book"/>
        <!-- block: number, publisher, edition, year etc. -->
        <xsl:call-template name="publisher-block"/>
        <!-- block: note -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @BOOKLET -->
  <xsl:template match="bibtex:booklet">
    <xsl:call-template name="output-bibitem">
        <xsl:with-param name="blocks">
        <!-- author(s) -->
        <xsl:call-template name="author-block"/>
        <!-- block: title, howpublished, adress, month, year -->
        <xsl:call-template name="title-block-in-booklet"/>
        <!-- block: note -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @INBOOK -->
  <xsl:template match="bibtex:inbook">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- author or editor -->
        <xsl:call-template name="author-or-editor-block"/>
        <!-- block: title, volume(series), chapter, pages -->
        <xsl:call-template name="title-block-in-book">
          <xsl:with-param name="chapter-and-pages" select="true()"/>
        </xsl:call-template>
        <!-- block: number, publisher, edition, year etc.  -->
        <xsl:call-template name="publisher-block"/>
        <!-- block: note  -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @INCOLLECTION -->
  <xsl:template match="bibtex:incollection">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- author(s) -->
        <xsl:call-template name="author-block"/>
        <!-- block: title -->
        <xsl:call-template name="title-block"/>
        <!-- block: booktitle etc. -->
        <xsl:call-template name="booktitle-block"/>
        <!-- block: note  -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @INPROCEEDINGS /  @CONFERENCE -->
  <xsl:template match="bibtex:inproceedings|bibtex:conference">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- author(s) -->
        <xsl:call-template name="author-block"/>
        <!-- block: title -->
        <xsl:call-template name="title-block"/>
        <!-- block: booktitle etc. -->
        <xsl:call-template name="booktitle-block-in-inproceedings"/>
        <!-- block: note  -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @MANUAL -->
  <xsl:template match="bibtex:manual">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- block: author | (org, add) -->
        <xsl:call-template name="author-block-in-manual"/>
        <!-- one or two block(s): title, (org, add)?, ed, date -->
        <xsl:call-template name="title-block-in-manual"/>
        <!-- block: note  -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @MASTERSTHESIS -->
  <xsl:template match="bibtex:mastersthesis">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <xsl:call-template name="author-block"/>
        <xsl:call-template name="title-block" />
        <!-- block: thesis type, school, address, date -->
        <xsl:call-template name="thesis-block" >
          <xsl:with-param name="default-type">Master's thesis</xsl:with-param>
        </xsl:call-template>
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @MISC -->
  <xsl:template match="bibtex:misc">
    <xsl:variable name="has-howpublished"
      select="my:exists(bibtex:howpublished)"/>
    <xsl:variable name="has-title"
      select="my:exists(bibtex:title)"/>
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- block: author, date? -->
        <xsl:call-template name="author-block">
          <xsl:with-param name="with-date"
            select="not($has-howpublished) and not($has-title)"/>
        </xsl:call-template>
        <!-- block: (title, date?)? -->
        <xsl:if test="$has-title">
          <xsl:call-template name="title-block">
            <xsl:with-param name="with-date" select="not($has-howpublished)"/>
          </xsl:call-template>
        </xsl:if>
        <!-- block?: howpublished, date  -->
        <xsl:if test="$has-howpublished">
          <xsl:call-template name="single-sentence-block">
            <xsl:with-param name="elements">
              <xsl:apply-templates select="bibtex:howpublished"/>
              <xsl:call-template name="date" />
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
        <!-- block: note  -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @PHDTHESIS -->
  <xsl:template match="bibtex:phdthesis">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <xsl:call-template name="author-block"/>
        <xsl:call-template name="title-block" >
          <xsl:with-param name="emphasize" select="true()"/>
        </xsl:call-template>
        <!-- block: thesis type, school, address, date -->
        <xsl:call-template name="thesis-block" >
          <xsl:with-param name="default-type" select="'PhD thesis'"/>
        </xsl:call-template>
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @PROCEEDINGS -->
  <xsl:template match="bibtex:proceedings">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <!-- block: editor or organization -->
        <xsl:call-template name="single-sentence-block">
          <xsl:with-param name="elements">
            <xsl:choose>
              <xsl:when test="my:empty(bibtex:editor)">
                <xsl:apply-templates select="bibtex:organization"/>
              </xsl:when>
              <xsl:otherwise>
                <my:word>
                  <xsl:call-template name="editors"/>
                </my:word>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:call-template>
        <!-- block: title, volume, number, series, address, organization?,
         publisher, date -->
        <xsl:call-template name="title-block-in-proceedings"/>
        <!-- block:note -->
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @TECHREPORT -->
  <xsl:template match="bibtex:techreport">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <xsl:call-template name="author-block"/>
        <xsl:call-template name="title-block"/>
        <xsl:call-template name="single-sentence-block">
          <xsl:with-param name="elements">
            <xsl:call-template name="techreport-number"/>
            <xsl:apply-templates select="bibtex:institution"/>
            <xsl:apply-templates select="bibtex:address"/>
            <xsl:call-template name="date"/>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:apply-templates select="bibtex:note" mode="block"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- @UNPUBLISHED -->
  <xsl:template match="bibtex:unpublished">
    <xsl:call-template name="output-bibitem">
      <xsl:with-param name="blocks">
        <xsl:call-template name="author-block"/>
        <xsl:call-template name="title-block"/>
        <xsl:call-template name="single-sentence-block">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:note"/>
            <xsl:call-template name="date"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- block: title, volume, number, series, address, organization?,
         publisher, date -->
  <xsl:template name="title-block-in-proceedings">
    <xsl:variable name="has-editor" select="my:exists(bibtex:editor)"/>
    <xsl:variable name="has-address" select="my:exists(bibtex:address)"/>
    <xsl:variable name="single-sentence"
          select="not($has-address) and my:empty(bibtex:publisher)
                 and (not($has-editor) or my:empty(bibtex:organization))"/>
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:title">
              <xsl:with-param name="emphasize" select="true()"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="bibtex:volume" mode="book"/>
            <xsl:call-template name="number-and-series"/>
            <xsl:if test="$has-address or $single-sentence">
              <xsl:apply-templates select="bibtex:address"/>
              <xsl:call-template name="date"/>
            </xsl:if>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:if test="not($single-sentence)">
          <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:if test="$has-editor">
              <xsl:apply-templates select="bibtex:organization"/>
            </xsl:if>
            <xsl:apply-templates select="bibtex:publisher"/>
            <xsl:if test="not($has-address)">
              <xsl:call-template name="date"/>
            </xsl:if>
          </xsl:with-param>
        </xsl:call-template>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="thesis-block" >
    <xsl:param name="default-type" as="xs:string"/>
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
        <my:word>
          <xsl:choose>
            <xsl:when test="my:exists(bibtex:type)">
                <!--ToDo change case to titlecase -->
                <xsl:value-of select="normalize-space(bibtex:type[1]/text())"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$default-type"/>
            </xsl:otherwise>
          </xsl:choose>
        </my:word>
        <xsl:apply-templates select="bibtex:school"/>
        <xsl:apply-templates select="bibtex:address"/>
        <xsl:call-template name="date"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="title-block-in-manual">
    <xsl:variable name="has-extra-block"
      select="(my:exists(bibtex:author) and
               (my:exists(bibtex:organization) or
               my:exists(bibtex:address))
               ) or (
               my:empty(bibtex:author) and
               my:empty(bibtex:organization) and
               my:exists(bibtex:address)
              )"
    />
    <!-- block: title, ((org, add)?, ed, date)? -->
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
        <xsl:apply-templates select="bibtex:title">
          <xsl:with-param name="emphasize" select="true()"/>
        </xsl:apply-templates>
        <xsl:if test="not($has-extra-block)">
          <xsl:apply-templates select="bibtex:edition"/>
          <xsl:call-template name="date"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
    <!-- block?: org, add, ed, date -->
    <xsl:if test="$has-extra-block">
      <xsl:call-template name="single-sentence-block">
        <xsl:with-param name="elements">
          <xsl:apply-templates select="bibtex:organization"/>
          <xsl:apply-templates select="bibtex:address"/>
          <xsl:apply-templates select="bibtex:edition"/>
          <xsl:call-template name="date"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="author-block">
    <xsl:param name="with-date" select="false()" as="xs:boolean"/>
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
        <xsl:if test="my:exists(bibtex:author)">
          <my:word>
            <xsl:apply-templates select="bibtex:author">
            <xsl:with-param
              name="person-count"
              select="count(bibtex:author)"
            />
            </xsl:apply-templates>
          </my:word>
        </xsl:if>
        <xsl:if test="$with-date">
          <xsl:call-template name="date"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="author-block-in-manual">
    <xsl:choose>
      <xsl:when test="my:empty(bibtex:author)">
        <xsl:if test="my:exists(bibtex:organization)">
          <xsl:call-template name="single-sentence-block">
            <xsl:with-param name="elements">
              <xsl:apply-templates select="bibtex:organization"/>
              <xsl:apply-templates select="bibtex:address"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- author(s) -->
        <xsl:call-template name="author-block"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="author-or-editor-block">
    <xsl:if test="my:exists(bibtex:author) or my:exists(bibtex:editor)">
      <my:block>
      <xsl:call-template name="sentence">
        <xsl:with-param name="elements">
          <my:word>
            <xsl:choose>
              <xsl:when test="my:exists(bibtex:author)">
                <xsl:apply-templates select="bibtex:author">
                  <xsl:with-param
                    name="person-count"
                    select="count(bibtex:author)"
                  />
                </xsl:apply-templates>
              </xsl:when>
              <xsl:otherwise>
                  <xsl:call-template name="editors"/>
              </xsl:otherwise>
            </xsl:choose>
          </my:word>
        </xsl:with-param>
      </xsl:call-template>
      </my:block>
    </xsl:if>
  </xsl:template>

  <xsl:template name="journal-block">
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
        <xsl:apply-templates select="bibtex:journal">
          <xsl:with-param name="emphasize" select="true()"/>
        </xsl:apply-templates>
        <!-- word: volume, number, pages -->
        <xsl:if test="my:exists(bibtex:volume) or
                      my:exists(bibtex:number) or
                      my:exists(bibtex:pages)">
          <my:word>
            <xsl:apply-templates select="bibtex:volume"/>
            <xsl:apply-templates select="bibtex:number"/>
            <xsl:apply-templates select="bibtex:pages">
              <xsl:with-param
                name="brief"
                select="my:exists(bibtex:volume) or my:exists(bibtex:number)"
              />
            </xsl:apply-templates>
          </my:word>
        </xsl:if>
        <xsl:call-template name="date"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="booktitle-block">
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:booktitle" mode="in-ed"/>
            <xsl:apply-templates select="bibtex:volume" mode="book"/>
            <xsl:call-template name="number-and-series">
              <xsl:with-param name="mid-sentence"
                select="my:exists(bibtex:booktitle) or my:exists(bibtex:volume)"/>
            </xsl:call-template>
            <xsl:call-template name="chapter-and-pages"/>
          </xsl:with-param>
        </xsl:call-template>
        <!-- publisher, address, edition, month and year -->
        <xsl:call-template name="publisher-sentence"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="booktitle-block-in-inproceedings">
    <xsl:variable name="has-address" select="my:exists(bibtex:address)"/>
    <xsl:variable name="has-organization-or-publisher"
        select="my:exists(bibtex:organization) or
        my:exists(bibtex:publisher)"
    />
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:booktitle" mode="in-ed"/>
            <xsl:apply-templates select="bibtex:volume" mode="book"/>
            <xsl:call-template name="number-and-series">
              <xsl:with-param name="mid-sentence"
                select="my:exists(bibtex:booktitle) or my:exists(bibtex:volume)"/>
            </xsl:call-template>
            <xsl:if test="my:exists(bibtex:pages)">
              <my:word>
                <xsl:apply-templates select="bibtex:pages"/>
              </my:word>
            </xsl:if>
            <xsl:if test="$has-address or not($has-organization-or-publisher)">
              <xsl:apply-templates select="bibtex:address"/>
              <xsl:call-template name="date"/>
            </xsl:if>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:if test="$has-address or $has-organization-or-publisher">
          <xsl:call-template name="sentence">
            <xsl:with-param name="elements">
              <xsl:apply-templates select="bibtex:organization"/>
              <xsl:apply-templates select="bibtex:publisher"/>
              <xsl:if test="not($has-address) and $has-organization-or-publisher">
                <xsl:call-template name="date"/>
              </xsl:if>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="title-block-in-booklet">
    <!--
      block: title (if howpublished or adress exists)
    -->
    <xsl:variable name="has-howpublished-or-address"
      select="my:exists(bibtex:howpublished) or my:exists(bibtex:address)"/>
    <xsl:call-template name="title-block">
      <xsl:with-param name="with-date"
        select="not($has-howpublished-or-address)"/>
    </xsl:call-template>
    <!--
      block?: howpublished, adress, date
    -->
    <xsl:if test="$has-howpublished-or-address">
      <xsl:call-template name="single-sentence-block">
        <xsl:with-param name="elements">
          <xsl:apply-templates select="bibtex:howpublished"/>
          <xsl:apply-templates select="bibtex:address"/>
          <xsl:call-template name="date"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="title-block">
    <xsl:param name="with-date" select="false()" as="xs:boolean"/>
    <xsl:param name="emphasize" select="false()" as="xs:boolean"/>
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
        <xsl:apply-templates select="bibtex:title" >
          <xsl:with-param name="emphasize" select="$emphasize"/>
        </xsl:apply-templates>
        <xsl:if test="$with-date">
          <xsl:call-template name="date"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="title-block-in-book">
    <xsl:param name="chapter-and-pages" select="false()" as="xs:boolean"/>
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
          <xsl:apply-templates select="bibtex:title">
            <xsl:with-param name="emphasize" select="true()"/>
          </xsl:apply-templates>
          <xsl:apply-templates select="bibtex:volume" mode="book"/>
          <xsl:if test="$chapter-and-pages">
            <xsl:call-template name="chapter-and-pages"/>
          </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="publisher-block">
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="number-and-series-sentence"/>
        <!-- publisher, address, edition, month and year -->
        <xsl:call-template name="publisher-sentence"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="publisher-sentence">
    <xsl:call-template name="sentence">
      <xsl:with-param name="elements">
        <xsl:apply-templates select="bibtex:publisher"/>
        <xsl:apply-templates select="bibtex:address"/>
        <xsl:apply-templates select="bibtex:edition"/>
        <xsl:call-template name="date"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="techreport-number">
    <xsl:variable name="has-type" select="my:exists(bibtex:type)"/>
    <xsl:variable name="has-number" select="my:exists(bibtex:number)"/>
    <my:word>
      <xsl:variable name="type"
        select="if($has-type)
            then
              bibtex:type[1]/text()
            else
              'Technical Report'"/>
      <xsl:value-of select="if($has-number)
          then
            my:tie-or-space-connect($type, bibtex:number[1]/text())
          else
            $type"/>
    </my:word>
  </xsl:template>

  <xsl:template name="number-and-series-sentence">
    <xsl:call-template name="sentence">
      <xsl:with-param name="elements">
        <xsl:call-template name="number-and-series"/>
      </xsl:with-param>
      </xsl:call-template>
  </xsl:template>

  <xsl:template name="number-and-series">
    <xsl:param name="mid-sentence" select="false()" as="xs:boolean"/>
    <!-- number or series when there is no volume -->
    <xsl:if test="not(my:exists(bibtex:volume)) and
      (
        my:exists(bibtex:number) or
        my:exists(bibtex:series)
      )">
      <my:word>
        <xsl:apply-templates select="bibtex:number" mode="book">
          <xsl:with-param name="mid-sentence" select="$mid-sentence"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="bibtex:series"/>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template name="chapter-and-pages">
    <xsl:if test="my:exists(bibtex:chapter)">
      <my:word>
      <!--
      <xsl:variable name="chap" select="if(my:empty(bibtex:type))
        then 'chapter' else lower-case(bibtex:type/text())"/>
        -->
      <xsl:variable name="chap" select="if(my:empty(bibtex:type))
        then 'chapter' else bibtex:type/text()"/>
      <xsl:value-of select="my:tie-or-space-connect($chap, bibtex:chapter/text())"/>
      </my:word>
    </xsl:if>
    <xsl:if test="my:exists(bibtex:pages)">
      <my:word>
        <xsl:apply-templates select="bibtex:pages"/>
      </my:word>
    </xsl:if>
  </xsl:template>


  <xsl:template name="date">
    <xsl:if test="my:exists(bibtex:year) or
                  my:exists(bibtex:month)">
      <my:word>
        <xsl:value-of select="bibtex:month"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="bibtex:year"/>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template name="editors">
    <xsl:param name="in" select="."/>
    <xsl:if test="my:exists($in/bibtex:editor)">
      <xsl:variable name="editor-count" select="count($in/bibtex:editor)"/>
      <xsl:apply-templates select="$in/bibtex:editor">
        <xsl:with-param name="person-count" select="$editor-count"/>
      </xsl:apply-templates>
      <xsl:value-of select="if($editor-count gt 1) then ', editors' else ', editor'"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:author|bibtex:editor">
    <xsl:param name="person-count" as="xs:integer" required="yes"/>
    <xsl:variable name="pos" select="position()"/>
    <xsl:if test="$pos ne 1">
      <xsl:variable name="is-last" select="($pos eq $person-count)"/>
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
            <xsl:text>et~al.</xsl:text>
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
    <xsl:variable name="ff" select="my:tie(text())"/>
    <xsl:if test="$ff ne ''">
      <xsl:value-of select="$ff"/>
      <xsl:value-of select="if(string-length($ff) le 2) then '~' else ' '"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibfunc:junior">
    <xsl:text>, </xsl:text>
    <xsl:value-of select="my:tie(text())"/>
  </xsl:template>

  <xsl:template match="bibfunc:last">
    <xsl:variable name="ll" select="normalize-space(text())"/>
    <xsl:variable name="von" select="if(matches($ll, '((^\p{Ll})|(.* \p{Ll}))\P{Lu}* .+'))
                                     then replace($ll,'(((^\p{Ll})|(.* \p{Ll}))\P{Lu}* ).+', '$1')
                                     else ''"/>
    <xsl:value-of select="my:tie-if-short(my:tie($von), my:tie(substring-after($ll, $von)))"/>
  </xsl:template>

  <xsl:template match="bibtex:note" mode="block">
    <xsl:call-template name="single-sentence-block">
      <xsl:with-param name="elements">
        <xsl:apply-templates select="."/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bibtex:pages">
    <xsl:param name="brief" as="xs:boolean" select="false()"/>
    <xsl:variable name="pp" select="normalize-space(text())"/>
    <xsl:if test="$pp ne ''">
      <xsl:choose>
        <xsl:when test="$brief">
          <xsl:apply-templates
            select="bibfunc:parse-pages($pp)"
            mode="brief"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates
            select="bibfunc:parse-pages($pp)"
            mode="long"
          />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
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
        <xsl:value-of select="if(ends-with(bibfunc:start-page/text(),'+')) then 'pages ' else 'page '"/>
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

  <xsl:template match="bibtex:volume">
    <xsl:variable name="vv" select="normalize-space(text())"/>
    <xsl:if test="$vv ne ''">
      <xsl:value-of select="normalize-space($vv)"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:volume" mode="book">
    <xsl:variable name="vv" select="normalize-space(text())"/>
    <xsl:if test="$vv ne ''">
      <my:word>
      <xsl:value-of select="my:tie-or-space-connect('volume', $vv)"/>
      <xsl:if test="my:exists(../bibtex:series)">
        <xsl:text> of </xsl:text>
        <xsl:value-of select="my:emphasize(../bibtex:series/text())"/>
      </xsl:if>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:number">
    <xsl:variable name="nn" select="normalize-space(text())"/>
    <xsl:if test="$nn ne ''">
      <xsl:text>(</xsl:text>
      <xsl:value-of select="normalize-space(text())"/>
      <xsl:text>)</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:number" mode="book">
    <xsl:param name="mid-sentence" select="false()" as="xs:boolean"/>
    <xsl:variable name="nn" select="normalize-space(text())"/>
    <xsl:variable name="num" select="if($mid-sentence)
        then ' number'
        else ' Number'"/>
    <xsl:value-of select="my:tie-or-space-connect($num, $nn)"/>
    <xsl:if test="my:exists(../bibtex:series)">
      <xsl:text> in</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:month|bibtex:year">
    <xsl:variable name="mm" select="normalize-space(text())" />
    <xsl:if test="$mm ne ''">
      <xsl:text> </xsl:text>
      <xsl:value-of select="$mm"/>
    </xsl:if>
  </xsl:template>

  <!-- To-do: titlecase for value if at beginning of sentence -->
  <xsl:template match="bibtex:edition">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:if test="$tt ne ''">
      <my:word>
        <xsl:value-of select="lower-case($tt)"/>
        <!--<xsl:value-of select="$tt"/>-->
        <xsl:text> edition</xsl:text>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:school|
                       bibtex:howpublished|
                       bibtex:publisher|
                       bibtex:address|
                       bibtex:title|
                       bibtex:journal|
                       bibtex:note|
                       bibtex:organization|
                       bibtex:institution">
    <xsl:param name="emphasize" select="false()" as="xs:boolean"/>
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:if test="$tt ne ''">
      <my:word>
        <xsl:value-of select="if($emphasize) then my:emphasize($tt) else $tt"/>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:booktitle" mode="in-ed">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:if test="$tt ne ''">
      <my:word>
        <xsl:text>In </xsl:text>
        <xsl:call-template name="editors">
          <xsl:with-param name="in" select=".."/>
        </xsl:call-template>
        <xsl:if test="my:exists(../bibtex:editor)">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="my:emphasize($tt)"/>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:series">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:value-of select="concat(' ',$tt)"/>
  </xsl:template>

  <xsl:template name="not-implemented">
    <xsl:message>
    <xsl:text>Warning: No output for </xsl:text>
    <xsl:value-of select="../@id"/>
    <xsl:text>.&#xA; Entry type </xsl:text>
    <xsl:value-of select="local-name()"/>
    <xsl:text> is not yet implemented.</xsl:text>
    </xsl:message>
  </xsl:template>

  <xsl:template match="*"/>

  <!-- NAMED TEMPLATES -->

  <xsl:template name="output-bibitem">
    <xsl:param name="blocks"/>
    <xsl:text>&#xA;\bibitem{</xsl:text>
    <xsl:value-of select="../@id" />
    <xsl:text>}&#xA;</xsl:text>
    <xsl:apply-templates select="$blocks/my:block"/>
  </xsl:template>

  <xsl:template match="my:block">
    <xsl:if test="position() ne 1">
      <xsl:text>\newblock </xsl:text>
    </xsl:if>
    <xsl:value-of select="normalize-space(text())"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template name="single-sentence-block">
    <xsl:param name="elements" required="yes"/>
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="block">
    <xsl:param name="contents"/>
    <xsl:variable name="tt" select="normalize-space($contents/text())"/>
    <xsl:if test="$tt ne ''">
      <my:block>
      <xsl:value-of select="replace($tt,'&#160;','~')"/>
      </my:block>
    </xsl:if>
  </xsl:template>

  <xsl:template name="sentence">
    <xsl:param name="elements" required="yes"/>
    <xsl:variable name="element-count" select="count($elements/my:word)"/>
    <xsl:apply-templates select="$elements/my:word">
      <xsl:with-param name="word-count" select="$element-count"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="my:word">
    <xsl:param name="word-count" as="xs:integer" required="yes"/>
    <xsl:variable name="pos" select="position()"/>
    <xsl:variable name="tt" select="replace(normalize-space(text()),'&#160;','~')"/>
    <xsl:if test="$pos ne 1">
      <xsl:text>,</xsl:text>
    </xsl:if>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$tt"/>
    <xsl:if test="($pos eq $word-count) and not(matches($tt,'[\.!\?]\}?$'))">
      <xsl:text>.</xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- STYLE SHEET FUNCTIONS -->

  <xsl:function name="my:emphasize">
    <xsl:param name="txt" as="xs:string" />
    <xsl:sequence select="concat('{\em ',$txt, '}')"/>
  </xsl:function>

  <xsl:function name="my:tie">
    <xsl:param name="str" as="xs:string"/>
    <xsl:variable name="tt" select="normalize-space($str)"/>
    <xsl:value-of select="my:tie-seq(tokenize($tt, ' '))"/>
  </xsl:function>

  <xsl:function name="my:tie-seq">
    <xsl:param name="seq" as="xs:string*"/>
    <xsl:variable name="cnt" select="count($seq)"/>
    <xsl:if test="exists($seq)">
      <xsl:variable name="sep" select="if(matches($seq[$cnt],'-')) then ' ' else '~'"/>
      <xsl:choose>
        <xsl:when test="$cnt le 2">
          <xsl:value-of select="string-join($seq, $sep)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="end" select="concat($seq[$cnt - 1],$sep, $seq[$cnt])"/>
          <xsl:variable name="middle" select="string-join(subsequence($seq, 2, $cnt - 3), ' ')"/>
          <xsl:value-of select="my:tie-if-short($seq[1], concat($middle,' ', $end))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:function>

  <xsl:function name="my:tie-or-space-connect">
    <xsl:param name="start" as="xs:string"/>
    <xsl:param name="end" as="xs:string"/>
    <xsl:variable name="st" select="normalize-space($start)"/>
    <xsl:variable name="ed" select="normalize-space($end)"/>
    <xsl:choose>
      <xsl:when test="$ed eq ''">
        <xsl:value-of select="$st"/>
      </xsl:when>
      <xsl:when test="string-length($ed) lt 3">
        <xsl:value-of select="concat($st, '~', $ed)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat($st, ' ', $ed)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:tie-if-short">
    <xsl:param name="start" as="xs:string"/>
    <xsl:param name="end" as="xs:string"/>
    <xsl:variable name="st" select="normalize-space($start)"/>
    <xsl:variable name="ed" select="normalize-space($end)"/>
    <xsl:choose>
      <xsl:when test="$st eq ''">
        <xsl:value-of select="$ed"/>
      </xsl:when>
      <xsl:when test="string-length($st) lt 3">
        <xsl:value-of select="concat($st, '~', $ed)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat($st, ' ', $ed)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:exists">
    <xsl:param name="n"/>
    <xsl:choose>
      <xsl:when test="empty($n)">
        <xsl:sequence select="false()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="tt" select="normalize-space(string-join($n/text(),' '))"/>
        <xsl:sequence select="$tt ne ''"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:empty">
    <xsl:param name="n"/>
    <xsl:sequence select="not(my:exists($n))"/>
  </xsl:function>

  <xsl:function name="my:normalize-count">
    <xsl:param name="a" />
    <xsl:param name="b" />
    <xsl:variable name="c" select="$a div 10"/>
    <xsl:choose>
      <xsl:when test="$c lt 1">
        <xsl:sequence select="$b"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="my:normalize-count($c, $b * 10)"/>
    </xsl:otherwise>
  </xsl:choose>
  </xsl:function>

  <xsl:function name="my:citations">
      <xsl:choose>
      <xsl:when test="$aux-file eq ''">
        <xsl:message>No aux file specified. Processing all entries in database.</xsl:message>
      </xsl:when>
      <xsl:when test="function-available('auxparser:xslt-parse', 2)">
       <xsl:message>
        <xsl:text>Top-level aux file: </xsl:text><xsl:value-of select="$aux-file"/>
      </xsl:message>
      </xsl:when>
      <xsl:otherwise>
       <xsl:message><xsl:text>Cannot parse aux files because Java function
net.sourceforge.bibtexml.AuxParser.xsltParse(String, String)
is not available. Processing all entries in database.
</xsl:text></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:sequence
      use-when="function-available('auxparser:xslt-parse', 2)"
      select="if($aux-file) then auxparser:xslt-parse($aux-file, $aux-encoding) else '*'"/>
    <xsl:sequence
      use-when="not(function-available('auxparser:xslt-parse', 2))"
      select="'*'"/>
  </xsl:function>
</xsl:stylesheet>


