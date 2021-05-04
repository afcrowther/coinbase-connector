package com.afcrowther.coinbase.connector.service;

import io.vertx.core.buffer.Buffer;

import static com.afcrowther.coinbase.connector.service.UtilsConstants.LATIN_1_TO_NUMBERS;
import static com.afcrowther.coinbase.connector.service.UtilsConstants.QUOTATION_MARKS;

/**
 * Provides some of the helper methods we need to work with {@link Buffer} objects, such as efficient parsing and
 * conversions.
 */
public class BufferUtils {

    private BufferUtils() {}

    /**
     * Parses an order book level, and returns the index that it closed at (the ']'). We assume no empty or malformed
     * levels are passed. We know that the format is:
     * [
     *    "buy",
     *    "10101.80000000",
     *    "0.162567"
     * ]
     *
     * @param buffer             The buffer containing the level
     * @param startIndex         The index of the first quotation mark of the first number (the price)
     * @param priceQuantityArray The array we will use to return the parsed level
     * @return The index that the level finished at (the closing square bracket of the level)
     */
    public static int parseOrderBookLevel(Buffer buffer, int startIndex, long[] priceQuantityArray) {
        // skip the open quotation mark
        int currentIndex = startIndex + 1;
        // parse the price
        currentIndex = parseNumber(buffer, currentIndex, priceQuantityArray, 0);
        // skip the closing quotation mark, comma, and, opening quotation mark
        currentIndex += 3;
        currentIndex = parseNumber(buffer, currentIndex, priceQuantityArray, 1);
        return currentIndex + 1;
    }

    /**
     * Takes a buffer and indexes between which the buffer contains only numbers and a decimal place (which will be in
     * byte representation) and converts that into a long. The decimal place will be treated as if it wasn't there, so
     * that 0.01 will be returned as the long value 1, or 1.01 would be returned as 101.
     *
     * This method only works on positive numbers, and any non-numeric characters will be ignored.
     *
     * @param buffer The buffer containing the string to be converted into a long
     * @return The converted long value
     */
    static long convertBufferToLong(Buffer buffer, int startIndex, int endIndex) {
        long result = 0;
        for (int i = startIndex; i < endIndex; i++) {
            byte next = LATIN_1_TO_NUMBERS[buffer.getByte(i)];
            if (next != -1) {
                result *= 10;
                result += next;
            }
        }
        return result;
    }

    /**
     * Parses a number given the appropriate starting index, and subsequently returns the index that the number finished
     * at (the closing quotation mark). The result will be passed back using the array and index specified in the
     * arguments.
     *
     * Returns the index of the character of the closing quotation mark surrounding the number
     */
    static int parseNumber(Buffer buffer, int startIndex, long[] output, int outputIndex) {
        boolean finishedNumber = false;
        int currentIndex = startIndex;
        byte next;
        while (!finishedNumber) {
            next = buffer.getByte(currentIndex);
            if (next == QUOTATION_MARKS) {
                finishedNumber = true;
                output[outputIndex] = BufferUtils.convertBufferToLong(buffer, startIndex, currentIndex);
            } else {
                currentIndex ++;
            }
        }
        return currentIndex++;
    }
}
