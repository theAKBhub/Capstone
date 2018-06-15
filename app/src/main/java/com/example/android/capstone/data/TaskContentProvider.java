package com.example.android.capstone.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.android.capstone.R;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Utils;
import java.util.ArrayList;
import timber.log.Timber;

/**
 * {@link ContentProvider} for TaskMate Database
 */

public class TaskContentProvider extends ContentProvider {

    // URI matcher code for the content URI for the entire table
    private static final int TASKS = 100;

    // URI matcher code for the content URI for a single item in the table
    private static final int TASK_ID = 101;

    //******************
    private static final int TASKS_UNION = 102;


    // Database Helper Object
    private TaskDbHelper mDbHelper;

    // UriMatcher object to match a content URI to a corresponding code
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private Context mContext;
    private ArrayList<String> mColumnsToValidate;


    // Static initializer - runs the first time anything is called from this class
    static {
        // Content URI of the form "content://package-name/tasks"
        // This URI is used to provide access to multiple table rows.
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_URI, TASKS);

        // Content URI of the form "content://package-name/tasks/#", where # is the ID value
        // This is used to provide access to single row of the table.
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_URI + "/#", TASK_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new TaskDbHelper(getContext());
        mContext = getContext();
        return true;
    }

    /**
     * Function - READ from table
     * Perform query for given URI and load the cursor with results fetched from table.
     * The returned result can have multiple rows or a single row, depending on given URI.
     *
     * @return cursor
     */
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get instance of readable database
        SQLiteDatabase sqLiteDBReadable = mDbHelper.getReadableDatabase();

        // Cursor to hold the query result
        Cursor cursor;

        // Check if the uri matches to a specific URI CODE
        int match = mUriMatcher.match(uri);

        switch (match) {

            case TASKS:
                cursor = sqLiteDBReadable.query(TaskEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TASK_ID:
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDBReadable.query(TaskEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.error_unknown_uri, uri));
        }

        // Set notification URI on Cursor so it knows when to update in the event the data in cursor changes
        cursor.setNotificationUri(mContext.getContentResolver(), uri);

        return cursor;
    }

    /**
     * Function - INSERT into table
     * This method inserts records in the table
     *
     * @return uri
     */
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        boolean isValidInsertInput;
        long id = -1;

        mColumnsToValidate = new ArrayList<>();
        mColumnsToValidate.add(TaskEntry.COLUMN_TASK_TITLE);
        mColumnsToValidate.add(TaskEntry.COLUMN_EXTRA_INFO_TYPE);
        mColumnsToValidate.add(TaskEntry.COLUMN_TAG_REPEAT);
        mColumnsToValidate.add(TaskEntry.COLUMN_TAG_COMPLETED);
        mColumnsToValidate.add(TaskEntry.COLUMN_DATE_ADDED);

        isValidInsertInput = validateInput(values, mColumnsToValidate);

        if (isValidInsertInput) {
            SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();
            id = sqLiteDBWritable.insert(TaskEntry.TABLE_NAME, null, values);
        }

        // Check if ID is -1, which means record insert has failed
        if (id < 0) {
            Timber.e(mContext.getString(R.string.error_db_insert), uri);
            return null;
        }

        // Notify all listeners that the data has changed
        mContext.getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID of the newly inserted row appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Function - DELETE from table
     * This method deletes records from the table
     *
     * @return number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = sqLiteDBWritable.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TASK_ID:
                // Delete a single row given by the ID in the URI
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDBWritable.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.error_unknown_uri, uri));
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Function - UPDATE table
     * This method updates table
     *
     * @return int
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {

        final int match = mUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                return prepareForUpdate(uri, values, selection, selectionArgs);

            case TASK_ID:
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return prepareForUpdate(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.error_unknown_uri, uri));
        }
    }

    /**
     * Method to determine type of URI used to query the table
     *
     * @return URI type
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                return TaskEntry.CONTENT_LIST_TYPE;

            case TASK_ID:
                return TaskEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException(mContext.getString(R.string.error_unknown_uri, uri));
        }
    }

    /**
     * This method validates input data for updates and applies the updates
     *
     * @return rows updated
     */
    public int prepareForUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        boolean isValidUpdateInput;

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        } else {

            mColumnsToValidate = new ArrayList<>();

            if (values.containsKey(TaskEntry.COLUMN_TASK_TITLE)) {
                mColumnsToValidate.add(TaskEntry.COLUMN_TASK_TITLE);

            } else if (values.containsKey(TaskEntry.COLUMN_EXTRA_INFO_TYPE)) {
                mColumnsToValidate.add(TaskEntry.COLUMN_EXTRA_INFO_TYPE);

            } else if (values.containsKey(TaskEntry.COLUMN_TAG_REPEAT)) {
                mColumnsToValidate.add(TaskEntry.COLUMN_TAG_REPEAT);

            } else if (values.containsKey(TaskEntry.COLUMN_TAG_COMPLETED)) {
                mColumnsToValidate.add(TaskEntry.COLUMN_TAG_COMPLETED);

            } else if (values.containsKey(TaskEntry.COLUMN_DATE_ADDED)) {
                mColumnsToValidate.add(TaskEntry.COLUMN_DATE_ADDED);
            }

            // validate inputs before update
            if (mColumnsToValidate.size() > 0) {
                isValidUpdateInput = validateInput(values, mColumnsToValidate);
            } else {
                isValidUpdateInput = true;
            }

            if (isValidUpdateInput) {
                SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();

                // Perform the update on the database and get the number of rows affected
                rowsUpdated = sqLiteDBWritable.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);

                // If 1 or more rows were updated, then notify all listeners that data at the given URI has changed
                if (rowsUpdated != 0) {
                    mContext.getContentResolver().notifyChange(uri, null);
                }
            }
            return rowsUpdated;
        }
    }

    /**
     * This method validates the input data before inserting/updating records to database
     *
     * @param values - ContentValues
     * @return true/false
     */
    public boolean validateInput(ContentValues values, ArrayList<String> columnArgs) {

        String taskTitle;
        String extraInfoType;
        String extraInfo;
        int tagRepeat;
        String repeatFrequency;
        int tagCompleted;
        String dateAdded;
        String dateCompleted;

        for (String column : columnArgs) {

            if (column.equals(TaskEntry.COLUMN_TASK_TITLE)) {
                // check for empty Task Title
                taskTitle = values.getAsString(TaskEntry.COLUMN_TASK_TITLE);
                if (Utils.isEmptyString(taskTitle)) { Timber.e(taskTitle);
                    throw new IllegalArgumentException(mContext.getString(R.string.error_missing_title));
                }

            } else if (column.equals(TaskEntry.COLUMN_EXTRA_INFO_TYPE)) {
                // check if Extra Info is available if Extra Info Type exists
                extraInfoType = values.getAsString(TaskEntry.COLUMN_EXTRA_INFO_TYPE);
                extraInfo = values.getAsString(TaskEntry.COLUMN_EXTRA_INFO);
                if (!Utils.isEmptyString(extraInfoType) && (Utils.isEmptyString(extraInfo))) {
                    throw new IllegalArgumentException(mContext.getString(R.string.error_missing_extra_info));
                }

            } else if (column.equals(TaskEntry.COLUMN_TAG_REPEAT)) {
                // check if Repeat Frequency is available if repeat tag is on
                tagRepeat = values.getAsInteger(TaskEntry.COLUMN_TAG_REPEAT);
                repeatFrequency = values.getAsString(TaskEntry.COLUMN_REPEAT_FREQUENCY);
                if (tagRepeat == 1 && Utils.isEmptyString(repeatFrequency)) {
                    throw new IllegalArgumentException(mContext.getString(R.string.error_missing_frequency));
                }

            } else if (column.equals(TaskEntry.COLUMN_TAG_COMPLETED)) {
                // check if completion date is available if completed tag is on
                tagCompleted = values.getAsInteger(TaskEntry.COLUMN_TAG_COMPLETED);
                dateCompleted = values.getAsString(TaskEntry.COLUMN_DATE_COMPLETED);
                if (tagCompleted == 1 && Utils.isEmptyString(dateCompleted)) {
                    throw new IllegalArgumentException(mContext.getString(R.string.error_missing_date_completed));
                }

            } else if (column.equals(TaskEntry.COLUMN_DATE_ADDED)) {
                // check for empty Task date added
                dateAdded = values.getAsString(TaskEntry.COLUMN_DATE_ADDED);
                if (Utils.isEmptyString(dateAdded)) {
                    throw new IllegalArgumentException(mContext.getString(R.string.error_date_added));
                }
            }
        }

        return true;
    }
}
