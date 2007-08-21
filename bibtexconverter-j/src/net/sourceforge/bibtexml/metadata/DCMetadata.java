package net.sourceforge.bibtexml.metadata;
/*
 * $Id$
 *
 * Copyright (c) 2006 Moritz Ringler
 * This class is derived from EntryRetriever by Oskar Ojala
 * (also in this package)
 *
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


/** A simple java bean representation of the Dublin Core Metadata
   Element Set (DCMES). The semiclolon ';' character is interpreted
   as a record separator on XML output for all String fields
   except description.
   @see http://dublincore.org/documents/2006/12/18/dces/
 **/
public class DCMetadata implements Serializable{
    static final String ISO_DATE = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String BIB_NAMESPACE = "http://bibtexml.sf.net/";
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

    /** Copy constructor. Identifier is not copied. **/
    public DCMetadata(DCMetadata meta){
        try{
            BeanInfo info = Introspector.getBeanInfo(DCMetadata.class);
            PropertyDescriptor[] desc = info.getPropertyDescriptors();
            for(PropertyDescriptor pd : desc){
                Object value = pd.getReadMethod().invoke(meta);
                if(value == null){
                    continue;
                }
                Method setter = pd.getWriteMethod();
                if(setter != null){
                    setter.invoke(this, value);
                }
            }
        } catch (Exception ex){
            throw new Error(ex);
        }
    }

    public static DCMetadata load(Preferences node){
        DCMetadata result = new DCMetadata();
        try{
            BeanInfo info = Introspector.getBeanInfo(DCMetadata.class);
            PropertyDescriptor[] desc = info.getPropertyDescriptors();
            for(PropertyDescriptor pd : desc){
                String key = pd.getName();
                Method setter = pd.getWriteMethod();
                if(setter != null){
                    Class c = pd.getPropertyType();
                    Object value = null;
                    if(String.class.equals(c)){
                        value = node.get(key, null);
                    } else if (Date.class.equals(c)){
                        Long ldate = node.getLong(key, Long.MAX_VALUE);
                        if(ldate != Long.MAX_VALUE){
                            value = new Date(ldate);
                        }
                    } else if (Locale.class.equals(c)){
                        value = localeFromString(node.get(key, null));
                    }
                    if(value != null){
                        setter.invoke(result, value);
                    }
                }
            }
        } catch (Exception ex){
            throw new Error(ex);
        }
        return result;
    }

    public void save(Preferences node){
        try{
            BeanInfo info = Introspector.getBeanInfo(DCMetadata.class);
            PropertyDescriptor[] desc = info.getPropertyDescriptors();
            for(PropertyDescriptor pd : desc){
                String key = pd.getName();
                Method getter = pd.getReadMethod();
                if(getter != null){
                    Object value = getter.invoke(this);
                    if(value != null){
                        Class c = pd.getPropertyType();
                        if(String.class.equals(c)){
                            node.put(key, value.toString());
                        } else if (Date.class.equals(c)){
                            node.putLong(key, ((Date) value).getTime());
                        } else if (Locale.class.equals(c)){
                            node.put(key, value.toString());
                        }
                    }
                }
            }
        } catch (Exception ex){
            throw new Error(ex);
        }
    }

    /**
     * Returns the value of creator.
     */
    public String getCreator()
    {
        return creator;
    }

    /**
     * Sets the value of creator.
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
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * Sets the value of format.
     * @param format The value to assign format.
     */
    public void setFormat(String format)
    {
        this.format = normalizeString(format);
    }

    /**
     * Returns the value of title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the value of title.
     * @param title The value to assign title.
     */
    public void setTitle(String title)
    {
        this.title = normalizeString(title);
    }

    /**
     * Returns the value of subject.
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * Sets the value of subject.
     * @param subject The value to assign subject.
     */
    public void setSubject(String subject)
    {
        this.subject = normalizeString(subject);
    }

    /**
     * Returns the value of description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the value of description.
     * @param description The value to assign description.
     */
    public void setDescription(String description)
    {
        this.description = normalizeString(description);
    }

