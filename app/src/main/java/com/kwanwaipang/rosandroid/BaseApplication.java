package com.kwanwaipang.rosandroid;

import android.app.Application;

import com.liphy.navigation.network.LiphyCloudManager;
import com.mapxus.map.mapxusmap.api.map.MapxusMapContext;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapxusMapContext.init(getApplicationContext());
        LiphyCloudManager.init(this);
    }
}
