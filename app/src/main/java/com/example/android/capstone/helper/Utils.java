package com.example.android.capstone.helper;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.widget.Toast;
import com.example.android.capstone.R;
import com.example.android.capstone.data.Task;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;

/**
 * This class contains common utility methods
 */

public class Utils {

    private static String sDateToday;
    private static String sDateTomorrow;
    private static String sDateWeek;
    private static String sDateMonth;
    static SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    /**
     * This is a private constructor and only meant to hold static variables and methods,
     * which can be accessed directly from the class name Utils
     */
    private void Utils() {
    }

    /**
     * Utility method to construct a Toast message
     *
     * @return Toast object
     */
    public static Toast showToastMessage(Context context, Toast toast, String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        return toast;
    }

    /**
     * Utility method to check if a string is empty or not
     *
     * @return TRUE (if empty string) / FALSE
     */
    public static boolean isEmptyString(String stringToCheck) {
        return (stringToCheck == null || stringToCheck.trim().length() == 0);
    }

    /**
     * Utility method to convert first letter of a string to Uppercase
     *
     * @return word with first letter uppercase
     */
    public static String convertStringToFirstCapital(String word) {
        return word.toUpperCase().charAt(0) + word.substring(1).toLowerCase();
    }

    /**
     * Utility method to convert string to Title Case
     *
     * @return string in Title Case
     */
    public static String convertStringToTitleCase(String inputString) {
        String[] wordsArray = inputString.split(" ");
        for (int i = 0; i < wordsArray.length; i++) {
            wordsArray[i] = convertStringToFirstCapital(wordsArray[i]);
        }

        return TextUtils.join(" ", wordsArray);
    }

    /**
     * Methods to set and get dates for Today, Tomorrow and Date after 7 days
     */
    public static void setDates() {

        Calendar calendar = Calendar.getInstance();
        sDateToday = inputFormatter.format(calendar.getTime());

        // add one day to the calendar date
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        sDateTomorrow = inputFormatter.format(calendar.getTime());

        // add 7 days to the calendar date
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        sDateWeek = inputFormatter.format(calendar.getTime());

        // add 30 days to the calendar date
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        sDateMonth = inputFormatter.format(calendar.getTime());
    }

    public static String getDateToday() {
        return sDateToday;
    }

    public static String getDateTomorrow() {
        return sDateTomorrow;
    }

    public static String getDateWeek() {
        return sDateWeek;
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    /**
     * Method to format date for EditActivity and DetailActivity
     *
     * @return formatted date
     */
    public static String getDisplayDate(String dateText) {
        SimpleDateFormat outputFormatter = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH);
        Date date;

        try {
            date = inputFormatter.parse(dateText);
        } catch (ParseException ex) {
            Timber.e(ex.getMessage());
            date = new Date();
        }

        return outputFormatter.format(date);
    }

    /**
     * Method to format date for ListActivity and Widget
     *
     * @return formatted date
     */
    public static String getDisplayListDate(String dateText) {
        SimpleDateFormat outputFormatter = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);
        Date date;

        if (isEmptyString(dateText)) {
            return null;
        }

        try {
            date = inputFormatter.parse(dateText);
        } catch (ParseException ex) {
            Timber.e(ex.getMessage());
            date = new Date();
        }

