package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;

import javax.xml.XMLConstants;

/**
 * @author Moritz Ringler
 */
public class MSVSchemaFactoryImpl extends RELAXNGSchemaFactoryImpl {
    
    public MSVSchemaFactoryImpl(ClassLoader cl) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super((VerifierFactory)
            Class.forName("com.sun.msv.verifier.jarv.RELAXNGFactoryImpl", 
                true,
                (cl == null)? MSVSchemaFactoryImpl.class.getClassLoader() : cl).newInstance());
    }
    
    public MSVSchemaFactoryImpl() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        this(null);
    }

    protected String getLanguageName() {
        return XMLConstants.RELAXNG_NS_URI;
    }
}
