package com.afcrowther.coinbase.connector;

public class Main {

    public static void main(String[] args) {
        try {
            if (args.length < 0) {
                getUsageString();
            }
            // do argument validation
            // create and configure vertx
            // create and configure verticle
            // start verticle
        } catch (Exception e) {

        } finally {
            // close verticle
        }
    }

    private static String getUsageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\n");
        sb.append("java -jar CoinbaseConnector.jar <market-name>");
        sb.append("  e.g. java -jar CoinbaseConnector BTC_USD");
        return sb.toString();
    }
}
