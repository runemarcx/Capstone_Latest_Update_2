package com.hcdc.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.hcdc.capstone.accounthandling.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        new Handler().postDelayed(() -> {
            checkUserAuthenticationStatus();
        }, 2000);
    }

    private void checkUserAuthenticationStatus() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, Homepage.class));
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        finish();
    }
}
