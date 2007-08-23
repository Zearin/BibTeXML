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

import java.util.Locale;

/**

 **/
public class LocaleUtils{
    private static LocaleUtils instance;

    private LocaleUtils(){
    }

    public static synchronized LocaleUtils getInstance(){
        if(instance == null){
            instance = new LocaleUtils();
        }
        return instance;
    }

    public Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException("Singleton");
    }

    public Locale localeFromString(String value){
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
}
