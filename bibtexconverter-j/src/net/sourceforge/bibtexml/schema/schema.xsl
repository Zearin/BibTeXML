<!-- $Id$ -->
<!-- This stylesheet allows to derived schemas with specific
     features from bibtexml-generic.rng --> 
<!-- Adapted from http://osteele.com/archives/2004/08/xml-schema-versions -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:m="http://bibtexml.sourceforge.net/version-annotations"
                exclude-result-prefixes="m"
                version="2.0">

  <!-- stylesheet invoker should specifiy this -->
  <!-- one of container, flat -->
  <xsl:param name="structure" select="'flat'"/>
  <!-- one of strict, loose -->
  <xsl:param name="datatypes" select="'strict'"/>
  <!-- one of core, user, arbitrary -->
  <xsl:param name="fields" select="'user'" />

  <!-- copy the document, except for elements that match
       another template -->
  <xsl:template name="copy" match="/|@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- if it has a fields attribute, only copy it if
       the fields matches -->
  <xsl:template match="*[@m:fields]">
    <xsl:if test="$fields=tokenize(@m:fields,' ')">
      <xsl:call-template name="copy"/>
    </xsl:if>
  </xsl:template>
  
  <!-- if it has a structure attribute, only copy it if
       the structure matches -->
  <xsl:template match="*[@m:structure]">
    <xsl:if test="@m:structure=$structure">
      <xsl:call-template name="copy"/>
    </xsl:if>
  </xsl:template>
  
  <!-- if it has a datatypes attribute, only copy it if
       the datatypes matches -->
  <xsl:template match="*[@m:datatypes]">
    <xsl:if test="@m:datatypes=$datatypes">
      <xsl:call-template name="copy"/>
    </xsl:if>
  </xsl:template>

  <!-- remove version annotations -->
  <xsl:template match="@m:*"/>
</xsl:stylesheet>
