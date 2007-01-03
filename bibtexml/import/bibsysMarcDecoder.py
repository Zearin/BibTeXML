#!/usr/bin/env python
"""
  Decoder for MARC bibliographic data from Bibsys
  Usage: python bibsysdecoder.py bibfile.marc > bibfile.xml

  v.6
  (c)2001-11-20 Vidar Bronken Gundersen
  http://bibtexml.sf.net/
  Reuse approved as long as this notification is kept.
"""


# MARC fields used in Bibsys:
# 001 : identification number (follows right after 001)
#    *001981985610
#        ^^^^^^^^^
#    Bibsys post number, we can create an url
#          http://wgate.bibsys.no/gate1/SHOW?objd=981985610
# 008 : control codes (follows right after 008)
#    pos. 30: h = thesis, d = dr.ing. thesis, c = ?
#    pos. 41-43: language
# 015 : $a NATIONAL BIBLIOGRAPHY NUMBER (NR)
# 020 : $a isbn number  $b type, e.g. paperpack
# 022 : $a issn number
# 060 : $a NATIONAL LIBRARY OF MEDICINE CALL NUMBER
# 080 : $a classification udc = UNIVERSAL DECIMAL CLASSIFICATION NUMBER
# 082 : $a classification ddc = dewey decimal
# 085 : $a classification ubt
# 086 : $a classification ubb/GOVERNMENT DOCUMENT CALL NUMBER
# 087 : $a classification local
# 100 : $a author
# 241 : $a translation of title  $w ??
# 245 : $a title  $b remainder of title  $c title responsibles  $w ??
# 250 : $a edition statement
# 260 : $a place of publication  $b name of publisher $c date of publication
# 300 : $a extent  $b physical details (illustrations)
# 500 : $a general note (often translated title, subtitle,
#                confidentiality or (first) edition)
# 600 : $a subject personal name
# 650 : $a subject topical term {lcsh}  $x general subdivision
# 660 : $a subject topical term {mesh}
# 687 : $a subject topical term {local}
# 691 : $a subject topical term {free/user-defined?}
# 699 : $a subject topical term {ntub}
# 700 : $a name (other/remaining authors, responsibles etc)
# 096 : $a owner organization  $b shelf location $n ??


import string, re


def marcxmlwriter(data):
    list = data.keys()
    for entry in list:
        try:
            listinner = data[entry].keys()
            for entryinner in listinner:
                print entry, entryinner, data[entry][entryinner]
        except:
            print entry, ' ', data[entry]
    print


def bibtexwriter(data):
    print '<bibtex:entry id="' + 'bibsys-id-' + data['001'] + '">'
    print ' <bibtex:book>'
    print '  <bibtex:author>' + data['245']['c'] + '</bibtex:author>'
    #if data.has_key('100'):
    #    print '    <!-- {author main} ' + data['100']['a'] + ' -->'
    #if data.has_key('700'):
    #    print '    <!-- {author added} ' + data['700']['a'] + ' -->'
    print '  <bibtex:title>' + data['245']['a'] + '</bibtex:title>'
    #if data['245'].has_key('b'):
    #    print '    <!-- {subtitle} ' + data['245']['b'] + ' -->'
    print '  <bibtex:publisher>' + data['260']['b'] + '</bibtex:publisher>'
    print '  <bibtex:year>' + data['260']['c'] + '</bibtex:year>'
    if data['260'].has_key('a'):
        print '  <bibtex:address>' + data['260']['a'] + '</bibtex:address>'
    if data.has_key('250'):
        print '  <bibtex:edition>' + data['250']['a'] + '</bibtex:edition>'
    if data.has_key('020'):
        print '  <bibtex:isbn>' + data['020']['a'] + '</bibtex:isbn>'
    if data.has_key('650'):
        print '  <bibtex:keywords>' + data['650']['a'] + '</bibtex:keywords>'
    #if data.has_key('660'):
    #    print '    <!-- {mesh} ' + data['660']['a'] + ' -->'
    #if data.has_key('687'):
    #    print '    <!-- {local} ' + data['687']['a'] + ' -->'
    #if data.has_key('691'):
    #    print '    <!-- {free} ' + data['691']['a'] + ' -->'
    #if data.has_key('699'):
    #    print '    <!-- {ntub} ' + data['699']['a'] + ' -->'
    print ' </bibtex:book>'
    print '</bibtex:entry>'
    print ''


def finishbibitem(data):
    # append subtitle to title
    if data['245'].has_key('b'):
        data['245']['a'] = data['245']['a'] + '; ' + data['245']['b']
    # join author fields
    if not(data['245'].has_key('c')):
        if data.has_key('100'):
            data['245']['c'] = data['100']['a']
        elif data.has_key('700'):
            data['245']['c'] = data['700']['a']
    # join subject fields
    if not(data.has_key('650')) and data.has_key('660'):
        data['650'] = data['660']
    #if data.has_key('650') or \
    #   data.has_key('660') or \
    #   data.has_key('687') or \
    #   data.has_key('691') or \
    #   data.has_key('699'):
    #       data[field][subfield[0]] = \
    #           data[field][subfield[0]] + ', ' + subfield[1:]
    # remove preceeding c from year entry
    if data['260'].has_key('c'):
       if data['260']['c'][0] == 'c':
           data['260']['c'] = data['260']['c'][1:]
    # output bibtex-xml tagged data
    bibtexwriter(data)


def marcdecoder(filecontents_source):
    for line in filecontents_source:
        #print line   # debugging: crash point in input data
        line = re.sub('&', '&amp;', line)
        line = re.sub('<', '&lt;', line)
        field = line[1:4]
        if line[:-1] == '' or line[:-1] == '\015':
            # empty line separates each bibl. entry (cr/lf problem?)
            finishbibitem(data)
        # irregular post format: id
        elif field == '001':
            # new item/book
            data = {}
            data[field] = line[4:-1]
        # irregular post format: language, publication type
        elif field == '008':
            data[field] = line[41:-1]
            #data[field] = line[30]
        # ignore list of copies (eksemplarliste)
        elif field == '096':
            pass
        else:
            line = line[7:-1]
            linespl = re.split('\$', line)
            # multiple entries: append data
            if not(data.has_key(field)):
                data[field] = {}
            for subfield in linespl:
                #print field, data[field]   # debugging
                if data[field].has_key(subfield[0]):
                    data[field][subfield[0]] = \
                        data[field][subfield[0]] + '; ' + subfield[1:]
                else:
                    data[field][subfield[0]] = subfield[1:]



def filehandler(filepath):
    try:
        fd = open(filepath, 'r')
        filecontents_source = fd.readlines()
        fd.close()
    except:
        print 'Could not open file:', filepath
    print '<?xml version="1.0" encoding="iso-8859-1"?>'
    print '<!DOCTYPE bibtex:file SYSTEM "bibtexml.dtd" >'
    print '<bibtex:file xmlns:bibtex="http://bibtexml.sf.net/">'
    print ''
    marcdecoder(filecontents_source)
    print '</bibtex:file>'
    print '''<!--
       This tool is only intended to do the rough work for you.
       Checklist:
       1. each post in your marc input data should be one line only
       2. check the bibliographic data for errors
       (c)2001 Vidar Bronken Gundersen
       marcdecoder.py at www.bitjungle.com/~bibtex/
    -->'''



# main program

def main():
    import sys
    if sys.argv[1:]:
        filepath = sys.argv[1]
    else:
        print 'No input file'
        sys.exit()
    filehandler(filepath)

if __name__ == "__main__": main()


# end python script
