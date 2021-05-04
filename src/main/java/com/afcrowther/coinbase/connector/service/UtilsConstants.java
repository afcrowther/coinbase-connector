package com.afcrowther.coinbase.connector.service;

import java.util.Arrays;

public class UtilsConstants {

    private UtilsConstants() {}

    static final byte QUOTATION_MARKS = 34;
    static final char EMPTY = ' ';
    static final char DECIMAL_POINT = 46;
    static final char ZERO = '0';
    // for any number value, this array holds it's equivalent Latin 1 character value in that index
    static final byte[] NUMBERS_TO_LATIN_1 = new byte[255];
    // vice-versa
    static final byte[] LATIN_1_TO_NUMBERS = new byte[255];

    static {
        Arrays.fill(NUMBERS_TO_LATIN_1, (byte) -1);
        for (int i = 0; i < 10; i++) {
            NUMBERS_TO_LATIN_1[i] = (byte) i;
        }
        Arrays.fill(LATIN_1_TO_NUMBERS, (byte) -1);
        for (int i = 48; i < 58; i++) {
            LATIN_1_TO_NUMBERS[i] = (byte) (i - 48);
        }
    }
}
