package com.hw.helloworld.BloggrsConnectPage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hw.helloworld.Bloggrs;
import com.hw.helloworld.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Bloggrs bloggrs;
    private TextView resultTextView;
    private Button getCategoriesButton;
    private Button getPostButton;
    private Button createCommentButton;
    private Button getTagsButton;
    private Button createContactButton;
    private String siteValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloggrs_example);
        String siteValue = getIntent().getStringExtra("siteValue");
        String publicKey = getIntent().getStringExtra("publicKey");

        resultTextView = findViewById(R.id.resultTextView);
        getCategoriesButton = findViewById(R.id.getCategoriesButton);
        getPostButton = findViewById(R.id.getPostButton);
//        createCommentButton = findViewById(R.id.getCategoriesButton);
//        getTagsButton = findViewById(R.id.getPostButton);
//        createContactButton = findViewById(R.id.getCategoriesButton);

        bloggrs = new Bloggrs(this);
        bloggrs.init(publicKey, siteValue)
                .thenAccept(initializedBloggrs -> {
                    runOnUiThread(() -> {
                        resultTextView.setText("Bloggrs initialized successfully");
                        enableButtons();
                    });
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error initializing Bloggrs" + throwable.toString(), throwable);
                    runOnUiThread(() -> resultTextView.setText("Error initializing Bloggrs" + throwable.toString()));
                    return null;
                });

        getCategories();
//        createCommentButton.setOnClickListener(v -> createComment());
//        getTagsButton.setOnClickListener(v -> getTags());
//        createContactButton.setOnClickListener(v -> createContact());
    }

    private void enableButtons() {
        getCategoriesButton.setEnabled(true);
        getPostButton.setEnabled(true);
//        createCommentButton.setEnabled(true);
//        getTagsButton.setEnabled(true);
//        createContactButton.setEnabled(true);
    }

    private void getCategories() {
        Map<String, String> options = new HashMap<>();
        options.put("page", "1");
        options.put("pageSize", "10");

        new Bloggrs.Categories(this.bloggrs).getCategories(options)
                .thenAccept(categories -> {
                    StringBuilder result = new StringBuilder("Categories:\n");
                    for (Bloggrs.Category category : categories) {
                        result.append(category.name).append("\n");
                    }
                    runOnUiThread(() -> resultTextView.setText(result.toString()));
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error getting categories", throwable);
                    runOnUiThread(() -> resultTextView.setText("Error getting categories"));
                    return null;
                });
    }

    private void getPost() {
        String postId = "1"; // Replace with an actual post ID
        new Bloggrs.Posts(this.bloggrs).getPost(postId)
                .thenAccept(post -> {
                    String result = "Post:\nTitle: " + post.title + "\nSlug: " + post.slug;
                    runOnUiThread(() -> resultTextView.setText(result));
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error getting post", throwable);
                    runOnUiThread(() -> resultTextView.setText("Error getting post"));
                    return null;
                });
    }

    private void createComment() {
        String postId = "1"; // Replace with an actual post ID
        String content = "This is a test comment";

        new Bloggrs.PostComments(this.bloggrs).createPostComment(postId, content)
                .thenAccept(comment -> {
                    String result = "Comment created:\nContent: " + comment.content;
                    runOnUiThread(() -> resultTextView.setText(result));
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error creating comment", throwable);
                    runOnUiThread(() -> resultTextView.setText("Error creating comment"));
                    return null;
                });
    }

    private void getTags() {
        new Bloggrs.Tags(this.bloggrs).getTags()
                .thenAccept(tags -> {
                    StringBuilder result = new StringBuilder("Tags:\n");
                    for (Bloggrs.Tag tag : tags) {
                        result.append(tag.name).append("\n");
                    }
                    runOnUiThread(() -> resultTextView.setText(result.toString()));
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error getting tags", throwable);
                    runOnUiThread(() -> resultTextView.setText("Error getting tags"));
                    return null;
                });
    }

    private void createContact() {
        Map<String, String> contactData = new HashMap<>();
        contactData.put("first_name", "John");
        contactData.put("last_name", "Doe");
        contactData.put("email", "john.doe@example.com");
        contactData.put("content", "This is a test contact message");

        new Bloggrs.BlogContacts(this.bloggrs).createBlogContact(contactData)
                .thenAccept(contact -> {
                    String result = "Contact created:\nName: " + contact.firstName + " " + contact.lastName;
                    runOnUiThread(() -> resultTextView.setText(result));
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error creating contact", throwable);
                    runOnUiThread(() -> resultTextView.setText("Error creating contact"));
                    return null;
                });
    }
}