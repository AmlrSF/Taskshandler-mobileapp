package com.example.task_manager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TasksActivity extends AppCompatActivity {

    private static final String API_URL = "http://10.0.2.2:8080/api/tasks"; // Update if necessary
    private OkHttpClient client;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        client = new OkHttpClient();
        container = findViewById(R.id.container);
        fetchTasks();
    }

    private void fetchTasks() {
        Request request = new Request.Builder()
                .url(API_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TasksActivity", "Failed to fetch tasks", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("TasksActivity", "Unexpected response code: " + response);
                    return;
                }

                try {
                    JSONArray tasks = new JSONArray(response.body().string());
                    runOnUiThread(() -> {
                        container.removeAllViews(); // Clear the layout before adding new views
                        for (int i = 0; i < tasks.length(); i++) {
                            try {
                                JSONObject task = tasks.getJSONObject(i);
                                String id = task.getString("id");
                                String title = task.getString("title");
                                String date = task.getString("dueDate"); // Ensure the key matches the JSON
                                String description = task.getString("description");
                                boolean isDeleted = task.optBoolean("isDeleted", false); // Check if the task is marked as deleted

                                if (!isDeleted) { // Only add tasks that are not deleted
                                    addCard(id, title, description, date);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e("TasksActivity", "Failed to parse JSON", e);
                }
            }
        });
    }

    private void addCard(String id, String title, String description, String date) {
        final View view = getLayoutInflater().inflate(R.layout.card, null);

        TextView titleView = view.findViewById(R.id.title);
        TextView dueView = view.findViewById(R.id.dueDate);
        TextView descriptionView = view.findViewById(R.id.description);
        Button delete = view.findViewById(R.id.delete);
        TextView idView = view.findViewById(R.id.id);

        idView.setText(id);
        titleView.setText(title);
        descriptionView.setText(description);
        dueView.setText(date);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markTaskAsDeleted(id); // Mark the task as deleted
                container.removeView(view); // Optionally remove the card from the layout
            }
        });

        container.addView(view);
    }

    private void markTaskAsDeleted(String id) {
        String url = API_URL + "/" + id ; // Assuming your API supports marking tasks as deleted

        // Here we send a PUT or POST request to update the task status
        Request request = new Request.Builder()
                .url(url)
                .post(null) // Sending an empty request body
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TasksActivity", "Failed to mark task as deleted", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("TasksActivity", "Unexpected response code: " + response);
                } else {
                    Log.i("TasksActivity", "Task successfully marked as deleted");
                    runOnUiThread(() -> fetchTasks()); // Refresh the task list after marking as deleted
                }
            }
        });
    }
}
