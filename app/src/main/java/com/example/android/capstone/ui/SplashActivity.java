package com.example.android.capstone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch Dashboard
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);

        // Close splash screen
        finish();

      /*  boolean insert = false;

        if (insert) {
            Uri uri;

            for (int i=0; i<5; i++) {
                ContentValues values = new ContentValues();
                values.put(TaskEntry.COLUMN_TASK_TITLE, "Task " + i);
                values.put(TaskEntry.COLUMN_CATEGORY, "Event");
                values.put(TaskEntry.COLUMN_PRIORITY, (new Random().nextInt(4)));
                values.put(TaskEntry.COLUMN_EXTRA_INFO_TYPE, Constants.EXTRA_INFO_LOCATION);
                values.put(TaskEntry.COLUMN_EXTRA_INFO, "Edinburgh UK");
                values.put(TaskEntry.COLUMN_DUE_DATE, "2018-06-1" + i);
                values.put(TaskEntry.COLUMN_TAG_REPEAT, "0");
                values.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, "");
                values.put(TaskEntry.COLUMN_TAG_COMPLETED, "0");
                values.put(TaskEntry.COLUMN_DATE_ADDED, "2018-05-1" + (i+5));
                values.put(TaskEntry.COLUMN_DATE_COMPLETED, "");
                values.put(TaskEntry.COLUMN_DATE_UPDATED, "2018-06-14");

                uri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
                if (uri == null) {
                    Toast.makeText(this, "Error in Insert", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Record inserted", Toast.LENGTH_SHORT).show();
                }
            }

            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_TASK_TITLE, "Task 150");
            values.put(TaskEntry.COLUMN_CATEGORY, "Event");
            values.put(TaskEntry.COLUMN_PRIORITY, 3);
            values.put(TaskEntry.COLUMN_EXTRA_INFO_TYPE, "Email");
            values.put(TaskEntry.COLUMN_EXTRA_INFO, "abc@abc.com");
            values.put(TaskEntry.COLUMN_DUE_DATE, "2018-05-01");
            values.put(TaskEntry.COLUMN_DUE_TIME, "");
            values.put(TaskEntry.COLUMN_TAG_REPEAT, "0");
            values.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, "");
            values.put(TaskEntry.COLUMN_TAG_COMPLETED, "1");
            values.put(TaskEntry.COLUMN_DATE_ADDED, "2018-06-14");
            values.put(TaskEntry.COLUMN_DATE_COMPLETED, "2018-06-16");
            values.put(TaskEntry.COLUMN_DATE_UPDATED, "2018-06-14");

            uri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, "Error in Insert", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Record inserted", Toast.LENGTH_SHORT).show();
            }


        } */

    }
}
