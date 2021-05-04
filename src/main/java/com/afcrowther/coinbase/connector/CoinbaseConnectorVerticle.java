package com.afcrowther.coinbase.connector;

import com.afcrowther.coinbase.connector.client.CoinbaseProClient;
import com.afcrowther.coinbase.connector.domain.coinbase.Product;
import com.afcrowther.coinbase.connector.exception.CoinbaseConnectionException;
import com.afcrowther.coinbase.connector.handlers.CoinbaseWebSocketMessageHandler;
import com.afcrowther.coinbase.connector.service.SimpleOrderBookAggregatorService;
import com.afcrowther.coinbase.connector.printer.ConsolePrinter;
import com.afcrowther.coinbase.connector.printer.Printer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.OpenSSLEngineOptions;

import static java.lang.String.format;

/**
 * This Verticle contains the logic around connecting to Coinbase. It performs the following steps:
 * <ul>
 *     <li>Do a check to see whether the market exists</li>
 *     <li>If valid, check that the market is online and currently open for trading</li>
 *     <li>If online and open for trading, connect to the start the WebSocket connection</li>
 * </ul>
 */
public class CoinbaseConnectorVerticle extends AbstractVerticle {

    private final String market;

    private CoinbaseWebSocketMessageHandler messageHandler;
    private HttpClient httpClient;
    private CoinbaseProClient coinbaseProClient;
    private WebSocket webSocket;

    public CoinbaseConnectorVerticle(String market) {
        this.market = market;
    }

    // only to be used for testing, live systems should use the vertx context provided in the class by default
    CoinbaseConnectorVerticle(String market, CoinbaseWebSocketMessageHandler messageHandler, Vertx vertx,
                              HttpClient httpClient, CoinbaseProClient client) {
        this.market = market;
        this.vertx = vertx;
        this.httpClient = httpClient;
        this.messageHandler = messageHandler;
        this.coinbaseProClient = client;
    }

    @Override
    public void start() {
        httpClient = vertx.createHttpClient(getHttpClientOptions());
        coinbaseProClient = new CoinbaseProClient(httpClient);
        connectToCoinbasePro();
    }

    @Override
    public void stop() {
        if (webSocket != null && !webSocket.isClosed()) {
            webSocket.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    protected void connectToCoinbasePro() {
        coinbaseProClient.getMarketInformation(market)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        Product marketInfo = ar.result();
                        if (marketInfo.getStatus().equals("offline")) {
                            throw new CoinbaseConnectionException(format("Market [%s] is currently offline, please try again later...", market));
                        } else if (marketInfo.isTradingDisabled()) {
                            throw new CoinbaseConnectionException(format("Trading on market [%s] is disabled, please try again later...", market));
                        } else {
                            // continue onto the WebSocket connection phase
                            connectToCoinbaseProWebSocket(marketInfo);
                        }
                    } else {
                        throw new CoinbaseConnectionException(ar.cause().getMessage());
                    }
                });
    }

    private void connectToCoinbaseProWebSocket(Product marketInfo) {
        if (messageHandler == null) {
            Printer printer = new ConsolePrinter(marketInfo);
            messageHandler = new CoinbaseWebSocketMessageHandler(new SimpleOrderBookAggregatorService(market), printer, vertx);
        }

        coinbaseProClient.subscribeToMarket(market, messageHandler)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        webSocket = ar.result();
                    } else {
                        throw new CoinbaseConnectionException(format("Failed to connect to Coinbase Pro WebSocket: [%s]", ar.cause().getMessage()));
                    }
                });
    }

    private HttpClientOptions getHttpClientOptions() {
        return new HttpClientOptions()
                // ssl by default
                .setSsl(true)
                // use tcnative boring-ssl, performance is much better than defaults
                .setOpenSslEngineOptions(new OpenSSLEngineOptions())
                // basic tcp tuning
                .setTcpNoDelay(true)
                .setTcpFastOpen(true)
                // 2MB - the initial snapshot from the l2 feed can be very large, around 800KB bytes on average for
                // the BTC-USD market, allow for some breathing room
                .setMaxWebSocketFrameSize(2000000)
                // some issues with setting up ssl connection to coinbase
                .setVerifyHost(false);
    }
}
