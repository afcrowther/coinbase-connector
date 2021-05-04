package com.afcrowther.coinbase.connector.exception;

import static java.lang.String.format;

public class InvalidMarketException extends RuntimeException {

    public InvalidMarketException(String marketString) {
        super(format("Invalid market [%s] specified...", marketString));
    }
}
