package com.afcrowther.coinbase.connector.client;

import com.afcrowther.coinbase.connector.domain.coinbase.Product;
import com.afcrowther.coinbase.connector.handlers.CoinbaseWebSocketMessageHandler;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoinbaseProClientTest {

    private static final String MARKET = "BTC-USD";
    private static final Buffer COINBASE_PRODUCT_RESPONSE_BODY = Buffer.buffer(
            "{\"id\":\"BTC-USD\",\"base_currency\":\"BTC\",\"quote_currency\":\"USD\",\"base_min_size\":\"0.0001\"," +
                    "\"base_max_size\":\"280\",\"quote_increment\":\"0.01\",\"base_increment\":\"0.00000001\"," +
                    "\"display_name\":\"BTC/USD\",\"min_market_funds\":\"5\",\"max_market_funds\":\"1000000\"," +
                    "\"margin_enabled\":false,\"post_only\":false,\"limit_only\":false,\"cancel_only\":false," +
                    "\"trading_disabled\":false,\"status\":\"online\",\"status_message\":\"\"}");

    private static CoinbaseProClient UNDER_TEST;

    @Mock
    public HttpClient httpClient;
    @Mock
    public HttpClientRequest request;
    @Mock
    public HttpClientResponse response;
    @Mock
    public WebSocket webSocket;
    @Mock
    public CoinbaseWebSocketMessageHandler messageHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(CoinbaseProClientTest.class);
        UNDER_TEST = new CoinbaseProClient(httpClient);
    }

    @Test
    public void testGetMarketInformation() {
        when(httpClient.request(any(HttpMethod.class), anyInt(), anyString(), anyString())).thenReturn(Future.succeededFuture(request));
        when(request.putHeader(anyString(), anyString())).thenReturn(request);
        when(request.response()).thenReturn(Future.succeededFuture(response));
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(Future.succeededFuture(COINBASE_PRODUCT_RESPONSE_BODY));
        when(request.end()).thenReturn(Future.succeededFuture());

        Product expected = Json.decodeValue(COINBASE_PRODUCT_RESPONSE_BODY, Product.class);
        Product actual = UNDER_TEST.getMarketInformation(MARKET).result();

        Assert.assertEquals(expected, actual);
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("User-Agent", "Vertx-Java");
        verify(httpClient, times(1)).request(HttpMethod.GET, 443, "api.pro.coinbase.com", "/products/BTC-USD");
        verify(request, times(1)).putHeader("User-Agent", "Java-Vertx");
        verify(request, times(1)).response();
        verify(response, times(1)).statusCode();
        verify(response, times(1)).body();
        verify(request, times(1)).end();
    }

    @Test
    public void testSubscribeToMarket() {
        when(httpClient.webSocket(any(Integer.class), any(), any())).thenReturn(Future.succeededFuture(webSocket));
        when(webSocket.handler(any())).thenReturn(webSocket);
        when(webSocket.writeFinalTextFrame(any())).thenReturn(Future.succeededFuture(null));

        String subscriptionFrame = "{\"type\": \"subscribe\",\"product_ids\":[\"BTC-USD\"],\"channels\":[\"level2\"]}";

        WebSocket actual = UNDER_TEST.subscribeToMarket(MARKET, messageHandler).result();

        Assert.assertEquals(webSocket, actual);

        verify(httpClient, times(1)).webSocket(443,"ws-feed.pro.coinbase.com", "");
        verify(webSocket, times(1)).writeFinalTextFrame(subscriptionFrame);
        verify(webSocket, times(1)).handler(messageHandler);
    }
}