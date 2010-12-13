/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2006 Moritz Ringler
 * $Id$
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
 * Contains definitions of language-dependent GUI features. Locale: de
 *
 * @version $Revision$ ($Date$)
 * @author Moritz Ringler
 */
public final class DefaultResourceBundle_de extends ListResourceBundle {

    static final Object[][] contents = {
        {"Find/Replace...", "Suchen/Ersetzen..."},
        {"Find...", "Suchen..."},
        {"Replace", "Ersetzen"},
        {"Find", "Suchen"},
        {"whole word", "Ganzes Wort"},
        {"W (whole word mnemonic)", "W"},
        {"case sensitve", "Gro\u00df/klein"},
        {"S (case sensitive mnemonic)", "G"},
        {"regular expression", "Reg. Ausdruck"},
        {"E (regular expression mnemonic)", "E"},
        {"F (Find/Replace... mnemonic)", "R"},
        {"F (Find... mnemonic)", "S"},
        {"Replace all", "Alle ersetzen"},
        {"Select all", "Ausw\u00e4hlen"},
        {"A (replace all mnemonic)", "A"},
        {"A (select all mnemonic)", "A"},
        {"C (cancel mnemonic)", "C"},
        {"L (Look for mnemonic)", "H"},
        {"Look for","Suchen nach"},
        {"Replace with", "Ersetzen durch"},
        {"I (with mnemonic)", "Z"},
        {"Interrupt", "Unterbrechen"},
        {"Clear", "Zur\u00fccksetzen"},
        {"System shell", "System Shell"},
        {"Working directory is ", "Das aktuelle Arbeitsverzeichnis ist "},
        {"Sort", "Sortieren"},
        {"Randomize", "Zuf\u00e4llige Reihenfolge"},
        {"Dock", "Andocken"},
        {"Float", "Abkoppeln"},
        {"Console", "Konsole"},
        {"Close", "Schlie\u00dfen"},

        {"EXT_INST_QUERY_FILE_PROMPT",
            "Kann in die aktuellen Java-Erweiterungs-Verzeichnisse nicht schreiben.\n" +
            "Bitte geben Sie ein Verzeichnis an, in das Sie schreiben k\u00f6nnen,\n"+
            "und starten Sie dieses Programm mit\n" +
            "java -Djava.ext.dirs={0}DIR [...]\n"},
        {"EXT_INST_QUERY_FILE_TITLE",
            "Keine Schreibberechtigung f\u00fcr Erweiterungs-Verzeichnisse"},
        {"EXT_INST_WRITE_ERROR_PROMPT",
            "<html>Kann in die Datei {0} im Verzeichnis {1} nicht schreiben.<br>" +
            "Bitte geben Sie einen anderen Namen an.</html>"},
        {"EXT_INST_WRITE_ERROR_TITLE",
            "Keine Schreibberechtigung"},
        {"EXT_INST_OVERWRITE_PROMPT_A",
            "Ein anderes {0} ist bereits installiert. \u00dcberschreiben?"},
        {"EXT_INST_OVERWRITE_PROMPT_B",
            "Eine Datei namens {0} ist bereits installiert. \u00dcberschreiben?"},
        {"EXT_INST_OVERWRITE_TITLE",
            "\u00dcberschreiben?"},

        {"LAF_INST_CANNOT_INSTALL_WARNING",
            "<html>Sie k\u00f6nnen keine neuen Look & Feels installieren,<br>"+
            "da Sie die Datei<br>{0}<br>"+
            "nicht \u00e4ndern d\u00fcrfen.</html>"},
        {"LAF_INST_CANNOT_INSTALL_TITLE",
            "Kann swing.properties nicht \u00e4ndern"},
        {"LAF_INST_PACKAGE_PROMPT",
            "Look and Feel Paket (.jar oder .zip), siehe z. B. http://javootoo.com. "
            + "Stellen Sie sicher, dass der zu installierende Look and Feel f\u00fcr  die Java"
            + "Version {0} geeignet ist. "
            + "Der Versuch, einen neuen Look and Feel zu installieren, kann die Anwendung zum Absturz bringen. "
        + "Daher beenden Sie bitte vorher Ihre Arbeit."},
        {"LAF_INST_PACKAGE_TITLE",
            "W\u00e4hlen Sie ein L&F Paket"},
        {"LAF_INST_INVALID_PACKAGE",
            "\"{0}\" existiert nicht oder ist keine gew\u00f6hnliche Datei."},
        {"LAF_INST_INVALID_FILE_TITLE",
            "Ung\u00fcltige Datei"},
        {"LAF_INST_NO_ZIP",
            "{0} ist keine Zip- oder Jar-Datei."},
        {"LAF_INST_NO_LAF",
            "Keine Look And Feels in Datei {0} gefunden."},
        {"LAF_INST_NO_LAF_TITLE",
            "Keine L&Fs gefunden "},
        {"LAF_INST_PICK_LAF_PROMPT",
            "W\u00e4hlen Sie eine der gefundenen L&F-Klassen:"},
        {"LAF_INST_PICK_LAF_TITLE",
            "L&F w\u00e4hlen"},
        {"LAF_INST_LAF_ALREADY_INSTALLED",
            "Look And Feel \"{0} ({1})\" ist bereits installiert."},
        {"LAF_INST_LAF_ALREADY_INSTALLED_TITLE",
            "L&F bereits installiert"},

        {"LAF_MENU_INST_NEW",
            "Neuen installieren..."}
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
