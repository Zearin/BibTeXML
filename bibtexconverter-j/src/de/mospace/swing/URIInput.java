package de.mospace.swing;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2006 Moritz Ringler
* $Id$
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

import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;

/**
* @author Moritz Ringler
* @version $Revision$ ($Date$)
*/
public class URIInput extends PathInput{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8937439528031134966L;

	public URIInput(String url){
        super(url);
    }

    public URIInput(String url, int fileSelectionMode){
        super(url, fileSelectionMode);
    }

    public URI getURI() throws URISyntaxException{
        String path = getTextfield().getText();
        File testfile = new File(path);
        URI result = testfile.isAbsolute()
                ? testfile.toURI()
                : new URI(path);
        getTextfield().setText(result.toString());
        return result;
    }

    public String getPath(){
        String result = getTextfield().getText();
        try{
            result = (new File(new URI(result))).toString();
        } catch (Exception ignore){
        }
        return result;
    }

    protected void browse(){
        URI oldURI = null;
        try{
            oldURI = getURI();
        } catch (URISyntaxException ignore){
        }
        super.browse();
        try{
            getURI();
        } catch (URISyntaxException ex){
            if(oldURI == null){
                getTextfield().setText("./");
            } else {
                getTextfield().setText(oldURI.toString());
            }
        }
    }
}