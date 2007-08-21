<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) Moritz Ringler, 2004                            -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to Endnote Export format.               -->
<xsl:stylesheet version="2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:foo="http://foo/bar/baz">
  <xsl:output method="text" indent="no" encoding="windows-1252"/>
  <xsl:strip-space elements="*"/>

  <xsl:include href="include/fn-parse-author.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates select="bibtex:file"/>
  </xsl:template>

  <xsl:template match="bibtex:file">
    <xsl:apply-templates select="bibtex:entry"/>
  </xsl:template>

    <!-- RIS spec requires TY to be first and ER to be last, apart from
         that tag order can be arbitrary
         see http://www.refman.com/support/risformat_fields_02.asp -->
  <xsl:template match="bibtex:entry">
        <!-- process immediate children: %0 -->
    <xsl:apply-templates select="*"/>
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%F'" />
      <xsl:with-param name="value" select="if (string-length(@id) > 20) then substring(@id, 1, 20) else @id" />
    </xsl:call-template>
        <!-- process all but the immediate descendants -->
    <xsl:apply-templates select="descendant::node() except *"/>
    <xsl:call-template name="end-of-record" />
  </xsl:template>

<!-- ENTRYTYPES -->

<!-- book -->
  <xsl:template match="bibtex:book|bibtex:booklet|bibtex:incollection">
    <xsl:text>%0 Book&#xA;</xsl:text>
  </xsl:template>

<!-- chapter  -->
  <xsl:template match="bibtex:inbook">
    <xsl:text>%0 Book Section&#xA;</xsl:text>
  </xsl:template>

<!-- conference proceedings -->
  <xsl:template match="bibtex:proceedings|bibtex:inproceedings|bibtex:conference">
    <xsl:text>%0 Conference Proceedings&#xA;</xsl:text>
  </xsl:template>

<!-- generic -->
  <xsl:template match="bibtex:manual|bibtex:unpublished|bibtex:misc">
    <xsl:text>%0 Generic&#xA;</xsl:text>
  </xsl:template>

<!-- article -->
  <xsl:template match="bibtex:article">
    <xsl:text>%0 Journal Article&#xA;</xsl:text>
  </xsl:template>

<!-- report -->
  <xsl:template match="bibtex:techreport">
    <xsl:text>%0 Report&#xA;</xsl:text>
  </xsl:template>

<!-- thesis -->
  <xsl:template match="bibtex:mastersthesis">
    <xsl:text>%0 Thesis&#xA;</xsl:text>
    <xsl:text>%9 Master&#xA;</xsl:text>
  </xsl:template>
  <xsl:template match="bibtex:phdthesis">
    <xsl:text>%0 Thesis&#xA;</xsl:text>
    <xsl:text>%9 PhD&#xA;</xsl:text>
  </xsl:template>

<!-- unpublished -->
  <xsl:template match="bibtex:unpublished">
    <xsl:text>%0 Manuscript&#xA;</xsl:text>
  </xsl:template>


<!-- ENW FIELDS -->

<!-- address -->
  <xsl:template match="bibtex:address">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%+'" />
    </xsl:call-template>
  </xsl:template>

<!-- author / editor -->
  <xsl:template
      match="bibtex:author|bibtex:editor">
    <xsl:variable name="enwid" select="if (local-name() eq 'author') then '%A ' else '%E '" />
    <xsl:for-each select="tokenize(normalize-space(text()), ' and ', 'i')">
      <xsl:value-of select="$enwid" />
      <xsl:apply-templates select="foo:parse-author(.)/foo:person"/>
      <xsl:text>&#xA;</xsl:text>
    </xsl:for-each>
  </xsl:template>

    <!-- von Last[, First[, Jr]], at most 255 charaters
    -->
  <xsl:template
      match="foo:person">
    <xsl:variable name="formatted-author">
      <xsl:value-of select="replace(foo:last,'&#160;', ' ')" />
      <xsl:if test="foo:first">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="replace(foo:first,'&#160;', ' ')"/>
        <xsl:if test="foo:junior">
          <xsl:text>, </xsl:text>
          <xsl:value-of select="replace(foo:junior,'&#160;', ' ')" />
        </xsl:if>
      </xsl:if>
    </xsl:variable>
    <xsl:value-of select="if(string-length($formatted-author) > 255) then substring($formatted-author, 1, 255) else $formatted-author"/>
  </xsl:template>


<!-- booktitle -->
  <xsl:template match="bibtex:booktitle">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%B'" />
    </xsl:call-template>
  </xsl:template>

<!-- issue/number -->
  <xsl:template match="bibtex:number">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%N'" />
    </xsl:call-template>
  </xsl:template>

