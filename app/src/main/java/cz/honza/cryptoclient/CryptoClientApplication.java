package cz.honza.cryptoclient;

import android.app.Application;


import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bitflyer.BitflyerExchange;
import org.knowm.xchange.bitmex.BitmexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.cexio.CexIOExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinmate.CoinmateExchange;
import org.knowm.xchange.gemini.v1.GeminiExchange;
import org.knowm.xchange.hitbtc.v2.HitbtcExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kucoin.KucoinExchange;
import org.knowm.xchange.lgo.LgoExchange;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.simulated.SimulatedExchange;

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
    public Map<String, GetStockInfoResponse> stockInfoResponseMap = new HashMap<>();


    private void initStocks() {
        STOCKS.add(BinanceExchange.class);
        STOCKS.add(BitflyerExchange.class);
        STOCKS.add(BitmexExchange.class);
        STOCKS.add(BitstampExchange.class);
        STOCKS.add(BittrexExchange.class);
        STOCKS.add(CexIOExchange.class);
        STOCKS.add(CoinbaseProExchange.class);
        STOCKS.add(CoinmateExchange.class);
        STOCKS.add(GeminiExchange.class);
        STOCKS.add(HitbtcExchange.class);
        STOCKS.add(KrakenExchange.class);
        STOCKS.add(KucoinExchange.class);
        STOCKS.add(LgoExchange.class);
        STOCKS.add(PoloniexExchange.class);
        STOCKS.add(SimulatedExchange.class);
    }

    public static CryptoClientApplication getInstance() {
        return instance;
    }

    public GetStockInfoResponse getStockInfo(String stock) {
        return stockInfoResponseMap.get(stock);
    }

    public GetStockInfoResponse getStockInfo(Class<? extends BaseExchange> stock) {
        return getStockInfo(stock.getSimpleName());
    }

    /**
     *
     * @param index zero based index from combobox
     * @param filter null if no filter
     * @return null (no IndexOutOfBoundException!) or found stock
     * */

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