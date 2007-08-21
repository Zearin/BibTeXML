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
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Constructs DCMetadata objects from SAX parse events from XML trees
such as those created by the DCMetadata.toXML() methods. **/
public class DCMetadataHandler extends DefaultHandler{
     private DCMetadata result = new DCMetadata();
     private static final Map<String, PropertyDescriptor> props = new TreeMap<String, PropertyDescriptor>();
     static{
         BeanInfo info = null;
         try{
             info = Introspector.getBeanInfo(DCMetadata.class);
         } catch (Exception ex){
             throw new Error(ex);
         }
         PropertyDescriptor[] desc = info.getPropertyDescriptors();
         for(PropertyDescriptor pd : desc){
             props.put(pd.getName(), pd);
         }
     }
     private transient String element;
     private transient StringBuilder contents;

     public DCMetadataHandler(){
         //sole constructor
     }

     public void startDocument(){
         result = new DCMetadata();
     }

     private void setResultProperty(String name, String val) throws ParseException{
         if(val == null || !props.containsKey(name)){
             //silently ignore null values and unknown metadata
             return;
         }
         String value = val.trim();
         if(value.length() != 0){
             if("date".equals(name)){
                 Date date = null;
                 try{
                     date = (new SimpleDateFormat(DCMetadata.ISO_DATE)).parse(value);
                 } catch (ParseException ex){
                     //go on trying
                 }
                 if(date == null){
                     try{
                         date = DateFormat.getDateTimeInstance().parse(value);
                     } catch (ParseException ex){
                         //go on trying
                     }
                 }
                if(date == null){
                    //last try
                     date = DateFormat.getDateInstance().parse(value);
                 }
                 result.setDate(date);
             } else if ("language".equals(name)){
                 Locale loc = DCMetadata.localeFromString(value);
                 if(loc != null){
                     result.setLanguage(loc);
                 }
             } else {
                 try{
                     if(!"description".equals(name)){
                         Method getter = props.get(name).getReadMethod();
                         Object oldValue = getter.invoke(result);
                         if(oldValue != null){
                             value = oldValue.toString() + "; " + value.toString();
                         }
                     }
                     Method setter = props.get(name).getWriteMethod();
                     setter.invoke(result, value);
                 } catch (Exception ex){
                     throw new Error(ex);
                 }
             }
         }
     }

     public void startElement(String uri, String localname, String rawname,
     Attributes atts){
         if(DCMetadata.DC_NAMESPACE.equals(uri)){
             element = localname;
         }
     }

     public void characters(char[] ch, int start, int length){
         if(element != null){
             if(contents == null){
                 contents = new StringBuilder();
             }
             contents.append(ch, start, length);
         }
     }

     public void endElement(String uri, String localname, String rawname) throws SAXException{
         if(DCMetadata.DC_NAMESPACE.equals(uri) && localname.equals(element)){
             try{
                 setResultProperty(localname, contents.toString());
             } catch (ParseException ex){
                 throw new SAXException(ex);
             }
             contents = null;
             element = null;
         }
     }

     public DCMetadata getParseResult(){
         return result;
     }
 }

