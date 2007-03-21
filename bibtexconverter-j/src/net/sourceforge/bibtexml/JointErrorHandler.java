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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.IOException;
import net.sourceforge.texlipse.model.ParseErrorMessage;
 
/** Allows to combine two error handlers. Each of the error
* handling methods delegates to both the methods of the first and the
* second handler (in this order).
*/
public class JointErrorHandler implements UniversalErrorHandler{
    private UniversalErrorHandler first;
    private UniversalErrorHandler second;
    
    public JointErrorHandler(UniversalErrorHandler first, UniversalErrorHandler second){
        this.first = first;
        this.second = second;
    }
        
    public void fatalError( SAXParseException e ) throws SAXException {
        if(first != null){
            first.fatalError(e);
        }
        second.fatalError(e);
    }
    
    public void error( SAXParseException e ) throws SAXException {
        if(first != null){
            first.error(e);
        }
        second.error(e);
    }
    
    public void warning( SAXParseException e ) throws SAXException {
        if(first != null){
            first.warning(e);
        }
        second.warning(e);
    }
    
    public void error(ParseErrorMessage e) throws IOException {
        if(first != null){
            first.error(e);
        }
        second.error(e);
    }
    
    public void reset(){
        if(first != null){
            first.reset();
        }
        second.reset();
    }
    
    public void setFirst(UniversalErrorHandler eh){
        first = eh;
    }
    
    public void setSecond(UniversalErrorHandler eh){
        first = eh;
    }
    
    public UniversalErrorHandler getFirst(){
        return first;
    }
    
    public UniversalErrorHandler getSecond(){
        return second;
    }
}