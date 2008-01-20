<?xml version="1.0"?>
<!-- $Id$ -->
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
