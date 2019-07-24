package com.example.android.capstone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import com.example.android.capstone.R;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.ui.TaskDetailActivity;
import com.example.android.capstone.ui.TaskListActivity;

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

        // click event handler for the title, launches Tasks List when the user clicks on title
        Intent titleIntent = new Intent(context, TaskListActivity.class);
        titleIntent.putExtra(Constants.INTENT_KEY_TASK_FILTER, 0);
        PendingIntent titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.textview_widget_header, titlePendingIntent);

        // click event handler for each list item on widget, launches Detail Activity with that Task
        Intent clickIntentTemplate = new Intent(context, TaskDetailActivity.class);
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.listview_widget, clickPendingIntentTemplate);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget);
    }

    /**
     * This method is called every 30 mins as specified on widgetinfo.xml, and also on device reboot
     *
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
