package com.example.android.capstone.ui;

import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.adapter.TaskCursorAdapter;
import com.example.android.capstone.alarm.AlarmNotificationService;
import com.example.android.capstone.alarm.AlarmSoundService;
import com.example.android.capstone.data.Task;
import com.example.android.capstone.data.TaskContract;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.helper.Utils;
import com.example.android.capstone.settings.SettingsActivity;
import com.example.android.capstone.widget.WidgetIntentService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;

public class TaskListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>, TaskCursorAdapter.ItemClickListener,
        TaskCursorAdapter.CheckClickListener {

    private static final String STATE_TASK_FILTER = "state_task_filter";
    private static final int TASK_LIST_LOADER_ID = 1;

    final Context mContext = this;
    private int mTaskFilterIndex;
    private List<String> mSpinnerList;
    private TaskCursorAdapter mTaskAdapter;
    private Task mTask;
    private Cursor mCursor;
    private Toast mToast;

    @BindView(R.id.clayout_tasklist)
    CoordinatorLayout mClayoutTasklist;
    @BindView(R.id.appbar_tasklist)
    AppBarLayout mAppbarTasklist;
    @BindView(R.id.toolbar_tasklist)
    Toolbar mToolbarTasklist;
    @BindView(R.id.spinner_toolbar_tasklist)
    Spinner mSpinnerToolbar;
    @BindView(R.id.textview_empty_state)
    TextView mTextViewEmptyState;
    @BindView(R.id.llayout_empty_state)
    LinearLayout mLlayoutEmptyState;
    @BindView(R.id.recyclerViewTasks)
    RecyclerView mRecyclerTasks;
    @BindView(R.id.fab_add_task)
    FloatingActionButton mFabAddTask;

    @BindString(R.string.error_save_msg)
    String mSaveErrorMessage;
    @BindString(R.string.info_save_success)
    String mSaveSuccessMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        ButterKnife.bind(this);

        // Receive Intent Extras
        if (savedInstanceState == null) {
            getIntentData();
        }

        // Prepare Toolbar widget
        setupToolbar();

        // Prepare Spinner
        setupSpinner();

        // Set up RecyclerView
        mTaskAdapter = new TaskCursorAdapter(this, this, this);
        mRecyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerTasks.setAdapter(mTaskAdapter);

        // Touch helper to the RecyclerView to recognize when a user swipes to delete an item
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                // get ID of the task to delete
                long id = (long) viewHolder.itemView.getTag();
                int position = viewHolder.getAdapterPosition();
                deleteTask(id);

                // Start IntentService to update widget
                WidgetIntentService.startActionUpdateWidgets(mContext);
            }
        }).attachToRecyclerView(mRecyclerTasks);

        // Launch EditActivity when FAB is clicked
        mFabAddTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, TaskEditActivity.class));
            }
        });

        // Initialize a loader
        getSupportLoaderManager().initLoader(TASK_LIST_LOADER_ID, null, this);

    }

    /**
     * Method invoked after this activity has been paused or restarted
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Restarts loader
        getSupportLoaderManager().restartLoader(TASK_LIST_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TASK_FILTER, mTaskFilterIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mTaskFilterIndex = savedInstanceState.getInt(STATE_TASK_FILTER);
        }
    }

    /**
     * Method to receive Intent Extra Data, which is the task filter
     */
    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mTaskFilterIndex = intent.getExtras().getInt(Constants.INTENT_KEY_TASK_FILTER);
            if (intent.hasExtra("action")) {
                String action = getIntent().getStringExtra("action");
                Log.d("XXX", action);
                if (action.equals("dismiss")) {
                    stopAlarm();
                }
            }
        }
    }

    /**
     * Method to customize the Toolbar widget
     */
    private void setupToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppbarTasklist.setElevation(4);
        }

        setSupportActionBar(mToolbarTasklist);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            getSupportActionBar().setTitle(getString(R.string.title_tasklist));
        } catch (NullPointerException ne) {
            Timber.e(ne.getMessage());
        }
    }

    /**
     * Method to set up Spinner in Toolbar
     */
    private void setupSpinner() {
        mSpinnerList = new ArrayList<>();
        mSpinnerList.add(Constants.HDG_TASKS_ALL);
        mSpinnerList.add(Constants.HDG_TASKS_PAST);
        mSpinnerList.add(Constants.HDG_TASKS_TODAY);
        mSpinnerList.add(Constants.HDG_TASKS_TOMORROW);
        mSpinnerList.add(Constants.HDG_TASKS_WEEK);
        mSpinnerList.add(Constants.HDG_TASKS_NODATE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, mSpinnerList);
        mSpinnerToolbar.setAdapter(adapter);
        mSpinnerToolbar.setSelection(mTaskFilterIndex, true);

        // Set OnItemSelected Listener for Spinner
        mSpinnerToolbar.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // set filter index based on spinner selection
        mTaskFilterIndex = position;

        // restarts loader
        getSupportLoaderManager().restartLoader(TASK_LIST_LOADER_ID, null, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private String getQuerySelectionClause() {

        String selectionClause = "(((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 0" +
                " OR " + TaskEntry.COLUMN_DATE_COMPLETED + " = '" + Utils.getDateToday() + "')";

        // Amend selection clause depending on task filter selected
        switch (mTaskFilterIndex) {
            case 1:
                // select tasks with past due date - with due date < TODAY
                selectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " < '" + Utils.getDateToday() + "'"
                        + " AND " + TaskEntry.COLUMN_DATE_COMPLETED + " == '' ))";
                break;

            case 2:
                // select tasks with due date = TODAY
                selectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " = '" + Utils.getDateToday() + "'))";
                break;

            case 3:
                // select tasks with due date = TOMORROW
                selectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " = '" + Utils.getDateTomorrow() + "'))";
                break;

            case 4:
                // select tasks for next week - with due date > TODAY and < 7 days from TODAY
                selectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " > '" + Utils.getDateToday() + "' AND " +
                        TaskEntry.COLUMN_DUE_DATE + " <= '" + Utils.getDateWeek() + "'))";
                break;

            case 5:
                // select tasks with no due date
                selectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " = ''))";
                break;

            case 0:
            default:
                selectionClause += "))";
                break;
        }

        return selectionClause;
    }

    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID
     * The loader returns task data as a Cursor or null if an error occurs.
     *
     * @param id - loader id
     * @param args - loader arguments
     * @return Loader
     */
    @SuppressWarnings("all")
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {

            // cursor that will hold all the task data
            Cursor mTaskData = null;

            // method called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    deliverResult(mTaskData); // Delivers any previously loaded data immediately
                } else {
                    forceLoad();  // Force a new load
                }
            }

            // method that performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(TaskEntry.CONTENT_URI,
                            new String[]{TaskEntry.PROJECTION_TASKS_LIST},
                            getQuerySelectionClause(),
                            null,
                            TaskEntry.RES_COLUMN_TAG_DUE_DATE + " DESC, " + TaskEntry.COLUMN_DUE_DATE
                    );

                } catch (Exception e) {
                    Timber.e(getString(R.string.error_fetch_data, e.getMessage()));
                    return null;
                }
            }

            // method to send the resultant cursor to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    /**
     * Called when a previously created loader has finished its load
     *
     * @param loader Loader that has finished
     * @param cursor data generated by the Loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update the data that the adapter uses to create ViewHolders
        if (cursor.getCount() > 0) {
            mLlayoutEmptyState.setVisibility(View.GONE);
            mTaskAdapter.swapCursor(cursor);
            mCursor = cursor;
        } else {
            mLlayoutEmptyState.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable
     *
     * @param loader Loader that is being reset
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTaskAdapter.swapCursor(null);
    }

    /**
     * Method to delete a task when the item has been swiped LEFT or RIGHT
     * @param id
     */
    private void deleteTask(long id) {
        // build URI with String row id appended
        String stringId = Long.toString(id);
        Uri uri = TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(stringId).build();

        // delete a single task using a ContentResolver
        int rowsDeleted = getContentResolver().delete(uri, null, null);

        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete
            Toast.makeText(this, getString(R.string.error_delete_failed), Toast.LENGTH_SHORT).show();
        } else {
            // restart the loader to re-query for all tasks after a deletion
            getSupportLoaderManager().restartLoader(TASK_LIST_LOADER_ID, null, TaskListActivity.this);

            Snackbar.make(mClayoutTasklist, getString(R.string.info_delete_success), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Interface Method to launch TaskDetailActivity with the item selected
     */
    @Override
    public void onItemClickListener(int itemId) {
        mCursor.moveToPosition(itemId);

        if (mCursor.getInt(mCursor.getColumnIndex(TaskEntry.COLUMN_TAG_COMPLETED)) == 1) {
            Utils.showToastMessage(mContext, mToast, getString(R.string.info_no_edit)).show();
        } else {

            mTask = Utils.getTaskObject(mCursor);

            ArrayList<Task> taskArrayList = new ArrayList<>();
            taskArrayList.add(mTask);

            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putParcelableArrayListExtra(Constants.INTENT_KEY_TASK, taskArrayList);
            startActivity(intent);
        }
    }

    /**
     * Interface method to handle task complete check onClick
     * @param itemId
     */
    @Override
    public void onCheckClickListener(int itemId) {
        mCursor.moveToPosition(itemId);
        int completeCheck = mCursor.getInt(mCursor.getColumnIndex(TaskEntry.COLUMN_TAG_COMPLETED));
        int tagRepeat = mCursor.getInt(mCursor.getColumnIndex(TaskEntry.COLUMN_TAG_REPEAT));

        ContentValues values = new ContentValues();

        switch (completeCheck) {
            case TaskEntry.TAG_NOT_COMPLETE:
                // mark task as complete
                values.put(TaskEntry.COLUMN_TAG_COMPLETED, TaskEntry.TAG_COMPLETE);
                values.put(TaskEntry.COLUMN_DATE_COMPLETED, Utils.getDateToday());

                // if it's a repeat task then insert another task based on repeat frequency
                if (tagRepeat == TaskEntry.TAG_REPEAT) {
                    addRepeatTask(itemId);
                }
                break;

            case TaskEntry.TAG_COMPLETE:
                // reset task
                values.put(TaskEntry.COLUMN_TAG_COMPLETED, TaskEntry.TAG_NOT_COMPLETE);
                values.put(TaskEntry.COLUMN_DATE_COMPLETED, "");
                break;
        }

        Uri uri = ContentUris
                .withAppendedId(TaskEntry.CONTENT_URI, mCursor.getLong(mCursor.getColumnIndex(TaskEntry._ID)));
        int numRowsUpdated = mContext.getContentResolver().update(uri, values, null, null);

        if ((numRowsUpdated > 0)) {
            // restart the loader to re-query for all tasks after a deletion
            getSupportLoaderManager().restartLoader(TASK_LIST_LOADER_ID, null, TaskListActivity.this);

            // Start IntentService to update widget
            WidgetIntentService.startActionUpdateWidgets(mContext);
        } else {
            Timber.e(mContext.getString(R.string.error_update_data));
        }
    }

    /**
     * Method to insert repeat task when the previous one has been completed
     */
    private void addRepeatTask(int itemId) {
        mCursor.moveToPosition(itemId);

        ContentValues values = new ContentValues();

        String repeatFrequency = mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_REPEAT_FREQUENCY));
        String dateDue = mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_DUE_DATE));
        String dateDueNew = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;

        try {
            date = formatter.parse(dateDue);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (Constants.TASK_REPEAT_DAILY.equals(repeatFrequency)) {
            dateDueNew = formatter.format(Utils.addDays(date, 1));
        } else if (Constants.TASK_REPEAT_WEEKLY.equals(repeatFrequency)) {
            dateDueNew = formatter.format(Utils.addDays(date, 7));
        } else if (Constants.TASK_REPEAT_MONTHLY.equals(repeatFrequency)) {
            dateDueNew = formatter.format(Utils.addDays(date, 30));
        }

        values.put(TaskEntry.COLUMN_TASK_TITLE, mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE)));
        values.put(TaskEntry.COLUMN_CATEGORY, mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_CATEGORY)));
        values.put(TaskEntry.COLUMN_PRIORITY, mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_PRIORITY)));
        values.put(TaskEntry.COLUMN_EXTRA_INFO_TYPE, mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_EXTRA_INFO_TYPE)));
        values.put(TaskEntry.COLUMN_EXTRA_INFO, mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_EXTRA_INFO)));
        values.put(TaskEntry.COLUMN_DUE_DATE, dateDueNew);
        values.put(TaskEntry.COLUMN_DUE_TIME, mCursor.getString(mCursor.getColumnIndex(TaskEntry.COLUMN_DUE_TIME)));
        values.put(TaskEntry.COLUMN_TAG_REPEAT, mCursor.getInt(mCursor.getColumnIndex(TaskEntry.COLUMN_TAG_REPEAT)));
        values.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, repeatFrequency);
        values.put(TaskEntry.COLUMN_TAG_COMPLETED, TaskEntry.TAG_NOT_COMPLETE);
        values.put(TaskEntry.COLUMN_DATE_ADDED, Utils.getDateToday());
        values.put(TaskEntry.COLUMN_DATE_COMPLETED, "");
        values.put(TaskEntry.COLUMN_DATE_UPDATED, Utils.getDateToday());

        Uri uri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, mSaveErrorMessage, Toast.LENGTH_SHORT).show();
        } else {
            WidgetIntentService.startActionUpdateWidgets(mContext);
            Toast.makeText(this, mSaveSuccessMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to inflate menu and add items to the action bar
     * @param menu
     * @return boolean flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Method to handle action bar item clicks
     * @param item
     * @return boolean flag
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void stopAlarm() {
        // Stop the Media Player Service to stop sound
        stopService(new Intent(this, AlarmSoundService.class));

        // remove the notification from notification tray
        NotificationManager nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(AlarmNotificationService.NOTIFICATION_ID);

        Toast.makeText(this, "Alarm stopped by User.", Toast.LENGTH_SHORT).show();
    }
}
