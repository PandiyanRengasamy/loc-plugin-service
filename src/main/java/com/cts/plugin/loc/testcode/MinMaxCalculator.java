package com.cts.plugin.loc.testcode;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Utility class to calculate minimum and maximum numbers
 * from arrays or collections of integers.
 */
public class MinMaxCalculator {

    private MinMaxCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the minimum value from an array of integers.
     *
     * @param numbers the array of integers
     * @return the minimum value
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static int min(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        int min = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < min) {
                min = numbers[i];
            }
        }
        return min;
    }

    /**
     * Returns the maximum value from an array of integers.
     *
     * @param numbers the array of integers
     * @return the maximum value
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static int max(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        int max = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
            }
        }
        return max;
    }

    /**
     * Returns the minimum value from a collection of integers.
     *
     * @param numbers the collection of integers
     * @return the minimum value
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public static int min(Collection<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Collection must not be null or empty");
        }
        return numbers.stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns the maximum value from a collection of integers.
     *
     * @param numbers the collection of integers
     * @return the maximum value
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public static int max(Collection<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Collection must not be null or empty");
        }
        return numbers.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns both min and max as a result object in a single pass over the array.
     *
     * @param numbers the array of integers
     * @return a {@link MinMaxResult} containing the min and max values
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static MinMaxResult minMax(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        int min = numbers[0];
        int max = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < min) min = numbers[i];
            if (numbers[i] > max) max = numbers[i];
        }
        return new MinMaxResult(min, max);
    }

    /**
     * Holds the result of a combined min/max calculation.
     */
    public static class MinMaxResult {
        private final int min;
        private final int max;

        public MinMaxResult(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String toString() {
            return "MinMaxResult{min=" + min + ", max=" + max + "}";
        }
    }

    // Quick demo
    public static void main(String[] args) {
        int[] numbers = {3, 1, 7, -2, 15, 4, 0};
        System.out.println("Numbers : " + Arrays.toString(numbers));
        System.out.println("Min     : " + min(numbers));
        System.out.println("Max     : " + max(numbers));
        System.out.println("MinMax  : " + minMax(numbers));
    }
}

