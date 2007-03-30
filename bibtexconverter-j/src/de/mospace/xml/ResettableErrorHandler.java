package de.mospace.xml;

import org.xml.sax.ErrorHandler;

public interface ResettableErrorHandler extends ErrorHandler{
    public void reset();
}