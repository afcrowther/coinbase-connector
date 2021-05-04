package com.afcrowther.coinbase.connector.service;

import org.junit.Assert;
import org.junit.Test;

public class LongUtilsTest {

    @Test
    public void testAppendLongToStringBuilder() {
        long value = 122024;
        StringBuilder output = new StringBuilder("some-test-data");

        String expected = "some-test-data1220.24";
        LongUtils.appendLongToStringBuilder(value, output, 2);
        String actual = output.toString();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendLongToStringBuilder_noDecimals() {
        long value = 12224;
        StringBuilder output = new StringBuilder("some-test-data");

        String expected = "some-test-data12224";
        LongUtils.appendLongToStringBuilder(value, output, 0);
        String actual = output.toString();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendLongToStringBuilder_leadingZeroesInDecimals() {
        long value = 564;
        StringBuilder output = new StringBuilder("some-test-data");

        String expected = "some-test-data0.0000564";
        LongUtils.appendLongToStringBuilder(value, output, 7);
        String actual = output.toString();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertStringToLong() {
        String s = "123";

        long expected = 123;
        long actual = LongUtils.convertStringToLong(s);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertStringToLong_decimal() {
        String s = "123.4056";

        long expected = 1234056;
        long actual = LongUtils.convertStringToLong(s);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertStringToLong_empty() {
        String s = "";

        long expected = 0;
        long actual = LongUtils.convertStringToLong(s);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertStringToLong_singleDigit() {
        String s = "3";

        long expected = 3;
        long actual = LongUtils.convertStringToLong(s);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_lowBoundary() {
        long input = 1000;

        int expected = 4;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_inBetweenBoundary() {
        long input = 5169;

        int expected = 4;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_upperBoundary() {
        long input = 999;

        int expected = 3;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_zero() {
        long input = 0;

        int expected = 1;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_singleDigit() {
        long input = 6;

        int expected = 1;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_negative() {
        long input = -999;

        int expected = 3;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_large() {
        long input = 55032049512458l;

        int expected = 14;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_largeLowerBoundary() {
        long input = 1000000000000000l;

        int expected = 16;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDigits_largeUpperBoundary() {
        long input = 999999999999999l;

        int expected = 15;
        int actual = LongUtils.getNumberOfDigits(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsJavaStdLib(input);

        Assert.assertEquals(expected, actual);

        actual = LongUtils.getNumberOfDigitsBinarySearch(input);

        Assert.assertEquals(expected, actual);
    }
}
