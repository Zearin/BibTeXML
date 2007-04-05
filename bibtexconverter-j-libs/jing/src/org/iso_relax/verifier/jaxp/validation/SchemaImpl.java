package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.VerifierConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class SchemaImpl extends Schema {
    private final org.iso_relax.verifier.Schema core;

    SchemaImpl(org.iso_relax.verifier.Schema core) throws VerifierConfigurationException {
        this.core = core;

        // checks if this works fine.
        // JARV shouldn't have let an exception to be thrown from the newVerifier method.
        core.newVerifier();
    }

    public Validator newValidator() {
        Validator result = null;
        try {
            result = new ValidatorImpl(core.newVerifier());
            //patched. assure that we have a DraconianErrorHandler as detailed in the spec
            result.setErrorHandler(new com.thaiopensource.xml.sax.DraconianErrorHandler());
        } catch (VerifierConfigurationException e) {
            // impossible
            throw new InternalError(e.getMessage());
        }
        return result;
    }

    public ValidatorHandler newValidatorHandler() {
        try {
            return new ValidatorHandlerImpl(core.newVerifier());
        } catch (SAXException e) {
            // impossible
            throw new InternalError(e.getMessage());
        } catch (VerifierConfigurationException e) {
            // impossible
            throw new InternalError(e.getMessage());
        }
    }
}
