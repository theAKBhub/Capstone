package com.example.android.capstone;

import android.app.Application;
import com.example.android.capstone.helper.Utils;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class BaseApplication extends Application {

    private static BaseApplication mApplicationInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationInstance = this;
        Timber.plant(new DebugTree());

        // Call Utility method for getting the necessary dates at startup
        Utils.setDates();
    }

    /**
     * Method that returns the application instance
     * @return ApplicationInstance
     */
    private static synchronized BaseApplication getApplicationInstance() {
        return mApplicationInstance;
    }

}
