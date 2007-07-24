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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;
import java.io.IOException;
import net.sourceforge.texlipse.model.ParseErrorMessage;

/** Default implementation of a universal error handler. The error and
* fatalError methods will always throw an exception. warning and reset
* do nothing. The wrap methods allows to dress ErrorHandlers
* and BibTeXErrorHandlers as UniversalErrorHandlers by providing them
* with this default behaviour.
*/
public class UniversalErrorHandlerAdapter implements UniversalErrorHandler{
    //BibTeXErrorHandler
    public void error(ParseErrorMessage e) throws IOException {
        throw new IOException(e.getMsg());
    }

    //ErrorHandler
    public void fatalError( SAXParseException e ) throws SAXException {
        throw new SAXException(e);
    }

    public void error( SAXParseException e ) throws SAXException {
        throw new SAXException(e);
    }

    public void warning( SAXParseException e ) throws SAXException {
        //do nothing
    }

    //ErrorListener
    public void fatalError( TransformerException e ) throws TransformerException {
        throw new TransformerException(e);
    }

    public void error( TransformerException e ) throws TransformerException {
        throw new TransformerException(e);
    }

    public void warning( TransformerException e ) throws TransformerException {
        //do nothing
    }

    public void reset(){
        //do nothing
    }

    public static UniversalErrorHandler wrap(ErrorHandler handler){
        return new WrappedErrorHandler(handler);
    }

    public static UniversalErrorHandler wrap(BibTeXErrorHandler handler){
        return new WrappedBibTeXErrorHandler(handler);
    }

    public static UniversalErrorHandler wrap(ErrorListener handler){
        return new WrappedErrorListener(handler);
    }

    private static class WrappedErrorListener extends UniversalErrorHandlerAdapter{
        private final ErrorListener eh;

        public WrappedErrorListener(ErrorListener handler){
            eh = handler;
        }

        @Override
        public void fatalError( TransformerException e ) throws TransformerException {
            eh.fatalError(e);
        }

        @Override
        public void error( TransformerException e ) throws TransformerException {
            eh.error(e);
        }

        @Override
        public void warning( TransformerException e ) throws TransformerException {
            eh.warning(e);
        }
    }

    private static class WrappedErrorHandler extends UniversalErrorHandlerAdapter{
        private final ErrorHandler reh;

        public WrappedErrorHandler(ErrorHandler handler){
            reh = handler;
        }

        @Override
        public void fatalError( SAXParseException e ) throws SAXException {
            reh.fatalError(e);
        }

        @Override
        public void error( SAXParseException e ) throws SAXException {
            reh.error(e);
        }

        @Override
        public void warning( SAXParseException e ) throws SAXException {
            reh.warning(e);
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