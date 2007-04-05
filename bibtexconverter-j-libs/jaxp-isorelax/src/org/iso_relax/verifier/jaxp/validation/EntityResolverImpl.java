package org.iso_relax.verifier.jaxp.validation;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSInput;

import java.io.IOException;

/**
 * {@link EntityResolver} that wraps the {@link LSResourceResolver}.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class EntityResolverImpl implements EntityResolver {
    private LSResourceResolver resolver;

    public EntityResolverImpl() {}

    public EntityResolverImpl(LSResourceResolver resolver) {
        setResolver(resolver);
    }

    public LSResourceResolver getResolver() {
        return resolver;
    }

    public void setResolver(LSResourceResolver resolver) {
        this.resolver = resolver;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if(resolver==null)
            return null;

        LSInput in = resolver.resolveResource("http://www.w3.org/TR/REC-xml",null,publicId,systemId,null);
        if(in==null)    return null;

        InputSource is = new InputSource();
        is.setByteStream(in.getByteStream());
        is.setCharacterStream(in.getCharacterStream());
        is.setEncoding(in.getEncoding());
        is.setPublicId(in.getPublicId());
        is.setSystemId(in.getSystemId());

        return is;
    }
}
