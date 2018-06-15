package com.example.android.capstone.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import com.example.android.capstone.helper.Constants;

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

        // Projection clause for count queries
        public final static String PROJECTION_TASKS_TODAY = QUERY_COUNT + RES_COLUMN_COUNT_TASKS_TODAY;

        // Projection clause for Lists that should display tasks order by due date ASC,
        // but tasks with no due date shown at last
        public final static String PROJECTION_TASKS_LIST = "*, CASE WHEN " + COLUMN_DUE_DATE + " = '' THEN 1 "
                    + "WHEN " + COLUMN_DUE_DATE + " != '' THEN 2 END " + RES_COLUMN_TAG_DUE_DATE;


        /**
         * Method to get return an integer value of the priority based on the String priority value
         *
         * @return priority (1, 2, 3 or 0)
         */
        public static int getPriority(String priorityText) {
            if (priorityText.equals(Constants.TASK_PRIORITY_HIGH)) {
                return 1;
            } else if (priorityText.equals(Constants.TASK_PRIORITY_MEDIUM)) {
                return 2;
            } else if (priorityText.equals(Constants.TASK_PRIORITY_LOW)) {
                return 3;
            }

            return 0;
        }

        /**
         * Method to get return the Repeat Frequency Tag
         *
         * @param frequency text
         * @return tag (D, M, W)
         */
        private static final String TAG_DAILY = "D";
        private static final String TAG_WEEKLY = "W";
        private static final String TAG_MONTHLY = "M";

        public static String getRepeatFrequency(String frequencyText) {
            if (frequencyText.equals(Constants.TASK_REPEAT_DAILY)) {
                return TAG_DAILY;
            } else if (frequencyText.equals(Constants.TASK_REPEAT_MONTHLY)) {
                return TAG_MONTHLY;
            } else if (frequencyText.equals(Constants.TASK_REPEAT_WEEKLY)) {
                return TAG_WEEKLY;
            }

            return null;
        }
    }

}
