package net.sourceforge.bibtexml.metadata;
/*
 * $Id: DCMetadata.java 293 2007-08-12 23:41:32Z ringler $
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/** A DCMetadata view. **/
public class DCMetadataController implements ActionListener {
    private DCMetadata model;
    private DCMetadataUI view;
    private final static Map<String, PropertyDescriptor> simplePDs = new HashMap<String, PropertyDescriptor>();
    static{
        try{
        BeanInfo info = Introspector.getBeanInfo(DCMetadata.class);
        for(PropertyDescriptor desc : info.getPropertyDescriptors()){
            if(!(desc instanceof IndexedPropertyDescriptor)){
                simplePDs.put(desc.getName(), desc);
            }
        }
        } catch (Exception ex){
            throw new Error(ex);
        }
    }

    public DCMetadataController(){
    }

    public void setModel(DCMetadata model){
        this.model = model;
        updateView();
    }

    public DCMetadata getModel(){
        return model;
    }

    public void setView(DCMetadataUI view){
        if(this.view != null){
            this.view.removeActionListener(this);
        }
        this.view = view;
        view.addActionListener(this);
        updateView();
    }

    public void updateView(){
        for(String prop : simplePDs.keySet()){
            updateView(prop);
        }
    }

    private void updateView(String property){
        try{
        if(model != null && view != null && property != null){
            PropertyDescriptor pd = simplePDs.get(property);
            if(pd != null){
                Method getter = pd.getReadMethod();
                Object value = getter.invoke(model);
                view.setValue(property, value);
            }
        }
        } catch (Exception ex){
            System.err.println(ex);
            System.err.flush();
        }
    }

    public void updateModel(){
        for(String prop : simplePDs.keySet()){
            updateModel(prop);
        }
    }

    private void updateModel(String property){
        try{
        if(model != null && view != null && property != null){
            Object value = view.getValue(property);
            if(value != null){
                PropertyDescriptor pd = simplePDs.get(property);
                if(pd != null){
                    Method setter = pd.getWriteMethod();
                    setter.invoke(model, value);
                }
            }
        }
        } catch (Exception ex){
            System.err.println(ex);
            System.err.flush();
        }
    }

    public void actionPerformed(ActionEvent e){
        try{
            updateModel(e.getActionCommand());
        } catch (Exception ex){
            System.err.println(ex);
            System.err.flush();
        }
    }
}
