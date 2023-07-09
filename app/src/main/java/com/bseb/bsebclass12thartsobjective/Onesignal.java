package com.bseb.bsebclass12thartsobjective;

import android.app.Application;

import com.onesignal.OneSignal;

public class Onesignal extends Application {

    private static final String ONESIGNAL_APP_ID = "2c6ef0e1-5ff0-42b8-acc8-3f3f024d4114";

    @Override
    public void onCreate() {
        super.onCreate();


// OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

    }
}
