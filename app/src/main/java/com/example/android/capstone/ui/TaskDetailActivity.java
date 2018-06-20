package com.example.android.capstone.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.data.Task;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.helper.Utils;
import java.util.ArrayList;

public class TaskDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int TASK_LOADER_ID = 1;
    private static final String BASE_URL_SEARCH = "http://www.google.com/search?q=";

    final Context mContext = this;
    private Task mTask;
    private String mGeoLocation;
    private String mAddress;
    private long mTaskId;
    private Uri mUri;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppbarLayout;
    @BindView(R.id.toolbar_detail_activity)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.textview_title)
    TextView mTextViewTitle;
    @BindView(R.id.textview_category)
    TextView mTextViewCategory;
    @BindView(R.id.textview_priority)
    TextView mTextViewPriority;
    @BindView(R.id.textview_date)
    TextView mTextViewDate;
    @BindView(R.id.textview_time)
    TextView mTextViewTime;
    @BindView(R.id.textview_repeat)
    TextView mTextViewRepeat;
    @BindView(R.id.textview_extra)
    TextView mTextViewExtra;

    @BindView(R.id.button_extra)
    Button mButtonExtra;

    @BindView(R.id.fab_edit_task)
    FloatingActionButton mFabEditTask;

    @BindString(R.string.default_no_data)
    String mNoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        ButterKnife.bind(this);

        // Prepare toolbar
        setupToolbar();

        // Receive Intent Data
        if (getIntent().getExtras() != null) {

            Intent intent = getIntent();

            if (intent.hasExtra(Constants.INTENT_KEY_TASK)) {
                // activity opened from TaskListActivity so all task details retrieved via Parcelable object
                ArrayList<Task> tasks = getIntent().getParcelableArrayListExtra(Constants.INTENT_KEY_TASK);
                mTask = tasks.get(0); Log.d("XXX", "repeat = " + mTask.getRepeatFrequency());
                displayTaskDetails();

            } else if (intent.hasExtra(Constants.INTENT_KEY_TASK_ID)) {
                // activity opened from widget, so start loader to get task details

                mTaskId = intent.getLongExtra(Constants.INTENT_KEY_TASK_ID, 0);
                mUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, mTaskId);
                getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
            }
        }

        // OffsetChanged Listener on Appbar to handle display of title in expanded and collapsed mode
        mAppbarLayout.addOnOffsetChangedListener(new OnOffsetChangedListener() {
            boolean isShowTitle = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                scrollRange = (scrollRange == -1) ? appBarLayout.getTotalScrollRange() : scrollRange;

                if ((scrollRange + verticalOffset) == 0) {
                    mCollapsingToolbar.setTitle(mTask.getTaskTitle());
                    isShowTitle = true;
                } else if (isShowTitle) {
                    mCollapsingToolbar.setTitle(" ");
                    isShowTitle = false;
                }
            }
        });

        mFabEditTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Task> taskArrayList = new ArrayList<>();
                taskArrayList.add(mTask);

                Intent intent = new Intent(mContext, TaskEditActivity.class);
                intent.putParcelableArrayListExtra(Constants.INTENT_KEY_TASK, taskArrayList);
                startActivity(intent);
            }
        });
    }

    /**
     * Method to customize the Toolbar widget
     */
    private void setupToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppbarLayout.setElevation(4);
        }

        setSupportActionBar(mToolbar);
        if (mToolbar != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * Method invoked after this activity has been paused or restarted
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mUri != null) {
            // Restarts loader
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(
                this,                       // Parent activity context
                mUri,                       // Table to query
                null,                       // Projection
                null,                       // Selection clause
                null,                       // Selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if(cursor.moveToFirst()) {
            mTask = Utils.getTaskObject(cursor);
            displayTaskDetails();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Default method overriden. No action here.
    }

    /**
     * Method to display all task details
     */
    private void displayTaskDetails() {
        // task title
        mTextViewTitle.setText(mTask.getTaskTitle());

        // task category
        mTextViewCategory.setText(mTask.getCategory());

        // priority
        mTextViewPriority.setText(Utils.getPriorityText(mTask.getPriority()));

        // due date
        String date = Utils.isEmptyString(mTask.getDueDate()) ? "" : mTask.getDueDate();
        mTextViewDate.setText(Utils.getDisplayDate(date));

        // due time
        mTextViewTime.setText(Utils.isEmptyString(mTask.getDueTime()) ? "" : mTask.getDueTime());

        // repeat frequency
        if (mTask.getTagRepeat() > 0) {
            mTextViewRepeat.setText((Utils.isEmptyString(mTask.getRepeatFrequency()) ? "" : mTask.getRepeatFrequency()));
        } else {
            mTextViewRepeat.setText("");
        }

        // extra info
        if (!Utils.isEmptyString(mTask.getExtraInfo())) {
            String extra = mTask.getExtraInfo();

            // if extra info has address remove Latitude & Longitude before displaying
            if (mTask.getExtraInfoType().equals(Constants.EXTRA_INFO_LOCATION)) {
                if (extra.contains("[") && extra.contains("]")) {
                    mGeoLocation = extra.substring(extra.indexOf("[") + 1, extra.indexOf("]"));

                    extra = extra.replace(extra.substring(extra.indexOf("["),
                            extra.indexOf("]")), "");
                    extra = extra.replace("]", "");

                    mAddress = extra;
                }
            }
            mTextViewExtra.setText(mTask.getExtraInfoType() + "\n\n" + extra);
        } else {
            mTextViewExtra.setText("");
        }

        // show button if task has extra info, else it's hidden
        if (!Utils.isEmptyString(mTask.getExtraInfo())) {
            mButtonExtra.setVisibility(View.VISIBLE);
            featureButtonExtra();
        } else {
            mButtonExtra.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Method to handle extra button
     */
    private void featureButtonExtra() {
        if (mTask.getExtraInfoType().equals(Constants.EXTRA_INFO_LOCATION)) {
            mButtonExtra.setText(getString(R.string.label_check_location));

            mButtonExtra.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:" + mGeoLocation + "?q=" + mGeoLocation + "(" + mAddress + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            });

        } else if (mTask.getExtraInfoType().equals(Constants.EXTRA_INFO_BARCODE)) {
            mButtonExtra.setText(getString(R.string.label_view_product));

            mButtonExtra.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri webpage = Uri.parse(BASE_URL_SEARCH + mTask.getExtraInfo().trim());
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

        } else if (mTask.getExtraInfoType().equals(Constants.EXTRA_INFO_EMAIL)) {
            mButtonExtra.setText(getString(R.string.label_email));

            mButtonExtra.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, mTask.getExtraInfo().trim());
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

        } else if (mTask.getExtraInfoType().equals(Constants.EXTRA_INFO_PHONE)) {
            mButtonExtra.setText(getString(R.string.label_call));

            mButtonExtra.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mTask.getExtraInfo().trim()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

}
