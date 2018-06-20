package com.example.android.capstone;

import android.app.Application;
import android.net.Uri;
import com.example.android.capstone.data.TaskContract;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Constants;
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

        // delete complete tasks
        deleteCompletedTasks();

        // delete tasks containing location information after 30 days
        deleteLocationTasks();
    }

    /**
     * Method that returns the application instance
     * @return ApplicationInstance
     */
    private static synchronized BaseApplication getApplicationInstance() {
        return mApplicationInstance;
    }

    /**
     * Method to remove from database completed tasks older than a day
     */
    private void deleteCompletedTasks() {
        String selection = "((" + TaskEntry.COLUMN_TAG_COMPLETED + " = 1 AND " +
                TaskEntry.COLUMN_DATE_COMPLETED + " < '" + Utils.getDateToday() + "'))";
        Uri uri = TaskContract.TaskEntry.CONTENT_URI;
        int rowsDeleted = getContentResolver().delete(uri, selection, null);

        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete
            Timber.i(getString(R.string.info_completed_no_delete), rowsDeleted);
        } else {
            Timber.i(getString(R.string.info_completed_tasks_delete), rowsDeleted);
        }
    }

    /**
     * Method to remove tasks containing location details that are more than 30 days old
     * to comply with Google Play Services policy
     */
    private void deleteLocationTasks() {
        String selection = "(((julianday('" + Utils.getDateToday() + "') - julianday("
                + TaskEntry.COLUMN_DATE_ADDED + ")) > 30 AND "
                + TaskEntry.COLUMN_EXTRA_INFO_TYPE + " LIKE '%"
                + Constants.EXTRA_INFO_LOCATION + "%'))";
        Uri uri = TaskContract.TaskEntry.CONTENT_URI;
        int rowsDeleted = getContentResolver().delete(uri, selection, null);

        if (rowsDeleted > 0) {
            Timber.i(getString(R.string.info_location_tasks_deleted), rowsDeleted);
        }
    }
}
