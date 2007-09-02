package de.mospace.xml;

import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/** The first errorhandler my be null, the second may not. */
public final class DraconianErrorHandler implements ResettableErrorHandler{
    private static ResettableErrorHandler instance;

    private DraconianErrorHandler(){
        //sole constructor
    }

    public static synchronized ResettableErrorHandler getInstance(){
        if(instance == null){
            instance = new DraconianErrorHandler();
        }
        return instance;
    }

    public synchronized void fatalError( SAXParseException e ) throws SAXException {
        throw e;
    }

    public synchronized void error( SAXParseException e ) throws SAXException {
        throw e;
    }

    public synchronized void warning( SAXParseException e ) throws SAXException {
        throw e;
    }

    public synchronized void reset(){

    }

}