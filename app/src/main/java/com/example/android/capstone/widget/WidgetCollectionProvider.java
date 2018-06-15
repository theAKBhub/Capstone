package com.example.android.capstone.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.android.capstone.R;

/**
 * This class controls the functionality of the widget
 */
public class WidgetCollectionProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Set layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Get an Intent to RemoteViews service needed to provide adapter for CollectionWidget
        Intent serviceIntent = new Intent(context, WidgetRemoteService.class);

        // Pass app widget id to the Intent
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        // Set Adapter to RemoteViews for the CollectionWidget
        remoteViews.setRemoteAdapter(appWidgetId, R.id.listview_widget, serviceIntent);

        // Set an empty view in case of no data
        remoteViews.setEmptyView(R.id.listview_widget, R.id.textview_empty_widget);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

    }

    /**
     * This method is called every 30 mins as specified on widgetinfo.xml, and also on device reboot
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds - ids of multiple instances of the widget
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetIntentService.startActionUpdateWidgets(context);
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        // There may be multiple instances of the widget so update all
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}
