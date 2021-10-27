package cz.honza.kryptoklient.data;

import org.knowm.xchange.dto.marketdata.Ticker;

public class GetTickerResponse extends Response {
    public Ticker ticker;

    public GetTickerResponse(Throwable throwable, Ticker ticker) {
        super(throwable);
        this.ticker = ticker;
    }
}
