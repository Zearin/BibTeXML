#!/usr/bin/env pyvows
# -*- coding: UTF-8 -*-


#---------------------------------------------------------------------
#   IMPORTS
#---------------------------------------------------------------------
#
#   Standard Library
from    __future__  import (print_function, )
from    codecs      import (open as open    )
from    os          import (path,           )
from    os.path     import (abspath,
                            basename,
                            dirname,        )
from    xml.etree   import (ElementTree,    )

import  re
import  difflib

WHITESPACE      = re.compile(r'\s',  re.MULTILINE)
CONSECUTIVE_WS  = re.compile(r'\s+', re.MULTILINE)

XML             = ElementTree.XML
ETree           = ElementTree.ElementTree

#
#   Testing
from    pyvows      import (Vows,
                            expect,
                            VowsAssertionError,)

#
#   Do not stray from the righteous `path`...
TEST_PATH       =   abspath(dirname(__file__))
MOD_PATH        =   path.join(TEST_PATH, '../')
EXAMPLES_PATH   =   path.join(MOD_PATH,  '../examples')

try:
    #   Import the file directly above this one
    #   (i.e., don’t use similar modules found in PYTHONPATH)
    import  sys
    _syspath = sys.path
    sys.path.insert(0, MOD_PATH)
    import bibtex2xml
    sys.path = _syspath
except ImportError as err:
    print(err)
    sys.exit(err)


#---------------------------------------------------------------------
#   HELPER UTILS
#---------------------------------------------------------------------
TESTDATA_NAMES   = ('demo'                   ,
                    'testcases-utf8'         ,)

                ### FIXME
                    #       These two produce non well-formed XML.
                    #
                    #       -   Fix the tests?
                    #       -   Fix the script?
                    #       -   ...?
                #    'testcases-uncommented'  ,
                #    'testcases'              ,
                #)

filehandler     = bibtex2xml.filehandler


absjoin = lambda pth, fyle: abspath(path.join(pth, fyle))


def open_as_str(filename, allowed_extensions=('.bib','.xml')):
    '''Opens `filename` and returns its contents as a string.'''

    assert isinstance(filename, str)
    assert filename[:-4] in TESTDATA_NAMES
    assert filename.endswith( allowed_extensions )

    suffix = filename[-4:]
    prefix = EXAMPLES_PATH if filename[-4:] == '.bib' else TEST_PATH

    filename        = absjoin(prefix, filename)
    string_contents = ''

    with open(filename, 'r') as f:
        for line in f:
            string_contents += line

    assert isinstance(string_contents, str)
    return string_contents


def bib2xml2str(filename):
    '''Converts bibtex in `filename` to an XML string.'''

    assert isinstance(filename, str)
    assert filename != ''

    filename    = '{filename}.bib'.format(filename=filename)
    filename    = absjoin(EXAMPLES_PATH, filename)
    filecontents_source = filehandler(filename)
    iterator    = bibtex2xml.contentshandler(filecontents_source)
    output      = [line for line in iterator]
    output      = ''.join(output)
    #output      = CONSECUTIVE_WS.sub(' ', output)
    return output


def xml2str(filename):
    '''Converts XML in `filename` to an XML string.'''

    assert isinstance(filename, str)
    assert filename != ''

    filename    = '{filename}.xml'.format(filename=filename)
    filename    = absjoin(TEST_PATH, filename)
    xmlfile     = filehandler(filename)
    xmlfile     = ''.join(xmlfile)
    #xmlfile     = CONSECUTIVE_WS.sub(' ', xmlfile)
    return xmlfile


def handle_ParseError(err, xml):
    '''Prints the line number and content of the XML where a parser error
    occurred.
    '''
    from pprint import pprint as pprint

    err         = str(err)
    line        = err.index('line ')
    col         = err.index(', column ')

    line_number = int(err[line+5:col])

    print('Error parsing XML at line {line}:'.format(line=line_number))
    context = line_number - 1
    xml     = xml.splitlines()

    for line in xrange(3):
        tab = ' ' * 4
        msg = '{tab}{line}{tab}{xml}'.format(
            tab  = tab,
            line = str(context) + ':',
            xml  = xml[line]
        )
        print(msg)
        context += 1

    print()


