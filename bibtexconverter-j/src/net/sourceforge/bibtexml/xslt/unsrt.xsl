<?xml version="1.0"?>
<!-- $Id: bibxml2bib.xsl 391 2007-12-23 17:42:35Z ringler $ -->
<!-- This stylesheet produces a latex thebibliography environment
     (bbl file) that is similar to what bibtex produces with the
     standard bibtex style unsrt.bst.
     Notable differences include:
      * braces are completely ignored (e.g. in author parsing)
      * case inside titles is not changed
      * some differences concerning tying with ~ under relatively rare
        circumstances
      * long lines are not wrapped
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
    <xsl:apply-templates select="bibtex:preamble"/>
    <xsl:if test="exists(bibtex:preamble)">
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

  <xsl:template match="bibtex:preamble">
    <xsl:value-of select="normalize-space(text())"/>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <xsl:apply-templates select="bibtex:*"/>
  </xsl:template>

  <!-- @ARTICLE -->
  <xsl:template match="bibtex:article">
    <!-- bibitem -->
    <xsl:call-template name="output-bibitem"/>
    <!-- author(s) -->
    <xsl:if test="exists(bibtex:author)">
      <xsl:call-template name="sentence">
        <xsl:with-param name="elements">
          <my:word>
          <xsl:apply-templates select="bibtex:author">
            <xsl:with-param
              name="person-count"
              select="count(bibtex:author)"
            />
          </xsl:apply-templates>
          </my:word>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
    <!--
      block: title
    -->
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:title"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <!--
      block: journal, volume, number, pages, month, year
    -->
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:journal">
              <xsl:with-param name="emphasize" select="true()"/>
            </xsl:apply-templates>
            <!-- word: volume, number, pages -->
            <xsl:if test="exists(bibtex:volume) or
                          exists(bibtex:number) or
                          exists(bibtex:pages)">
              <my:word>
                <xsl:apply-templates select="bibtex:volume"/>
                <xsl:apply-templates select="bibtex:number"/>
                <xsl:apply-templates select="bibtex:pages">
                  <xsl:with-param
                    name="has-volume-or-number"
                    select="exists(bibtex:volume) or exists(bibtex:number)"
                  />
                </xsl:apply-templates>
              </my:word>
            </xsl:if>
            <xsl:call-template name="date"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <!--
      block: note
    -->
    <xsl:apply-templates select="bibtex:note" mode="block"/>
  </xsl:template>

  <xsl:template match="bibtex:author|bibtex:editor">
    <xsl:param name="person-count" as="xs:integer"/>
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
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="."/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
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
    <xsl:variable name="pp" select="normalize-space(text())"/>
    <xsl:if test="$pp ne ''">
      <xsl:choose>
        <xsl:when test="$has-volume-or-number">
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

  <xsl:template match="bibtex:volume">
    <xsl:variable name="vv" select="normalize-space(text())"/>
    <xsl:if test="$vv ne ''">
      <xsl:value-of select="normalize-space($vv)"/>
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

  <xsl:template match="bibtex:month|bibtex:year">
    <xsl:variable name="mm" select="normalize-space(text())" />
    <xsl:if test="$mm ne ''">
      <xsl:text> </xsl:text>
      <xsl:value-of select="$mm"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="output-bibitem">
    <xsl:text>&#xA;\bibitem{</xsl:text>
    <xsl:value-of select="../@id" />
    <xsl:text>}&#xA;</xsl:text>
  </xsl:template>


  <!-- @BOOK -->
  <xsl:template match="bibtex:book">
  <!--
  crossref missing$
    { format.bvolume output
      new.block
      format.number.series output
      new.sentence
      publisher "publisher" output.check
      address output
    }
    { new.block
      format.book.crossref output.nonnull
    }
  if$
  format.edition output
  format.date "year" output.check
  new.block
  note output
  fin.entry
}-->
     <!-- bibitem -->
    <xsl:call-template name="output-bibitem"/>
    <!-- author or editor -->
    <xsl:if test="exists(bibtex:author) or exists(bibtex:editor)">
      <xsl:choose>
        <xsl:when test="exists(bibtex:author)">
          <xsl:apply-templates select="bibtex:author">
            <xsl:with-param
              name="person-count"
              select="count(bibtex:author)"
            />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
            <xsl:variable
              name="editor-count"
              select="count(bibtex:editor)"/>
            <xsl:apply-templates select="bibtex:editor">
            <xsl:with-param
              name="person-count"
              select="$editor-count"
            />
          </xsl:apply-templates>
          <xsl:value-of select="if($editor-count gt 1)
            then ', editors' else ', editor'"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>.&#xA;</xsl:text>
    </xsl:if>
    <!--
      block: title, volume(series)
    -->
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
              <xsl:apply-templates select="bibtex:title">
                <xsl:with-param name="emphasize" select="true()"/>
              </xsl:apply-templates>
              <xsl:apply-templates select="bibtex:volume" mode="book"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <!--
      block: number, publisher, edition, year etc.
    -->
    <xsl:call-template name="block">
      <xsl:with-param name="contents">
        <!-- number or series when there is no volume -->
        <xsl:if test="not(exists(bibtex:volume)) and
              (
                exists(bibtex:number) or
                exists(bibtex:series)
              )">
          <xsl:call-template name="sentence">
            <xsl:with-param name="elements">
              <my:word>
                <xsl:apply-templates select="bibtex:number" mode="book"/>
                <xsl:apply-templates select="bibtex:series"/>
              </my:word>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
        <!-- publisher, address, edition, month and year -->
        <xsl:call-template name="sentence">
          <xsl:with-param name="elements">
            <xsl:apply-templates select="bibtex:publisher"/>
            <xsl:apply-templates select="bibtex:address"/>
            <xsl:apply-templates select="bibtex:edition"/>
            <xsl:call-template name="date"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <!--
      block: note
    -->
    <xsl:apply-templates select="bibtex:note" mode="block"/>
  </xsl:template>


  <xsl:template match="bibtex:edition">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:if test="$tt ne ''">
      <my:word>
        <xsl:value-of select="lower-case($tt)"/>
        <xsl:text> edition</xsl:text>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:publisher|bibtex:address|bibtex:title|bibtex:journal|bibtex:note">
    <xsl:param name="emphasize" select="false()" as="xs:boolean"/>
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:if test="$tt ne ''">
      <my:word>
        <xsl:value-of select="if($emphasize) then my:emphasize($tt) else $tt"/>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template name="date">
    <xsl:if test="exists(bibtex:year) or
                  exists(bibtex:month)">
      <my:word>
        <xsl:value-of select="bibtex:month"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="bibtex:year"/>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:series">
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:value-of select="concat(' ',$tt)"/>
  </xsl:template>

  <xsl:template match="bibtex:number" mode="book">
    <xsl:variable name="nn" select="normalize-space(text())"/>
    <xsl:text> Number~</xsl:text>
    <xsl:value-of select="$nn"/>
    <xsl:if test="exists(../bibtex:series)">
      <xsl:text> in</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:volume" mode="book">
    <xsl:variable name="vv" select="normalize-space(text())"/>
    <xsl:if test="$vv ne ''">
      <my:word>
      <xsl:text>volume~</xsl:text>
      <xsl:value-of select="$vv"/>
      <xsl:if test="exists(../bibtex:series)">
        <xsl:text> of </xsl:text>
        <xsl:value-of select="my:emphasize(../bibtex:series)"/>
      </xsl:if>
      </my:word>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:booklet">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:inbook">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:incollection">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:inproceedings">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:conference">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:manual">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:mastersthesis">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:misc">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:phdthesis">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:proceedings">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:techreport">
    <xsl:call-template name="not-implemented"/>
  </xsl:template>

  <xsl:template match="bibtex:unpublished">
    <xsl:call-template name="not-implemented"/>
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

  <xsl:template name="block">
    <xsl:param name="contents"/>
    <xsl:variable name="tt" select="normalize-space($contents/text())"/>
    <xsl:if test="$tt ne ''">
      <xsl:text>\newblock </xsl:text>
      <xsl:value-of select="$tt"/>
      <xsl:text>&#xA;</xsl:text>
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
    <xsl:variable name="tt" select="normalize-space(text())"/>
    <xsl:if test="$pos ne 1">
      <xsl:text>,</xsl:text>
    </xsl:if>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$tt"/>
    <xsl:if test="($pos eq $word-count) and not(matches($tt,'[\.!\?]\}?$'))">
      <xsl:text>.</xsl:text>
    </xsl:if>
  </xsl:template>


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
    <xsl:choose>
      <xsl:when test="$cnt le 2">
        <xsl:value-of select="string-join($seq, '~')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="end" select="concat($seq[$cnt - 1],'~', $seq[$cnt])"/>
        <xsl:variable name="middle" select="string-join(subsequence($seq, 2, $cnt - 3), ' ')"/>
        <xsl:value-of select="my:tie-if-short($seq[1], concat($middle,' ', $end))"/>
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
</xsl:stylesheet>


