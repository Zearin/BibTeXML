<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) Moritz Ringler, 2004                            -->
<!-- XSLT stylesheet that converts bibliographic data    -->
<!-- from BibXML to RIS format.                          -->
<!-- The end-of-line comments avoid unwanted line breaks -->
<!-- in the output. Do not remove them!                  -->
<xsl:stylesheet version="2.0"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:bibtex="http://bibtexml.sf.net/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" indent="no" encoding="windows-1252"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <xsl:apply-templates select="bibtex:file"/>
    </xsl:template>

    <xsl:template match="bibtex:file">
        <xsl:apply-templates select="bibtex:entry"/>
    </xsl:template>

    <xsl:template match="bibtex:entry">
        <xsl:apply-templates select="*"/>
        <xsl:call-template name="field">
            <xsl:with-param name="risid" select="'ID'" />
            <xsl:with-param name="value" select="@id" />
        </xsl:call-template>
        <xsl:apply-templates select="descendant::node() except *"/>
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
        <xsl:variable name="value"
                select="if (contains(.,',')) then replace(text(),'&#160;',' ') else if (contains(.,',')) then text() else concat( replace(normalize-space(.),'&#160;',' '), ', ', replace(replace(.,'[^ ]+$',''),'&#160;',' ') )"/>
        <xsl:call-template name="field">
            <xsl:with-param
                    name="risid"
                    select="if (local-name() eq 'author') then 'AU' else 'ED'" />
            <xsl:with-param
                    name="value"
                    select="$value" />
        </xsl:call-template>
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
        <xsl:call-template name="field">
            <xsl:with-param
                    name="risid"
                    select="if(contains(.,'.')) then 'JO' else 'JF'" />
        </xsl:call-template>
    </xsl:template>

<!-- keywords -->
    <xsl:template match="bibtex:keywords|bibtex:category">
        <xsl:if test="empty(./*)">
            <xsl:call-template name="field">
                <xsl:with-param name="risid" select="'KW'" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="bibtex:keyword">
        <xsl:if test="empty(./*)">
            <xsl:call-template name="field">
                <xsl:with-param name="risid" select="'KW'" />
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
        <xsl:variable
                name="tokens"
                select="tokenize(bibtex:pages,'(-)|(--)')" />
        <xsl:choose>
            <xsl:when test="contains(bibtex:pages,'-')">
                <xsl:call-template name="field">
                    <xsl:with-param name="risid" select="'SP'" />
                    <xsl:with-param name="value" select="$tokens[1]" />
                </xsl:call-template>
                <xsl:call-template name="field">
                    <xsl:with-param name="risid" select="'EP'" />
                    <xsl:with-param name="value" select="$tokens[2]" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="field">
                    <xsl:with-param name="risid" select="'SP'" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

<!-- titel -->
    <xsl:template match="bibtex:title">
        <xsl:call-template name="field">
            <xsl:with-param name="risid" select="'TI'"/>
        </xsl:call-template>
    </xsl:template>
    
<!-- url -->
    <xsl:template match="bibtex:url" priority="2">
        <xsl:apply-templates select="." mode="internal" />
    </xsl:template>

    <xsl:template match="bibtex:doi" priority="2">
        <xsl:if test="not(exists(../bibtex:url)) or (../bibtex:url eq '')">
            <xsl:apply-templates select="." mode="internal" />
        </xsl:if>
    </xsl:template>

    <xsl:template match="bibtex:howpublished">
    <!--
	special case for the often used
	howpublished = "\url{http://www.example.com/}",
    -->
        <xsl:if test="contains(.,'\url') and (not(exists(../bibtex:url)) or (../bibtex:url eq '')) and (not(exists(../bibtex:doi)) or (../bibtex:doi eq ''))">
            <xsl:apply-templates select="." mode="internal" />
        </xsl:if>
    </xsl:template>

    <xsl:template
            match="bibtex:url|bibtex:doi|bibtex:howpublished"
            mode="internal"
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
    
<!-- endofrecord -->
    <xsl:template name="end-of-record">
        <xsl:text>ER  -&#xA;&#xA;</xsl:text>
    </xsl:template>

    <xsl:template match="text()" priority="0.5" />

</xsl:stylesheet>
