<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) 2007 Moritz Ringler -->
<xsl:transform version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bibfunc="http://bibtexml.sf.net/functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="bibfunc xsl xs">

       <!-- returns a bibfunc:person node with the
        children
        bibfunc:last
        bibfunc:first
        bibfunc:junior
        where all elements except last may or may not be present
   -->
  <xsl:function name="bibfunc:parse-author">
    <xsl:param name="author-raw" as="xs:string"/>
       <!-- asterisk is not allowed in author, keywords or periodical name
                 see http://www.refman.com/support/risformat_fields_02.asp -->
    <xsl:variable name="author" select="normalize-space(replace($author-raw, '\*', ''))"/>
    <xsl:variable name="parts" select="tokenize($author, ',')" />
    <xsl:variable name="numparts" select="count($parts)" />
    <xsl:variable name="result">
      <bibfunc:person>
        <xsl:choose>
          <xsl:when test="$numparts ge 3">
                    <!-- von last, junior, first -->
            <bibfunc:first>
              <xsl:value-of select="normalize-space($parts[3])" />
            </bibfunc:first>
            <bibfunc:junior>
              <xsl:value-of select="normalize-space($parts[2])" />
            </bibfunc:junior>
            <bibfunc:last>
              <xsl:value-of select="normalize-space($parts[1])" />
            </bibfunc:last>
          </xsl:when>
          <xsl:when test="$numparts eq 2">
                    <!-- von last, first -->
            <bibfunc:first>
              <xsl:value-of select="normalize-space($parts[2])" />
            </bibfunc:first>
            <bibfunc:last>
              <xsl:value-of select="normalize-space($parts[1])" />
            </bibfunc:last>
          </xsl:when>
          <xsl:otherwise>
                    <!-- first von last -->
            <xsl:choose>
              <xsl:when test="matches(normalize-space($author), ' \p{Ll}')">
                                <!-- we have a word starting with a lowercase char, must be a von particle -->
                <bibfunc:first>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*?) (\p{Ll}.*)$', '$1'))"/>
                </bibfunc:first>
                <bibfunc:last>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*?) (\p{Ll}.*)$', '$2'))"/>
                </bibfunc:last>
              </xsl:when>
              <xsl:when test="matches(normalize-space($author), ' \p{Lu}')">
                <bibfunc:first>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*) (\p{Lu}.*)$', '$1'))"/>
                </bibfunc:first>
                <bibfunc:last>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*) (\p{Lu}.*)$', '$2'))"/>
                </bibfunc:last>
              </xsl:when>
              <xsl:otherwise>
                <bibfunc:last>
                  <xsl:value-of select="normalize-space($author)"/>
                </bibfunc:last>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </bibfunc:person>
    </xsl:variable>
    <xsl:sequence select="$result"/>
  </xsl:function>

  <xsl:function name="bibfunc:parse-pages">
    <xsl:param name="pages-raw" as="xs:string"/>
    <xsl:variable name="result">
      <bibfunc:pages>
      <xsl:choose>
        <xsl:when test="contains($pages-raw,'-')">
          <xsl:variable name="tokens" select="tokenize($pages-raw,'(-)|(--)')" />
          <bibfunc:start-page>
            <xsl:value-of select="normalize-space($tokens[1])" />
          </bibfunc:start-page>
          <bibfunc:end-page>
            <xsl:value-of select="normalize-space($tokens[2])" />
          </bibfunc:end-page>
        </xsl:when>
        <xsl:otherwise>
          <bibfunc:start-page>
            <xsl:value-of select="$pages-raw" />
          </bibfunc:start-page>
        </xsl:otherwise>
      </xsl:choose>
      </bibfunc:pages>
    </xsl:variable>
    <xsl:sequence select="$result"/>
  </xsl:function>

<xsl:function name="bibfunc:normalize-doi">
    <xsl:param name="doi" as="xs:string" />
    <!-- Step 1 : remove leading and trailing whitespace -->
    <xsl:variable name="step1" select="normalize-space($doi)"/>
    <!-- Step 2 : remove a possible leading "urn:doi:" -->
    <xsl:variable name="step2"
      select="if (starts-with($step1, 'urn:doi:')) then substring-after($step1, 'urn:doi:') else $step1"/>
    <!-- Step 3 : remove a possible leading "doi:" -->
    <xsl:sequence
      select="if (starts-with($step1, 'doi:')) then substring-after($step1, 'doi:') else $step2"/>
  </xsl:function>

  <xsl:function name="bibfunc:doi-to-url">
    <xsl:param name="doi" as="xs:string" />
    <xsl:param name="proxy" as="xs:string" />
    <xsl:sequence select="concat($proxy, bibfunc:normalize-doi($doi))"/>
  </xsl:function>

  <xsl:function name="bibfunc:doi-to-url">
    <xsl:param name="doi" as="xs:string" />
    <xsl:sequence select="bibfunc:doi-to-url($doi, 'http://dx.doi.org/')"/>
  </xsl:function>

</xsl:transform>
