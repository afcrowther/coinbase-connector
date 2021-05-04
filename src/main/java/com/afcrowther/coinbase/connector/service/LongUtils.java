package com.afcrowther.coinbase.connector.service;

import static com.afcrowther.coinbase.connector.service.UtilsConstants.*;

public class LongUtils {

    private LongUtils() {}

    /**
     * Convert a long into its String representation, using the StringBuilder provided as the medium for returning
     * the converted value (avoiding any new allocations).
     *
     * This method only works on positive long values, anything negative will do nothing to the StringBuilder.
     *
     * @param value             The value that we want converted to it's Latin 1 representation.
     * @param sb                The StringBuilder we want to append to
     * @param numDecimalPoints  The number of decimal places of the final string
     */
    public static void appendLongToStringBuilder(long value, StringBuilder sb, int numDecimalPoints) {
        if (value < 0) {
            return;
        }
        int numberOfDigits = Math.max(getNumberOfDigits(value), numDecimalPoints + 1);
        // add dummy values for each digit
        for (int i = 0; i < numberOfDigits; i++) {
            sb.append(EMPTY);
        }
        // add dummy value for the decimal (if required)
        if (numDecimalPoints != 0) {
            sb.append(EMPTY);
            numberOfDigits++;
        }
        // append the numbers in a backwards order, adding the decimal where required
        int index = sb.length() - 1;
        int indexOfDecimal = index - numDecimalPoints;
        while (value != 0) {
            char next = (char) (ZERO + value % 10);
            value = value / 10;
            sb.setCharAt(index--, next);
            if (index == indexOfDecimal) {
                sb.setCharAt(index--, DECIMAL_POINT);
            }
        }
        // allow for situations with leading zeroes in the output, e.g. 0.000421, the value will be set to 0 so we
        // now need to copy an missing zeros (and decimal places)
        while (index > indexOfDecimal - 2) {
            if (index == indexOfDecimal) {
                sb.setCharAt(index--, DECIMAL_POINT);
            } else {
                sb.setCharAt(index--, ZERO);
            }
        }
    }

    /**
     * <p>
     *     Converts a number String to a primitive long. If the String contains any other character than a number the
     *     other character is ignored, so be sure to only call this method with a properly formed numerical String. This
     *     also has the (intended) effect of ignoring any decimal places in the String, such that the String 1.01 will
     *     be returned as the long number 101.
     * </p>
     * </br>
     * <p>
     *     This method will also ignore any negation such that it only returns positive numbers, even if the input
     *     string indicates a negative number.
     * </p>
     *
     * @param value The number string which we want to convert to it's long value
     * @return The resulting long number
     */
    public static long convertStringToLong(String value) {
        long result = 0;
        for (char c : value.toCharArray()) {
            byte next = LATIN_1_TO_NUMBERS[c];
            if (next != -1) {
                result *= 10;
                result += next;
            }
        }
        return result;
    }

    // todo:: JMH these
    /**
     * <p>
     *     This method will return a value of 1 for the input number 0.
     * </p>
     * <p>
     *     Internally this method uses cached values and comparisons to figure out the result with the observation that
     *     any long value can have at most 19 digits. So warning to anyone reading this code, it is messy! However,
     *     it is also faster that using actual math and it has the benefit of no floating point conversions.
     *     </br>
     *     Alternatives to this method would be to use the {@link Math#log10(double)} method, or, to use the formula:
     *     "{@code log a (b) = log2 (b) / log2 (a)}" which may perform better (we could also cache the result of
     *     log 2(10)).
     * </p>
     *
     * @param value The number to get the number of digits of
     * @return The number of digits in the provided number
     */
    static int getNumberOfDigits(long value) {
        if (value > 0) {
            value = -value;
        }

        return value <= -1000000000000000000l ? 19 :
               value <= -100000000000000000l ? 18 :
               value <= -10000000000000000l ? 17 :
               value <= -1000000000000000l ? 16 :
               value <= -100000000000000l ? 15 :
               value <= -10000000000000l ? 14 :
               value <= -1000000000000l ? 13 :
               value <= -100000000000l ? 12 :
               value <= -10000000000l ? 11 :
               value <= -1000000000l ? 10 :
               value <= -100000000l ? 9 :
               value <= -10000000l ? 8 :
               value <= -1000000l ? 7 :
               value <= -100000l ? 6 :
               value <= -10000l ? 5 :
               value <= -1000l ? 4 :
               value <= -100l ? 3 :
               value <= -10l ? 2 : 1;
    }

    private static final long[] LOW_LONG_WITH_I_DIGITS = new long[] { 10l, 100l, 1000l, 10000l, 100000l, 1000000l,
            10000000l, 100000000l, 1000000000l, 10000000000l, 100000000000l, 1000000000000l, 10000000000000l,
            100000000000000l, 1000000000000000l, 10000000000000000l, 100000000000000000l, 1000000000000000000l };

    /**
     * <p>
     *     This method returns the number of digits in a number. This is done using a modified binary search whereby we
     *     can compare the number of value of the long until we find the point at which the number provided is bigger
     *     than {@code 1 * 10^n} where n is the number of digits but smaller than {@code 1 * 10^(n+1)}.
     * </p>
     * <br/>
     * <p>
     *     We can reduce the number of comparisons we make over linear search by doing a binary search. The Java std
     *     library makes the note that a linear scan is preferred because the distribution of numbers tested leans
     *     heavily towards zero, however in our case that is not true, as we are not expecting this method to be called
     *     with any 0 values (this method is called to construct the console printing output, we don't print levels with
     *     0 quantity or price, and in fact we will tend towards larger amount of digits as the numbers passed to this
     *     method will often be numbers with large amounts of decimal places but with the actual decimal place character
     *     removed, see - {@link Long#stringSize(long)}. However, unlike normal binary search, we will always perform
     *     ceil(log(n)) comparisons (n being the size of the search space, 18 in this case) as we aren't ever guessing rather
     *     we are dividing the search space until we arrive at a point that is within one place of the result, we can
     *     then use a comparison to the mid + 1 or mid - 1 index (depending on if we have searched up or down
     *     previously)
     * </p>
     * <br/>
     */
    static int getNumberOfDigitsBinarySearch(long value) {
        if (value < 0) {
            value = -value;
        }
        if (value == 0) {
            return 1;
        }

        int low = 0, high = 18;
        while (low <= high) {
            // safe from overflow so can use this method to calculate mid point
            int mid = (low + high) >>> 1;
            long midValue = LOW_LONG_WITH_I_DIGITS[mid];

            // check if we've finished
            if (mid == low || mid == high) {
                if (value < midValue) {
                    return mid + 1;
                } else if (value > LOW_LONG_WITH_I_DIGITS[mid + 1] ) {
                    return mid + 3;
                } else {
                    return mid + 2;
                }
            }

            if (value < midValue) {
                high = mid - 1;
            } else if (value > midValue) {
                low = mid + 1;
            } else {
                // just in case we get an exact match (i.e. the number is 10000)
                return mid + 2;
            }
        }
        // will never reach this
        return -1;
    }

    /**
     * A slight modification of the {@link Long#stringSize(long)} method.
     *
     * @param value
     * @return
     */
    static int getNumberOfDigitsJavaStdLib(long value) {
        if (value >= 0) {
            value = -value;
        }
        long p = -10;
        for (int i = 1; i < 19; i++) {
            if (value > p)
                return i;
            p = 10 * p;
        }
        return 19;
    }
}
