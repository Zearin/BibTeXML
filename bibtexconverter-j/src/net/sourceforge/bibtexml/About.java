package net.sourceforge.bibtexml;

/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
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

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * A Dialog that shows author and license information, opened
 * with the Help->About menu command.
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 */
final class About extends JDialog implements ActionListener {

    public About(JFrame parent, ImageIcon logo, String saxonVersion) {
        super(parent);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        init(logo, saxonVersion);
        pack();
    }

    /**Component initialization*/
    private void init(final ImageIcon logo, final String saxonVersion) {
        final Container cp = getContentPane();
        setTitle("About BibTeXConverter");
        setResizable(false);

        String version = "";
        try{
            final Properties p = new Properties();
            p.load(getClass().getResourceAsStream("version.properties"));
            version = p.getProperty("version") + " (build " + p.getProperty("build") + ")";
        } catch (Exception ex){
            ex.printStackTrace();
        }

        final JLabel image = new JLabel(logo);
        final JLabel text = new JLabel(
            "<html>" +
            "<b>BibTeXConverter " + version + "</b><br>" +

            /*
            "<a href=\"http://mp3dings.sourceforge.net\">" +
            "http://mp3dings.sourceforge.net</a><br><br>" +
            */

            "&copy; 2006-2007, Moritz Ringler<br><br>" +
            "<b>BibTeX-Parser:</b><br>" +
            "<u>texlipse.sf.net</u><br>&copy; Oskar Ojala 2004-2005<br><br>" +
            "<b>RELAX NG validator:</b><br>" +
            "<u>jarv</u><br>&copy; SF ISO-RELAX Project 2001-2002<br>" +
            "<u>jarv-jaxp bridge</u><br>&copy; K. Kawaguchi 2006, M.Ringler 2007<br>" +
            "<u>jing</u><br>&copy; Thai Open Source 2001-2003, M. Ringler 2007<br>" +
            "<u>msv</u><br>&copy; Sun Microsystems, Inc. 2001-2007<br>" +
            "<br>" +
            /*"<u>bibtexml.sf.net</u><br>bibtex2xml.py: &copy; Vidar Bronken Gundersen, Sara Sprenkle<br>" +
            "Java port: &copy; Moritz Ringler, 2006<br><br>" +*/
            "<b>BibTeXML schemas</b><br>" +
            "&copy; 2003-2007, V. B. Gundersen, Z. W. Hendrikse, M. Kuhlmann, M. Ringler" +
            "<br><br>" +
            "<b>XSLT stylesheets</b><br>" +
            "&copy; 2003-2007, V. B. Gundersen, Z. W. Hendrikse, M. Ringler" +
            "<br><br>" +
            ((saxonVersion == null)? "" : ("<b>XSLT-Processor:</b><br>"  + saxonVersion + "<br><br>"))
            + "Running on <br>" +
            "Java " + System.getProperty("java.version")+
            " (" + System.getProperty("java.vendor") + ")<br></html>"
        );
        text.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 10));

        final JTextArea license = new JTextArea(
        "This program is free software; you can redistribute it and/or\n"+
        "modify it under the terms of the GNU General Public License\n"+
        "as published by the Free Software Foundation; either version 2\n"+
        "of the License, or (at your option) any later version.\n"+
        "\n"+
        "This program is distributed in the hope that it will be useful,\n"+
        "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
        "GNU General Public License for more details.\n"+
        "\n"+
        "You should have received a copy of the GNU General Public License\n"+
        "along with this program; if not, write to the Free Software\n"+
        "Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA\n"+
        "\n" );
        license.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(5,5,5,5, cp.getBackground()),
            BorderFactory.createEmptyBorder(5,5,5,5)
        ));

        final JButton button =
                new JButton(UIManager.getString("OptionPane.okButtonText"));
        button.addActionListener(this);

        cp.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        cp.add(image, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;

        cp.add(text, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 1;

        cp.add(license, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        cp.add(button, gbc);
    }

    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(final WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            dispose();
        }
        super.processWindowEvent(e);
    }

    /**Close the dialog on a button event*/
    public void actionPerformed(final ActionEvent e) {
        dispose();
    }

}
