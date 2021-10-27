package cz.honza.kryptoklient.nologin;

import android.os.Handler;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cz.honza.kryptoklient.KryptoKlientApplication;
import cz.honza.kryptoklient.data.GetStockInfoResponse;
import cz.honza.kryptoklient.data.GetTickerResponse;

public class StockUpdater {

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
    public static void refreshStock(final Class<? extends BaseExchange> stockClass, boolean force) {
        if (stockClass == null) {
            KryptoKlientApplication.getInstance().mainUpdater.refreshStock();
            return;
        }
        final String stockName = stockClass.getSimpleName();
        if (!force) {

            final GetStockInfoResponse getStockInfoResponse = KryptoKlientApplication.getInstance().stockInfoResponseMap.get(stockName);
            if (getStockInfoResponse != null && getStockInfoResponse.isValid() && getStockInfoResponse.isFresh()) {
                KryptoKlientApplication.getInstance().mainUpdater.refreshStock();
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
                        KryptoKlientApplication.getInstance().stockInfoResponseMap.put(stockName, getStockInfoResponse);
                        KryptoKlientApplication.getInstance().mainUpdater.refreshStock();
                    }
                });
            }
        }.start();
    }

    /**
     * Run from the main thread
     */
    public static void refreshTicker(final Class<? extends BaseExchange> stockClass, CurrencyPair currencyPair, boolean force) {
        final GetStockInfoResponse stock = KryptoKlientApplication.getInstance().getStockInfo(stockClass);
        final GetTickerResponse getTickerResponseCache = stock.tickersMap.get(currencyPair);
        if (!force && getTickerResponseCache != null && getTickerResponseCache.isValid() && getTickerResponseCache.isFresh()) {
            KryptoKlientApplication.getInstance().mainUpdater.refreshTicker();
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
                        KryptoKlientApplication.getInstance().mainUpdater.refreshTicker();
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
