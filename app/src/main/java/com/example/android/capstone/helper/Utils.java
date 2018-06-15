package com.example.android.capstone.helper;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * This class contains common utility methods
 */

public class Utils {

    private static String sDateToday;
    private static String sDateTomorrow;
    private static String sDateWeek;

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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        sDateToday = dateFormat.format(calendar.getTime());

        // add one day to the calendar date
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        sDateTomorrow = dateFormat.format(calendar.getTime());

        // add 7 days to the calendar date
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        sDateWeek = dateFormat.format(calendar.getTime());
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
}
