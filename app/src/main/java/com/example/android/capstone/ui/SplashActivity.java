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
    }
}
