package com.example.android.capstone.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.example.android.capstone.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WidgetIntentService extends IntentService {

    private static final String ACTION_UPDATE_WIDGET = "com.example.android.capstone.widget.action.UPDATE";
    private static final String EXTRA_TASK_ID = "com.example.android.capstone.widget.extra.TASK_ID";

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    /**
     * Starts this service to perform action UPDATE with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, WidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
                handleActionUpdateWidegt();
            }
        }
    }

    /**
     * Handle action UpdateWidget in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateWidegt() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetCollectionProvider.class));

        // Trigger widget data update by forcing a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget);

        // Time to update all widgets
        WidgetCollectionProvider.updateAppWidgets(this, appWidgetManager, appWidgetIds);
    }
}
