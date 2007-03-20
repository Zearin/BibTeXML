package net.sourceforge.bibtexml;
/*
 * $Id: TeXLipseParser.java 131 2007-03-19 17:34:53Z ringler $
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

public class UniversalErrorHandlerAdapter implements UniversalErrorHandler{
    private UniversalErrorHandler first;
    
    public void error(ParseErrorMessage e) throws IOException {
        throw new IOException(e.getMsg());
    }
    
    public void fatalError( SAXParseException e ) throws SAXException {
        throw new SAXException(e);
    }
        
    public void error( SAXParseException e ) throws SAXException {
        throw new SAXException(e);
    }
        
    public void warning( SAXParseException e ) throws SAXException {
    }
    
    public void reset(){
    }
    
    public static UniversalErrorHandler wrap(ResettableErrorHandler handler){
        return new WrappedResettableErrorHandler(handler);
    }
    
    public static UniversalErrorHandler wrap(BibTeXErrorHandler handler){
        return new WrappedBibTeXErrorHandler(handler);
    }
    
    private static class WrappedResettableErrorHandler extends UniversalErrorHandlerAdapter{
        private final ResettableErrorHandler reh;
        
        public WrappedResettableErrorHandler(ResettableErrorHandler handler){
            reh = handler;
        }
        
        @Override
        public void fatalError( SAXParseException e ) throws SAXException {
            reh.fatalError(e);
        }
        
        @Override
        public void error( SAXParseException e ) throws SAXException {
            reh.fatalError(e);
        }
        
        @Override
        public void warning( SAXParseException e ) throws SAXException {
            reh.warning(e);
        }
        
        @Override
        public void reset(){
            reh.reset();
        }
    }
    
    private static class WrappedBibTeXErrorHandler extends UniversalErrorHandlerAdapter{
        private final BibTeXErrorHandler beh;
        
        public WrappedBibTeXErrorHandler(BibTeXErrorHandler handler){
            beh = handler;
        }
        
        @Override
        public void error(ParseErrorMessage e) throws IOException {
            beh.error(e);
        }
        
        @Override
        public void reset(){
            beh.reset();
        }
    }
    
}