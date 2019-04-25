package org.caonima;

import android.app.Application;

import org.caonima.service.DataCenterService;

public class CaoNiMaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataCenterService.startDataCenterService(this);
    }


}
