package com.example.android.capstone.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch Dashboard
        startActivity(new Intent(this, DashboardActivity.class));

        // Close splash screen
        finish();

        boolean insert = false;

        if (!insert) {
            Uri uri;

            for (int i=0; i<5; i++) {
                ContentValues values = new ContentValues();
                values.put(TaskEntry.COLUMN_TASK_TITLE, "Task " + i);
                values.put(TaskEntry.COLUMN_CATEGORY, "Event");
                values.put(TaskEntry.COLUMN_PRIORITY, (new Random().nextInt(4)));
                values.put(TaskEntry.COLUMN_DUE_DATE, "2018-06-1" + (i+4));
                values.put(TaskEntry.COLUMN_TAG_REPEAT, "0");
                values.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, "");
                values.put(TaskEntry.COLUMN_TAG_COMPLETED, "0");
                values.put(TaskEntry.COLUMN_DATE_ADDED, "2018-06-14");
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
            values.put(TaskEntry.COLUMN_TASK_TITLE, "Task no date");
            values.put(TaskEntry.COLUMN_CATEGORY, "Event");
            values.put(TaskEntry.COLUMN_PRIORITY, 3);
            values.put(TaskEntry.COLUMN_EXTRA_INFO_TYPE, "Email");
            values.put(TaskEntry.COLUMN_EXTRA_INFO, "abc@abc.com");
            values.put(TaskEntry.COLUMN_DUE_DATE, "");
            values.put(TaskEntry.COLUMN_DUE_TIME, "");
            values.put(TaskEntry.COLUMN_TAG_REPEAT, "0");
            values.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, "");
            values.put(TaskEntry.COLUMN_TAG_COMPLETED, "0");
            values.put(TaskEntry.COLUMN_DATE_ADDED, "2018-06-14");
            values.put(TaskEntry.COLUMN_DATE_COMPLETED, "");
            values.put(TaskEntry.COLUMN_DATE_UPDATED, "2018-06-14");

            uri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, "Error in Insert", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Record inserted", Toast.LENGTH_SHORT).show();
            }

        }

    }
}
