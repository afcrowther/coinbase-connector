package com.afcrowther.coinbase.connector.domain.coinbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Arrays;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

/**
 * Models the snapshot of the order book that we receive at the start of a l2 order book subscription. Documentation
 * can be found here: https://docs.pro.coinbase.com/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_ABSENT)
public class OrderBookSnapshot {

    private String type;
    private String productId;
    private String[][] bids;
    private String[][] asks;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProductId() {
        return productId;
    }

    @JsonSetter("product_id")
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String[][] getBids() {
        return bids;
    }

    public void setBids(String[][] bids) {
        this.bids = bids;
    }

    public String[][] getAsks() {
        return asks;
    }

    public void setAsks(String[][] asks) {
        this.asks = asks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderBookSnapshot that = (OrderBookSnapshot) o;
        return Objects.equals(type, that.type) && Objects.equals(productId, that.productId) && Arrays.equals(bids, that.bids) && Arrays.equals(asks, that.asks);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, productId);
        result = 31 * result + Arrays.hashCode(bids);
        result = 31 * result + Arrays.hashCode(asks);
        return result;
    }
}
