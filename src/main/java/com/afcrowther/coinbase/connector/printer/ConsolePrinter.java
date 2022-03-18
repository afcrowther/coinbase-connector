package com.afcrowther.coinbase.connector.printer;

import com.afcrowther.coinbase.connector.domain.coinbase.Product;
import com.afcrowther.coinbase.connector.service.LongUtils;

import java.util.concurrent.atomic.AtomicLongArray;

public class ConsolePrinter implements Printer {

    private static final char TAB = '\t';
    private static final char NEW_LINE = '\n';
    private static final String TITLE_LINE = "Price \t\t\t Quantity";
    private static final String SPREAD = "Spread: ";
    // rudimentary attempt at making the
    private static final StringBuilder HEADER =
            new StringBuilder("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + TITLE_LINE + NEW_LINE);
    private static final int HEADER_LENGTH = HEADER.length();
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";

    private final int priceDecimals;
    private final int quantityDecimals;

    public ConsolePrinter() {
        this.priceDecimals = 0;
        this.quantityDecimals = 0;
    }

    public ConsolePrinter(Product marketInfo) {
        // get the number of decimal places for price and quantity
        this.priceDecimals = getNumberOfDecimalPlaces(marketInfo.getQuoteIncrement());
        this.quantityDecimals = getNumberOfDecimalPlaces(marketInfo.getBaseIncrement());
    }

    @Override
    public void printLine(String line) {
        System.out.println(line);
    }

    /**
     * This is an attempt at printing the current state of the order book (top 10 bid and ask levels) to the console.
     *
     * We need the priceDecimals and quantityDecimals arguments so that we can convert properly back from longs to the
     * String representation of the price and quantities, including the decimal places.
     *
     * This should not be called from more than one thread as it uses a shared StringBuilder object which is not safe
     * for concurrent modification.
     */
    @Override
    public void printOrderBook(AtomicLongArray asks, AtomicLongArray bids) {
        printLine(buildOrderBookStringRepresentation(asks, bids));
        clean();
    }

    protected void clean() {
        HEADER.delete(HEADER_LENGTH, HEADER.length());
    }

    protected StringBuilder buildOrderBookStringRepresentation(AtomicLongArray asks, AtomicLongArray bids) {
        long lowAsk = -1;
        long highBid = -1;
        HEADER.append(ANSI_RED);
        HEADER.append(NEW_LINE);
        for (int i = 0; i < asks.length(); i += 2) {
            if (asks.get(i) != -1) {
                LongUtils.appendLongToStringBuilder(asks.get(i), HEADER, priceDecimals);
                HEADER.append(TAB);
                HEADER.append(TAB);
                LongUtils.appendLongToStringBuilder(asks.get(i + 1), HEADER, quantityDecimals);
                HEADER.append(NEW_LINE);
                lowAsk = asks.get(i);
            }
        }
        HEADER.append(ANSI_RESET);
        HEADER.append(ANSI_GREEN);
        HEADER.append(NEW_LINE);
        for (int i = 0; i < bids.length(); i += 2) {
            if (bids.get(i) != -1) {
                LongUtils.appendLongToStringBuilder(bids.get(i), HEADER, priceDecimals);
                HEADER.append(TAB);
                HEADER.append(TAB);
                LongUtils.appendLongToStringBuilder(bids.get(i + 1), HEADER, quantityDecimals);
                HEADER.append(NEW_LINE);
                if (highBid == -1) {
                    highBid = bids.get(i);
                }
            }
        }
        HEADER.append(ANSI_RESET);
        HEADER.append(NEW_LINE);
        HEADER.append(NEW_LINE);
        HEADER.append(SPREAD);
        if (highBid != -1 && lowAsk != -1) {
            LongUtils.appendLongToStringBuilder(lowAsk - highBid, HEADER, priceDecimals);
        } else {
            HEADER.append("N/A");
        }
        HEADER.append(NEW_LINE);
        return HEADER;
    }

    /**
     * The following is the kind of expected format of the base and quote increments:
     * "base_increment": "0.00000001",
     * "quote_increment": "0.01000000"
     *
     * We have to return 8 for the first and 2 for the second in these cases.
     *
     * @param numberString
     * @return
     */
    protected int getNumberOfDecimalPlaces(String numberString) {
        int decimalIdx = numberString.indexOf('.');
        if (decimalIdx != -1) {
            int fromTheEnd = numberString.length() - 1 - decimalIdx;
            char[] chars = numberString.toCharArray();
            int trailingZeroes = 0;
            for (int i = chars.length - 1; i > 0; i--) {
                if (chars[i] == '0') {
                    trailingZeroes++;
                } else {
                    break;
                }
            }
            return fromTheEnd - trailingZeroes;
        }
        return 0;
    }
}
