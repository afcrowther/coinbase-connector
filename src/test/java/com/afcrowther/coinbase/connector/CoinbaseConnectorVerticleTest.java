package com.afcrowther.coinbase.connector;

import com.afcrowther.coinbase.connector.client.CoinbaseProClient;
import com.afcrowther.coinbase.connector.domain.coinbase.Product;
import com.afcrowther.coinbase.connector.exception.CoinbaseConnectionException;
import com.afcrowther.coinbase.connector.handlers.CoinbaseWebSocketMessageHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoinbaseConnectorVerticleTest {

    private static final String MARKET = "BTC-USD";

    private static CoinbaseConnectorVerticle UNDER_TEST;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Mock
    public CoinbaseWebSocketMessageHandler messageHandler;
    @Mock
    public Vertx vertx;
    @Mock
    public HttpClient httpClient;
    @Mock
    public CoinbaseProClient coinbaseProClient;
    @Mock
    public WebSocket webSocket;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(CoinbaseConnectorVerticleTest.class);
        UNDER_TEST = new CoinbaseConnectorVerticle(MARKET, messageHandler, vertx, httpClient, coinbaseProClient);
    }

    @Test
    public void testConnectToCoinbasePro() {
        Product marketInfo = new Product();
        marketInfo.setStatus("online");
        marketInfo.setTradingDisabled(false);

        when(coinbaseProClient.getMarketInformation(MARKET)).thenReturn(Future.succeededFuture(marketInfo));
        when(coinbaseProClient.subscribeToMarket(MARKET, messageHandler)).thenReturn(Future.succeededFuture(webSocket));

        UNDER_TEST.connectToCoinbasePro();

        verify(coinbaseProClient, times(1)).getMarketInformation(MARKET);
        verify(coinbaseProClient, times(1)).subscribeToMarket(MARKET, messageHandler);
    }

    @Test
    public void testConnectToCoinbasePro_failedMarketInfoRequest() {
        when(coinbaseProClient.getMarketInformation(MARKET)).thenReturn(Future.failedFuture("Some error"));
        when(coinbaseProClient.subscribeToMarket(MARKET, messageHandler)).thenReturn(Future.succeededFuture(webSocket));
        exceptionRule.expect(CoinbaseConnectionException.class);

        UNDER_TEST.connectToCoinbasePro();

        verify(coinbaseProClient, times(1)).getMarketInformation(MARKET);
        verify(coinbaseProClient, times(0)).subscribeToMarket(MARKET, messageHandler);
    }

    @Test
    public void testConnectToCoinbasePro_marketOffline() {
        Product marketInfo = new Product();
        marketInfo.setStatus("offline");
        marketInfo.setTradingDisabled(false);

        when(coinbaseProClient.getMarketInformation(MARKET)).thenReturn(Future.succeededFuture(marketInfo));
        when(coinbaseProClient.subscribeToMarket(MARKET, messageHandler)).thenReturn(Future.succeededFuture(webSocket));
        exceptionRule.expect(CoinbaseConnectionException.class);
        exceptionRule.expectMessage("Market [BTC-USD] is currently offline, please try again later...");

        UNDER_TEST.connectToCoinbasePro();

        verify(coinbaseProClient, times(1)).getMarketInformation(MARKET);
        verify(coinbaseProClient, times(0)).subscribeToMarket(MARKET, messageHandler);
    }

    @Test
    public void testConnectToCoinbasePro_tradingDisabled() {
        Product marketInfo = new Product();
        marketInfo.setStatus("online");
        marketInfo.setTradingDisabled(true);

        when(coinbaseProClient.getMarketInformation(MARKET)).thenReturn(Future.succeededFuture(marketInfo));
        when(coinbaseProClient.subscribeToMarket(MARKET, messageHandler)).thenReturn(Future.succeededFuture(webSocket));
        exceptionRule.expect(CoinbaseConnectionException.class);
        exceptionRule.expectMessage("Trading on market [BTC-USD] is disabled, please try again later...");

        UNDER_TEST.connectToCoinbasePro();

        verify(coinbaseProClient, times(1)).getMarketInformation(MARKET);
        verify(coinbaseProClient, times(0)).subscribeToMarket(MARKET, messageHandler);
    }

    @Test
    public void testConnectToCoinbasePro_webSocketConnectionFailed() {
        Product marketInfo = new Product();
        marketInfo.setStatus("online");
        marketInfo.setTradingDisabled(false);

        when(coinbaseProClient.getMarketInformation(MARKET)).thenReturn(Future.succeededFuture(marketInfo));
        when(coinbaseProClient.subscribeToMarket(MARKET, messageHandler)).thenReturn(Future.failedFuture("Some error"));
        exceptionRule.expect(CoinbaseConnectionException.class);

        UNDER_TEST.connectToCoinbasePro();

        verify(coinbaseProClient, times(1)).getMarketInformation(MARKET);
        verify(coinbaseProClient, times(1)).subscribeToMarket(MARKET, messageHandler);
    }
}
