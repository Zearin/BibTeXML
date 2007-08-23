package net.sourceforge.bibtexml.util;
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
import java.util.Date;
import java.util.Locale;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;
import org.jdom.Element;

/**

 **/
public class BeanUtils{
    private static BeanUtils instance;

    private BeanUtils(){
    }

    public static synchronized BeanUtils getInstance(){
        if(instance == null){
            instance = new BeanUtils();
        }
        return instance;
    }

    public Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException("Singleton");
    }

    /** Creates a new bean with the same public properties
        as the argument. **/
    public <T> T copyBean(Object bean, Class<T> clazz) throws java.beans.IntrospectionException{
        T result = null;
        try{
        result = clazz.newInstance();
        BeanInfo info = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] desc = info.getPropertyDescriptors();
        for(PropertyDescriptor pd : desc){
            Method getter = pd.getReadMethod();
            if(getter == null){
                continue;
            }
            Object value = getter.invoke(bean);
            if(value == null){
                continue;
            }
            Method setter = pd.getWriteMethod();
            if(setter != null){
                setter.invoke(result, value);
            }
        }
        } catch (IntrospectionException ex){
            throw ex;
        } catch (Exception ex){
            IntrospectionException ex2 = new IntrospectionException(ex.getMessage());
            ex2.initCause(ex);
            throw ex2;
        }
        return result;
    }

    /** Loads a bean saved using {@link #save}. **/
    public <T> T load(Preferences node, Class<T> clazz) throws java.beans.IntrospectionException{
        T result = null;
        try{
        result = clazz.newInstance();
        BeanInfo info = Introspector.getBeanInfo(clazz);
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
                    value = LocaleUtils.getInstance().localeFromString(node.get(key, null));
                }
                if(value != null){
                    setter.invoke(result, value);
                }
            }
        }
        } catch (IntrospectionException ex){
            throw ex;
        } catch (Exception ex){
            IntrospectionException ex2 = new IntrospectionException(ex.getMessage());
            ex2.initCause(ex);
            throw ex2;
        }
        return result;
    }

    public void save(Preferences node, Object bean) throws java.beans.IntrospectionException{
        BeanInfo info = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] desc = info.getPropertyDescriptors();
        for(PropertyDescriptor pd : desc){
            String key = pd.getName();
            Method getter = pd.getReadMethod();
            if(getter != null){
                Object value = null;
                try{
                    value = getter.invoke(this);
                } catch (Exception ex) {
                    //ignore silently
                }
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
    }

    public static interface JDOMPropertyHandler{
        public Element[] toElements(Class type, String name, Object value);
    }

    public Element toXML(JDOMPropertyHandler handler, Element container, Object bean){
        try{
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] desc = info.getPropertyDescriptors();
            for(PropertyDescriptor pd : desc){
                Method getter = pd.getReadMethod();
                if(getter == null){
                    continue;
                }
                Object value = getter.invoke(bean);
                String name = pd.getName();
                if(value == null || "class".equals(name)){
                    continue;
                }
                Class type = pd.getPropertyType();
                for(Element e : handler.toElements(type, name, value)){
                    container.addContent(e);
                }
            }
        } catch (Exception ex){
            throw new Error(ex);
        }
        return container;
    }

}
