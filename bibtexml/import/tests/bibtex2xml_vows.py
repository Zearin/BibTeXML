#!/usr/bin/env pyvows
# -*- coding: UTF-8 -*-


#---------------------------------------------------------------------
#   IMPORTS
#---------------------------------------------------------------------
from    pyvows  import (Vows, expect)

try:
    #   Import the file directly above this one
    #   (Don’t use anything found in normal PYTHONPATHs)
    import  sys
    sys.path.insert( 0, '../' )
    import bibtex2xml
except ImportError as e:
    sys.exit(e)


#---------------------------------------------------------------------
#   TEST UTILITIES
#---------------------------------------------------------------------
testDataPrefix = '../examples'
testData = (    open( testDataPrefix + '/testcases-utf8.bib', 'r' )          ,
                open( testDataPrefix + '/testcases-uncommented.bib', 'r' )   ,
                open( testDataPrefix + '/testcases.bib', 'r' )               ,
                open( testDataPrefix + '/demo.bib', 'r' )
            )



#---------------------------------------------------------------------
#   BEGIN THE TESTS
#---------------------------------------------------------------------
@Vows.batch
class ContentsHandler(Vows.Context):

    def topic(self):
        return bibtex2xml.contentshandler


    class TestDataZero(Vows.Context):
        def topic(self, parent_topic):
            return parent_topic( testData[0] )

        def contentshandler_should_return_string(self, topic):
            expect(isinstance( topic, str)).to_be_true()

        def contentshandler_output_matches_previous(self, topic):
            XML             =   open('tests/testcases-utf8.xml','r')
            testcases_str   =   ''.join([line for line in XML])
            XML.close()
            expect(topic).to_be_like( testcases_str )


    class TestDataOne(Vows.Context):
        def topic(self, parent_topic):
            return parent_topic( testData[1] )

        def contentshandler_should_return_string(self, topic):
            expect(isinstance( topic, str)).to_be_true()

        def contentshandler_output_matches_previous(self, topic):
            XML             =   open('tests/testcases-uncommented.xml','r')
            testcases_str   =   ''.join([line for line in XML])
            XML.close()
            expect(topic).to_be_like( testcases_str )


    class TestDataTwo(Vows.Context):
        def topic(self, parent_topic):
            return parent_topic( testData[2] )

        def contentshandler_should_return_string(self, topic):
            expect(isinstance( topic, str)).to_be_true()

        def contentshandler_output_matches_previous(self, topic):
            XML             =   open('tests/testcases.xml','r')
            testcases_str   =   ''.join([line for line in XML])
            XML.close()
            expect(topic).to_be_like( testcases_str )


    class TestDataThree(Vows.Context):
        def topic(self, parent_topic):
            return parent_topic( testData[3] )

        def contentshandler_should_return_string(self, topic):
            expect(isinstance( topic, str)).to_be_true()

        def contentshandler_output_matches_previous(self, topic):
            XML             =   open('tests/demo.xml','r')
            testcases_str   =   ''.join([line for line in XML])
            XML.close()
            expect(topic).to_be_like( testcases_str )


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
