package cz.honza.cryptoclient.nologin;

import android.os.Handler;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cz.honza.cryptoclient.CryptoClientApplication;
import cz.honza.cryptoclient.data.GetStockInfoResponse;
import cz.honza.cryptoclient.data.GetTickerResponse;

public class StockUpdater {
    private static Class<? extends BaseExchange> getStockClass(int index) {
        if (index >= 0 && index < CryptoClientApplication.getInstance().STOCKS.size()) {
            return CryptoClientApplication.getInstance().STOCKS.get(index);
        } else {
            return null;
        }
    }

    private static GetStockInfoResponse getStock(Class<? extends BaseExchange> stockClass) {

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
    public static void refreshStock(final int index, boolean force) {
        CryptoClientApplication.getInstance().selectedStock = index;
        final Class<? extends BaseExchange> stockClass = getStockClass(index);
        final String stockName = stockClass.getSimpleName();
        if (!force) {

            final GetStockInfoResponse getStockInfoResponse = CryptoClientApplication.getInstance().stockInfoResponseMap.get(stockName);
            if (getStockInfoResponse != null && getStockInfoResponse.isValid() && getStockInfoResponse.isFresh()) {
                CryptoClientApplication.getInstance().mainUpdater.refreshStock(stockName);
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
                        CryptoClientApplication.getInstance().stockInfoResponseMap.put(stockName, getStockInfoResponse);
                        CryptoClientApplication.getInstance().mainUpdater.refreshStock(stockName);
                    }
                });
            }
        }.start();
    }

    /**
     * Run from the main thread
     */
    public static void refreshTicker(final GetStockInfoResponse stock, CurrencyPair currencyPair, boolean force) {

        final GetTickerResponse getTickerResponseCache = stock.tickersMap.get(currencyPair);
        if (!force && getTickerResponseCache != null && getTickerResponseCache.isValid() && getTickerResponseCache.isFresh()) {
            CryptoClientApplication.getInstance().mainUpdater.refreshTicker(getTickerResponseCache);
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
                        CryptoClientApplication.getInstance().mainUpdater.refreshTicker(getTickerResponse);
                    }
                });
            }
        }.start();
    }

    private static GetTickerResponse getTicker(Exchange stock, CurrencyPair currencyPair) {
        try {
            MarketDataService marketDataService = stock.getMarketDataService();
            return new GetTickerResponse(null, marketDataService.getTicker(currencyPair));
        } catch (Throwable t) {
            return new GetTickerResponse(t, null);
        }
    }
}
