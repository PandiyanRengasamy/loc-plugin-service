package com.cts.plugin.loc.testcode;

public class StringProcessor {
    // ...existing code... (removed dangling Javadoc comment)
    /**
     * Return the sum of three integers.
     *
     * @param a first addend
     * @param b second addend
     * @param c third addend
     * @return a + b + c
     */
    public static int sum(int a, int b, int c) {
        return a + b + c;
    }

    public static void main(String[] args) {
        // simple demonstration
        int result = sum(2, 3, 4);
        System.out.println("Sum of 2, 3 and 4 is: " + result);
    }
}