<!-- journal -->
  <xsl:template
      match="bibtex:journal">
    <xsl:call-template name="field255">
      <xsl:with-param
          name="enwid"
          select="'%B'" />
            <!-- asterisk is not allowed in author, keywords or periodical name
                 see http://www.refman.com/support/risformat_fields_02.asp -->
      <xsl:with-param name="value" select="replace(normalize-space(.), '\*', '') "/>
    </xsl:call-template>
  </xsl:template>

<!-- keywords -->
  <xsl:template match="bibtex:keywords|bibtex:category">
    <xsl:if test="empty(./*)">
            <!-- asterisk is not allowed in author, keywords or periodical name
                     see http://www.refman.com/support/risformat_fields_02.asp -->
      <xsl:call-template name="field255">
        <xsl:with-param name="enwid" select="'%K'" />
        <xsl:with-param name="value" select="replace(text(), '\*', '')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:keyword">
    <xsl:if test="empty(./*)">
      <xsl:call-template name="field">
        <xsl:with-param name="enwid" select="'%K'" />
                <!-- asterisk is not allowed in author, keywords or periodical name
                     see http://www.refman.com/support/risformat_fields_02.asp -->
        <xsl:with-param name="value" select="replace(normalize-space(.), '\*', '') "/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

<!--notes-->
  <xsl:template match="bibtex:notes|bibtex:note">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%O'" />
    </xsl:call-template>
  </xsl:template>

<!-- abstract -->
  <xsl:template
      match="bibtex:abstract">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%X'" />
    </xsl:call-template>
  </xsl:template>

<!-- publisher -->
  <xsl:template match="bibtex:publisher|bibtex:organization| bibtex:institution|bibtex:school">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%I'" />
    </xsl:call-template>
  </xsl:template>

<!-- issn/isbn -->
  <xsl:template match="bibtex:isbn|bibtex:issn">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'SN'" />
      <xsl:with-param
          name="value"
          select="concat(local-name(), ':', normalize-space(.))" />
    </xsl:call-template>
  </xsl:template>


<!-- starting page/end page -->
  <xsl:template match="bibtex:pages">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%P'" />
      <xsl:with-param
          name="value"
          select="replace(normalize-space(.), '--+', '-')" />
    </xsl:call-template>
  </xsl:template>

<!-- titel -->
  <xsl:template match="bibtex:title">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%T'"/>
    </xsl:call-template>
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%!'"/>
    </xsl:call-template>
  </xsl:template>

<!-- url -->
  <xsl:template match="bibtex:url" priority="2">
    <xsl:apply-templates select="." mode="url" />
  </xsl:template>

  <xsl:template match="bibtex:doi" priority="2">
        <!-- you can do other stuff here -->
    <xsl:apply-templates select="." mode="url" />
  </xsl:template>

  <xsl:template match="bibtex:howpublished" priority="2">
        <!-- you can do other stuff here -->
    <xsl:apply-templates select="." mode="url" />
  </xsl:template>

  <xsl:template
      match="bibtex:url|bibtex:doi|bibtex:howpublished"
      mode="url"
      priority="1">
    <xsl:if test="not(./text() eq '')">
      <xsl:call-template name="field">
        <xsl:with-param name="enwid" select="'%U'"/>
        <xsl:with-param
            name="value"
            select="if(local-name() eq 'doi') then concat('http://dx.doi.org/', text()) else if(local-name() eq 'howpublished') then substring-after(text(),'\url') else text()" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

<!-- volume -->
  <xsl:template match="bibtex:volume">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%V'" />
    </xsl:call-template>
  </xsl:template>

<!-- year -->
  <xsl:template match="bibtex:year">
    <xsl:call-template name="field">
      <xsl:with-param name="enwid" select="'%8'" />
      <xsl:with-param name="value" select="concat(normalize-space(.),'///')" />
    </xsl:call-template>
  </xsl:template>

<!-- NAMED TEMPLATES -->

<!-- field -->
  <xsl:template name="field">
    <xsl:param name="enwid" as="xs:string"/>
    <xsl:param name="value" select="./text()" as="xs:string"/>
    <xsl:value-of select="$enwid" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="normalize-space($value)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template name="field255">
    <xsl:param name="enwid" as="xs:string"/>
    <xsl:param name="value" select="./text()" as="xs:string"/>
    <xsl:variable name="value-trimmed" select="normalize-space($value)"/>
    <xsl:value-of select="$enwid" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="if(string-length($value-trimmed) > 255) then substring($value-trimmed, 1, 255) else $value-trimmed"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

<!-- endofrecord -->
  <xsl:template name="end-of-record">
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="text()" priority="0.5" />

</xsl:stylesheet>
