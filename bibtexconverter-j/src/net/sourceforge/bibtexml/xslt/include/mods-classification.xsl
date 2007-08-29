<?xml version="1.0" encoding="utf-8"?>
<!-- $Id: bibxml2mods32.xsl 338 2007-08-27 17:27:58Z ringler $
     (c) Moritz Ringler, 2007

      XSLT stylesheet that converts bibliographic data from BibXML to MODS v3.2.

      Source:
      http://www.loc.gov/marc/sourcecode/classification/classificationsource.html
-->
<xsl:transform version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bibtex="http://bibtexml.sf.net/"
    xmlns:mods="http://www.loc.gov/mods/v3">

  <xsl:template name="classification">
    <xsl:apply-templates select="bibtex:accs" />
    <xsl:apply-templates select="bibtex:anscr" />
    <xsl:apply-templates select="bibtex:blsrissc" />
    <xsl:apply-templates select="bibtex:cacodoc" />
    <xsl:apply-templates select="bibtex:clutscny" />
    <xsl:apply-templates select="bibtex:cstud" />
    <xsl:apply-templates select="bibtex:ddc" />
    <xsl:apply-templates select="bibtex:fcps" />
    <xsl:apply-templates select="bibtex:inspec" />
    <xsl:apply-templates select="bibtex:kssb" />
    <xsl:apply-templates select="bibtex:laclaw" />
    <xsl:apply-templates select="bibtex:lcc" />
    <xsl:apply-templates select="bibtex:mmlcc" />
    <xsl:apply-templates select="bibtex:moys" />
    <xsl:apply-templates select="bibtex:msc" />
    <xsl:apply-templates select="bibtex:naics" />
    <xsl:apply-templates select="bibtex:nasasscg" />
    <xsl:apply-templates select="bibtex:nicem" />
    <xsl:apply-templates select="bibtex:njb" />
    <xsl:apply-templates select="bibtex:nlm" />
    <xsl:apply-templates select="bibtex:rswk" />
    <xsl:apply-templates select="bibtex:rubbk" />
    <xsl:apply-templates select="bibtex:rubbkd" />
    <xsl:apply-templates select="bibtex:rubbkk" />
    <xsl:apply-templates select="bibtex:rubbkm" />
    <xsl:apply-templates select="bibtex:rubbkmv" />
    <xsl:apply-templates select="bibtex:rubbkn" />
    <xsl:apply-templates select="bibtex:rubbknp" />
    <xsl:apply-templates select="bibtex:rubbko" />
    <xsl:apply-templates select="bibtex:rueskl" />
    <xsl:apply-templates select="bibtex:rugasnti" />
    <xsl:apply-templates select="bibtex:udc" />
    <xsl:apply-templates select="bibtex:usgslcs" />
    <xsl:apply-templates select="bibtex:vsiso" />
    <xsl:apply-templates select="bibtex:ardocs" />
    <xsl:apply-templates select="bibtex:azdocs" />
    <xsl:apply-templates select="bibtex:cacodoc" />
    <xsl:apply-templates select="bibtex:cadocs" />
    <xsl:apply-templates select="bibtex:codocs" />
    <xsl:apply-templates select="bibtex:fldocs" />
    <xsl:apply-templates select="bibtex:gadocs" />
    <xsl:apply-templates select="bibtex:iadocs" />
    <xsl:apply-templates select="bibtex:ksdocs" />
    <xsl:apply-templates select="bibtex:ladocs" />
    <xsl:apply-templates select="bibtex:midocs" />
    <xsl:apply-templates select="bibtex:modocs" />
    <xsl:apply-templates select="bibtex:msdocs" />
    <xsl:apply-templates select="bibtex:naics" />
    <xsl:apply-templates select="bibtex:nbdocs" />
    <xsl:apply-templates select="bibtex:ncdocs" />
    <xsl:apply-templates select="bibtex:nmdocs" />
    <xsl:apply-templates select="bibtex:nvdocs" />
    <xsl:apply-templates select="bibtex:nydocs" />
    <xsl:apply-templates select="bibtex:ohdocs" />
    <xsl:apply-templates select="bibtex:okdocs" />
    <xsl:apply-templates select="bibtex:ordocs" />
    <xsl:apply-templates select="bibtex:padocs" />
    <xsl:apply-templates select="bibtex:pssppbkj" />
    <xsl:apply-templates select="bibtex:ridocs" />
    <xsl:apply-templates select="bibtex:scdocs" />
    <xsl:apply-templates select="bibtex:sddocs" />
    <xsl:apply-templates select="bibtex:sudocs" />
    <xsl:apply-templates select="bibtex:swank" />
    <xsl:apply-templates select="bibtex:txdocs" />
    <xsl:apply-templates select="bibtex:undocs" />
    <xsl:apply-templates select="bibtex:upsylon" />
    <xsl:apply-templates select="bibtex:utdocs" />
    <xsl:apply-templates select="bibtex:wadocs" />
    <xsl:apply-templates select="bibtex:widocs" />
    <xsl:apply-templates select="bibtex:wydocs" />
  </xsl:template>

  <xsl:template match="bibtex:accs">
    <xsl:comment>Annehurst curriculum classification system. (West Lafayette, IN: Kappa Delta
    Pi)</xsl:comment>
    <mods:classification authority="accs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:anscr">
    <xsl:comment>The Alpha-numeric system for classification of recordings (Williamsport,
    PA: Bro-Dart)</xsl:comment>
    <mods:classification authority="anscr">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:blsrissc">
    <xsl:comment>British Library - Science reference information service subject classification
    (London: British Library)</xsl:comment>
    <mods:classification authority="blsrissc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:cacodoc">
    <xsl:comment>CODOC [Canadian federal and provincial government documents classification
    scheme] (Toronto: Office of Library Coordination, Council of Ontario Universities)</xsl:comment>
    <mods:classification authority="cacodoc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:clutscny">
    <xsl:comment>Classification of the Library of Union Theological Seminary in the City
    of New York (New York : Union Theological Seminary)</xsl:comment>
    <mods:classification authority="clutscny">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:cstud">
    <xsl:comment>Classificatieschema's Bibliotheek TU Delft (Delft : Technische Universiteit
    Delft, Bibliotheek)</xsl:comment>
    <mods:classification authority="cstud">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ddc">
    <xsl:comment>Dewey decimal classification and relative index (Albany, NY : Forest Press)
  </xsl:comment>
    <mods:classification authority="ddc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:fcps">
    <xsl:comment>Class FC: a classification for Canadian history <i>and</i> Class PS8000:
    a classification for Canadian literature. (Ottawa: National Library of Canada)</xsl:comment>
    <mods:classification authority="fcps">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:inspec">
    <xsl:comment> INSPEC classification (Edison, NJ : INSPEC Inc.)</xsl:comment>
    <mods:classification authority="inspec">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:kssb">
    <xsl:comment> Klassifikationssystem for svenska bibliotek (Lund : Bibliotekstjanst) </xsl:comment>
    <mods:classification authority="kssb">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:laclaw">
    <xsl:comment>Los Angeles County Law Library, class K-Law. (Los Angeles: County Law Library)</xsl:comment>
    <mods:classification authority="laclaw">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:lcc">
    <xsl:comment>Library of Congress classification (Washington : CDS) </xsl:comment>
    <mods:classification authority="lcc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:mmlcc">
    <xsl:comment>Manual of map library classification and cataloguing (London: Ministry of
    Defence)</xsl:comment>
    <mods:classification authority="mmlcc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:moys">
    <xsl:comment>Moys, Elizabeth M. Moys classification and thesaurus for legal materials.
    (London: Bowker-Saur)</xsl:comment>
    <mods:classification authority="moys">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:msc">
    <xsl:comment>Mathematical subject classification (Providence, RI: American Mathematical
    Society)</xsl:comment>
    <mods:classification authority="msc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:naics">
    <xsl:comment>North American industry classification system (Wash. DC: OMB, GPO)</xsl:comment>
    <mods:classification authority="naics">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:nasasscg">
    <xsl:comment> NASA scope and subject category guide (Hanover, MD : NASA, Scientific and
    Technical Information Program)</xsl:comment>
    <mods:classification authority="nasasscg">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:nicem">
    <xsl:comment>NICEM subject headings and classification system (Albuquerque, NM: National
    Information Center for Educational Media)</xsl:comment>
    <mods:classification authority="nicem">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:njb">
    <xsl:comment>Nihon jisshin bunruihō = Nippon decimal classification (Tōkyō
    : Nihon Toshokan Kyōkai)</xsl:comment>
    <mods:classification authority="njb">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:nlm">
    <xsl:comment>National Library of Medicine classification. (Bethesda, MD: NLM)</xsl:comment>
    <mods:classification authority="nlm">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:rswk">
    <xsl:comment>Regeln für den Schlagwortkatalog (Berlin: Deutsches Bibliotheksinstitut)</xsl:comment>
    <mods:classification authority="rswk">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rubbk">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    nauchnykh bibliotek v 30-ti tomakh (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbk">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rubbkd">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    detskikh bibliotek v 1 t. (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbkd">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rubbkk">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    kraevedcheskikh katalogov bibliotek (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbkk">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rubbkm">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    massovykh bibliotek v 1. t. (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbkm">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rubbkmv">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    massovykh voennykh bibliotek (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbkmv">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rubbkn">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    nauchnykh bibliotek v 4-kh tomakh (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbkn">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:comment>
      Pereizdaniíà tablits bibliotechno-bibliograficheskoi klassifikatsii
      dlíà nauchnykh bibliotek v 30-ti tomakh (Moskva: Kniga)
  </xsl:comment>
  <mods:classification authority="rubbknp">
    <xsl:value-of select="bibtex:rubbknp"/>
  </mods:classification>

  <xsl:template match="bibtex:rubbko">
    <xsl:comment>Tablitsy bibliotechno-bibliograficheskoi klassifikatsii dlíà
    oblastvykh bibliotek v 4-kh tomakh (Moskva: Kniga)</xsl:comment>
    <mods:classification authority="rubbko">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rueskl">
    <xsl:comment>Edinaíà skhema klassifikatsii literatury dlíà
    knigoizdaniíà v SSSR. (Moskva: "Kniga")</xsl:comment>
    <mods:classification authority="rueskl">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:rugasnti">
    <xsl:comment>Rubrikator Gosudarstvennoi avtomatizirovannoi sistemy nauchno-tekhnicheskoi
    informatsii. (Moskva: Vsesoí&#249;znyi institut nauchnoi i tekhnicheskoi
    informatsii)</xsl:comment>
    <mods:classification authority="rugasnti">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:udc">
    <xsl:comment>Universal decimal classification (London: British Standards Institute)</xsl:comment>
    <mods:classification authority="udc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:usgslcs">
    <xsl:comment> U.S. Geological Survey library classification (Reston, VA : U.S. Geological
    Survey Library)</xsl:comment>
    <mods:classification authority="usgslcs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:vsiso">
    <xsl:comment>Vlaamse SISO [schema voor de indeling van de systematische catalogus in
    openbare bibliotheken] (Antwerpen: VLABIN-VBC)</xsl:comment>
    <mods:classification authority="vsiso">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ardocs">
    <xsl:comment>Arkansas state documents classification scheme. (Little Rock: Arkansas State
    Library, Documents Services Section, State Publications Unit)</xsl:comment>
    <mods:classification authority="ardocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:azdocs">
    <xsl:comment>Arizona documents: KWOC manual. (Phoenix: State Documents Center, Arizona
    Dept. of Library, Archives and Public Records)</xsl:comment>
    <mods:classification authority="azdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:cacodoc">
    <xsl:comment>CODOC [Canadian federal and provincial government documents classification
    scheme] (Toronto: Office of Library Coordination, Council of Ontario Universities)</xsl:comment>
    <mods:classification authority="cacodoc">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:cadocs">
    <xsl:comment>California state agency classification scheme. (Sacramento: Government Publications
    Section, California State Library) </xsl:comment>
    <mods:classification authority="cadocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:codocs">
    <xsl:comment>Colorado State Publications Depository and Distribution Center. Classification
    schedule. (Denver: Colorado State Library)</xsl:comment>
    <mods:classification authority="codocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:fldocs">
    <xsl:comment>A Keyword-in-context to Florida public documents in the Florida Atlantic
    University Library (Tallahassee: Department of State, State Library)</xsl:comment>
    <mods:classification authority="fldocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:gadocs">
    <xsl:comment>Official publications of the State of Georgia: list of classes with index
    by name of agency and subject/keyword (Athens, GA: University of Georgia Libraries,
    Government Documents Department)</xsl:comment>
    <mods:classification authority="gadocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:iadocs">
    <xsl:comment>Classification of Iowa state documents (Des Moines: State Library of Iowa)</xsl:comment>
    <mods:classification authority="iadocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:ksdocs">
    <xsl:comment>State documents of Kansas: list of classes (Topeka: State Library of Kansas)</xsl:comment>
    <mods:classification authority="ksdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ladocs">
    <xsl:comment>Louisiana documents classification schedule (Baton Rouge: Louisiana State
    Library)</xsl:comment>
    <mods:classification authority="ladocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:midocs">
    <xsl:comment>Michigan documents classification scheme (Lansing: Michigan Dept. of Education,
    State Library Services)</xsl:comment>
    <mods:classification authority="midocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:modocs">
    <xsl:comment>Missouri state documents classification: post-reorganization agency codes
    and form divisions. (Jefferson City: Missouri State Library)</xsl:comment>
    <mods:classification authority="modocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:msdocs">
    <xsl:comment>Mississippi state government publications. Vol. 1 (July 1975/June 1980)-
    (Jackson: Mississippi Library Commission)</xsl:comment>
    <mods:classification authority="msdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:naics">
    <xsl:comment>North American industry classification system (Wash. DC: OMB, GPO)</xsl:comment>
    <mods:classification authority="naics">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:nbdocs">
    <xsl:comment>Guide to Nebraska state agencies: state publications classification and
    ordering directory. (Lincoln: Nebraska Publications Clearinghouse, Nebraska
    Library Commission)</xsl:comment>
    <mods:classification authority="nbdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ncdocs">
    <xsl:comment>Classification scheme for North Carolina state publications: as applied
    to the documents collection of the N.C. Dept. of Cultural Resources. (Raleigh:
    The State Library)</xsl:comment>
    <mods:classification authority="ncdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:nmdocs">
    <xsl:comment>The New Mexico state documents classification system. (Santa Fe: New Mexico
    State Library)</xsl:comment>
    <mods:classification authority="nmdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:nvdocs">
    <xsl:comment>Nevada state documents. (Carson City: Nevada State Library and Archives)</xsl:comment>
    <mods:classification authority="nvdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:nydocs">
    <xsl:comment>New York state documents: an introductory manual. (Albany: New York State
    Library)</xsl:comment>
    <mods:classification authority="nydocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ohdocs">
    <xsl:comment>Ohio documents classification scheme. (Columbus: State Library of Ohio)</xsl:comment>
    <mods:classification authority="ohdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:okdocs">
    <xsl:comment>Oklahoma state documents classification and list of Oklahoma state agencies
    from statehood to the present. (Oklahoma City: Oklahoma Dept. of Libraries)</xsl:comment>
    <mods:classification authority="okdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ordocs">
    <xsl:comment>OrDocs: history authority list and classification scheme for Oregon state
    agencies. (Salem: Oregon State Library)</xsl:comment>
    <mods:classification authority="ordocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:padocs">
    <xsl:comment>Classification scheme for Pennsylvania state publications. (Harrisburg:
    State Library of Pennsylvania)</xsl:comment>
    <mods:classification authority="padocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:pssppbkj">
    <xsl:comment>Popis strucnih skupina i svih podskupina s podacima o broju kataloskih jedinica
    (Zagreb: Nacionalna i Sveucilisna Biblioteka)</xsl:comment>
    <mods:classification authority="pssppbkj">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:ridocs">
    <xsl:comment>Alphabetical list of state agencies and corresponding Swank classification.
    (Providence: Rhode Island State Library)</xsl:comment>
    <mods:classification authority="ridocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:scdocs">
    <xsl:comment>South Carolina state documents classification system. (Columbia: State Library)</xsl:comment>
    <mods:classification authority="scdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:sddocs">
    <xsl:comment>[South Dakota] State documents classification schedule (Pierre: South Dakota
    State Library)</xsl:comment>
    <mods:classification authority="sddocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:sudocs">
    <xsl:comment>Superintendent of Documents classification system.</xsl:comment>
    <mods:classification authority="sudocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:swank">
    <xsl:comment>Swank, Raynard Coe. A classification for state, county, and municipal documents
    (Boulder, CO: University of Colorado Library)</xsl:comment>
    <mods:classification authority="swank">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:txdocs">
    <xsl:comment>Texas state documents classification &amp; almost compleat [sic] list of Texas
    state agencies from statehood to the present. (Austin: Legislative Reference
    Library &amp; Government Publications Library, Texas State Library)</xsl:comment>
    <mods:classification authority="txdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>
  <xsl:template match="bibtex:undocs">
    <xsl:comment>United Nations document series symbols: 1946-77 cumulative. (New York: UN)</xsl:comment>
    <mods:classification authority="undocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:upsylon">
    <xsl:comment>UPSYLON: classification systématique de la Bibliothèque de
    la Faculté de psychologie et des sciences de l'éducation de
    l'Université catholique de Louvain (Louvain-la-Neuve, Belgique: La
    Faculté)</xsl:comment>
    <mods:classification authority="upsylon">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:utdocs">
    <xsl:comment>Utah documents classification schedules. (Salt Lake City: Utah State Library
    Division)</xsl:comment>
    <mods:classification authority="utdocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:wadocs">
    <xsl:comment>Washington State Library state documents collection: State documents call
    number (Olympia : State Library) </xsl:comment>
    <mods:classification authority="wadocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:widocs">
    <xsl:comment>Organizing Wisconsin public documents: cataloging and classification of
    documents at the State Historical Society Library. (Madison: Division for
    Library Services, Bureau for Reference and Loan Services)</xsl:comment>
    <mods:classification authority="widocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

  <xsl:template match="bibtex:wydocs">
    <xsl:comment>WyDocs: the Wyoming state documents classification system. (Cheyenne, WY:
    Department of Administration and Information, Wyoming State Library)</xsl:comment>
    <mods:classification authority="wydocs">
      <xsl:apply-templates />
    </mods:classification>
  </xsl:template>

</xsl:transform>