    /**
     * Returns the value of publisher.
     */
    public String getPublisher()
    {
        return publisher;
    }

    /**
     * Sets the value of publisher.
     * @param publisher The value to assign publisher.
     */
    public void setPublisher(String publisher)
    {
        this.publisher = normalizeString(publisher);
    }

    /**
     * Returns the value of contributor.
     */
    public String getContributor()
    {
        return contributor;
    }

    /**
     * Sets the value of contributor.
     * @param contributor The value to assign contributor.
     */
    public void setContributor(String contributor)
    {
        this.contributor = normalizeString(contributor);
    }

    /**
     * Returns the value of date.
     */
    public Date getDate()
    {
        return (date == null)? date : (Date) date.clone();
    }

    /**
     * Sets the value of date.
     * @param date The value to assign date.
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
     * Returns the value of type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the value of type.
     * @param type The value to assign type.
     */
    public void setType(String type)
    {
        this.type = normalizeString(type);
    }

    /**
     * Returns the value of identifier.
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Sets the value of identifier.
     * @param identifier The value to assign identifier.
     */
    public void setIdentifier(String identifier)
    {
        this.identifier = normalizeString(identifier);
    }

    /**
     * Returns the value of source.
     */
    public String getSource()
    {
        return source;
    }

    /**
     * Sets the value of source.
     * @param source The value to assign source.
     */
    public void setSource(String source)
    {
        this.source = normalizeString(source);
    }

    /**
     * Returns the value of language.
     */
    public Locale getLanguage()
    {
        return language;
    }

    /**
     * Sets the value of language.
     * @param language The value to assign language.
     */
    public void setLanguage(Locale language)
    {
        this.language = language;
    }

    /**
     * Returns the value of relation.
     */
    public String getRelation()
    {
        return relation;
    }

    /**
     * Sets the value of relation.
     * @param relation The value to assign relation.
     */
    public void setRelation(String relation)
    {
        this.relation = normalizeString(relation);
    }

    /**
     * Returns the value of coverage.
     */
    public String getCoverage()
    {
        return coverage;
    }

    /**
     * Sets the value of coverage.
     * @param coverage The value to assign coverage.
     */
    public void setCoverage(String coverage)
    {
        this.coverage = normalizeString(coverage);
    }

    /**
     * Returns the value of rights.
     */
    public String getRights()
    {
        return rights;
    }

    /**
     * Sets the value of rights.
     * @param rights The value to assign rights.
     */
    public void setRights(String rights)
    {
        this.rights = normalizeString(rights);
    }

    public Document toXML(){
        Document result = null;
        try{
            BeanInfo info = Introspector.getBeanInfo(DCMetadata.class);
            PropertyDescriptor[] desc = info.getPropertyDescriptors();
            Element root = new Element("metadata", BIB_NAMESPACE);
            root.addNamespaceDeclaration(DC_NAMESPACE);
            for(PropertyDescriptor pd : desc){
                Object value = pd.getReadMethod().invoke(this);
                String name = pd.getName();
                if(value == null || "class".equals(name)){
                    continue;
                }
                Class type = pd.getPropertyType();
                if (String.class.equals(type) && !"description".equals(name)){
                    String[] entries = value.toString().split("\\s*;\\s*");
                    for(String entry : entries){
                        if(entry.length() != 0){
                            Element metanode = new Element(name, DC_NAMESPACE);
                            metanode.setText(entry);
                            root.addContent(metanode);
                        }
                    }
                } else {
                    Element metanode = new Element(name, DC_NAMESPACE);
                    if (Date.class.equals(type)){
                        metanode.setText((new SimpleDateFormat(ISO_DATE)).format((Date) value));
                    } else if(Locale.class.equals(type)){
                        metanode.setText(value.toString().replaceAll("_","-"));
                    } else {
                        metanode.setText(value.toString());
                    }
                    root.addContent(metanode);
                }
            }
            result = new Document(root);
        } catch (Exception ex){
            throw new Error(ex);
        }
        return result;
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
                output(d.getMetadata().toXML(), System.out);
    }

}
