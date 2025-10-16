// WelcomeActivity.java
package com.activitytracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {
    private EditText usernameEditText;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        usernameEditText = findViewById(R.id.username);
        continueButton = findViewById(R.id.buttonProceed);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();

                // Here, we save the username in SharedPreferences regardless of whether
                // it's empty or not. This allows us to retrieve it later in MainActivity.
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Welcome.this);
                preferences.edit().putString("username", username).apply();

                // We create the intent and set the username as an extra. Even if the
                // username is empty, it's okay because we'll be checking this in MainActivity.
                Intent intent = new Intent(Welcome.this, MainActivity.class);
                intent.putExtra("username", username);

                // Start MainActivity and finish WelcomeActivity.
                startActivity(intent);
                finish();
            }
        });
    }
}
