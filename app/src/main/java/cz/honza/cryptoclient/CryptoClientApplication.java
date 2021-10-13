package cz.honza.cryptoclient;

import android.app.Application;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bitfinex.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinmate.CoinmateExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.gemini.v1.GeminiExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.honza.cryptoclient.data.GetStockInfoResponse;
import cz.honza.cryptoclient.data.GetTickerResponse;
import cz.honza.cryptoclient.gui.MainActivity;

public class CryptoClientApplication extends Application {
    private static CryptoClientApplication instance = null;


    public MainActivity mainActivity;
    public final List<Class<? extends BaseExchange>> STOCKS = new ArrayList<>();
    public int selectedStock = 0;
    public Map<String, Integer> selectedPair = new HashMap<>();
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

    @Override
    public void onCreate() {
        super.onCreate();
        initStocks();
        instance = this;
    }

    private Class<? extends BaseExchange> getStockClass(int index) {
        if (index >= 0 && index < CryptoClientApplication.getInstance().STOCKS.size()) {
            return CryptoClientApplication.getInstance().STOCKS.get(index);
        } else {
            return null;
        }
    }

    private GetStockInfoResponse getStock(Class<? extends BaseExchange> stockClass) {

        try {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(stockClass);
            List<CurrencyPair> pairs = exchange.getExchangeSymbols().stream().sorted(Comparator.comparing(CurrencyPair::toString)).collect(Collectors.toList());
            return new GetStockInfoResponse(null, exchange, pairs);
        } catch (Throwable t) {
            return new GetStockInfoResponse(t, null, null);
        }
    }

    /**
     * Run from the main thread
     */
    public void refreshStock(final int index, boolean force) {
        selectedStock = index;
        final Class<? extends BaseExchange> stockClass = getStockClass(index);
        final String stockName = stockClass.getSimpleName();
        if (!force) {

            final GetStockInfoResponse getStockInfoResponse = stockInfoResponseMap.get(stockName);
            if (getStockInfoResponse != null && getStockInfoResponse.isValid() && getStockInfoResponse.isFresh()) {
                mainActivity.refreshStock(stockName);
                return;
            }
        }

        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                final GetStockInfoResponse getStockInfoResponse = getStock(stockClass);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stockInfoResponseMap.put(stockName, getStockInfoResponse);
                        mainActivity.refreshStock(stockName);
                    }
                });
            }
        }.start();
    }


    private GetTickerResponse getTicker(Exchange stock, CurrencyPair currencyPair) {
        try {
            MarketDataService marketDataService = stock.getMarketDataService();
            return new GetTickerResponse(null, marketDataService.getTicker(currencyPair));
        } catch (Throwable t) {
            return new GetTickerResponse(t, null);
        }
    }

    /**
     * Run from the main thread
     */
    public void refreshTicker(final GetStockInfoResponse stock, CurrencyPair currencyPair, boolean force) {

        final GetTickerResponse getTickerResponseCache = stock.tickersMap.get(currencyPair);
        if (!force && getTickerResponseCache != null && getTickerResponseCache.isValid() && getTickerResponseCache.isFresh()) {
            mainActivity.refreshTicker(getTickerResponseCache);
            return;
        }
        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {

                final GetTickerResponse getTickerResponse = getTicker(stock.exchange, currencyPair);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stock.tickersMap.put(currencyPair, getTickerResponse);
                        mainActivity.refreshTicker(getTickerResponse);
                    }
                });
            }
        }.start();
    }
}