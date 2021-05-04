package com.afcrowther.coinbase.connector.service;

import com.afcrowther.coinbase.connector.domain.Side;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLongArray;

import static com.afcrowther.coinbase.connector.domain.Side.ASK;
import static com.afcrowther.coinbase.connector.domain.Side.BID;

public class SimpleOrderBookAggregatorService extends OrderBookAggregatorService {

    private final TreeMap<Long, Long> asks;
    private final TreeMap<Long, Long> bids;

    public SimpleOrderBookAggregatorService(String market) {
        super(market);
        // to allow for big market movements where levels that we want may be removed, we allocate 20 instead of 10 spaces
        this.asks = new TreeMap<>();
        this.bids = new TreeMap<>(Comparator.reverseOrder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean updateOrderBookLevel(Side side) {
        long price = priceQuantityArray[0];
        long quantity = priceQuantityArray[1];
        TreeMap<Long, Long> workingOn = side.equals(BID) ? bids : asks;
        boolean applied = false;
        if (quantity == 0) {
            // remove level
            if (workingOn.containsKey(price)) {
                workingOn.remove(price);
                applied = true;
            }
        } else  if (workingOn.containsKey(price)) {
            // we can replace a level that already exists
            workingOn.put(price, quantity);
            applied = true;
        } else if (workingOn.size() >= maxSize) {
            // we need to remove a level due to size constraints
            if (side.equals(BID) && bids.lastKey() < price) {
                workingOn.remove(bids.lastKey());
                workingOn.put(price, quantity);
                applied = true;
            } else if (side.equals(ASK) && asks.lastKey() > price) {
                workingOn.remove(asks.lastKey());
                workingOn.put(price, quantity);
                applied = true;
            }
        } else {
            workingOn.put(price, quantity);
            applied = true;
        }
        return applied;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyToOutputArray(AtomicLongArray out, Side side) {
        if (side == BID) {
            copyBids(out);
        } else {
            copyAsks(out);
        }
    }

    private void copyBids(AtomicLongArray out) {
        int idx = 0;
        for (Map.Entry<Long, Long> level : bids.entrySet()) {
            // use lazySet to take advantage of cpu store buffers
            out.lazySet(idx++, level.getKey());
            out.lazySet(idx++, level.getValue());
            if (idx == 20) {
                break;
            }
        }
        // fill the rest with empty levels, for example, in the case that we had 10 levels before and now we have 9, we
        // won't have got rid of the 10th yet at this point, so we need to handle these cases, -1 will denote an empty
        // level at the end of the array
        while (idx < 20) {
            out.lazySet(idx++, -1);
        }
    }

    private void copyAsks(AtomicLongArray out) {
        int idx = 19;
        for (Map.Entry<Long, Long> level : asks.entrySet()) {
            // use lazySet to take advantage of cpu store buffers
            out.lazySet(idx - 1, level.getKey());
            out.lazySet(idx--, level.getValue());
            idx--;
            if (idx == -1) {
                break;
            }
        }
        // fill the rest with empty levels, for example, in the case that we had 10 levels before and now we have 9, we
        // won't have got rid of the 10th yet at this point, so we need to handle these cases, -1 will denote an empty
        // level at the end of the array
        while (idx > -1) {
            out.lazySet(idx--, -1);
        }
    }
}