        return outputFormatter.format(date);
    }

    /**
     * Method to return the Task Category options for Spinner
     *
     * @return List<String> of categories
     */
    public static List<String> getTaskCategories(Context context) {
        List<String> categories = new ArrayList<>();

        categories.add(context.getString(R.string.hint_spinner_category));
        categories.add(Constants.TASK_CATEGORY_PERSONAL);
        categories.add(Constants.TASK_CATEGORY_BUSINESS);
        categories.add(Constants.TASK_CATEGORY_HOME);
        categories.add(Constants.TASK_CATEGORY_OCCASION);
        categories.add(Constants.TASK_CATEGORY_EVENT);
        categories.add(Constants.TASK_CATEGORY_HEALTH);
        categories.add(Constants.TASK_CATEGORY_LEISURE);
        categories.add(Constants.TASK_CATEGORY_TRAVEL);
        categories.add(Constants.TASK_CATEGORY_MISC);

        return categories;
    }

    /**
     * Method to return the Task Priority options for Spinner
     *
     * @return List<String> of priority
     */
    public static List<String> getTaskPriorities(Context context) {
        List<String> priorities = new ArrayList<>();

        priorities.add(context.getString(R.string.hint_spinner_priority));
        priorities.add(Constants.TASK_PRIORITY_HIGH);
        priorities.add(Constants.TASK_PRIORITY_MEDIUM);
        priorities.add(Constants.TASK_PRIORITY_LOW);

        return priorities;
    }

    /**
     * Method to return the Task Repeat options for Spinner
     *
     * @return List<String> of priority
     */
    public static List<String> getRepeatOptions(Context context) {
        List<String> priorities = new ArrayList<>();

        priorities.add(context.getString(R.string.hint_spinner_repeat));
        priorities.add(Constants.TASK_REPEAT_DAILY);
        priorities.add(Constants.TASK_REPEAT_WEEKLY);
        priorities.add(Constants.TASK_REPEAT_MONTHLY);

        return priorities;
    }

    /**
     * Method to return the Extra Info options for Spinner
     *
     * @return List<String> of extra info types
     */
    public static List<String> getExtraInfoOptions(Context context) {
        List<String> extraInfoOptions = new ArrayList<>();

        extraInfoOptions.add(context.getString(R.string.hint_spinner_extrainfo));
        extraInfoOptions.add(Constants.EXTRA_INFO_PHONE);
        extraInfoOptions.add(Constants.EXTRA_INFO_EMAIL);
        extraInfoOptions.add(Constants.EXTRA_INFO_LOCATION);
        extraInfoOptions.add(Constants.EXTRA_INFO_BARCODE);

        return extraInfoOptions;
    }

    /**
     * Method to create and populate a Task object from the cursor data
     * This task parcelable object will be used to pass between activities
     *
     * @return {@link Task} object
     */
    public static Task getTaskObject(Cursor cursor) {
        Task task = new Task();

        task.setTaskId(cursor.getLong(cursor.getColumnIndex(TaskEntry._ID)));
        task.setTaskTitle(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE)));
        task.setCategory(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_CATEGORY)));
        task.setPriority(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_PRIORITY)));
        task.setExtraInfoType(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_EXTRA_INFO_TYPE)));
        task.setExtraInfo(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_EXTRA_INFO)));
        task.setDueDate(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DUE_DATE)));
        task.setDueTime(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DUE_TIME)));
        task.setTagRepeat(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_TAG_REPEAT)));
        task.setRepeatFrequency(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_REPEAT_FREQUENCY)));
        task.setDateAdded(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DATE_ADDED)));
        return task;
    }

    /**
     * Method to get return an integer value of the priority based on the String priority value
     *
     * @return priority (1, 2, 3 or 0)
     */
    public static int getPriority(String priorityText) {
        if (!isEmptyString(priorityText)) {
            if (priorityText.equals(Constants.TASK_PRIORITY_HIGH)) {
                return 1;
            } else if (priorityText.equals(Constants.TASK_PRIORITY_MEDIUM)) {
                return 2;
            } else if (priorityText.equals(Constants.TASK_PRIORITY_LOW)) {
                return 3;
            }
        }

        return 0;
    }

    /**
     * Method to return priority display text based on integer value
     *
     * @return priority text
     */
    public static String getPriorityText(int priority) {
        switch (priority) {
            case 1:
                return Constants.TASK_PRIORITY_HIGH;
            case 2:
                return Constants.TASK_PRIORITY_MEDIUM;
            case 3:
                return Constants.TASK_PRIORITY_LOW;
        }

        return Constants.LABEL_NONE;
    }

    /**
     * Method to get return index of extra info selection
     * (required for spinner selection when existing task is edited)
     *
     * @return index (1, 2, 3, 4 or 0)
     */
    public static int getExtraInfoTypeIndex(String extra) {
        if (!isEmptyString(extra)) {
            if (extra.equals(Constants.EXTRA_INFO_PHONE)) {
                return 1;
            } else if (extra.equals(Constants.EXTRA_INFO_EMAIL)) {
                return 2;
            } else if (extra.equals(Constants.EXTRA_INFO_LOCATION)) {
                return 3;
            } else if (extra.equals(Constants.EXTRA_INFO_BARCODE)) {
                return 4;
            }
        }

        return 0;
    }

    /**
     * Method to get return index of category selection
     * (required for spinner selection when existing task is edited)
     *
     * @return index
     */
    public static int getCategoryIndex(String category) {
        if (category.equals(Constants.TASK_CATEGORY_PERSONAL)) {
            return 1;
        } else if (category.equals(Constants.TASK_CATEGORY_BUSINESS)) {
            return 2;
        } else if (category.equals(Constants.TASK_CATEGORY_HOME)) {
            return 3;
        } else if (category.equals(Constants.TASK_CATEGORY_OCCASION)) {
            return 4;
        } else if (category.equals(Constants.TASK_CATEGORY_EVENT)) {
            return 5;
        } else if (category.equals(Constants.TASK_CATEGORY_HEALTH)) {
            return 6;
        } else if (category.equals(Constants.TASK_CATEGORY_LEISURE)) {
            return 7;
        } else if (category.equals(Constants.TASK_CATEGORY_TRAVEL)) {
            return 8;
        } else if (category.equals(Constants.TASK_CATEGORY_MISC)) {
            return 9;
        }

        return 0;
    }

    /**
     * Method to return index of repeat selection
     *
     * @return index
     */
    public static int getRepeatIndex(String repeat) {
        if (!isEmptyString(repeat)) {
            if (repeat.equals(Constants.TASK_REPEAT_DAILY)) {
                return 1;
            } else if (repeat.equals(Constants.TASK_REPEAT_WEEKLY)) {
                return 2;
            } else if (repeat.equals(Constants.TASK_REPEAT_MONTHLY)) {
                return 3;
            }
        }

        return 0;
    }

    /**
     * Method to convert long value of time to display value
     * @param timeLong
     * @return formatted time value
     */
    public static String getDisplayTime(long timeLong) {
        String timeText = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        timeText = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute);
        return timeText;
    }
}
