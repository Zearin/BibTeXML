package net.sourceforge.bibtexml.metadata;

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
                 String[] parts = value.split("[-_]");
                 Locale loc = null;
                 switch(parts.length){
                     case 1: loc = new Locale(val); break;
                     case 2: loc = new Locale(parts[0], parts[1]); break;
                     default: loc = new Locale(parts[0], parts[1], parts[2]);
                 }
                 if(loc != null){
                     result.setLanguage(loc);
                 }
             } else {
                 try{
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

