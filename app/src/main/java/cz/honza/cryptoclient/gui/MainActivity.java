package cz.honza.cryptoclient.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.knowm.xchange.BaseExchange;

import org.knowm.xchange.currency.CurrencyPair;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import cz.honza.cryptoclient.CryptoClientApplication;
import cz.honza.cryptoclient.R;
import cz.honza.cryptoclient.data.GetStockInfoResponse;
import cz.honza.cryptoclient.data.GetTickerResponse;

public class MainActivity extends Activity implements MainUpdater {

    private TextView mBidAsk;
    private Spinner mStocks;
    private Spinner mPairs;
    private View mRefresh;

    private void initPairs(GetStockInfoResponse getStockInfoResponse) {
        mPairs.setVisibility(View.VISIBLE);
        final List<String> pairsString = getStockInfoResponse.currencyPairs.stream().map(pair -> pair.toString()).collect(Collectors.toList());
        final ArrayAdapter adapter = adapterFromPairs(pairsString);

        mPairs.setAdapter(adapter);
        mPairs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String pair = pairsString.get(i);
                CurrencyPair currencyPair = getStockInfoResponse.currencyPairs.stream().filter(p -> p.toString().equals(pair)).findFirst().get();
                CryptoClientApplication.getInstance().refreshTicker(getStockInfoResponse, currencyPair, false);
                getStockInfoResponse.selectedPair = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                CryptoClientApplication.getInstance().refreshTicker(getStockInfoResponse, null, false);
                getStockInfoResponse.selectedPair = -1;
            }
        });
        mPairs.setSelection(getStockInfoResponse.selectedPair);
    }

    @Override
    public void refreshStock(String simpleName) {
        GetStockInfoResponse getStockInfoResponse = CryptoClientApplication.getInstance().stockInfoResponseMap.get(simpleName);
        if (!getStockInfoResponse.isValid()) {
            Toast.makeText(MainActivity.this, getStockInfoResponse.getError(), Toast.LENGTH_LONG).show();
            mPairs.setVisibility(View.GONE);
            mBidAsk.setText(getStockInfoResponse.getError());
            return;
        }
        initPairs(getStockInfoResponse);
    }

    private String formatDate(long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(ms));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(calendar.getTime());
    }

    @Override
    public void refreshTicker(GetTickerResponse getTickerResponse) {

        if (!getTickerResponse.isValid()) {
            mBidAsk.setText(getTickerResponse.getError());
        } else {
            mBidAsk.setText("Bid = " + getTickerResponse.ticker.getBid() +
                    ", Ask = " + getTickerResponse.ticker.getAsk() + "\n" + formatDate(getTickerResponse.created));
        }
    }


    protected ArrayAdapter adapterFromPairs(List<String> pairs) {

        ArrayAdapter adapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pairs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter adapterFromStocks(List<Class<? extends BaseExchange>> stocks) {

        ArrayAdapter adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                        stocks.stream().map(stock -> stock.getSimpleName()).collect(Collectors.toList()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void initFields() {
        setContentView(R.layout.activity_main);

        mBidAsk = findViewById(R.id.main_bid_ask);
        mStocks = findViewById(R.id.main_stock);
        mPairs = findViewById(R.id.main_pair);
        mRefresh = findViewById(R.id.main_refresh);
    }

    private void initStocks() {
        ArrayAdapter adapter = adapterFromStocks(CryptoClientApplication.getInstance().STOCKS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStocks.setAdapter(adapter);
        mStocks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CryptoClientApplication.getInstance().refreshStock(i, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                CryptoClientApplication.getInstance().refreshStock(-1, false);
            }
        });
        mStocks.setSelection(CryptoClientApplication.getInstance().selectedStock);
    }

    private void initRefresh() {
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPairs.getVisibility() == View.VISIBLE) {

                    String stock = CryptoClientApplication.getInstance().STOCKS.get(mStocks.getSelectedItemPosition()).getSimpleName();
                    GetStockInfoResponse getStockInfoResponse = CryptoClientApplication.getInstance().stockInfoResponseMap.get(stock);
                    CryptoClientApplication.getInstance().refreshTicker(
                            getStockInfoResponse, getStockInfoResponse.currencyPairs.get(getStockInfoResponse.selectedPair), true);

                } else {
                    CryptoClientApplication.getInstance().refreshStock(mStocks.getSelectedItemPosition(), true);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CryptoClientApplication.getInstance().mainUpdater = this;
        initFields();
        initStocks();
        initRefresh();
    }
}