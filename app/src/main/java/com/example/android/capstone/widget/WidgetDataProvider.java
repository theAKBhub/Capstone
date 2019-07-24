package com.example.android.capstone.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.android.capstone.R;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.helper.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * WidgetDataProvider class acts as the adapter for the collection view widget, and provides
 * RemoteViews to the widget in the getViewAt method.
 * This class is similar to the Adapter class of a ListView.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext = null;
    private Cursor mCursor;
    private int mAppWidgetId;
    List<String> mCollection = new ArrayList<>();

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    // Called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();

        if (mCursor != null) {
            mCursor.close();
        }

        // select only first 5 tasks order by DUE DATE in ascending order
        mCursor = mContext.getContentResolver().query(TaskEntry.CONTENT_URI,
                new String[]{TaskEntry.PROJECTION_TASKS_LIST},
                TaskEntry.COLUMN_DATE_COMPLETED + " LIKE ''",
                null,
                TaskEntry.RES_COLUMN_TAG_DUE_DATE + " DESC, " + TaskEntry.COLUMN_DUE_DATE + " ASC LIMIT 5");

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    /**
     * Method that populates all the RemoteViews associated with the widget.
     * Functions similar to onBindViewHolder method in an Adapter.
     *
     * @return remote view
     */
    @Override
    public RemoteViews getViewAt(int position) {

        if (mCursor == null || mCursor.getCount() == 0) {
            return null;
        }
        mCursor.moveToPosition(position);

        int idIndex = mCursor.getColumnIndex(TaskEntry._ID);
        int titleIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int dateIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_DUE_DATE);

        long taskId = mCursor.getLong(idIndex);
        String taskTitle = mCursor.getString(titleIndex);

        // update widget
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_layout);
        view.setTextViewText(R.id.textview_widget_task, taskTitle);

        if (!Utils.isEmptyString(mCursor.getString(dateIndex))) {
            view.setTextViewText(R.id.textview_widget_date, Utils.getDisplayDate(mCursor.getString(dateIndex)));
        } else {
            view.setTextViewText(R.id.textview_widget_date, "");
        }

        // onClick PendingIntent Template using the specific task Id for each item individually
        Bundle extras = new Bundle();
        extras.putLong(Constants.INTENT_KEY_TASK_ID, taskId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        view.setOnClickFillInIntent(R.id.llayout_widget_task, fillInIntent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position; // TODO
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
