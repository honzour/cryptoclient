package cz.honza.cryptoclient.gui;

import cz.honza.cryptoclient.data.GetTickerResponse;

public interface MainUpdater {
    void refreshStock();
    void refreshTicker(GetTickerResponse getTickerResponse);
}
