package com.afcrowther.coinbase.connector.exception;

public class CoinbaseConnectionException extends RuntimeException {

    public CoinbaseConnectionException(String message) {
        super(message);
    }
}
