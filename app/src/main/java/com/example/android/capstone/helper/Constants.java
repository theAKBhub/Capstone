package com.example.android.capstone.helper;

/**
 * This class declares all constant variables used across the application
 */

public class Constants {

    /** Constants for Task Categories */
    public static final String TASK_CATEGORY_BUSINESS = "Business";
    public static final String TASK_CATEGORY_PERSONAL = "Personal";
    public static final String TASK_CATEGORY_HOME = "Home";
    public static final String TASK_CATEGORY_HEALTH = "Health";
    public static final String TASK_CATEGORY_TRAVEL = "Travel";
    public static final String TASK_CATEGORY_LEISURE = "Leisure";
    public static final String TASK_CATEGORY_OCCASION = "Occasion";
    public static final String TASK_CATEGORY_EVENT = "Event";
    public static final String TASK_CATEGORY_MISC = "Miscellaneous";

    /** Constants for Task Priority */
    public static final String TASK_PRIORITY_HIGH = "High";
    public static final String TASK_PRIORITY_MEDIUM = "Medium";
    public static final String TASK_PRIORITY_LOW = "Low";
    public static final String LABEL_NONE = "None";

    /** Constants for Task Repeat Frequency */
    public static final String TASK_REPEAT_DAILY = "Daily";
    public static final String TASK_REPEAT_WEEKLY = "Weekly";
    public static final String TASK_REPEAT_MONTHLY = "Monthly";

    /** Constants for Task Extra Info Types */
    public static final String EXTRA_INFO_PHONE = "Contact Phone";
    public static final String EXTRA_INFO_EMAIL = "Contact Email";
    public static final String EXTRA_INFO_LOCATION = "Map Location";
    public static final String EXTRA_INFO_BARCODE = "Product Barcode";

    /** Constants for Task Heading */
    public static final String HDG_TASKS = "Tasks";
    public static final String HDG_TASKS_ALL = "All";
    public static final String HDG_TASKS_PAST = "Past Due Date";
    public static final String HDG_TASKS_TODAY = "Today";
    public static final String HDG_TASKS_TOMORROW = "Tomorrow";
    public static final String HDG_TASKS_WEEK = "Next 7 Days";
    public static final String HDG_TASKS_NODATE = "Not Dated";

    /** Other Constants */
    public static final int PHONE_PICKER_REQUEST = 1;
    public static final int EMAIL_PICKER_REQUEST = 2;
    public static final int PLACE_PICKER_REQUEST = 3;
    public static final int CAMERA_REQUEST_CODE = 4;
    public static final int BARCODE_READER_REQUEST_CODE = 5;
    public static final int GMS_REQUEST_CODE = 9001;
    public static final String INTENT_KEY_TASK_FILTER = "key_task_filter";
    public static final String INTENT_KEY_TASK = "key_task";
    public static final String INTENT_KEY_TASK_ID = "key_task_id";
    public static final String INTENT_KEY_BARCODE = "key_barcode";

}
