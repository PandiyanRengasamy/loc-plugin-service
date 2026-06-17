package com.cts.plugin.loc.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {
    @Test
    void testCountWords_nullOrEmpty() {
        assertEquals(0, TextUtils.countWords(null));
        assertEquals(0, TextUtils.countWords(""));
        assertEquals(0, TextUtils.countWords("   "));
    }

    @Test
    void testCountWords_singleWord() {
        assertEquals(1, TextUtils.countWords("hello"));
    }

    @Test
    void testCountWords_multipleWords() {
        assertEquals(2, TextUtils.countWords("hello world"));
        assertEquals(4, TextUtils.countWords("this is test"));
        assertEquals(5, TextUtils.countWords("  one  two   three four five  "));
    }

    @Test
    void testCountWords_withPunctuation() {
        assertEquals(4, TextUtils.countWords("Hello, world! This? Test."));
    }
}

