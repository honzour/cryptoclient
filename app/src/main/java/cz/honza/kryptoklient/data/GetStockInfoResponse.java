package cz.honza.kryptoklient.data;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetStockInfoResponse extends Response {
    public Exchange exchange;
    public List<CurrencyPair> currencyPairs;
    public Map<CurrencyPair, GetTickerResponse> tickersMap = new HashMap<>();

    public GetStockInfoResponse(Throwable throwable, Exchange exchange, List<CurrencyPair> currencyPairs) {
        super(throwable);
        this.exchange = exchange;
        this.currencyPairs = currencyPairs;
    }
}
