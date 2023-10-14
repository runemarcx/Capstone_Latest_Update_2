package com.hcdc.capstone;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hcdc.capstone.accounthandling.LoginActivity;

public class BaseActivity extends AppCompatActivity {

    private boolean shouldRedirectToLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRedirectToLogin) {
            redirectToLogin();
        } else if (!isLoggedIn()) {
            shouldRedirectToLogin = true;
            redirectToLogin();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, getClass());
        if (getClass() == Homepage.class) {
            return;
        }
        boolean isActivityInStack = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
        if (isActivityInStack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isLoggedIn() {
        return true;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
