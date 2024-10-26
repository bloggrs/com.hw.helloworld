package com.hw.helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView blogsRecyclerView;
    private BlogAdapter blogAdapter;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bloggrs_connect);

        EditText siteEditText = findViewById(R.id.siteEditText);
        CardView connectButton = findViewById(R.id.connectButton);
        blogsRecyclerView = findViewById(R.id.blogsRecyclerView);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        connectButton.setOnClickListener(v -> {
            String value = siteEditText.getText().toString();
            Log.d("MainActivity", "Button clicked, value: " + value);
            Intent intent = new Intent(MainActivity.this, com.hw.helloworld.BloggrsConnectPage.MainActivity.class);
            intent.putExtra("siteValue", value);
            startActivity(intent);
        });

        // Initialize RecyclerView
        blogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Call to fetch blogs
        fetchBlogs();
    }

    private void fetchBlogs() {
        loadingSpinner.setVisibility(View.VISIBLE); // Show loading spinner
        new FetchBlogsTask().execute();
    }

    private class FetchBlogsTask extends AsyncTask<Void, Void, List<Blog>> {
        @Override
        protected List<Blog> doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/v1/blogs");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }
                reader.close();

                // Parse JSON response
                JsonObject jsonObject = JsonParser.parseString(jsonResponse.toString()).getAsJsonObject();
                JsonArray blogsArray = jsonObject.getAsJsonObject("data").getAsJsonArray("blogs");
                return new Gson().fromJson(blogsArray, new TypeToken<List<Blog>>(){}.getType());

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Blog> blogs) {
            loadingSpinner.setVisibility(View.GONE); // Hide loading spinner
            if (blogs != null) {
                Toast.makeText(MainActivity.this, "Fetched " + blogs.size() + " blogs", Toast.LENGTH_SHORT).show();
                // Set the adapter with fetched blogs
                blogAdapter = new BlogAdapter(MainActivity.this, blogs); // Change is here
                blogsRecyclerView.setAdapter(blogAdapter);
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch blogs", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
