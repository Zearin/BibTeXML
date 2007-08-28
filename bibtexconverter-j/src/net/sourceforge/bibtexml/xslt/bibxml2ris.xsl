<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) Moritz Ringler, 2004-2007                       -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to RIS format.                          -->
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
        <!-- process immediate children: TY -->
    <xsl:apply-templates select="*"/>
        <!-- ID: this field has a maximum length of 20 characters
             see http://www.refman.com/support/risformat_tags_01.asp -->
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'ID'" />
      <xsl:with-param name="value" select="if (string-length(@id) > 20) then substring(@id, 1, 20) else @id" />
    </xsl:call-template>
        <!-- process all but the immediate descendants -->
    <xsl:apply-templates select="descendant::node() except *"/>
        <!-- ER -->
    <xsl:call-template name="end-of-record" />
  </xsl:template>

<!-- RIS ENTRYTYPES IN ALPHABETIC ORDER -->

<!-- book -->
  <xsl:template match="bibtex:book|bibtex:booklet|bibtex:incollection">
    <xsl:text>TY  - BOOK&#xA;</xsl:text>
  </xsl:template>

<!-- chapter  -->
  <xsl:template match="bibtex:inbook">
    <xsl:text>TY  - CHAP&#xA;</xsl:text>
  </xsl:template>

<!-- conference proceedings -->
  <xsl:template match="bibtex:proceedings|bibtex:inproceedings|bibtex:conference">
    <xsl:text>TY  - CONF&#xA;</xsl:text>
  </xsl:template>

<!-- generic -->
  <xsl:template match="bibtex:manual|bibtex:unpublished|bibtex:misc">
    <xsl:text>TY  - GEN&#xA;</xsl:text>
  </xsl:template>

<!-- article -->
  <xsl:template match="bibtex:article">
    <xsl:text>TY  - JOUR&#xA;</xsl:text>
  </xsl:template>

<!-- report -->
  <xsl:template match="bibtex:techreport">
    <xsl:text>TY  - RPRT&#xA;</xsl:text>
  </xsl:template>

<!-- thesis -->
  <xsl:template match="bibtex:mastersthesis|bibtex:phdthesis">
    <xsl:text>TY  - THES&#xA;</xsl:text>
  </xsl:template>

<!-- unpublished -->
  <xsl:template match="bibtex:unpublished">
    <xsl:text>TY  - UNPB&#xA;</xsl:text>
  </xsl:template>


<!-- RIS FIELDS IN ALPHABETIC ORDER -->

<!-- address -->
  <xsl:template match="bibtex:address">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'AD'" />
    </xsl:call-template>
  </xsl:template>

<!-- author / editor -->
  <xsl:template
      match="bibtex:author|bibtex:editor">
    <xsl:variable name="risid" select="if (local-name() eq 'author') then 'AU  - ' else 'ED  - '" />
    <xsl:for-each select="tokenize(normalize-space(text()), ' and ', 'i')">
      <xsl:value-of select="$risid" />
      <xsl:apply-templates select="foo:parse-author(.)/foo:person"/>
      <xsl:text>&#xA;</xsl:text>
    </xsl:for-each>
  </xsl:template>

    <!-- von Last[,First[,Jr]], at most 255 charaters
         see http://www.refman.com/support/risformat_tags_02.asp
    -->
  <xsl:template
      match="foo:person">
    <xsl:variable name="formatted-author">
      <xsl:value-of select="replace(foo:last,'&#160;', ' ')" />
      <xsl:if test="foo:first">
        <xsl:text>,</xsl:text>
        <xsl:value-of select="replace(foo:first,'&#160;', ' ')"/>
        <xsl:if test="foo:junior">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="replace(foo:junior,'&#160;', ' ')" />
        </xsl:if>
      </xsl:if>
    </xsl:variable>
    <xsl:value-of select="if(string-length($formatted-author) > 255) then substring($formatted-author, 1, 255) else $formatted-author"/>
  </xsl:template>


