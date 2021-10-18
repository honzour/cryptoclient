package cz.honza.cryptoclient;

import android.app.Application;


import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bitfinex.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinmate.CoinmateExchange;
import org.knowm.xchange.gemini.v1.GeminiExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.poloniex.PoloniexExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cz.honza.cryptoclient.data.GetStockInfoResponse;
import cz.honza.cryptoclient.gui.MainUpdater;

public class CryptoClientApplication extends Application {
    private static CryptoClientApplication instance = null;


    public MainUpdater mainUpdater;
    private final List<Class<? extends BaseExchange>> STOCKS = new ArrayList<>();
    public int selectedStock = 0;
    public Map<String, GetStockInfoResponse> stockInfoResponseMap = new HashMap<>();


    private void initStocks() {
        STOCKS.add(BitstampExchange.class);
        STOCKS.add(CoinmateExchange.class);
        STOCKS.add(GeminiExchange.class);
        STOCKS.add(PoloniexExchange.class);
        STOCKS.add(BinanceExchange.class);
        STOCKS.add(BitstampExchange.class);
        STOCKS.add(BitfinexExchange.class);
        STOCKS.add(KrakenExchange.class);
        STOCKS.add(CoinbaseProExchange.class);
    }

    public static CryptoClientApplication getInstance() {
        return instance;
    }

    /**
     *
     * @param index zero based index from combobox
     * @param filter null if no filter
     * @return null (no IndexOutOfBoundException!) or found stock
     */
    public Class<? extends BaseExchange> getStock(int index, String filter) {
        if (index < 0 || index >= STOCKS.size()) {
            return null;
        }
        if (filter == null || "".equals(filter)) {
            return STOCKS.get(index);
        }
        Optional<Class<? extends BaseExchange>> found =
                STOCKS.stream().filter(stock -> stock.getSimpleName().contains(filter)).skip(index).findFirst();
        return found.orElse(null);
    }

    public List<Class<? extends BaseExchange>> getStocks(String filter) {
        if (filter == null || "".equals(filter)) {
            return STOCKS;
        }
        return STOCKS.stream().filter(stock -> stock.getSimpleName().contains(filter)).collect(Collectors.toList());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initStocks();
        instance = this;
    }
}