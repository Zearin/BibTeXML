/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2006 Moritz Ringler
 * $Id: DefaultResourceBundle.java,v 1.3 2007/01/17 21:05:59 ringler Exp $
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
package de.mospace.swing;

import java.util.ListResourceBundle;

/**
 * Contains definitions of language-dependent GUI features. Default locale: en
 *
 * @version $Revision: 1.3 $ ($Date: 2007/01/17 21:05:59 $)
 * @author Moritz Ringler
 */
public final class DefaultResourceBundle extends ListResourceBundle {

    static final Object[][] contents = {
        {"Find/Replace...", "Find/Replace..."},
        {"Find...", "Find..."},
        {"Replace", "Replace"},
        {"Find", "Find"},
        {"whole word", "whole word"},
        {"W (whole word mnemonic)", "W"},
        {"case sensitve", "case sensitve"},
        {"S (case sensitive mnemonic)", "S"},
        {"regular expression", "regular expression"},
        {"E (regular expression mnemonic)", "E"},
        {"F (Find/Replace... mnemonic)", "F"},
        {"F (Find... mnemonic)", "F"},
        {"Replace all", "Replace all"},
        {"Select all", "Select all"},
        {"A (replace all mnemonic)", "A"},
        {"A (select all mnemonic)", "A"},
        {"C (cancel mnemonic)", "C"},
        {"Look for","Look for"},
        {"L (Look for mnemonic)", "L"},
        {"Replace with", "Replace with"},
        {"I (with mnemonic)", "I"},
        {"Interrupt", "Interrupt"},
        {"Clear", "Clear"},
        {"System shell", "System shell"},
        {"Working directory is ", "Working directory is "},
        {"Sort", "Sort"},
        {"Randomize", "Randomize"},
        {"Dock", "Dock"},
        {"Float", "Float"},
        {"Console", "Console"},
        {"Close", "Close"},

        {"EXT_INST_QUERY_FILE_PROMPT",
            "Cannot write to current Java extension directories.\n" +
            "Please specify a directory you can write to and start "+
            "your application with\n" +
            "java -Djava.ext.dirs={0}DIR [...]\n"},
        {"EXT_INST_QUERY_FILE_TITLE",
            "Extension directories not writable"},
        {"EXT_INST_WRITE_ERROR_PROMPT",
            "<html>Cannot write to file {0} in directory {1}.<br>" +
            "Please specify another name.</html>"},
        {"EXT_INST_WRITE_ERROR_TITLE",
            "No write permission"},
        {"EXT_INST_OVERWRITE_PROMPT_A",
            "A different {0} is already installed. Overwrite?"},
        {"EXT_INST_OVERWRITE_PROMPT_B",
            "A file named {0} is already installed. Overwrite?"},
        {"EXT_INST_OVERWRITE_TITLE",
            "Overwrite?"},

        {"LAF_INST_CANNOT_INSTALL_WARNING",
            "<html>You cannot install new Look & Feels<br>"+
            "because you do not have write permission for<br>{0}</html>"},
        {"LAF_INST_CANNOT_INSTALL_TITLE",
            "Cannot write swing.properties"},
        {"LAF_INST_PACKAGE_PROMPT",
            "Look and Feel package (.jar or .zip), see e.g. http://javootoo.com."+
        "Please use only Look and Feels that are built to work with java version {0}." +
        "Trying to install a new look and feel might crash the application so make sure you do not have any unfinished work."},
        {"LAF_INST_PACKAGE_TITLE",
            "Pick L&f package"},
        {"LAF_INST_INVALID_PACKAGE",
            "\"{0}\" does not exist or is not a normal file."},
        {"LAF_INST_INVALID_FILE_TITLE",
            "Invalid file"},
        {"LAF_INST_NO_ZIP",
            "{0} is not a zip or jar file."},
        {"LAF_INST_NO_LAF",
            "No Look And Feel found in file {0}."},
        {"LAF_INST_NO_LAF_TITLE",
            "No L&F found"},
        {"LAF_INST_PICK_LAF_PROMPT",
            "Pick one of the LaF classes found:"},
        {"LAF_INST_PICK_LAF_TITLE",
            "Pick LaF"},
        {"LAF_INST_LAF_ALREADY_INSTALLED",
            "Look And Feel \"{0} ({1})\" is already installed."},
        {"LAF_INST_LAF_ALREADY_INSTALLED_TITLE",
            "L&F already installed"},

        {"LAF_MENU_INST_NEW",
            "Install new..."}
   };

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object[][] getContents() {
        return contents;
    }
}
