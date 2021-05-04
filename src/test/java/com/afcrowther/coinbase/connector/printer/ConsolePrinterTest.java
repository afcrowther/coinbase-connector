package com.afcrowther.coinbase.connector.printer;

import com.afcrowther.coinbase.connector.domain.coinbase.Product;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLongArray;

public class ConsolePrinterTest {

    private ConsolePrinter UNDER_TEST;

    @Before
    public void setup() {
        Product marketInfo = new Product();
        marketInfo.setBaseIncrement("0.00000001");
        marketInfo.setQuoteIncrement("0.01");
        UNDER_TEST = new ConsolePrinter(marketInfo);
        UNDER_TEST.clean();
    }

    @Test
    public void testBuildOrderBookStringRepresentation() {
        AtomicLongArray asks = new AtomicLongArray(new long[] { 343444, 1000000000, 343440, 1578900, 343424, 859700000, 343418, 582780904, 343417,
                86343450, 343414, 299982311, 343410, 58000000, 343390, 582816063, 343359, 582860160, 343333,
                5900100350l });
        AtomicLongArray bids = new AtomicLongArray(new long[] { 343303, 31000000, 343302, 200576190, 343301, 1719605004,
                343272, 1342894532, 343271, 930000000, 343264, 1582697127, 343238, 4601780, 343205, 100000000, 343203,
                179690823, 343158, 314660000 });

        String expected = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nPrice \t\t\t Quantity\n" +
                "\u001B[31m\n3434.44\t\t10.00000000\n3434.40\t\t0.01578900\n3434.24\t\t8.59700000\n3434.18\t\t" +
                "5.82780904\n3434.17\t\t0.86343450\n3434.14\t\t2.99982311\n3434.10\t\t0.58000000\n3433.90\t\t" +
                "5.82816063\n3433.59\t\t5.82860160\n3433.33\t\t59.00100350\n\u001B[0m\u001B[32m\n3433.03\t\t" +
                "0.31000000\n3433.02\t\t2.00576190\n3433.01\t\t17.19605004\n3432.72\t\t13.42894532\n3432.71\t\t" +
                "9.30000000\n3432.64\t\t15.82697127\n3432.38\t\t0.04601780\n3432.05\t\t1.00000000\n3432.03\t\t" +
                "1.79690823\n3431.58\t\t3.14660000\n\u001B[0m";
        String actual = UNDER_TEST.buildOrderBookStringRepresentation(asks, bids).toString();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testBuildOrderBookStringRepresentation_emptyLevels() {
        AtomicLongArray asks = new AtomicLongArray(new long[] { 343444, 1000000000, 343440, 1578900, 343424, 859700000, 343418, 582780904, 343417,
                86343450, 343414, 299982311, 343410, 58000000, 343390, 582816063, -1, -1, -1,
                -1 });
        AtomicLongArray bids = new AtomicLongArray(new long[] { 343303, 31000000, 343302, 200576190, 343301, 1719605004,
                343272, 1342894532, 343271, 930000000, 343264, 1582697127, 343238, 4601780, 343205, 100000000, 343203,
                179690823, -1, -1 });

        String expected = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nPrice \t\t\t Quantity\n" +
                "\u001B[31m\n3434.44\t\t10.00000000\n3434.40\t\t0.01578900\n3434.24\t\t8.59700000\n3434.18\t\t" +
                "5.82780904\n3434.17\t\t0.86343450\n3434.14\t\t2.99982311\n3434.10\t\t0.58000000\n3433.90\t\t" +
                "5.82816063\n\u001B[0m\u001B[32m\n3433.03\t\t" +
                "0.31000000\n3433.02\t\t2.00576190\n3433.01\t\t17.19605004\n3432.72\t\t13.42894532\n3432.71\t\t" +
                "9.30000000\n3432.64\t\t15.82697127\n3432.38\t\t0.04601780\n3432.05\t\t1.00000000\n3432.03\t\t" +
                "1.79690823\n\u001B[0m";
        String actual = UNDER_TEST.buildOrderBookStringRepresentation(asks, bids).toString();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDecimalPlaces() {
        String input = "0.00000001";
        int expected = 8;
        int actual = UNDER_TEST.getNumberOfDecimalPlaces(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDecimalPlaces_trailingZeros() {
        String input = "0.01000000";
        int expected = 2;
        int actual = UNDER_TEST.getNumberOfDecimalPlaces(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDecimalPlaces_noDecimal() {
        String input = "100000001";
        int expected = 0;
        int actual = UNDER_TEST.getNumberOfDecimalPlaces(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetNumberOfDecimalPlaces_shorterString() {
        String input = "0.01";
        int expected = 2;
        int actual = UNDER_TEST.getNumberOfDecimalPlaces(input);

        Assert.assertEquals(expected, actual);
    }
}
