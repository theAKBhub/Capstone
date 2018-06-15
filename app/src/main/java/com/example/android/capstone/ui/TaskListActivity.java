package com.example.android.capstone.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.adapter.TaskCursorAdapter;
import com.example.android.capstone.data.TaskContract;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.helper.Utils;
import com.example.android.capstone.widget.WidgetIntentService;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class TaskListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_TASK_FILTER = "state_task_filter";
    private static final int TASK_LIST_LOADER_ID = 1;

    final Context mContext = this;
    private int mTaskFilterIndex;
    private List<String> mSpinnerList;
    private TaskCursorAdapter mTaskAdapter;
    private String mSelectionClause;

    @BindView(R.id.appbar_tasklist)
    AppBarLayout mAppbarTasklist;
    @BindView(R.id.toolbar_tasklist)
    Toolbar mToolbarTasklist;
    @BindView(R.id.spinner_toolbar_tasklist)
    Spinner mSpinnerToolbar;
    @BindView(R.id.textview_empty_state)
    TextView mTextViewEmptyState;
    @BindView(R.id.recyclerViewTasks)
    RecyclerView mRecyclerTasks;


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
        mTaskAdapter = new TaskCursorAdapter(this);
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

                // build URI with String row id appended
                String stringId = Long.toString(id);
                Uri uri = TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(stringId).build();

                // delete a single task using a ContentResolver
                getContentResolver().delete(uri, null, null);

                // restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(TASK_LIST_LOADER_ID, null, TaskListActivity.this);

                // Start IntentService to update widget
                WidgetIntentService.startActionUpdateWidgets(mContext);

            }
        }).attachToRecyclerView(mRecyclerTasks);

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
        if (getIntent().getExtras() != null) {
            mTaskFilterIndex = getIntent().getExtras().getInt(Constants.INTENT_KEY_TASK_FILTER);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinner, mSpinnerList);
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

        mSelectionClause = "((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 0" /*+
                TaskEntry.COLUMN_DATE_COMPLETED + " LIKE '" + Utils.getDateToday() + "'"*/;

        // set selection clause depending on task filter selected
        switch (mTaskFilterIndex) {
            case 1:
                mSelectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " < '" + Utils.getDateToday() + "'"
                        + " AND " + TaskEntry.COLUMN_DUE_DATE + " != '' ))";
                break;

            case 2:
                mSelectionClause += " AND " + TaskEntry.COLUMN_DUE_DATE + " = '" + Utils.getDateToday() + "'))";
                break;

            case 3:
                mSelectionClause = "((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 0 AND " +
                        TaskEntry.COLUMN_DUE_DATE + " = '" + Utils.getDateTomorrow() + "'))";
                break;

            case 4:
                mSelectionClause = "((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 0 AND " +
                        TaskEntry.COLUMN_DUE_DATE + " > '" + Utils.getDateToday() + "' AND " +
                        TaskEntry.COLUMN_DUE_DATE + " <= '" + Utils.getDateWeek() + "'))";
                break;

            case 5:
                mSelectionClause = "((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 0 AND " +
                        TaskEntry.COLUMN_DUE_DATE + " = ''))";
                break;

            case 0:
            default:
                mSelectionClause = "((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 0))";
                break;
        }


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
                            mSelectionClause,
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
        mTaskAdapter.swapCursor(cursor);
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

}
