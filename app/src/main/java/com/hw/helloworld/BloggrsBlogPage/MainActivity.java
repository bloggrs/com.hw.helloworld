package com.hw.helloworld.BloggrsBlogPage;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hw.helloworld.Bloggrs;
import com.hw.helloworld.R;
import com.hw.helloworld.Bloggrs.Post; // Adjust the package name as necessary

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Typeface; // Import this line

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Bloggrs bloggrs;
    private LinearLayout categoriesLinearLayout;
    private LinearLayout postsListView; // Reference to the posts ListView
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bloggrs_blog_layout);

        String siteValue = getIntent().getStringExtra("siteValue");
        String publicKey = getIntent().getStringExtra("publicKey");
        Log.d(TAG, "Received siteValue: " + siteValue);

        TextView siteTextView = findViewById(R.id.siteTextView);
        siteTextView.setText(siteValue != null ? siteValue : "No site value received");


        bloggrs = new Bloggrs(this);
        bloggrs.init(publicKey, siteValue)
                .thenAccept(initializedBloggrs -> {
                    runOnUiThread(() -> {
                        getCategories();
                        getPosts(); // Fetch posts after initialization
                    });
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error initializing Bloggrs: " + throwable.toString(), throwable);
                    return null;
                });
        categoriesLinearLayout = findViewById(R.id.categoriesListView);
        postsListView = findViewById(R.id.postsListView); // Initialize the posts ListView
        resultTextView = findViewById(R.id.resultTextView);
    }

    private void getPosts() {
        Map<String, String> options = new HashMap<>();
        // Add any required query parameters to the options map
        options.put("postId", "1"); // Example option; adjust as necessary

        Bloggrs.Posts postsApi = new Bloggrs.Posts(this.bloggrs);

        postsApi.getPosts(options)
                .thenAccept(this::handlePostResult)
                .exceptionally(this::handlePostError);
    }

    private void handlePostResult(List<Bloggrs.Post> posts) {
        runOnUiThread(() -> {
            postsListView.removeAllViews(); // Clear previous posts
            for (Post post : posts) {
                LinearLayout postLayout = new LinearLayout(this);
                postLayout.setOrientation(LinearLayout.VERTICAL);
                postLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // Title TextView
                TextView titleTextView = new TextView(this);
                titleTextView.setText(post.title);
                titleTextView.setTextSize(18);
                titleTextView.setTypeface(null, Typeface.BOLD);
                titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                titleTextView.setPadding(0, 0, 0, 8); // Adjust margin if needed

                // Content TextView
                TextView contentTextView = getTextView(post);

                // Info TextView
                TextView infoTextView = new TextView(this);
                infoTextView.setText("0 likes | 0 comments | " + post.createdAt);

                // Add Views to Layout
                postLayout.addView(titleTextView);
                postLayout.addView(contentTextView);
                postLayout.addView(infoTextView);

                postsListView.addView(postLayout); // No need for runOnUiThread here as it's already in the UI thread
            }
        });
    }

    @NonNull
    private TextView getTextView(Post post) {
        TextView contentTextView = new TextView(this);
        contentTextView.setText(post.htmlContent); // Use htmlContent instead of content

        // LayoutParams for Content with margin
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, 8, 0, 8); // Set top and bottom margins
        contentTextView.setLayoutParams(contentParams);
        return contentTextView;
    }

    private Void handlePostError(Throwable throwable) {
        Log.e(TAG, "Error getting post", throwable);
        runOnUiThread(() -> resultTextView.setText("Error getting post"));
        return null;
    }

    private void getCategories() {
        Map<String, String> options = new HashMap<>();
        options.put("page", "1");
        options.put("pageSize", "10");

        new Bloggrs.Categories(this.bloggrs).getCategories(options)
                .thenAccept(categories -> {
                    runOnUiThread(() -> {
                        categoriesLinearLayout.removeAllViews(); // Clear existing items
                        for (Bloggrs.Category category : categories) {
                            TextView categoryTextView = new TextView(this);
                            categoryTextView.setText(category.name);
                            categoryTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            categoryTextView.setPadding(16, 8, 16, 8);
                            categoriesLinearLayout.addView(categoryTextView);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error getting categories", throwable);
                    return null;
                });
    }
}
