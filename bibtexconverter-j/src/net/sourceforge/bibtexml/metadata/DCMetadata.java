package net.sourceforge.bibtexml.metadata;
/*
* $Id$
*
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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import net.sourceforge.bibtexml.util.BeanUtils;
import net.sourceforge.bibtexml.util.BeanUtils.JDOMPropertyHandler;


/** A simple java bean representation of the Dublin Core Metadata
Element Set (DCMES). The semiclolon ';' character is interpreted
as a record separator on XML output for all String fields
except description.
@see <a href="http://dublincore.org/documents/2006/12/18/dces/">Dublin Core Dublin Core Metadata Element Set, Version 1.1</a>
**/
public class DCMetadata implements Serializable{
    static final String ISO_DATE = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";
    public static final Namespace DC_NAMESPACE = Namespace.getNamespace("dc", DC_NAMESPACE_URI);

    private String creator;
    private String format;
    private String title;
    private String subject;
    private String description;
    private String publisher;
    private String contributor;
    private Date date;
    private String type;
    private String identifier;
    private String source;
    private Locale language;
    private String relation;
    private String coverage;
    private String rights;

    public DCMetadata(){
    }

    /** Returns a new DCMetadata instance with the same public properties
        as this object. **/
    public DCMetadata createCopy(){
        try{
            return BeanUtils.getInstance().copyBean(this, getClass());
        } catch (Exception ex){
            throw new Error(ex);
        }
    }

    /** Creates a new DCMetada object and initializes its state from the
        specified preferences node.
        @see #save
        **/
    public static DCMetadata load(Preferences node){
        try{
            return BeanUtils.getInstance().load(node, DCMetadata.class);
        } catch (Exception ex){
            throw new Error(ex);
        }
    }

    /** Saves the state of this DCMetada object to the
        specified preferences node.
        @see #load
        **/
    public void save(Preferences node){
        try{
            BeanUtils.getInstance().save(node, this);
        } catch (Exception ex){
            throw new Error(ex);
        }
    }

    /**
    * Returns the value of creator.
    * URI  http://purl.org/dc/elements/1.1/creator
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#creator">Dublin Core Dublin Core Metadata Element Set, Version 1.1: creator</a>
    */
    public String getCreator()
    {
        return creator;
    }

    /**
    * Sets the value of creator.
    * URI  http://purl.org/dc/elements/1.1/creator
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#creator">Dublin Core Dublin Core Metadata Element Set, Version 1.1: creator</a>
    * @param creator The value to assign creator.
    */
    public void setCreator(String creator)
    {
        this.creator = normalizeString(creator);
    }

    private static String normalizeString(String s){
        String result = null;
        if(s != null){
            result = s.trim();
            if(result.length() == 0){
                result = null;
            } else {
                result = result.replaceAll("\\s+", " ");
            }
        }
        return result;
    }

    /**
    * Returns the value of format.
    * URI  http://purl.org/dc/elements/1.1/format
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#format">Dublin Core Dublin Core Metadata Element Set, Version 1.1: format</a>
    */
    public String getFormat()
    {
        return format;
    }

    /**
    * Sets the value of format.
    * URI  http://purl.org/dc/elements/1.1/format
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#format">Dublin Core Dublin Core Metadata Element Set, Version 1.1: format</a>
    * @param format The value to assign format.
    */
    public void setFormat(String format)
    {
        this.format = normalizeString(format);
    }

    /**
    * Returns the value of title.
    * URI  http://purl.org/dc/elements/1.1/title
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#title">Dublin Core Dublin Core Metadata Element Set, Version 1.1: title</a>
    */
    public String getTitle()
    {
        return title;
    }

    /**
    * Sets the value of title.
    * URI  http://purl.org/dc/elements/1.1/title
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#title">Dublin Core Dublin Core Metadata Element Set, Version 1.1: title</a>
    * @param title The value to assign title.
    */
    public void setTitle(String title)
    {
        this.title = normalizeString(title);
    }

    /**
    * Returns the value of subject.
    * URI  http://purl.org/dc/elements/1.1/subject
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#subject">Dublin Core Dublin Core Metadata Element Set, Version 1.1: subject</a>
    */
    public String getSubject()
    {
        return subject;
    }

    /**
    * Sets the value of subject.
    * URI  http://purl.org/dc/elements/1.1/subject
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#subject">Dublin Core Dublin Core Metadata Element Set, Version 1.1: subject</a>
    * @param subject The value to assign subject.
    */
    public void setSubject(String subject)
    {
        this.subject = normalizeString(subject);
    }

