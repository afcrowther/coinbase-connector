package com.afcrowther.coinbase.connector.handlers;

import com.afcrowther.coinbase.connector.exception.CoinbaseConnectionException;
import com.afcrowther.coinbase.connector.printer.Printer;
import com.afcrowther.coinbase.connector.service.OrderBookAggregatorService;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoinbaseWebSocketMessageHandlerTest {

    private CoinbaseWebSocketMessageHandler UNDER_TEST;

    @Mock
    OrderBookAggregatorService orderBookAggregatorService;
    @Mock
    Printer printer;
    @Mock
    Vertx vertx;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(CoinbaseWebSocketMessageHandlerTest.class);
        UNDER_TEST = new CoinbaseWebSocketMessageHandler(orderBookAggregatorService, printer, vertx);
    }

    @Test(expected = CoinbaseConnectionException.class)
    public void testHandle_errorMessage() {
        String errorMessage = "{\"type\":\"error\",\"message\":\"error message\"}";
        UNDER_TEST.handle(Buffer.buffer(errorMessage));
    }

    @Test
    public void testHandle_snapshotMessage() {
        when(vertx.executeBlocking(any(Handler.class))).thenReturn(Future.succeededFuture());

        long[] arr = new long[20];
        Arrays.fill(arr, 0);
        AtomicLongArray array = new AtomicLongArray(arr);

        String snapshotMessage = "{\"type\":\"snapshot\",\"product_id\":\"BTC-USD\",\"bids\":[[\"10101.10\"," +
                "\"0.45054140\"]],\"asks\":[[\"10102.55\",\"0.57753524\"]]}";
        UNDER_TEST.handle(Buffer.buffer(snapshotMessage));

        verify(orderBookAggregatorService, times(1)).updateOrderBook(any(), any(), any());
        verify(vertx, times(1)).executeBlocking(any(Handler.class));
    }

    @Test
    public void testHandle_updateMessage() {
        when(vertx.executeBlocking(any(Handler.class))).thenReturn(Future.succeededFuture());
        when(orderBookAggregatorService.updateOrderBook(any(), any(), any(), any())).thenReturn(true);

        String updateMessage = "{\"type\":\"l2update\",\"product_id\":\"BTC-USD\",\"changes\":[[\"sell\"," +
                "\"54430.53\",\"0.00000000\"]],\"time\":\"2021-04-28T15:27:02.932274Z\"}";
        Buffer b = Buffer.buffer(updateMessage);
        UNDER_TEST.handle(b);

        verify(orderBookAggregatorService, times(1)).updateOrderBook(eq(b), any(), any(), any());
        verify(vertx, times(1)).executeBlocking(any(Handler.class));
    }

    @Test
    public void testHandle_updateMessage_noUpdates() {
        when(vertx.executeBlocking(any(Handler.class))).thenReturn(Future.succeededFuture());
        when(orderBookAggregatorService.updateOrderBook(any(), any(), any(), any())).thenReturn(false);

        String updateMessage = "{\"type\":\"l2update\",\"product_id\":\"BTC-USD\",\"changes\":[[\"sell\"," +
                "\"54430.53\",\"0.00000000\"]],\"time\":\"2021-04-28T15:27:02.932274Z\"}";
        Buffer b = Buffer.buffer(updateMessage);
        UNDER_TEST.handle(b);

        verify(orderBookAggregatorService, times(1)).updateOrderBook(eq(b), any(), any(), any());
        verify(vertx, times(0)).executeBlocking(any(Handler.class));
    }
}
