package com.activitytracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_GPX_FILE = 1;

    private Button pickFileButton;
    private Button viewStatsButton;
    private TextView resultTextView;
    private String gpxFileContent;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        pickFileButton = findViewById(R.id.pickFileButton);
        viewStatsButton = findViewById(R.id.viewStatsButton);
        resultTextView = findViewById(R.id.resultTextView);

        Intent intent = getIntent();
        // First, attempt to get the username from the Intent
        username = intent.getStringExtra("username");
        String result = intent.getStringExtra("result");
        // If a result is provided in the Intent, display it
        if (result != null && !result.isEmpty()) {
            resultTextView.setText(result);
        }

        // If the username is not provided in the Intent or it's empty, attempt to get it from SharedPreferences
        if (username == null || username.isEmpty()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            username = preferences.getString("username", "");
        }
        Log.i("MainActivity", "Username after onCreate: " + username);

        pickFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
            }
        });

        viewStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileStatsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ActivityTrackerChannel";
            String description = "Channel for Activity Tracker notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("ActivityTracker", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        if (result != null && !result.isEmpty()) {
            resultTextView.setText(result);
        }

        // Attempt to get the username from the Intent
        String usernameFromIntent = intent.getStringExtra("username");
        // If a username is provided in the Intent, use it
        if (usernameFromIntent != null && !usernameFromIntent.isEmpty()) {
            username = usernameFromIntent;
        } else {
            // If the username is not provided in the Intent, attempt to get it from SharedPreferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            username = preferences.getString("username", "");
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_GPX_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == SELECT_GPX_FILE && resultCode == RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append('\n');
                    }
                    gpxFileContent = stringBuilder.toString();
                    new SendDataTask().execute(username, gpxFileContent);
                } catch (Exception e) {
                    Log.e("MainActivity", "Error reading file: " + e.getMessage());
                }
            }
        }
    }

    private class SendDataTask extends AsyncTask<String, Void, Pair<String, String>> {
        @Override
        protected Pair<String, String> doInBackground(String... params) {
            String username = params[0];
            String gpxFileContent = params[1];
            String result = null;
            try {
                String host = "192.168.1.3"; // Replace with your server IP
                int port = 12346; // Replace with your server port
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Send the request type
                out.writeObject("ACTIVITY_STATS_REQUEST");

                // Convert the GPX file content into a list of strings
                List<String> gpxLines = new ArrayList<>(Arrays.asList(gpxFileContent.split("\\n")));

                // Send GPX lines
                out.writeObject(gpxLines);
                out.flush();

                // Read the response
                Object response = in.readObject();
                if (response instanceof String) {
                    // Split the response to extract the result and username
                    String[] parts = ((String) response).split("%%%");
                    result = parts[0];
                    username = parts.length > 1 ? parts[1] : "";

                } else {
                    Log.e("MainActivity", "Unexpected response received: " + response.toString());
                }
                Log.i("MainActivity", "GPX file contents after parsing: " + result);
                // Close
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                Log.e("MainActivity", "Error sending request: " + e.getMessage());
                return new Pair<>(null, e.toString());
            }
            return new Pair<>(username, result);
        }

        @Override
        protected void onPostExecute(Pair<String, String> resultPair) {
            if (resultPair != null && resultPair.second != null) {
                // Format the results
                String result = resultPair.second.replace("ActivityResult{user=", "")
                        .replace(" totalDistance=", "\nTotal Distance: ")
                        .replace(" totalClimb=", "\nTotal Climb: ")
                        .replace(" totalTime=", "\nTotal Time: ")
                        .replace(" averageSpeed=", "\nAverage Speed: ")
                        .replace("}", "");
                // Display the formatted results
                resultTextView.setText(result);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                preferences.edit().putString("username", resultPair.first).apply();

                // ...
                showNotification(result);

                Log.i("MainActivity", "Username in onPostExecute: " + resultPair.first);

                MainActivity.this.username = resultPair.first;

                Log.i("MainActivity", "Username after SendDataTask: " + MainActivity.this.username);

            } else {
                resultTextView.setText("An error occurred while processing the request.");
            }
        }
    }

    private void showNotification(String result) {
        try {
            // Create an intent that will be fired when the user clicks the notification
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("result", result);
            intent.putExtra("username", username); // Add this line
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ActivityTracker")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("GPX Processing Complete")
                    .setContentText("Your activity has been processed. Check the results.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);  // Automatically removes the notification when the user taps it

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // Attempt to display the notification
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Log.e("MainActivity", "Permission denied. Could not display notification.");
        }
    }

}
