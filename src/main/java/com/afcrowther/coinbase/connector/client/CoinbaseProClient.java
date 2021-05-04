package com.afcrowther.coinbase.connector.client;

import com.afcrowther.coinbase.connector.domain.coinbase.Product;
import com.afcrowther.coinbase.connector.exception.CoinbaseConnectionException;
import com.afcrowther.coinbase.connector.exception.InvalidMarketException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;

import static java.lang.String.format;

public class CoinbaseProClient {

    private static final String COINBASE_PRO_WEBSOCKET_HOST = "ws-feed.pro.coinbase.com";
    private static final String COINBASE_PRO_REST_HOST = "api.pro.coinbase.com";
    private static final String COINBASE_PRO_PRODUCTS_REQUEST_URI = "/products/%s";
    private static final int COINBASE_PRO_PORT = 443;
    private static final String COINBASE_PRO_SUBSCRIPTION_REQUEST_STRING =
            "{\"type\": \"subscribe\",\"product_ids\":[\"%s\"],\"channels\":[\"level2\"]}";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USET_AGENT_VALUE = "Java-Vertx";

    private final HttpClient httpClient;

    public CoinbaseProClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns a {@link Product} object with the details of the market that is provided.
     *
     * @param market The market that we want the details of
     * @return A complete product object with details of the market requested
     */
    public Future<Product> getMarketInformation(String market) {
        String requestUriWithMarket = format(COINBASE_PRO_PRODUCTS_REQUEST_URI, market);
        Future<HttpClientRequest> requestFuture =
                httpClient.request(HttpMethod.GET, COINBASE_PRO_PORT, COINBASE_PRO_REST_HOST, requestUriWithMarket);
        return requestFuture.flatMap(request -> {
            Future<Product> productFuture = request
                    .putHeader(USER_AGENT_HEADER, USET_AGENT_VALUE)
                    .response()
                    .flatMap(response -> {
                        int status = response.statusCode();
                        if (status == 404) {
                            throw new InvalidMarketException(market);
                        } else if (status != 200) {
                            throw new CoinbaseConnectionException(format(
                                    "Error retrieving the details of the market [%s] from Coinbase, status code " +
                                            "received from coinbase was [%s]", market, response.statusCode()));
                        } else {
                            return response.body().map(buffer -> Json.decodeValue(buffer, Product.class));
                        }
                    });
            request.end();
            return productFuture;
        });
    }

    /**
     * Subscribes to the provided market and registers the handler provided to receive incoming WebSocket messages.
     *
     * @param market            The market to subscribe to, expected in "BTC-USD" format.
     * @param messageHandler    The {@link Handler<Buffer> that will handle incoming messages}
     * @return The WebSocket that has been initialized
     */
    public Future<WebSocket> subscribeToMarket(String market, Handler<Buffer> messageHandler) {
        return httpClient.webSocket(COINBASE_PRO_PORT, COINBASE_PRO_WEBSOCKET_HOST, "")
                .map(webSocket -> {
                    webSocket.handler(messageHandler)
                            .writeFinalTextFrame(format(COINBASE_PRO_SUBSCRIPTION_REQUEST_STRING, market));
                    return webSocket;
                });
    }
}