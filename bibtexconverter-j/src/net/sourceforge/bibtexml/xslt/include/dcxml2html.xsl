<?xml version="1.0"?>
<!-- $Id$ -->
<!-- (c) 2007 Moritz Ringler -->
<xsl:transform version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        exclude-result-prefixes="dc">

    <xsl:template match="dc:*">
        <xsl:if test="not(local-name() eq 'date' or local-name() eq 'format')">
        <meta>
            <xsl:attribute name="name">
                <xsl:value-of select="concat('DC.',local-name(.))"/>
            </xsl:attribute>
            <xsl:attribute name="content">
                <xsl:value-of select="normalize-space(text())"/>
            </xsl:attribute>
        </meta>
        </xsl:if>
    </xsl:template>

</xsl:transform>
