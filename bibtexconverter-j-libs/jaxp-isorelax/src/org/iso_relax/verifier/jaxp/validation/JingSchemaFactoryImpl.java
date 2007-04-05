package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;

import javax.xml.XMLConstants;

/**
 * @author Moritz Ringler
 */
public class JingSchemaFactoryImpl extends RELAXNGSchemaFactoryImpl {
    
    public JingSchemaFactoryImpl(ClassLoader cl) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super((VerifierFactory)
            Class.forName("com.thaiopensource.relaxng.jarv.VerifierFactoryImpl", 
                true,
                (cl == null)? JingSchemaFactoryImpl.class.getClassLoader() : cl).newInstance());
    }
    
    public JingSchemaFactoryImpl() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        this(null);
    }

    protected String getLanguageName() {
        return XMLConstants.RELAXNG_NS_URI;
    }
}
