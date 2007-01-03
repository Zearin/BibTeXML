#!/bin/sh
python import/bibtex2xml.py examples/demo.bib > demo.xml
rnv schema/bibtexml-ext.rnc demo.xml
xsltproc output/starter.xsl demo.xml >demo-output.html