#---------------------------------------------------------------------
#
#   BEGIN THE TESTS!
#
#---------------------------------------------------------------------
@Vows.batch
class ForEachFile(Vows.Context):
    def topic(self):
        for name in TESTDATA_NAMES:
            yield name


    class StringTests(Vows.Context):
        ''' Checks this test (not the bibtex2xml.py script).
            Ensures that passed-in filenames (without extension) are
            converted to strings without error.
        '''
        def topic(self, parent_topic):
            for conversion in (bib2xml2str, xml2str):
                yield conversion(parent_topic)

        def should_be_a_string(self, topic):
            expect(topic).to_be_instance_of(str)

        def should_not_be_empty(self, topic):
            expect(topic).Not.to_be_empty()

        def should_not_be_a_list(self, topic):
            expect(topic).Not.to_be_instance_of(list)


    class ElementTreeTests(Vows.Context):
        ''' Checks (mostly) this test suite's use of ElementTree.

            (I'm not used to Python + XML, so I'm pretty much learning
            from scratch...)
        '''
        def topic(self, parent_topic):
            #   Messy for now.  Clean up later.
            #
            #   Must declare cleanup functions first...
            #

            def normalize_ws(item):
                return re.sub(CONSECUTIVE_WS, ' ', item)

            def to_ElementTree(item):
                try:
                    item  = ElementTree.fromstring(item)
                    item  = ETree(item)
                except ElementTree.ParseError as e:
                    exc     = sys.exc_info()[1]
                    handle_ParseError(exc, item)
                return item

            result, expected = (bib2xml2str(parent_topic),
                                xml2str(parent_topic))
            result, expected = (normalize_ws(result),
                                normalize_ws(expected))
            result, expected = (to_ElementTree(result),
                                to_ElementTree(expected))
            return (result, expected)

        def is_an_ElementTree(self, topic):
            for i in topic:
                expect(i).to_be_instance_of(ElementTree.ElementTree)

        ###
        ### FIXME!!!
        ###
        #   These vows need LOTS of cleanup.
        #   There's currently a great deal of repetition.
        ###
        def should_have_the_same_length(self, topic):
            result, expected = topic

            r_items = [i for i in result.iter()]
            e_items = [i for i in expected.iter()]

            r_len  = len(r_items)
            e_len  = len(e_items)

            expect(r_len).to_equal(e_len)

        def should_have_the_same_tag(self, topic):
            result, expected = topic

            r_items = [i for i in result.iter()]
            e_items = [i for i in expected.iter()]

            for idx, r in enumerate(r_items):
                expect(r.tag).to_be_like(e_items[idx].tag)

        def should_have_the_same_text(self, topic):
            result, expected = topic

            r_items = [i for i in result.iter()]
            e_items = [i for i in expected.iter()]

            for idx, item in enumerate(r_items):
                is_empty = lambda txt: (txt is not None and
                                        txt is not ''   and
                                        not CONSECUTIVE_WS.match(txt))

                r_text, e_text = ([i for i in item.itertext()],
                                  [i for i in e_items[idx].itertext()])
                # filter whitespace
                r_text, e_text = (filter(is_empty, r_text),
                                  filter(is_empty, r_text))

                expect(r_text).to_equal(e_text)



#     class WhenWeTestXMLAsElementTrees(Vows.Context):
#         def topic(self, filename):
#             return filename

#         class ElementTreeTests(compareXMLContext(filename)):
#             pass


#             class Both(Vows.Context):
#
#                 def topic(self, xmlfile, output):
#                     xmlfile =   CONSECUTIVE_WS.sub(' ', xmlfile)
#                     output  =   CONSECUTIVE_WS.sub(' ', output)
#                     return (output, xmlfile)
#
#                 def should_have_same_length_after_removing_whitespace(self, topic):
#                     output, xml = topic
#                     expect(output).to_length(len(xml))
#
#                 def should_match_after_removing_whitespace(self, topic):
#                     output, xml = topic
#                     output  = WHITESPACE.sub('',output)
#                     xml     = WHITESPACE.sub('',xml)
#                     expect(output).to_equal(xml)

# class TestRemovebraces(Vows.Context):
#     def topic(self):
#         return TestRemovebraces()
#
#     def test_removebraces(self, topic):
#         # expect(removebraces(str)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestBibtexauthor(Vows.Context):
#     def topic(self):
#         return TestBibtexauthor()
#
#     def test_bibtexauthor(self, topic):
#         # expect(bibtexauthor(data)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestBibtextitle(Vows.Context):
#     def topic(self):
#         return TestBibtextitle()
#
#     def test_bibtextitle(self, topic):
#         # expect(bibtextitle(data)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestBibtexkeyword(Vows.Context):
#     def topic(self):
#         return TestBibtexkeyword()
#
#     def test_bibtexkeyword(self, topic):
#         # expect(bibtexkeyword(data)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestCapitalizetitle(Vows.Context):
#     def topic(self):
#         return TestCapitalizetitle()
#
#     def test_capitalizetitle(self, topic):
#         # expect(capitalizetitle(data)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestBibtexdecoder(Vows.Context):
#     def topic(self):
#         return TestBibtexdecoder()
#
#     def test_bibtexdecoder(self, topic):
#         # expect(bibtexdecoder(filecontents_source)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestVerifyOutOfBraces(Vows.Context):
#     def topic(self):
#         return TestVerifyOutOfBraces()
#
#     def test_verify_out_of_braces(self, topic):
#         # expect(verify_out_of_braces(line, abbr)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestConcatLine(Vows.Context):
#     def topic(self):
#         return TestConcatLine()
#
#     def test_concat_line(self, topic):
#         # expect(concat_line(line)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestBibtexReplaceAbbreviations(Vows.Context):
#     def topic(self):
#         return TestBibtexReplaceAbbreviations()
#
#     def test_bibtex_replace_abbreviations(self, topic):
#         # expect(bibtex_replace_abbreviations(filecontents_source)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestNoOuterParens(Vows.Context):
#     def topic(self):
#         return TestNoOuterParens()
#
#     def test_no_outer_parens(self, topic):
#         # expect(no_outer_parens(filecontents)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestBibtexwasher(Vows.Context):
#     def topic(self):
#         return TestBibtexwasher()
#
#     def test_bibtexwasher(self, topic):
#         # expect(bibtexwasher(filecontents_source)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestContentshandler(Vows.Context):
#     def topic(self):
#         return TestContentshandler()
#
#     def test_contentshandler(self, topic):
#         # expect(contentshandler(filecontents_source)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestFilehandler(Vows.Context):
#     def topic(self):
#         return TestFilehandler()
#
#     def test_filehandler(self, topic):
#         # expect(filehandler(filepath)).to_equal(expected)
#         pass # TODO: implement your test here
#
#
# class TestMain(Vows.Context):
#     def topic(self):
#         return TestMain()
#
#     def test_main(self, topic):
#         # expect(main()).to_equal(expected)
#         pass # TODO: implement your test here
#
#