<!-- booktitle -->
  <xsl:template match="bibtex:booktitle">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'BT'" />
    </xsl:call-template>
  </xsl:template>

<!-- end page EP : see SP below -->

<!-- issue/number -->
  <xsl:template match="bibtex:number">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'IS'" />
    </xsl:call-template>
  </xsl:template>

<!-- journal -->
  <xsl:template
      match="bibtex:journal">
    <xsl:call-template name="field255">
      <xsl:with-param
          name="risid"
          select="if(contains(.,'.')) then 'JO' else 'JF'" />
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
        <xsl:with-param name="risid" select="'KW'" />
        <xsl:with-param name="value" select="replace(text(), '\*', '')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibtex:keyword">
    <xsl:if test="empty(./*)">
      <xsl:call-template name="field">
        <xsl:with-param name="risid" select="'KW'" />
                <!-- asterisk is not allowed in author, keywords or periodical name
                     see http://www.refman.com/support/risformat_fields_02.asp -->
        <xsl:with-param name="value" select="replace(normalize-space(.), '\*', '') "/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

<!--notes-->
  <xsl:template match="bibtex:notes|bibtex:note">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'N1'" />
    </xsl:call-template>
  </xsl:template>

<!-- abstract -->
  <xsl:template
      match="bibtex:abstract">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'N2'" />
    </xsl:call-template>
  </xsl:template>

<!-- publisher -->
  <xsl:template match="bibtex:publisher|bibtex:organization| bibtex:institution|bibtex:school">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'PB'" />
    </xsl:call-template>
  </xsl:template>

<!-- issn/isbn -->
  <xsl:template match="bibtex:isbn|bibtex:issn">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'SN'" />
      <xsl:with-param
          name="value"
          select="concat(local-name(), ':', normalize-space(.))" />
    </xsl:call-template>
  </xsl:template>


<!-- starting page/end page -->
  <xsl:template match="bibtex:pages">
    <xsl:apply-templates select="foo:parse-pages(text())/foo:pages/*"/>
  </xsl:template>

  <xsl:template match="foo:start-page">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'SP'" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="foo:end-page">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'EP'" />
    </xsl:call-template>
  </xsl:template>

<!-- titel -->
  <xsl:template match="bibtex:title">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'TI'"/>
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
        <xsl:with-param name="risid" select="'UR'"/>
        <xsl:with-param
            name="value"
            select="if(local-name() eq 'doi') then concat('http://dx.doi.org/', text()) else if(local-name() eq 'howpublished') then substring-after(text(),'\url') else text()" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

<!-- volume -->
  <xsl:template match="bibtex:volume">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'VL'" />
    </xsl:call-template>
  </xsl:template>

<!-- year -->
  <xsl:template match="bibtex:year">
    <xsl:call-template name="field">
      <xsl:with-param name="risid" select="'Y1'" />
      <xsl:with-param name="value" select="concat(normalize-space(.),'///')" />
    </xsl:call-template>
  </xsl:template>

<!-- NAMED TEMPLATES -->

<!-- field -->
  <xsl:template name="field">
    <xsl:param name="risid" as="xs:string"/>
    <xsl:param name="value" select="./text()" as="xs:string"/>
    <xsl:value-of select="$risid" />
    <xsl:text>  - </xsl:text>
    <xsl:value-of select="normalize-space($value)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template name="field255">
    <xsl:param name="risid" as="xs:string"/>
    <xsl:param name="value" select="./text()" as="xs:string"/>
    <xsl:variable name="value-trimmed" select="normalize-space($value)"/>
    <xsl:value-of select="$risid" />
    <xsl:text>  - </xsl:text>
    <xsl:value-of select="if(string-length($value-trimmed) > 255) then substring($value-trimmed, 1, 255) else $value-trimmed"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

<!-- endofrecord -->
  <xsl:template name="end-of-record">
    <xsl:text>ER  -&#xA;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="text()" priority="0.5" />

</xsl:stylesheet>
