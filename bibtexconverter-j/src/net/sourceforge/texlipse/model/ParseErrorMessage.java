/*
 * $Id$
 *
 * Copyright (c) 2004-2005 Oskar Ojala
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sourceforge.texlipse.model;

/**
 * A document parse error message.
 *
 * @author Oskar Ojala
 */
public class ParseErrorMessage {

    private int line;
    private int pos;
    private int length;
    private String msg;
    private int severity;

    /**
     * Constructs a new error message.
     *
     * @param line The line the error occurs on
     * @param pos The position of the error on that line
     * @param length The length of the erroneous input (from <code>pos</code> onwards)
     * @param msg The error message
     * @param severity The severity of the error (as defined in <code>IMarker</code>)
     */
    public ParseErrorMessage(int line, int pos, int length, String msg, int severity) {
        this.line = line;
        this.pos = pos;
        this.length = length;
        this.msg = msg;
        this.severity = severity;
    }

    /**
     * @return Returns the length.
     */
    public int getLength() {
        return length;
    }
    /**
     * @return Returns the line.
     */
    public int getLine() {
        return line;
    }
    /**
     * @return Returns the msg.
     */
    public String getMsg() {
        return msg;
    }
    /**
     * @return Returns the pos.
     */
    public int getPos() {
        return pos;
    }

    /**
     * @return Returns the severity.
     */
    public int getSeverity() {
        return severity;
    }

    public String toString(){
        StringBuilder svalue = new StringBuilder(1024);
        svalue.append("Parse error at line ").append(getLine()).append(": ");
        svalue.append(getMsg()).append("\n");
        svalue.append("Position: ").append(pos).append(", length: ").append(getLength());
        return svalue.toString();
    }
}
