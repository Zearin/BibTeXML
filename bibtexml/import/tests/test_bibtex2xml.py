#!/usr/bin/env pyvows

import pyvows


class TestRemovebraces(Vows.Context):
    def topic(self):
        return TestRemovebraces()

    def test_removebraces(self, topic):
        # expect(removebraces(str)).to_equal(expected)
        pass # TODO: implement your test here


class TestBibtexauthor(Vows.Context):
    def topic(self):
        return TestBibtexauthor()

    def test_bibtexauthor(self, topic):
        # expect(bibtexauthor(data)).to_equal(expected)
        pass # TODO: implement your test here


class TestBibtextitle(Vows.Context):
    def topic(self):
        return TestBibtextitle()

    def test_bibtextitle(self, topic):
        # expect(bibtextitle(data)).to_equal(expected)
        pass # TODO: implement your test here


class TestBibtexkeyword(Vows.Context):
    def topic(self):
        return TestBibtexkeyword()

    def test_bibtexkeyword(self, topic):
        # expect(bibtexkeyword(data)).to_equal(expected)
        pass # TODO: implement your test here


class TestCapitalizetitle(Vows.Context):
    def topic(self):
        return TestCapitalizetitle()

    def test_capitalizetitle(self, topic):
        # expect(capitalizetitle(data)).to_equal(expected)
        pass # TODO: implement your test here


class TestBibtexdecoder(Vows.Context):
    def topic(self):
        return TestBibtexdecoder()

    def test_bibtexdecoder(self, topic):
        # expect(bibtexdecoder(filecontents_source)).to_equal(expected)
        pass # TODO: implement your test here


class TestVerifyOutOfBraces(Vows.Context):
    def topic(self):
        return TestVerifyOutOfBraces()

    def test_verify_out_of_braces(self, topic):
        # expect(verify_out_of_braces(line, abbr)).to_equal(expected)
        pass # TODO: implement your test here


class TestConcatLine(Vows.Context):
    def topic(self):
        return TestConcatLine()

    def test_concat_line(self, topic):
        # expect(concat_line(line)).to_equal(expected)
        pass # TODO: implement your test here


class TestBibtexReplaceAbbreviations(Vows.Context):
    def topic(self):
        return TestBibtexReplaceAbbreviations()

    def test_bibtex_replace_abbreviations(self, topic):
        # expect(bibtex_replace_abbreviations(filecontents_source)).to_equal(expected)
        pass # TODO: implement your test here


class TestNoOuterParens(Vows.Context):
    def topic(self):
        return TestNoOuterParens()

    def test_no_outer_parens(self, topic):
        # expect(no_outer_parens(filecontents)).to_equal(expected)
        pass # TODO: implement your test here


class TestBibtexwasher(Vows.Context):
    def topic(self):
        return TestBibtexwasher()

    def test_bibtexwasher(self, topic):
        # expect(bibtexwasher(filecontents_source)).to_equal(expected)
        pass # TODO: implement your test here


class TestContentshandler(Vows.Context):
    def topic(self):
        return TestContentshandler()

    def test_contentshandler(self, topic):
        # expect(contentshandler(filecontents_source)).to_equal(expected)
        pass # TODO: implement your test here


class TestFilehandler(Vows.Context):
    def topic(self):
        return TestFilehandler()

    def test_filehandler(self, topic):
        # expect(filehandler(filepath)).to_equal(expected)
        pass # TODO: implement your test here


class TestMain(Vows.Context):
    def topic(self):
        return TestMain()

    def test_main(self, topic):
        # expect(main()).to_equal(expected)
        pass # TODO: implement your test here


