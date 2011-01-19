package de.mospace.xml;

import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/** The first errorhandler my be null, the second may not. */
public class JointErrorHandler implements ResettableErrorHandler{
    private ResettableErrorHandler first;
    private ResettableErrorHandler second;

    public JointErrorHandler(ResettableErrorHandler first, ResettableErrorHandler second){
        this.first = first;
        this.second = second;
    }

    @Override
    public synchronized void fatalError( SAXParseException e ) throws SAXException {
        if(first != null){
            first.fatalError(e);
        }
        second.fatalError(e);
    }

    @Override
    public synchronized void error( SAXParseException e ) throws SAXException {
        if(first != null){
            first.error(e);
        }
        second.error(e);
    }

    @Override
    public synchronized void warning( SAXParseException e ) throws SAXException {
        if(first != null){
            first.error(e);
        }
        second.error(e);
    }

    @Override
    public synchronized void reset(){
        if(first != null){
            first.reset();
        }
        second.reset();
    }

    public void setFirst(ResettableErrorHandler eh){
        first = eh;
    }

    public void setSecond(ResettableErrorHandler eh){
        second = eh;
    }

    public ResettableErrorHandler getFirst(){
        return first;
    }

    public ResettableErrorHandler getSecond(){
        return second;
    }
}