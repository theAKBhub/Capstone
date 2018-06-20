package com.example.android.capstone.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import com.example.android.capstone.helper.Utils;

/**
 * Database schema for Task Database
 */

public class TaskContract {

    /**
     * Empty Constructor
     */
    private TaskContract() {
    }

    /**
     * ContentProvider Authority
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.capstone";

    /**
     * ContentProvider Base Uri
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path appended to base URI for possible URIs
     */
    public static final String PATH_URI = "tasks";

    public static final long INVALID_TASK_ID = -1;

    /**
     * Inner class that defines constant values for the Tasks table.
     * Each entry in the table represents a single task.
     */
    public static class TaskEntry implements BaseColumns {

        // Content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_URI);

        // MIME type of the {@link #CONTENT_URI} for a list of Tasks
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_URI;

        // MIME type of the {@link #CONTENT_URI} for a single Task
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_URI;

        // Name of database table
        public static final String TABLE_NAME = "tasks";

        // Aggregrate count query
        public static final String QUERY_COUNT = "count(*) AS ";

        // Columns of table
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_TASK_TITLE = "task_title";
        public final static String COLUMN_CATEGORY = "category";
        public final static String COLUMN_PRIORITY = "priority";
        public final static String COLUMN_EXTRA_INFO_TYPE = "extra_info_type";
        public final static String COLUMN_EXTRA_INFO = "extra_info";
        public final static String COLUMN_DUE_DATE = "due_date";
        public final static String COLUMN_DUE_TIME = "due_time";
        public final static String COLUMN_TAG_REPEAT = "tag_repeat";
        public final static String COLUMN_REPEAT_FREQUENCY = "repeat_frequency";
        public final static String COLUMN_TAG_COMPLETED = "tag_completed";
        public final static String COLUMN_DATE_ADDED = "date_added";
        public final static String COLUMN_DATE_COMPLETED = "date_completed";
        public final static String COLUMN_DATE_UPDATED = "date_updated";

        // Resultant Columns used only in queries
        public final static String RES_COLUMN_COUNT_TASKS_ALL = "count_all";
        public final static String RES_COLUMN_COUNT_TASKS_PAST = "count_past";
        public final static String RES_COLUMN_COUNT_TASKS_TODAY = "count_today";
        public final static String RES_COLUMN_COUNT_TASKS_TOMORROW = "count_tomorrow";
        public final static String RES_COLUMN_COUNT_TASKS_WEEK = "count_week";
        public final static String RES_COLUMN_COUNT_TASKS_NODATE = "count_nodate";
        public final static String RES_COLUMN_TAG_DUE_DATE = "tag_due_date";

        // Projection for count queries
        public final static String PROJECTION_TASKS_TODAY = QUERY_COUNT + RES_COLUMN_COUNT_TASKS_TODAY;

        // Projection for Lists that should display tasks order by due date ASC,
        // but tasks with no due date shown at last
        public final static String PROJECTION_TASKS_LIST = _ID + ", " + COLUMN_TASK_TITLE + ", "
                + COLUMN_CATEGORY + ", " + COLUMN_PRIORITY
                + ", " + COLUMN_EXTRA_INFO_TYPE + ", " + COLUMN_EXTRA_INFO + ", " + COLUMN_DUE_DATE
                + ", " + COLUMN_DUE_TIME + ", " + COLUMN_TAG_REPEAT + ", " + COLUMN_REPEAT_FREQUENCY
                + ", " + COLUMN_TAG_COMPLETED + ", " + COLUMN_DATE_COMPLETED + ", " + COLUMN_DATE_ADDED
                + ", " + COLUMN_DATE_UPDATED
                + ", CASE WHEN " + COLUMN_DUE_DATE + " = '' THEN 1 "
                + "WHEN " + COLUMN_DUE_DATE + " != '' THEN 2 END " + RES_COLUMN_TAG_DUE_DATE;

        // Projection for Counts displayed in Dashboard
        public final static String PROJECTION_COUNTS = " COUNT(*) AS " + RES_COLUMN_COUNT_TASKS_ALL + ","
                + " SUM(CASE WHEN " + COLUMN_DUE_DATE + " < '" + Utils.getDateToday() + "' THEN 1 ELSE 0 END)"
                + " AS " + RES_COLUMN_COUNT_TASKS_PAST + ","

                + " SUM(CASE WHEN " + COLUMN_DUE_DATE + " = '" + Utils.getDateToday() + "' THEN 1 ELSE 0 END)"
                + " AS " + RES_COLUMN_COUNT_TASKS_TODAY + ","

                + " SUM(CASE WHEN " + COLUMN_DUE_DATE + " = '" + Utils.getDateTomorrow() + "' THEN 1 ELSE 0 END)"
                + " AS " + RES_COLUMN_COUNT_TASKS_TOMORROW + ","

                + " SUM(CASE WHEN (" + COLUMN_DUE_DATE + " > '" + Utils.getDateToday() + "' AND "
                + COLUMN_DUE_DATE + " <= '" + Utils.getDateWeek() + "') THEN 1 ELSE 0 END)"
                + " AS " + RES_COLUMN_COUNT_TASKS_WEEK + ","

                + " SUM(CASE WHEN " + COLUMN_DUE_DATE + " = '' THEN 1 ELSE 0 END)"
                + " AS " + RES_COLUMN_COUNT_TASKS_NODATE + " ";


        // Possible values for Task Completed Tag
        public static final int TAG_COMPLETE = 1;
        public static final int TAG_NOT_COMPLETE = 0;

        // Possible values for Task Repeat Tag
        public static final int TAG_REPEAT = 1;
        public static final int TAG_NOT_REPEAT = 0;

    }

}
