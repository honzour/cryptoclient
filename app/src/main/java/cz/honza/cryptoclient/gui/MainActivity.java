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
import java.util.List;
import java.util.stream.Collectors;

import cz.honza.cryptoclient.CryptoClientApplication;
import cz.honza.cryptoclient.R;
import cz.honza.cryptoclient.data.GetStockInfoResponse;
import cz.honza.cryptoclient.data.GetTickerResponse;

public class MainActivity extends Activity {

    private TextView mBidAsk;
    private Spinner mStocks;
    private Spinner mPairs;

    public void refreshStock(String simpleName) {
        GetStockInfoResponse getStockInfoResponse = CryptoClientApplication.getInstance().stockInfoResponseMap.get(simpleName);
        if (!getStockInfoResponse.isValid()) {
            Toast.makeText(MainActivity.this, getStockInfoResponse.getError(), Toast.LENGTH_LONG).show();
            mPairs.setVisibility(View.GONE);
            mBidAsk.setText(getStockInfoResponse.getError());
            return;
        }

        mPairs.setVisibility(View.VISIBLE);
        final List<String> pairsString = getStockInfoResponse.currencyPairs.stream().map(pair -> pair.toString()).sorted().collect(Collectors.toList());
        final ArrayAdapter adapter = adapterFromPairs(pairsString);

        mPairs.setAdapter(adapter);
        mPairs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String pair = pairsString.get(i);
                CurrencyPair currencyPair = getStockInfoResponse.currencyPairs.stream().filter(p -> p.toString().equals(pair)).findFirst().get();
                CryptoClientApplication.getInstance().refreshTicker(getStockInfoResponse, currencyPair);
                getStockInfoResponse.selectedPair = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                CryptoClientApplication.getInstance().refreshTicker(getStockInfoResponse, null);
                getStockInfoResponse.selectedPair = -1;
            }
        });
        mPairs.setSelection(getStockInfoResponse.selectedPair);
    }

    public void refreshTicker(GetTickerResponse getTickerResponse) {

        if (!getTickerResponse.isValid()) {
            mBidAsk.setText(getTickerResponse.getError());
        } else {
            mBidAsk.setText("Bid = " + getTickerResponse.ticker.getBid() +
                    ", Ask = " + getTickerResponse.ticker.getAsk());
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CryptoClientApplication.getInstance().mainActivity = this;
        setContentView(R.layout.activity_main);

        mBidAsk = findViewById(R.id.bid_ask);
        mStocks = findViewById(R.id.main_stock);
        mPairs = findViewById(R.id.main_pair);

        ArrayAdapter adapter = adapterFromStocks(CryptoClientApplication.getInstance().STOCKS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStocks.setAdapter(adapter);
        mStocks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CryptoClientApplication.getInstance().refreshStock(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                CryptoClientApplication.getInstance().refreshStock(-1);
            }
        });
        mStocks.setSelection(CryptoClientApplication.getInstance().selectedStock);
    }
}