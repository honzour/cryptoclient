package cz.honza.cryptoclient.gui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.knowm.xchange.BaseExchange;

import org.knowm.xchange.currency.CurrencyPair;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import cz.honza.cryptoclient.CryptoClientApplication;
import cz.honza.cryptoclient.R;
import cz.honza.cryptoclient.data.GetStockInfoResponse;
import cz.honza.cryptoclient.data.GetTickerResponse;
import cz.honza.cryptoclient.nologin.StockUpdater;

public class MainActivity extends Activity implements MainUpdater {

    private TextView mBidAsk;
    private Spinner mStocks;
    private Spinner mPairs;
    private View mRefresh;
    private EditText mStockFilter;
    private EditText mPairFilter;

    private GetStockInfoResponse getStockInfo() {
        int selected = mStocks.getSelectedItemPosition();
        Class<? extends BaseExchange> stockClass = CryptoClientApplication.getInstance().getStock(selected, mStockFilter.getText().toString());
        if (stockClass == null) {
            return null;
        }
        String simpleName = stockClass.getSimpleName();
        return CryptoClientApplication.getInstance().stockInfoResponseMap.get(simpleName);
    }

    private List<CurrencyPair> getPairs() {
        GetStockInfoResponse getStockInfoResponse = getStockInfo();
        if (getStockInfoResponse == null) {
            return Collections.emptyList();
        }
        return getStockInfoResponse.currencyPairs.stream()
               .filter(s -> s.toString().contains(mPairFilter.getText())).collect(Collectors.toList());
    }

    private CurrencyPair getPair() {
        List<CurrencyPair> pairs = getPairs();
        int pos = mPairs.getSelectedItemPosition();
        if (pos >= 0 && pos < pairs.size()) {
            return pairs.get(pos);
        }
        return null;
    }

    private List<String> getPairsString() {
        return getPairs().stream().map(p -> p.toString()).collect(Collectors.toList());
    }

    private void initPairs() {

        mPairs.setVisibility(View.VISIBLE);
        final List<String> pairsString = getPairsString();
        final ArrayAdapter adapter = adapterFromPairs(pairsString);
        mPairs.setAdapter(adapter);
        mPairs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GetStockInfoResponse getStockInfoResponse = getStockInfo();
                CurrencyPair currencyPair = getPair();
                StockUpdater.refreshTicker(getStockInfoResponse, currencyPair, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO
            }
        });
    }



    @Override
    public void refreshStock() {
        GetStockInfoResponse getStockInfoResponse = getStockInfo();
        if (getStockInfoResponse == null) {
            mPairs.setVisibility(View.GONE);
            mBidAsk.setText("");
            return;
        }
        if (!getStockInfoResponse.isValid()) {
            Toast.makeText(MainActivity.this, getStockInfoResponse.getError(), Toast.LENGTH_LONG).show();
            mPairs.setVisibility(View.GONE);
            mBidAsk.setText(getStockInfoResponse.getError());
            return;
        }
        initPairs();
    }

    private String formatDate(long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(ms));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(calendar.getTime());
    }

    @Override
    public void refreshTicker() {
        GetStockInfoResponse getStockInfoResponse = getStockInfo();

        CurrencyPair currencyPair = getPair();
        if (currencyPair == null) {
            return;
        }
        GetTickerResponse getTickerResponse = getStockInfoResponse.tickersMap.get(currencyPair);
        if (getTickerResponse == null) {
            mBidAsk.setText("");
            return;
        }
        if (!getTickerResponse.isValid()) {
            mBidAsk.setText(getTickerResponse.getError());
        } else {
            mBidAsk.setText(getResources().getText(R.string.bid).toString() + " = " +
                    getTickerResponse.ticker.getBid() +
                    ", " + getResources().getText(R.string.ask).toString() + " = " +
                    getTickerResponse.ticker.getAsk() + "\n" +
                    formatDate(getTickerResponse.created));
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
        mPairFilter = findViewById(R.id.main_filter_currency);
        mStockFilter = findViewById(R.id.main_filter_stock);
    }

    private void initStockFilter() {
        mStockFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                initStocks();
            }
        });
    }

    private void initPairFilter() {
        mPairFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                initPairs();
            }
        });
    }

    private void initStocks() {
        ArrayAdapter adapter = adapterFromStocks(CryptoClientApplication.getInstance().getStocks(mStockFilter.getText().toString()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStocks.setAdapter(adapter);
        mStocks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CryptoClientApplication.getInstance().selectedStock = i;
                StockUpdater.refreshStock(i, mStockFilter.getText().toString(), false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                CryptoClientApplication.getInstance().selectedStock = -1;
                StockUpdater.refreshStock(-1, null, false);
            }
        });
        mStocks.setSelection(CryptoClientApplication.getInstance().selectedStock);
        StockUpdater.refreshStock(mStocks.getSelectedItemPosition(), mStockFilter.getText().toString(), false);
    }

    private void initRefresh() {
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPairs.getVisibility() == View.VISIBLE) {
                    GetStockInfoResponse getStockInfoResponse = getStockInfo();
                    StockUpdater.refreshTicker(getStockInfoResponse, getPair(),true);

                } else {
                    StockUpdater.refreshStock(mStocks.getSelectedItemPosition(), mStockFilter.getText().toString(),true);
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
        initStockFilter();
        initPairFilter();
    }
}