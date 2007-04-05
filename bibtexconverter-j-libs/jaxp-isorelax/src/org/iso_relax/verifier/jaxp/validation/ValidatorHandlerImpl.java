package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierHandler;
import org.iso_relax.verifier.jaxp.validation.EntityResolverImpl;
import org.iso_relax.verifier.impl.ForkContentHandler;
import org.xml.sax.*;
import org.w3c.dom.ls.LSResourceResolver;

import javax.xml.validation.ValidatorHandler;
import javax.xml.validation.TypeInfoProvider;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class ValidatorHandlerImpl extends ValidatorHandler {
    private final Verifier verifier;
    private final VerifierHandler verifierHandler;
    /** SAX events will be fed to this handler. */
    private ContentHandler receiver;
    /**
     * User-specified handler. Could be null.
     * Kept just so that we can implement {@link #getContentHandler()}.
     */
    private ContentHandler userHandler;
    private ErrorHandler errorHandler;
    private LSResourceResolver resourceResolver;

    ValidatorHandlerImpl(Verifier verifier) throws SAXException {
        this.verifier = verifier;
        this.verifierHandler = verifier.getVerifierHandler();
    }

    public void setContentHandler(ContentHandler userHandler) {
        this.userHandler = userHandler;
        if(userHandler==null)
            receiver = verifierHandler;
        else
            receiver = new ForkContentHandler(verifierHandler,userHandler);
    }

    public ContentHandler getContentHandler() {
        return userHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        verifier.setErrorHandler(errorHandler);
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setResourceResolver(LSResourceResolver resourceResolver) {
        verifier.setEntityResolver(new EntityResolverImpl((resourceResolver)));
        this.resourceResolver = resourceResolver;
    }

    public LSResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public TypeInfoProvider getTypeInfoProvider() {
        // not doable with JARV
        return null;
    }

    public void setDocumentLocator (Locator locator) {
        receiver.setDocumentLocator(locator);
    }

    public void startDocument ()
	throws SAXException {
        receiver.startDocument();
    }

    public void endDocument()
	throws SAXException {
        receiver.endDocument();
    }

    public void startPrefixMapping (String prefix, String uri)
	throws SAXException {
        receiver.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping (String prefix)
	throws SAXException {
        receiver.endPrefixMapping(prefix);
    }

    public void startElement (String uri, String localName,
			      String qName, Attributes atts)
	throws SAXException {
        receiver.startElement(uri, localName, qName, atts);
    }

    public void endElement (String uri, String localName,
			    String qName)
	throws SAXException {
        receiver.endElement(uri, localName, qName);
    }

    public void characters (char ch[], int start, int length)
	throws SAXException {
        receiver.characters(ch, start, length);
    }

    public void ignorableWhitespace (char ch[], int start, int length)
	throws SAXException {
        receiver.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction (String target, String data)
	throws SAXException {
        receiver.processingInstruction(target, data);
    }

    public void skippedEntity (String name)
	throws SAXException {
        receiver.skippedEntity(name);
    }


}
