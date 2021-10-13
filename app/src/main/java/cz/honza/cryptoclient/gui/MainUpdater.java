package cz.honza.cryptoclient.gui;

import cz.honza.cryptoclient.data.GetTickerResponse;

public interface MainUpdater {
    void refreshStock(String simpleName);
    void refreshTicker(GetTickerResponse getTickerResponse);
}
