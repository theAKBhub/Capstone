package com.example.android.capstone.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.capstone.R;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import timber.log.Timber;

/**
 * Database helper for Tasks table. Manages database creation and version management.
 */

public class TaskDbHelper extends SQLiteOpenHelper {

    // Name of the database file
    private static final String DATABASE_NAME = "taskmate.db";

    // Database version
    private static final int DATABASE_VERSION = 1;

    private Context mContext;

    /**
     * Default Constructor
     */
    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    /**
     * Function - CREATE table
     * This method is called when the database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String TYPE_TEXT = " TEXT";
        final String TYPE_INTEGER = " INTEGER";
        final String NOT_NULL = " NOT NULL";
        final String COMMA_SEP = ", ";

        // Create a String that contains the SQL statement to create the table
        String SQL_CREATE_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME
                + " ("
                + TaskEntry._ID + TYPE_INTEGER + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
                + TaskEntry.COLUMN_TASK_TITLE + TYPE_TEXT + NOT_NULL + COMMA_SEP
                + TaskEntry.COLUMN_CATEGORY + TYPE_TEXT + NOT_NULL + COMMA_SEP
                + TaskEntry.COLUMN_PRIORITY + TYPE_INTEGER + NOT_NULL + COMMA_SEP
                + TaskEntry.COLUMN_EXTRA_INFO_TYPE + TYPE_TEXT + COMMA_SEP
                + TaskEntry.COLUMN_EXTRA_INFO + TYPE_TEXT + COMMA_SEP
                + TaskEntry.COLUMN_DUE_DATE + TYPE_TEXT + COMMA_SEP
                + TaskEntry.COLUMN_DUE_TIME + TYPE_TEXT + COMMA_SEP
                + TaskEntry.COLUMN_TAG_REPEAT + TYPE_INTEGER + NOT_NULL + COMMA_SEP
                + TaskEntry.COLUMN_REPEAT_FREQUENCY + TYPE_TEXT + COMMA_SEP
                + TaskEntry.COLUMN_TAG_COMPLETED + TYPE_INTEGER + NOT_NULL + COMMA_SEP
                + TaskEntry.COLUMN_DATE_ADDED + TYPE_TEXT + NOT_NULL + COMMA_SEP
                + TaskEntry.COLUMN_DATE_COMPLETED + TYPE_TEXT + COMMA_SEP
                + TaskEntry.COLUMN_DATE_UPDATED + TYPE_TEXT + NOT_NULL
                + ")";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Timber.d(mContext.getString(R.string.info_db_upgrade), oldVersion, newVersion);
        switch (newVersion) {
            case 2:
                // SQL to execute
                break;
            case 3:
                // SQL to execute
                break;
        }
    }
}
