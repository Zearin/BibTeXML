package org.iso_relax.verifier.jaxp.validation;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

/**
 * Sample main class for a test drive
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class Main {
    public static void main(String[] args) throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(args[0]);
        System.out.println("SchemaFactory is "+sf.getClass().getName());
        Schema s = sf.newSchema(new File(args[1]));
        System.out.println("Schema is "+s.getClass().getName());

        s.newValidator().validate(new StreamSource(args[2]));
    }
}
