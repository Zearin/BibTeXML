package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.jaxp.validation.EntityResolverImpl;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
abstract class SchemaFactoryImpl extends SchemaFactory {

    private final VerifierFactory core;
    private ErrorHandler errorHandler;
    private LSResourceResolver resourceResolver;

    SchemaFactoryImpl() throws VerifierConfigurationException {
        this.core = VerifierFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
    }
    
    SchemaFactoryImpl(VerifierFactory jarvFactory){
        this.core = jarvFactory;
    }

    protected abstract String getLanguageName();

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setResourceResolver(LSResourceResolver resourceResolver) {
        core.setEntityResolver(new EntityResolverImpl(resourceResolver));
        this.resourceResolver = resourceResolver;
    }

    public LSResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public Schema newSchema(Source[] schemas) throws SAXException {
        if(schemas.length!=1)
            throw new UnsupportedOperationException();

        try {
            return new SchemaImpl(compileSchema(schemas[0]));
        } catch (IOException e) {
            throw reportError(e);
        } catch (VerifierConfigurationException e) {
            throw reportError(e);
        } catch (TransformerException e) {
            throw reportError(e);
        }
    }

    private SAXException reportError(Exception e) throws SAXException {
        SAXParseException spe = new SAXParseException(e.getMessage(),null,null,-1,-1,e);
        if(errorHandler!=null)
            errorHandler.error(spe);
        return spe;
    }

    private org.iso_relax.verifier.Schema compileSchema(Source s) throws IOException, VerifierConfigurationException, SAXException, TransformerException {
        if( s instanceof StreamSource ) {
            StreamSource ss = (StreamSource)s;
            InputSource is = new InputSource();
            is.setByteStream(ss.getInputStream());
            is.setCharacterStream(ss.getReader());
            is.setSystemId(ss.getSystemId());
            is.setPublicId(ss.getPublicId());
            return core.compileSchema(is);
        }
        // we can't directly parse SAX and DOM into JARV,
        // so turn it into stream first.
        StringWriter w = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(s,new StreamResult(w));
        return compileSchema(new StreamSource(new StringReader(w.toString())));
    }

    public Schema newSchema() throws SAXException {
        throw new UnsupportedOperationException();
    }

    public boolean isSchemaLanguageSupported(String schemaLanguage) {
        return schemaLanguage.equals(getLanguageName());
    }
}
