<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) 2007 Moritz Ringler -->
<xsl:transform version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:foo="http://foo/bar/baz"
    xmlns:xs="http://www.w3.org/2001/XMLSchema">

       <!-- returns a foo:person node with the
        children
        foo:last
        foo:first
        foo:junior
        where all elements except last may or may not be present
   -->
  <xsl:function name="foo:parse-author">
    <xsl:param name="author-raw" as="xs:string"/>
       <!-- asterisk is not allowed in author, keywords or periodical name
                 see http://www.refman.com/support/risformat_fields_02.asp -->
    <xsl:variable name="author" select="normalize-space(replace($author-raw, '\*', ''))"/>
    <xsl:variable name="parts" select="tokenize($author, ',')" />
    <xsl:variable name="numparts" select="count($parts)" />
    <xsl:variable name="result">
      <foo:person>
        <xsl:choose>
          <xsl:when test="$numparts ge 3">
                    <!-- von last, junior, first -->
            <foo:first>
              <xsl:value-of select="normalize-space($parts[3])" />
            </foo:first>
            <foo:junior>
              <xsl:value-of select="normalize-space($parts[2])" />
            </foo:junior>
            <foo:last>
              <xsl:value-of select="normalize-space($parts[1])" />
            </foo:last>
          </xsl:when>
          <xsl:when test="$numparts eq 2">
                    <!-- von last, first -->
            <foo:first>
              <xsl:value-of select="normalize-space($parts[2])" />
            </foo:first>
            <foo:last>
              <xsl:value-of select="normalize-space($parts[1])" />
            </foo:last>
          </xsl:when>
          <xsl:otherwise>
                    <!-- first von last -->
            <xsl:choose>
              <xsl:when test="matches(normalize-space($author), ' \p{Ll}')">
                                <!-- we have a word starting with a lowercase char, must be a von particle -->
                <foo:first>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*?) (\p{Ll}.*)$', '$1'))"/>
                </foo:first>
                <foo:last>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*?) (\p{Ll}.*)$', '$2'))"/>
                </foo:last>
              </xsl:when>
              <xsl:when test="matches(normalize-space($author), ' \p{Lu}')">
                <foo:first>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*) (\p{Lu}.*)$', '$1'))"/>
                </foo:first>
                <foo:last>
                  <xsl:value-of select="normalize-space(replace($author, '^(.*) (\p{Lu}.*)$', '$2'))"/>
                </foo:last>
              </xsl:when>
              <xsl:otherwise>
                <foo:last>
                  <xsl:value-of select="normalize-space($author)"/>
                </foo:last>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </foo:person>
    </xsl:variable>
    <xsl:sequence select="$result"/>
  </xsl:function>

  <xsl:function name="foo:parse-pages">
    <xsl:param name="pages-raw" as="xs:string"/>
    <xsl:variable name="result">
      <foo:pages>
      <xsl:choose>
        <xsl:when test="contains($pages-raw,'-')">
          <xsl:variable name="tokens" select="tokenize($pages-raw,'(-)|(--)')" />
          <foo:start-page>
            <xsl:value-of select="normalize-space($tokens[1])" />
          </foo:start-page>
          <foo:end-page>
            <xsl:value-of select="normalize-space($tokens[2])" />
          </foo:end-page>
        </xsl:when>
        <xsl:otherwise>
          <foo:start-page>
            <xsl:value-of select="$pages-raw" />
          </foo:start-page>
        </xsl:otherwise>
      </xsl:choose>
      </foo:pages>
    </xsl:variable>
    <xsl:sequence select="$result"/>
  </xsl:function>

</xsl:transform>
