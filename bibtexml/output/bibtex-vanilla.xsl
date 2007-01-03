<?xml version="1.0"?>
<!--
  - File: $Id: bibtexml2bibtex.xsl,v 1.1 2002/01/12 13:10:14 kuhlmann Exp $
  - 
  - Convert plain vanilla BibTeXML to BibTeX
  - Copyright (C) 2001, 2002 Marco Kuhlmann <mk@mcqm.net>
  - 
  - This program is free software; you can redistribute it and/or
  - modify it under the terms of the GNU General Public License
  - as published by the Free Software Foundation; either version 2
  - of the License, or (at your option) any later version.
  - 
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  - GNU General Public License for more details.
  - 
  - You should have received a copy of the GNU General Public License
  - along with this program; if not, write to the Free Software
  - Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  - 
 -->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bibtex="http://bibtexml.sf.net/">
  <xsl:output method="text"
	      media-type="application/x-bibtex" />
  <xsl:include href="include/extended.xsl"/>

  <xsl:template match="bibtex:file">
    <xsl:call-template name="bibtexml-latex-warning"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="bibtex:entry">
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="bibtex:article">@Article{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:book">@Book{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:booklet">@Booklet{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:conference">@Conference{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:inbook">@InBook{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:incollection">@InCollection{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:inproceedings">@InProceedings{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:manual">@Manual{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:mastersthesis">@MastersThesis{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:misc">@Misc{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:phdthesis">@PhdThesis{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:proceedings">@Proceedings{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:techreport">@TechReport{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>
  <xsl:template match="bibtex:unpublished">@Unpublished{<xsl:value-of select="../@id"/><xsl:apply-templates/>}
</xsl:template>

  <xsl:template match="bibtex:address">,
  address =         {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:annote">,
  annote =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:author">,
  author =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:booktitle">,
  booktitle =       {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:chapter">,
  chapter =         {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:crossref">,
  crossref =        {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:edition">,
  edition =         {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:editor">,
  editor =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:howpublished">,
  howpublished =    {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:institution">,
  institution =     {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:journal">,
  journal =         {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:key">,
  key =             {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:month">,
  month =           {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:note">,
  note =            {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:number">,
  number =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:organization">,
  organization =    {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:pages">,
  pages =           {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:publisher">,
  publisher =       {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:school">,
  school =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:series">,
  series =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:title">,
  title =           {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:type">,
  type =            {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:volume">,
  volume =          {<xsl:apply-templates/>}</xsl:template>
  <xsl:template match="bibtex:year">,
  year =            {<xsl:apply-templates/>}</xsl:template>

  <xsl:template match="*" />

</xsl:stylesheet>
