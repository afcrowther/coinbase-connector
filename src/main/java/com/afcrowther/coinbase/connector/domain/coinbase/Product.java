package com.afcrowther.coinbase.connector.domain.coinbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

/**
 * Models the "product" response from the Coinbase Pro Rest API. The attributes are basically the details of a
 * particular market, including it's current status (online or offline), details can be found here:
 * https://docs.pro.coinbase.com/#products
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_ABSENT)
public class Product {

    private String id;
    private String displayName;
    private String baseCurrency;
    private String quoteCurrency;
    private String baseIncrement;
    private String quoteIncrement;
    private String baseMinSize;
    private String minMarketFunds;
    private String maxMarketFunds;
    private String status;
    private String statusMessage;
    private boolean cancelOnly;
    private boolean limitOnly;
    private boolean postOnly;
    private boolean tradingDisabled;

    public Product() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonSetter("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    @JsonSetter("base_currency")
    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    @JsonSetter("quote_currency")
    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public String getBaseIncrement() {
        return baseIncrement;
    }

    @JsonSetter("base_increment")
    public void setBaseIncrement(String baseIncrement) {
        this.baseIncrement = baseIncrement;
    }

    public String getQuoteIncrement() {
        return quoteIncrement;
    }

    @JsonSetter("quote_increment")
    public void setQuoteIncrement(String quoteIncrement) {
        this.quoteIncrement = quoteIncrement;
    }

    public String getBaseMinSize() {
        return baseMinSize;
    }

    @JsonSetter("base_min_size")
    public void setBaseMinSize(String baseMinSize) {
        this.baseMinSize = baseMinSize;
    }

    public String getMinMarketFunds() {
        return minMarketFunds;
    }

    @JsonSetter("min_market_funds")
    public void setMinMarketFunds(String mixMarketFunds) {
        this.minMarketFunds = mixMarketFunds;
    }

    public String getMaxMarketFunds() {
        return maxMarketFunds;
    }

    @JsonSetter("max_market_funds")
    public void setMaxMarketFunds(String maxMarketFunds) {
        this.maxMarketFunds = maxMarketFunds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    @JsonSetter("status_message")
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean getCancelOnly() {
        return cancelOnly;
    }

    @JsonSetter("cancel_only")
    public void setCancelOnly(boolean cancelOnly) {
        this.cancelOnly = cancelOnly;
    }

    public boolean isLimitOnly() {
        return limitOnly;
    }

    @JsonSetter("limit_only")
    public void setLimitOnly(boolean limitOnly) {
        this.limitOnly = limitOnly;
    }

    public boolean isPostOnly() {
        return postOnly;
    }

    @JsonSetter("post_only")
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public boolean isTradingDisabled() {
        return tradingDisabled;
    }

    @JsonSetter("trading_disabled")
    public void setTradingDisabled(boolean tradingDisabled) {
        this.tradingDisabled = tradingDisabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return cancelOnly == product.cancelOnly && limitOnly == product.limitOnly && postOnly == product.postOnly &&
                tradingDisabled == product.tradingDisabled && Objects.equals(id, product.id) &&
                Objects.equals(displayName, product.displayName) &&
                Objects.equals(baseCurrency, product.baseCurrency) &&
                Objects.equals(quoteCurrency, product.quoteCurrency) &&
                Objects.equals(baseIncrement, product.baseIncrement) &&
                Objects.equals(quoteIncrement, product.quoteIncrement) &&
                Objects.equals(baseMinSize, product.baseMinSize) &&
                Objects.equals(minMarketFunds, product.minMarketFunds) &&
                Objects.equals(maxMarketFunds, product.maxMarketFunds) &&
                Objects.equals(status, product.status) && Objects.equals(statusMessage, product.statusMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, baseCurrency, quoteCurrency, baseIncrement, quoteIncrement, baseMinSize,
                minMarketFunds, maxMarketFunds, status, statusMessage, cancelOnly, limitOnly, postOnly,
                tradingDisabled);
    }
}
