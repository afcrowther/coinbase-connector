package com.afcrowther.coinbase.connector.service;

import com.afcrowther.coinbase.connector.domain.coinbase.OrderBookSnapshot;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLongArray;

import static org.mockito.Mockito.*;

public class SimpleOrderBookAggregatorServiceTest {

    private final AtomicLongArray bidsOutput = new AtomicLongArray(20);
    private final AtomicLongArray asksOutput = new AtomicLongArray(20);

    private SimpleOrderBookAggregatorService UNDER_TEST;

    @Before
    public void setup() {
        UNDER_TEST = new SimpleOrderBookAggregatorService("ETH-USD");
    }

    @Test
    public void testUpdateOrderBook_snapshot() throws Exception {
        String snapshotString = getResourceAsString("data/snapshot.txt");
        OrderBookSnapshot snapshot = Json.decodeValue(snapshotString, OrderBookSnapshot.class);

        long[] bidsExpected = new long[] { 340249, 47440000, 340248, 1137592625, 340227, 290000000, 340226, 1720880000,
                340214, 1590000000, 340213, 167000000, 340212, 167000000, 340203, 66034179, 340199, 55413887, 340193,
                56104752 };
        long[] asksExpected = new long[] { 340466, 353000000, 340453, 58440224, 340450, 113800000, 340432, 79370000,
                340415, 94679443, 340410, 100000000, 340398, 15000000, 340392, 113800000, 340379, 147168432, 340366,
                247908992 };
        UNDER_TEST.updateOrderBook(snapshot, bidsOutput, asksOutput);

        Assert.assertArrayEquals(bidsExpected, toArray(bidsOutput));
        Assert.assertArrayEquals(asksExpected, toArray(asksOutput));
    }

    @Test
    public void testUpdateOrderBook_update() throws Exception {
        String snapshotString = getResourceAsString("data/snapshot.txt");
        OrderBookSnapshot snapshot = Json.decodeValue(snapshotString, OrderBookSnapshot.class);

        UNDER_TEST.updateOrderBook(snapshot, bidsOutput, asksOutput);

        long[] bidsExpected = new long[] { 340249, 4500000, 340248, 1137592625, 340227, 290000000, 340226, 1720880000,
                340214, 1590000000, 340213, 167000000, 340212, 167000000, 340203, 66034179, 340199, 55413887, 340193,
                56104752 };
        long[] asksExpected = new long[] { 340453, 58440224, 340450, 113800000, 340432, 79370000,
                340415, 94679443, 340410, 100000000, 340398, 15000000, 340392, 113800000, 340379, 147168432, 340366,
                247908992, 340358, 120000000 };

        String updates = "{\"type\":\"l2update\",\"product_id\":\"ETH-USD\",\"changes\":[[\"sell\",\"3403.58\",\"1.20000000\"], [\"buy\",\"3402.49\",\"0.04500000\"]],\"time\":\"2021-04-28T15:27:02.721425Z\"}";

        boolean updated = UNDER_TEST.updateOrderBook(Buffer.buffer(updates), bidsOutput, asksOutput, Future.succeededFuture());

        Assert.assertTrue(updated);
        Assert.assertArrayEquals(bidsExpected, toArray(bidsOutput));
        Assert.assertArrayEquals(asksExpected, toArray(asksOutput));
    }

    @Test
    public void testUpdateOrderBook_updateRemovesLevels() throws Exception {
        String snapshotString = getResourceAsString("data/snapshot.txt");
        OrderBookSnapshot snapshot = Json.decodeValue(snapshotString, OrderBookSnapshot.class);

        UNDER_TEST.updateOrderBook(snapshot, bidsOutput, asksOutput);

        long[] bidsExpected = new long[] { 340248, 1137592625, 340227, 290000000, 340226, 1720880000,
                340214, 1590000000, 340213, 167000000, 340212, 167000000, 340203, 66034179, 340199, 55413887, 340193,
                56104752, 340184, 321133965 };
        long[] asksExpected = new long[] { 340476, 111970550, 340466, 353000000, 340453, 58440224, 340450, 113800000, 340432, 79370000,
                340415, 94679443, 340410, 100000000, 340398, 15000000, 340392, 113800000, 340379, 147168432 };

        String updates = "{\"type\":\"l2update\",\"product_id\":\"ETH-USD\",\"changes\":[[\"sell\",\"3403.66\",\"0.00000000\"], [\"buy\",\"3402.49\",\"0.00000000\"]],\"time\":\"2021-04-28T15:27:02.721425Z\"}";

        boolean updated = UNDER_TEST.updateOrderBook(Buffer.buffer(updates), bidsOutput, asksOutput, Future.succeededFuture());

        Assert.assertTrue(updated);
        Assert.assertArrayEquals(bidsExpected, toArray(bidsOutput));
        Assert.assertArrayEquals(asksExpected, toArray(asksOutput));
    }

    @Test
    public void testUpdateOrderBook_updateOutOfMarket() throws Exception {
        String snapshotString = getResourceAsString("data/snapshot.txt");
        OrderBookSnapshot snapshot = Json.decodeValue(snapshotString, OrderBookSnapshot.class);

        UNDER_TEST.updateOrderBook(snapshot, bidsOutput, asksOutput);
        AtomicLongArray bidsOut = mock(AtomicLongArray.class);
        AtomicLongArray asksOut = mock(AtomicLongArray.class);

        String updates = "{\"type\":\"l2update\",\"product_id\":\"ETH-USD\",\"changes\":[[\"sell\",\"34030.66\",\"0.00000000\"], [\"buy\",\"1.49\",\"0.00000000\"]],\"time\":\"2021-04-28T15:27:02.721425Z\"}";

        boolean updated = UNDER_TEST.updateOrderBook(Buffer.buffer(updates), bidsOutput, asksOutput, Future.succeededFuture());

        Assert.assertFalse(updated);
        // should have made no changes to the output arrays
        verifyNoMoreInteractions(bidsOut);
        verifyNoMoreInteractions(asksOut);
    }

    private String getResourceAsString(String resource) throws Exception {
        URL resourceUrl = this.getClass().getClassLoader().getResource(resource);
        return Files.readString(Path.of(resourceUrl.toURI()));
    }

    private long[] toArray(AtomicLongArray arr) {
        long[] out = new long[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            out[i] = arr.get(i);
        }
        return out;
    }
}
