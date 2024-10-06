package com.example.task_manager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button add, tasks;
    AlertDialog dialog;
    LinearLayout layout;

    private static final String API_URL = "http://10.0.2.2:8080/api/tasks";  // Replace localhost with actual server IP for real devices
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        add = findViewById(R.id.add);
        tasks = findViewById(R.id.tasks);

        buildDialog();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();  // Show the task input dialog
            }
        });

        // Navigate to TasksActivity to display the task list
        tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TasksActivity.class);
                startActivity(intent);
            }
        });
    }

    // Builds a dialog for user to input task details
    public void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        final EditText titleEdit = view.findViewById(R.id.titleEdit);
        final EditText descriptionEdit = view.findViewById(R.id.descriptionEdit);

        builder.setView(view);
        builder.setTitle("Enter your Task")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = titleEdit.getText().toString();
                        String description = descriptionEdit.getText().toString();

                        // Validate input before proceeding
                        if (title.isEmpty() || description.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Please enter both title and description", Toast.LENGTH_SHORT).show();
                        } else {
                            addTaskToApi(title, description);  // Send data to API
                            titleEdit.setText("");  // Clear the fields after sending
                            descriptionEdit.setText("");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, just dismiss the dialog
                    }
                });

        dialog = builder.create();
    }

    // Method to send task data to API via POST request
    private void addTaskToApi(String title, String description) {
        // Create dynamic due date (current date for now)
        String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        // Prepare JSON data
        JSONObject taskData = new JSONObject();
        try {
            taskData.put("title", title);
            taskData.put("description", description);
            taskData.put("dueDate", currentDate);  // Dynamic due date
            taskData.put("status", "pending");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Log the JSON data before sending the request
        Log.d("MainActivity", "Task Data: " + taskData.toString());

        // Create request body
        RequestBody body = RequestBody.create(
                taskData.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        // Create request
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        // Send request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Failed to send task", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to send task to server", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("MainActivity", "Unexpected response code: " + response);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Unexpected response: " + response.code(), Toast.LENGTH_SHORT).show());
                } else {
                    Log.i("MainActivity", "Task successfully added");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Method to dynamically add task cards (optional)
    private void addCard(String title, String description) {
        final View view = getLayoutInflater().inflate(R.layout.card, null);

        TextView titleView = view.findViewById(R.id.title);
        TextView descriptionView = view.findViewById(R.id.description);
        Button delete = view.findViewById(R.id.delete);

        titleView.setText(title);
        descriptionView.setText(description);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeView(view);
            }
        });

        layout.addView(view);
    }
}
