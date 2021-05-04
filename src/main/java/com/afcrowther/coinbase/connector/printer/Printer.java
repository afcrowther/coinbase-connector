package com.afcrowther.coinbase.connector.printer;

import java.util.concurrent.atomic.AtomicLongArray;

public interface Printer {

    void printLine(String line);

    default void printLine(StringBuilder lines) {
        printLine(lines.toString());
    }

    void printOrderBook(AtomicLongArray asks, AtomicLongArray bids);
}