    /**
    * Returns the value of description.
    * URI  http://purl.org/dc/elements/1.1/description
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#description">Dublin Core Dublin Core Metadata Element Set, Version 1.1: description</a>
    */
    public String getDescription()
    {
        return description;
    }

    /**
    * Sets the value of description.
    * URI  http://purl.org/dc/elements/1.1/description
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#description">Dublin Core Dublin Core Metadata Element Set, Version 1.1: description</a>
    * @param description The value to assign description.
    */
    public void setDescription(String description)
    {
        this.description = normalizeString(description);
    }

    /**
    * Returns the value of publisher.
    * URI  http://purl.org/dc/elements/1.1/publisher
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#publisher">Dublin Core Dublin Core Metadata Element Set, Version 1.1: publisher</a>
    */
    public String getPublisher()
    {
        return publisher;
    }

    /**
    * Sets the value of publisher.
    * URI  http://purl.org/dc/elements/1.1/publisher
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#publisher">Dublin Core Dublin Core Metadata Element Set, Version 1.1: publisher</a>
    * @param publisher The value to assign publisher.
    */
    public void setPublisher(String publisher)
    {
        this.publisher = normalizeString(publisher);
    }

    /**
    * Returns the value of contributor.
    * URI  http://purl.org/dc/elements/1.1/contributor
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#contributor">Dublin Core Dublin Core Metadata Element Set, Version 1.1: contributor</a>
    */
    public String getContributor()
    {
        return contributor;
    }

    /**
    * Sets the value of contributor.
    * URI  http://purl.org/dc/elements/1.1/contributor
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#contributor">Dublin Core Dublin Core Metadata Element Set, Version 1.1: contributor</a>
    * @param contributor The value to assign contributor.
    */
    public void setContributor(String contributor)
    {
        this.contributor = normalizeString(contributor);
    }

    /**
    * Returns the value of date.
    * URI  http://purl.org/dc/elements/1.1/date
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#date">Dublin Core Dublin Core Metadata Element Set, Version 1.1: date</a>
    */
    public Date getDate()
    {
        return (date == null)? date : (Date) date.clone();
    }

    /**
    * Sets the value of date.
    * URI  http://purl.org/dc/elements/1.1/date
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#date">Dublin Core Dublin Core Metadata Element Set, Version 1.1: date</a>
    * @param date The value to assign date.
    */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
    * Returns the value of type.
    * URI  http://purl.org/dc/elements/1.1/type
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#type">Dublin Core Dublin Core Metadata Element Set, Version 1.1: type</a>
    */
    public String getType()
    {
        return type;
    }

    /**
    * Sets the value of type.
    * URI  http://purl.org/dc/elements/1.1/type
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#type">Dublin Core Dublin Core Metadata Element Set, Version 1.1: type</a>
    * @param type The value to assign type.
    */
    public void setType(String type)
    {
        this.type = normalizeString(type);
    }

    /**
    * Returns the value of identifier.
    * URI  http://purl.org/dc/elements/1.1/identifier
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#identifier">Dublin Core Dublin Core Metadata Element Set, Version 1.1: identifier</a>
    */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
    * Sets the value of identifier.
    * URI  http://purl.org/dc/elements/1.1/identifier
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#identifier">Dublin Core Dublin Core Metadata Element Set, Version 1.1: identifier</a>
    * @param identifier The value to assign identifier.
    */
    public void setIdentifier(String identifier)
    {
        this.identifier = normalizeString(identifier);
    }

    /**
    * Returns the value of source.
    * URI  http://purl.org/dc/elements/1.1/source
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#source">Dublin Core Dublin Core Metadata Element Set, Version 1.1: source</a>
    */
    public String getSource()
    {
        return source;
    }

    /**
    * Sets the value of source.
    * URI  http://purl.org/dc/elements/1.1/source
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#source">Dublin Core Dublin Core Metadata Element Set, Version 1.1: source</a>
    * @param source The value to assign source.
    */
    public void setSource(String source)
    {
        this.source = normalizeString(source);
    }

    /**
    * Returns the value of language.
    * URI  http://purl.org/dc/elements/1.1/language
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#language">Dublin Core Dublin Core Metadata Element Set, Version 1.1: language</a>
    */
    public Locale getLanguage()
    {
        return language;
    }

    /**
    * Sets the value of language.
    * URI  http://purl.org/dc/elements/1.1/language
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#language">Dublin Core Dublin Core Metadata Element Set, Version 1.1: language</a>
    * @param language The value to assign language.
    */
    public void setLanguage(Locale language)
    {
        this.language = language;
    }

    /**
    * Returns the value of relation.
    * URI  http://purl.org/dc/elements/1.1/relation
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#relation">Dublin Core Dublin Core Metadata Element Set, Version 1.1: relation</a>
    */
    public String getRelation()
    {
        return relation;
    }

