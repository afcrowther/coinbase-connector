package com.afcrowther.coinbase.connector.service;

import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Test;

public class BufferUtilsTest {

    @Test
    public void parseOrderBookLevel_buy() {
        Buffer input = Buffer.buffer("{\"type\":\"l2update\",\"product_id\":\"BTC-USD\",\"changes\":[[\"buy\",\"54157.12\",\"0.01119600\"]],\"time\":\"2021-04-28T15:27:02.931174Z\"}");

        long[] output = new long[2];
        long expectedPrice = 5415712;
        long expectedQuantity = 1119600;
        int expectedIndexOut = 83;

        int indexOut = BufferUtils.parseOrderBookLevel(input, 60, output);

        Assert.assertEquals(expectedPrice, output[0]);
        Assert.assertEquals(expectedQuantity, output[1]);
        Assert.assertEquals(expectedIndexOut, indexOut);
    }

    @Test
    public void parseOrderBookLevel_sell() {
        Buffer input = Buffer.buffer("{\"type\":\"l2update\",\"product_id\":\"BTC-USD\",\"changes\":[[\"sell\",\"54157.12\",\"0.01119600\"]],\"time\":\"2021-04-28T15:27:02.931174Z\"}");

        long[] output = new long[2];
        long expectedPrice = 5415712;
        long expectedQuantity = 1119600;
        int expectedIndexOut = 84;

        int indexOut = BufferUtils.parseOrderBookLevel(input, 61, output);

        Assert.assertEquals(expectedPrice, output[0]);
        Assert.assertEquals(expectedQuantity, output[1]);
        Assert.assertEquals(expectedIndexOut, indexOut);
    }

    @Test
    public void testConvertBufferToLong() {
        Buffer input = Buffer.buffer("1345".getBytes());

        long expected = 1345;
        long actual = BufferUtils.convertBufferToLong(input, 0, 4);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertBufferToLong_withDecimal() {
        Buffer input = Buffer.buffer("13.45".getBytes());

        long expected = 1345;
        long actual = BufferUtils.convertBufferToLong(input, 0, 5);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParseNumber() {
        Buffer input = Buffer.buffer("\"123.45\"".getBytes());
        long[] output = new long[2];

        long expected = 12345;
        int expectedIndexOut = 7;
        int indexOut = BufferUtils.parseNumber(input, 1, output, 0);

        Assert.assertEquals(expected, output[0]);
        Assert.assertEquals(expectedIndexOut, indexOut);
    }

    @Test
    public void testParseNumber_secondOutputIndex() {
        Buffer input = Buffer.buffer("\"123.45\"".getBytes());
        long[] output = new long[2];

        long expected = 12345;
        int expectedIndexOut = 7;

        int indexOut = BufferUtils.parseNumber(input, 1, output, 1);

        Assert.assertEquals(expected, output[1]);
        Assert.assertEquals(expectedIndexOut, indexOut);
    }
}
