package com.cts.plugin.loc.service;

import java.util.Collections;
import java.util.List;

/**
 * Utility class to find the minimum and maximum number from a list of integers.
 */
public class MinMaxFinder {

    /**
     * Finds the minimum value in a list.
     *
     * @param numbers the list of integers
     * @return the minimum integer value
     * @throws IllegalArgumentException if the list is null or empty
     */
    public static int findMin(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        return Collections.min(numbers);
    }

    /**
     * Finds the maximum value in a list.
     *
     * @param numbers the list of integers
     * @return the maximum integer value
     * @throws IllegalArgumentException if the list is null or empty
     */
    public static int findMax(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        return Collections.max(numbers);
    }

    /**
     * Returns both min and max as a result record.
     *
     * @param numbers the list of integers
     * @return a MinMaxResult containing min and max values
     */
    public static MinMaxResult findMinAndMax(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int num : numbers) {
            if (num < min) min = num;
            if (num > max) max = num;
        }
        return new MinMaxResult(min, max);
    }

    /**
     * Record to hold both minimum and maximum values.
     */
    public record MinMaxResult(int min, int max) {
        @Override
        public String toString() {
            return "MinMaxResult { min=" + min + ", max=" + max + " }";
        }
    }

    // Example usage
    public static void main(String[] args) {
        List<Integer> numbers = List.of(3, 1, 7, 2, 9, 4, 6, 5, 8);

        System.out.println("List: " + numbers);
        System.out.println("Min: " + findMin(numbers));
        System.out.println("Max: " + findMax(numbers));

        MinMaxResult result = findMinAndMax(numbers);
        System.out.println(result);
    }
}