    /**
    * Sets the value of relation.
    * URI  http://purl.org/dc/elements/1.1/relation
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#relation">Dublin Core Dublin Core Metadata Element Set, Version 1.1: relation</a>
    * @param relation The value to assign relation.
    */
    public void setRelation(String relation)
    {
        this.relation = normalizeString(relation);
    }

    /**
    * Returns the value of coverage.
    * URI  http://purl.org/dc/elements/1.1/coverage
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#coverage">Dublin Core Dublin Core Metadata Element Set, Version 1.1: coverage</a>
    */
    public String getCoverage()
    {
        return coverage;
    }

    /**
    * Sets the value of coverage.
    * URI  http://purl.org/dc/elements/1.1/coverage
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#coverage">Dublin Core Dublin Core Metadata Element Set, Version 1.1: coverage</a>
    * @param coverage The value to assign coverage.
    */
    public void setCoverage(String coverage)
    {
        this.coverage = normalizeString(coverage);
    }

    /**
    * Returns the value of rights.
    * URI  http://purl.org/dc/elements/1.1/rights
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#rights">Dublin Core Dublin Core Metadata Element Set, Version 1.1: rights</a>
    */
    public String getRights()
    {
        return rights;
    }

    /**
    * Returns the value of rights.
    * URI  http://purl.org/dc/elements/1.1/rights
    * @see <a href="http://dublincore.org/documents/2006/12/18/dces/#rights">Dublin Core Dublin Core Metadata Element Set, Version 1.1: rights</a>
    * @param rights The value to assign rights.
    */
    public void setRights(String rights)
    {
        this.rights = normalizeString(rights);
    }

    /** Adds an XML representation of this metadata object to the
        specified JDOM Element.
        @see <a href="http://dublincore.org/documents/2003/04/02/dc-xml-guidelines/">Guidelines for implementing Dublin Core in XML</a>
    **/
    public Element toXML(Element container){
        container.addNamespaceDeclaration(DC_NAMESPACE);
        return BeanUtils.getInstance().toXML(jdomPropertyHandler(), container, this);
    }

    private static JDOMPropertyHandler myPropertyHandler;
    protected synchronized JDOMPropertyHandler jdomPropertyHandler(){
        if(myPropertyHandler == null){
            myPropertyHandler = new JDOMPropertyHandler(){
                public Element[] toElements(Class type, String name, Object value){
                    Element[] result = null;
                    if (String.class.equals(type) && !"description".equals(name)){
                        result = toElements(name, value.toString());
                    } else {
                        Element metanode = new Element(name, DC_NAMESPACE);
                        if (Date.class.equals(type)){
                            metanode.setText((new SimpleDateFormat(ISO_DATE)).format((Date) value));
                        } else if(Locale.class.equals(type)){
                            metanode.setText(value.toString().replaceAll("_","-"));
                        } else if(String.class.equals(type)){
                            metanode.setText(value.toString());
                        }
                        result = new Element[]{metanode};
                    }
                    return result;
                }

                private Element[] toElements(String name, String value){
                    List<Element> result = new ArrayList<Element>();
                    String[] entries = value.split("\\s*;\\s*");
                    for(String entry : entries){
                        if(entry.length() != 0){
                            Element metanode = new Element(name, DC_NAMESPACE);
                            metanode.setText(entry);
                            result.add(metanode);
                        }
                    }
                    return result.toArray(new Element[result.size()]);
                }
            };
        }
        return myPropertyHandler;
    }

    public static DCMetadata fromXML(InputSource in, EntityResolver resolver) throws IOException, SAXException{
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        DCMetadataHandler metahandler = new DCMetadataHandler();
        reader.setContentHandler(metahandler);
        if(resolver != null){
            reader.setEntityResolver(resolver);
        }
        reader.parse(in);
        return metahandler.getParseResult();
    }

    static Locale localeFromString(String value){
        if(value == null || value.length() == 0){
            return null;
        }
        String[] parts = value.split("[-_]");
        Locale loc = null;
        switch(parts.length){
        case 1: loc = new Locale(value); break;
        case 2: loc = new Locale(parts[0], parts[1]); break;
            default: loc = new Locale(parts[0], parts[1], parts[2]);
        }
        return loc;
    }

    public static void main(String[] args) throws Exception{
        DCMetadataDialog d = new DCMetadataDialog(new DCMetadata(), null, "Test");
        d.pack();
        d.setModal(true);
        d.setVisible(true);
        (new XMLOutputter(Format.getPrettyFormat())).
        output(d.getMetadata().toXML(new Element("metadata")), System.out);
    }

}
