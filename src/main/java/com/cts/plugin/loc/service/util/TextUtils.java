package com.cts.plugin.loc.service.util;

public class TextUtils {
    /**
     * Counts the number of words in the given text. Words are separated by whitespace.
     * @param text the input text
     * @return the number of words
     */
    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // Split by one or more whitespace characters
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
}

