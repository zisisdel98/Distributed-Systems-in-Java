package com.activitytracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProfileStatsActivity extends AppCompatActivity {
    private TextView profileStatsTextView;
    private String username;

    private Button buttonAverageStats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_stats);

        profileStatsTextView = findViewById(R.id.profileStatsTextView);
        buttonAverageStats = findViewById(R.id.button_average_stats);
        // Get username from the intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Fetch and display user profile stats
        new FetchUserProfileTask().execute(username);

        buttonAverageStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileStatsActivity.this, AverageStatsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }

    private class FetchUserProfileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            Log.i("MainActivity", "Username before USER_STATS_REQUEST: " + username);

            String userProfile = "";
            try {
                String host = "192.168.1.3"; // Replace with your server IP
                int port = 12346; // Replace with your server port
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Send the request type
                out.writeObject("USER_STATS_REQUEST");

                // Send username
                out.writeObject(username);

                // Read the response
                Object response = in.readObject();
                if (response instanceof String) {
                    userProfile = (String) response;
                } else {
                    Log.e("MainActivity", "Unexpected response received: " + response.toString());
                }

                // Close
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                Log.e("MainActivity", "Error sending request: " + e.getMessage());
            }
            return userProfile;
        }

        @Override
        protected void onPostExecute(String userProfile) {
            if (userProfile != null && !userProfile.isEmpty()) {
                profileStatsTextView.setText(userProfile);
            } else {
                profileStatsTextView.setText("An error occurred while retrieving user profile.");
            }
        }
    }
}