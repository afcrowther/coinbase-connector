package com.afcrowther.coinbase.connector;

import com.afcrowther.coinbase.connector.printer.ConsolePrinter;
import com.afcrowther.coinbase.connector.printer.Printer;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class CoinbaseConnector {

    private static final String MARKET_STRING_REGEX = "[a-zA-Z]+-[a-zA-Z]+";
    private static final Pattern MARKET_STRING_REGEX_PATTERN = Pattern.compile(MARKET_STRING_REGEX);
    private static final String CLOSED_CONNECTOR = "Coinbase Connector closed...";
    private static final StringBuilder INITIALIZING_CONNECTOR = new StringBuilder("Initializing Coinbase Connector, for market: ");

    private final CLI cli;
    private final Printer printer;
    private Vertx vertx;
    private String verticleDeploymentId;

    public CoinbaseConnector(CLI cli, Printer printer) {
        this.cli = cli;
        this.printer = printer;
    }

    private void initialize(String[] args) {
        CommandLine commandLine = cli.parse(Arrays.asList(args));
        if (!commandLine.isValid() || commandLine.isAskingForHelp()) {
            printUsageAndExit();
        }

        String market = commandLine.getArgumentValue("market");
        printer.printLine(INITIALIZING_CONNECTOR.append(market));
        // do initial check on market to make sure it is properly formed
        if (!MARKET_STRING_REGEX_PATTERN.matcher(args[0]).matches()) {
            printer.printLine("The market provided must be in the '<Currency>-<Currency>' format, e.g. BTC-USD");
            printUsageAndExit();
        }

        VertxOptions vertxOptions = new VertxOptions()
                // only need one event loop thread, mainly used for the websocket messages and handler
                .setEventLoopPoolSize(1)
                // only need one worker thread, mainly used for the printing to console
                .setWorkerPoolSize(1)
                .setInternalBlockingPoolSize(2)
                // use epoll event loop group if available (has to be added explicitly to dependencies)
                .setPreferNativeTransport(true);
        vertx = Vertx.vertx(vertxOptions);
        // set the uncaught exception handler so that we can shut the program down nicely in case of unrecoverable error
        vertx.exceptionHandler(getExceptionHandler());

        verticleDeploymentId = vertx.deployVerticle(new CoinbaseConnectorVerticle(market)).result();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            printer.printLine(CLOSED_CONNECTOR);
            if (verticleDeploymentId != null) {
                vertx.undeploy(verticleDeploymentId).result();
            }
        }));
    }

    private void printUsageAndExit() {
        StringBuilder sb = new StringBuilder();
        cli.usage(sb);
        printer.printLine(sb);
        System.exit(1);
    }

    private static CLI getCli() {
        return CLI.create("Coinbase Connector")
                .setSummary(
                        "The Coinbase Connector will subscribe to the Coinbase Pro market provided in the command " +
                                "line argument, and print the top 10 bids and asks available on the order book for " +
                                "that market on an ongoing basis.")
                .addArgument(new Argument()
                        .setArgName("market")
                        .setIndex(0)
                        .setRequired(true)
                        .setDescription("The market for which you want to receive order book updates, e.g. 'BTC-USD'"))
                .addOption(new Option()
                        .setLongName("help").setShortName("h").setFlag(true).setHelp(true));
    }

    private Handler<Throwable> getExceptionHandler() {
        return th -> {
            StringBuilder sb = new StringBuilder();
            sb.append(format("Error encountered while connecting to Coinbase Pro, message: [%s]\n", th.getMessage()));
            sb.append("\nBeginning to shutdown the Coinbase Connector...");
            printer.printLine(sb);
            Future<Void> undeployFuture;
            if (verticleDeploymentId != null) {
                undeployFuture = vertx.undeploy(verticleDeploymentId);
            } else {
                undeployFuture = Future.succeededFuture();
            }
            undeployFuture.onComplete(result -> System.exit(1));
        };
    }

    public static void main(String[] args) {
        // initialize dependencies
        CLI cli = getCli();
        CoinbaseConnector coinbaseConnector = new CoinbaseConnector(cli, new ConsolePrinter());

        coinbaseConnector.initialize(args);
    }
}
