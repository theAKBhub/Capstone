package com.example.android.capstone.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.adapter.DashboardGridAdapter;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.data.TaskDbHelper;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.helper.Utils;
import timber.log.Timber;

public class DashboardActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final Context mContext = this;
    private static final int TASK_LOADER_ID = 1;
    private static final int ALL_TASKS_FILTER_INDEX = 4;

    private int mCountAllTasks;
    private int mCountTasksPastDate;
    private int mCountTasksToday;
    private int mCountTasksTomorrow;
    private int mCountTasksNextWeek;
    private int mCountTasksNoDate;

    @BindView(R.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.gridview_task_counts)
    GridView mGridViewCounts;
    @BindView(R.id.progress_indicator)
    ProgressBar mProgressIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);

        // Prepare Toolbar widget
        setupToolbar();

        // Start the loader
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        // Set ItemClickListener on GridView items
        mGridViewCounts.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openTasksList(position);
            }
        });
    }

    /**
     * Method invoked after this activity has been paused or restarted
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Restarts all loader
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }

    /**
     * Method to customize the Toolbar widget
     */
    private void setupToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppbar.setElevation(4);
        }

        setSupportActionBar(mToolbar);

        try {
            getSupportActionBar().setTitle(getString(R.string.title_dashboard));
        } catch (NullPointerException ne) {
            Timber.e(ne.getMessage());
        }
    }

    /**
     * Method to prepare GridView for the Dashboard
     */
    private void setupGridView() {

        int[] counts = {mCountAllTasks, mCountTasksPastDate, mCountTasksToday, mCountTasksTomorrow,
                mCountTasksNextWeek, mCountTasksNoDate};
        String[] headings = {
                Constants.HDG_TASKS_ALL,
                Constants.HDG_TASKS_PAST,
                Constants.HDG_TASKS_TODAY,
                Constants.HDG_TASKS_TOMORROW,
                Constants.HDG_TASKS_WEEK,
                Constants.HDG_TASKS_NODATE
        };

        int actionBarHeight = 0;
        int marginSizeDp = 85; //including vertical spacing, and top and bottom margins
        int gridHeight;

        // Calculate GridView height
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float scale = metrics.density;
        int marginSizePx = Math.round(marginSizeDp * scale);

        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                    getResources().getDisplayMetrics());
        }

        gridHeight = metrics.heightPixels - marginSizePx - actionBarHeight;

        // set adapter to GridView
        mGridViewCounts.setAdapter(new DashboardGridAdapter(mContext, counts, headings, gridHeight));
    }

    private String getDashboardRawQuery() {
        return "SELECT"
                + " COUNT(*) AS " + TaskEntry.RES_COLUMN_COUNT_TASKS_ALL + ","
                + " SUM(CASE WHEN " + TaskEntry.COLUMN_DUE_DATE + " < '" + Utils.getDateToday() + "' THEN 1 ELSE 0 END) "
                        + "AS " + TaskEntry.RES_COLUMN_COUNT_TASKS_PAST + ","
                + " SUM(CASE WHEN " + TaskEntry.COLUMN_DUE_DATE + " = '" + Utils.getDateToday() + "' THEN 1 ELSE 0 END) "
                        + "AS " + TaskEntry.RES_COLUMN_COUNT_TASKS_TODAY + ","
                + " SUM(CASE WHEN " + TaskEntry.COLUMN_DUE_DATE + " = '" + Utils.getDateTomorrow() + "' THEN 1 ELSE 0 END) "
                        + "AS " + TaskEntry.RES_COLUMN_COUNT_TASKS_TOMORROW + ","
                + " SUM(CASE WHEN (" + TaskEntry.COLUMN_DUE_DATE + " > '" + Utils.getDateToday() + "' AND "
                        + TaskEntry.COLUMN_DUE_DATE + " <= '" + Utils.getDateWeek() + "') THEN 1 ELSE 0 END) "
                        + "AS " + TaskEntry.RES_COLUMN_COUNT_TASKS_WEEK + ","
                + " SUM(CASE WHEN " + TaskEntry.COLUMN_DUE_DATE + " = '' THEN 1 ELSE 0 END) "
                        + "AS " + TaskEntry.RES_COLUMN_COUNT_TASKS_NODATE
                        + " FROM " + TaskEntry.TABLE_NAME;
    }

    /**
     * Method to instantiate and return a new AsyncTaskLoader with the given ID.
     * This loader will return query result data as a Cursor if the query is successful and null otherwise.
     */
    @SuppressWarnings("all")
    @Override
    public Loader<Cursor> onCreateLoader(final int id, @Nullable Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor that will hold all the query result
            Cursor mTaskData = null;

            // This method is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    deliverResult(mTaskData); // delivers any previously loaded data immediately
                } else {
                    forceLoad(); // force a new load
                }
            }

            // This method performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {

                try {
                    TaskDbHelper dbHelper = new TaskDbHelper(getContext());
                    SQLiteDatabase sqLiteDBReadable = dbHelper.getReadableDatabase();
                    Cursor cursor = sqLiteDBReadable.rawQuery(getDashboardRawQuery(), null);

                    return cursor;

                } catch (Exception e) {
                    Timber.e(getString(R.string.error_fetch_data, e.getMessage()));
                    return null;
                }
            }

            // This method sends the Cursor holding the query result to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    /**
     * Method called when a previously created loader has finished its load
     *
     * @param loader The Loader that has finished.
     * @param cursor The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            mCountAllTasks = cursor.getInt(cursor.getColumnIndex(TaskEntry.RES_COLUMN_COUNT_TASKS_ALL));
            mCountTasksPastDate = cursor.getInt(cursor.getColumnIndex(TaskEntry.RES_COLUMN_COUNT_TASKS_PAST));
            mCountTasksToday = cursor.getInt(cursor.getColumnIndex(TaskEntry.RES_COLUMN_COUNT_TASKS_TODAY));
            mCountTasksTomorrow = cursor.getInt(cursor.getColumnIndex(TaskEntry.RES_COLUMN_COUNT_TASKS_TOMORROW));
            mCountTasksNextWeek = cursor.getInt(cursor.getColumnIndex(TaskEntry.RES_COLUMN_COUNT_TASKS_WEEK));
            mCountTasksNoDate = cursor.getInt(cursor.getColumnIndex(TaskEntry.RES_COLUMN_COUNT_TASKS_NODATE));
        }

        mProgressIndicator.setVisibility(View.GONE);
        setupGridView();
    }

    /**
     * Method called when a previously created loader is being reset, thus making its data unavailable.
     * It removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    /**
     * Method to launch Tasks List
     * @param taskFilterIndex
     */
    public void openTasksList(int taskFilterIndex) {
        Intent intent = new Intent(this, TaskListActivity.class);
        intent.putExtra(Constants.INTENT_KEY_TASK_FILTER, taskFilterIndex);
        startActivity(intent);
    }
}
