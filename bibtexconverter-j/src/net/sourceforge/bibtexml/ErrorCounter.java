package net.sourceforge.bibtexml;
/*
 * $Id$
 * (c) Moritz Ringler, 2006

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
 
import de.mospace.xml.ResettableErrorHandler;
import org.xml.sax.SAXParseException;
import net.sourceforge.texlipse.model.ParseErrorMessage;
class ErrorCounter implements UniversalErrorHandler{
    private int count = 0;
    
    public ErrorCounter(){
    }
    
    public void fatalError( SAXParseException e ){
        count++;
    }
    
    public void error( SAXParseException ex ){
        count++;
    }
    
    public void warning( SAXParseException e ){
        count++;
    }
    
    public void error(ParseErrorMessage e){
        count++;
    }
    
    public boolean hasError(){
        return count > 0;
    }
    
    public int getErrorCount(){
        return count;
    }
    
    public void reset(){
        count = 0;
    }
}
