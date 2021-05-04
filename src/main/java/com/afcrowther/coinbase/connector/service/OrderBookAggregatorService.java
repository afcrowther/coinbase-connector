package com.afcrowther.coinbase.connector.service;

import com.afcrowther.coinbase.connector.domain.Side;
import com.afcrowther.coinbase.connector.domain.coinbase.OrderBookSnapshot;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.util.concurrent.atomic.AtomicLongArray;

import static com.afcrowther.coinbase.connector.domain.Side.ASK;
import static com.afcrowther.coinbase.connector.domain.Side.BID;
import static com.afcrowther.coinbase.connector.service.LongUtils.convertStringToLong;

/**
 * This class and all of it's subclasses are not thread safe, if you try using an instantiation of this object amongst
 * multiple threads you will not have a good time!
 *
 * This has been abstracted into an abstract class so that we can easily implement and test different backing data
 * structure implementations, so that we can pick the most efficient one for the patterns we see.
 */
public abstract class OrderBookAggregatorService {

    // represent characters we need as Latin 1 bytes
    private static final byte B = 98;
    private static final byte OPEN_SQUARE_BRACKET = 91;
    private static final byte CLOSE_SQUARE_BRACKET = 93;

    protected final int maxSize;
    // used to pass around the current price/quantity pair being worked on at any time
    protected final long[] priceQuantityArray;
    protected final String market;

    public OrderBookAggregatorService(String market) {
        // to allow for big market movements where levels that we want may be removed, we allocate 20 instead of 10
        // spaces on each side of the order book
        this.maxSize = 20;
        // pre allocate an array to pass order book levels around
        this.priceQuantityArray = new long[2];
        this.market = market;
    }

    /**
     * Updates the order book level using the level that can currently be found in priceQuantityArray variable.
     *
     * This method is not intended to be called from outside this class or it's subclasses.
     *
     * @param side The side of the order book update
     * @return Whether or not this update occurred at a level we are tracking
     */
    protected abstract boolean updateOrderBookLevel(Side side);

    /**
     * Copies the order book data to the output arrays provided.
     *
     * @param out  The output array for the order book
     * @param side Which side (bid or ask) of the order book we are copying
     */
    protected abstract void copyToOutputArray(AtomicLongArray out, Side side);

    /**
     * Updates the order book with the provided order book snapshot, this is only expected to happen once (at the start
     * of the websocket feed). The bidsOut and asksOut arrays will be filled with the top 10 levels before returning.
     *
     * @param snapshot The order book snapshot
     * @param bidsOut  The output array for the top 10 bids
     * @param asksOut  The output array for the top 10 asks
     */
    public void updateOrderBook(OrderBookSnapshot snapshot, AtomicLongArray bidsOut, AtomicLongArray asksOut) {
        String[][] asks = snapshot.getAsks();
        for (int i = 0; i < maxSize && i < asks.length; i++) {
            String[] level = asks[i];
            priceQuantityArray[0] = convertStringToLong(level[0]);
            priceQuantityArray[1] = convertStringToLong(level[1]);
            updateOrderBookLevel(ASK);
        }
        String[][] bids = snapshot.getBids();
        for (int i = 0; i < maxSize && i < bids.length; i++) {
            String[] level = bids[i];
            priceQuantityArray[0] = convertStringToLong(level[0]);
            priceQuantityArray[1] = convertStringToLong(level[1]);
            updateOrderBookLevel(BID);
        }
        copyToOutputArray(asksOut, ASK);
        copyToOutputArray(bidsOut, BID);
    }

    /**
     * <p>
     *     As we know the pattern of the requests coming in we can make some optimizations. The patten of each l2update
     *     message is as follows: {@code
     *     {"type":"l2update","product_id":"BTC-USD","changes":[["buy","10101.80000000","0.162567"]],"time":"2019-08-14T20:42:27.265Z"}
     *     }
     * </p>
     * <p>
     *     Using this, we can firstly skip to the point at which we expect to see changes, and then parse each level
     *     one by one without allocating. We will then pass a copy of the top 10 of each side of the order book back
     *     to the caller.
     * </p>
     * <p>
     *     The output arrays will only be copied to if there is a change to the section of the order book (aka within
     *     "maxSize" of the highest bid and lowest ask for the bid and ask output arrays respectively.
     * </p>
     *
     * @param buffer            The buffer containing the l2update message
     * @param bidsOut           The bid prices array to copy back to the caller, every even index will hold a price and
     *                          every odd index will hold it's associated quantity
     * @param asksOut           The bid quantities array to copy back to the caller, every even index will hold a price
     *                          and every odd index will hold it's associated quantity
     * @param outputFree        A future that dictates whether or not the previous output operation (in this case
     *                          printing is complete, we shouldn't touch the output array(s) unless it is ready.
     * @return Whether or not any changes have been made to the output arrays
     */
    public boolean updateOrderBook(Buffer buffer, AtomicLongArray bidsOut, AtomicLongArray asksOut,
                                   Future<Void> outputFree) {
        boolean finished = false;
        boolean asksChangeMade = false;
        boolean bidsChangeMade = false;
        // start at the first open square bracket of the first element of the changes array
        int currentIndex = 46 + market.length();
        while (!finished) {
            byte next = buffer.getByte(currentIndex);
            if (next == CLOSE_SQUARE_BRACKET) {
                finished = true;
            } else if (next == OPEN_SQUARE_BRACKET) {
                // used to see if we have a buy or sell
                currentIndex += 2;
                boolean isBid = buffer.getByte(currentIndex) == B;
                if (isBid) {
                    currentIndex += 5;
                    currentIndex = BufferUtils.parseOrderBookLevel(buffer, currentIndex, priceQuantityArray);
                    bidsChangeMade = updateOrderBookLevel(BID) || bidsChangeMade;
                } else {
                    currentIndex += 6;
                    currentIndex = BufferUtils.parseOrderBookLevel(buffer, currentIndex, priceQuantityArray);
                    asksChangeMade = updateOrderBookLevel(ASK) || asksChangeMade;
                }
            }
            // ignore anything else (such as commas)
            currentIndex++;
        }
        if (outputFree.isComplete()) {
            // we can skip copying one of the arrays if there have been no changes to it
            if (asksChangeMade) {
                copyToOutputArray(asksOut, ASK);
            }
            if (bidsChangeMade) {
                copyToOutputArray(bidsOut, BID);
            }
        }

        return asksChangeMade || bidsChangeMade;
    }
}
