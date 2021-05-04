package com.afcrowther.coinbase.connector.handlers;

import com.afcrowther.coinbase.connector.domain.coinbase.ErrorMessage;
import com.afcrowther.coinbase.connector.domain.coinbase.OrderBookSnapshot;
import com.afcrowther.coinbase.connector.exception.CoinbaseConnectionException;
import com.afcrowther.coinbase.connector.service.OrderBookAggregatorService;
import com.afcrowther.coinbase.connector.printer.Printer;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLongArray;

import static java.lang.String.format;

/**
 * This class handles the messages that are received from the Coinbase Pro WebSocket feed, specifically the l2update,
 * error and snapshot messages that can be returned (these are the only ones we expect from this connection).
 */
public class CoinbaseWebSocketMessageHandler implements Handler<Buffer> {

    private static final byte TWO = 50;
    private static final byte E = 101;
    private static final byte L = 108;
    private static final byte N = 110;
    private static final byte R = 114;
    private static final byte S = 115;

    private final OrderBookAggregatorService orderBookAggregatorService;
    private final Printer printer;
    private final Vertx vertx;
    private final Handler<Promise<Void>> printerHandler;
    private volatile Future<Void> printerFuture;
    // pre allocate the arrays we are using to move the current top 10 of the order book around
    private final AtomicLongArray bids;
    private final AtomicLongArray asks;

    public CoinbaseWebSocketMessageHandler(OrderBookAggregatorService orderBookAggregatorService, Printer printer, Vertx vertx) {
        this.orderBookAggregatorService = orderBookAggregatorService;
        this.printer = printer;
        this.vertx = vertx;
        this.printerHandler = printerHandler();
        this.printerFuture = Future.succeededFuture();
        // pre allocate the arrays we are going to use to move the prices and quantities we want around
        this.bids = new AtomicLongArray(20);
        this.asks = new AtomicLongArray(20);
    }

    /**
     * This handler always operates on the same thread, in a sense it is single threaded in nature, therefore we do not
     * have to worry about any cache contention or synchronization. However, on items we are sharing with the blocking
     * handler calls (which are handled by another thread), we need to apply some synchronization. We will also apply
     * single writer principle to the data that is shared, such that only one thread will be responsible for modifying
     * any piece of data to limit stalls in our cpu pipelines -
     * https://mechanical-sympathy.blogspot.com/2011/09/single-writer-principle.html
     *
     * @param buffer The incoming WebSocket message as bytes wrapped in a Vertx {@link Buffer} object
     */
    @Override
    public void handle(Buffer buffer) {
        // given the message structure '{"type":"[snapshot/l2update/error]' and the messages that we can expect
        // (https://docs.pro.coinbase.com/#channels), we can safely just match the first two letters of the message
        // type field to find out what type this message is
        byte first = buffer.getByte(9);
        byte second = buffer.getByte(10);
        if (first == E && second == R) {
            handleErrorMessage(buffer);
        } else if (first == S && second == N) {
            handleSnapshotMessage(buffer);
        } else if (first == L && second == TWO) {
            handleOrderBookUpdateMessage(buffer);
        }
    }

    private void handleOrderBookUpdateMessage(Buffer buffer) {
        boolean updated = orderBookAggregatorService.updateOrderBook(buffer, bids, asks, printerFuture);
        if (updated) {
            printerFuture = vertx.executeBlocking(printerHandler);
        }
    }

    // handle any error by throwing a runtime exception thus shutting the program, in future we could handle different
    // cases differently, e.g. have a graceful retry mechanism in the case of a recoverable error
    private void handleErrorMessage(Buffer buffer) {
        // we don't mind garbage if we're closing the program anyway
        ErrorMessage errorMessage = Json.decodeValue(buffer, ErrorMessage.class);
        throw new CoinbaseConnectionException(format(
                "Error received from Coinbase Pro WebSocket feed: [type: %s, message: %s]",
                errorMessage.getType(), errorMessage.getMessage()));
    }

    private void handleSnapshotMessage(Buffer buffer) {
        // we don't mind allocating for this message as although it is big, it is a one off and will be collected fairly
        // promptly and shouldn't ever reach further than eden space
        OrderBookSnapshot snapshot = Json.decodeValue(buffer, OrderBookSnapshot.class);
        System.out.println(buffer.toString(Charset.defaultCharset()));
        orderBookAggregatorService.updateOrderBook(snapshot, bids, asks);
        printerFuture = vertx.executeBlocking(printerHandler);
    }

    private Handler<Promise<Void>> printerHandler() {
        return promise -> {
            printer.printOrderBook(asks, bids);
            promise.complete();
        };
    }
}
