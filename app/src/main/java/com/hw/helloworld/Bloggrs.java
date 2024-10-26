package com.hw.helloworld;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request; // Make sure this is the only import of Request
import okhttp3.RequestBody;
import okhttp3.Response;

public class Bloggrs {
    private static final String TAG = "Bloggrs";
    private static final String SERVER_URL = "http://10.0.2.2:4000/api/v1";
    private static final String PREF_NAME = "BloggrsPrefs";
    private static final String PREF_API_KEY = "api_key";
    private static final String PREF_BLOG_ID = "blog_id";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_USER_ID = "user_id";

    private String apiKey;
    private String blogId;
    private Context context;
    private static OkHttpClient client;
    private SharedPreferences prefs;

    private Categories categories;
    private Posts posts;
    private Auth auth;
    private Pages pages;
    private General general;
    private BlogContacts blogContacts;
    private PostComments postComments;
    private Tags tags;
    private JSONObject blogObject;

    public Bloggrs(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.apiKey = "b4eac4e5-13ea-4fd8-9e42-7290f0072a0c";
        this.blogId = prefs.getString(PREF_BLOG_ID, null);
        this.categories = new Categories(this);
        this.posts = new Posts(this);
        this.auth = new Auth(this);
        this.pages = new Pages(this);
        this.general = new General(this);
        this.blogContacts = new BlogContacts(this);
        this.postComments = new PostComments(this);
        this.tags = new Tags(this);
    }

    public CompletableFuture<Bloggrs> init(String apiKey, String siteValue) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return initializeInternal(apiKey, siteValue);
            } catch (IOException e) {
                Log.e(TAG, "Failed to initialize", e);
                throw new RuntimeException("Failed to initialize" + e.toString(), e);
            }
        });
    }

    private Bloggrs initializeInternal(String apiKey, String siteValue) throws IOException {
        this.apiKey = apiKey;
        String url;
        if (apiKey == null) {
            // TODO: Implement subdomain logic for Android
            url = SERVER_URL + "/blogs/" + siteValue + " /api_key";
        } else {
            url = SERVER_URL + "/blogs/" + siteValue + " /api_key";
//            url = SERVER_URL + "/blogs/api_key";
        }

        Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseData = response.body().string();
            JSONObject jsonObject = null; // Declare and initialize to null

            try {
                jsonObject = new JSONObject(responseData);
                // Further processing
            } catch (JSONException e) {
                e.printStackTrace(); // Handle the exception
            }

// Now you can safely check if jsonObject is not null before using it
            JSONObject blogObject = null;
            if (jsonObject != null) {
                try {
                    blogObject = jsonObject.getJSONObject("data").getJSONObject("blog");
                    // Further processing with blogObject
                    this.blogObject = blogObject;
                } catch (JSONException e) {
                    e.printStackTrace(); // Handle this exception as well
                }
            }
            try {
                // Assuming blogObject is already initialized and not null
                this.blogId = blogObject.getString("id");
            } catch (JSONException e) {
                e.printStackTrace(); // Handle the exception (e.g., log it, notify the user)
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_API_KEY, apiKey);
            editor.putString(PREF_BLOG_ID, blogId);
            editor.apply();

            return this;
        }
    }

    // ... (previous methods remain unchanged)

    public static class Categories {
        private Bloggrs bloggrs;

        public Categories(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<List<Category>> getCategories(Map<String, String> options) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(SERVER_URL + "/blogs/" + this.bloggrs.blogId + "/categories").newBuilder();
                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                    }

                    Request request = new okhttp3.Request.Builder()
                            .url(urlBuilder.build())
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray categoriesArray = jsonObject.getJSONObject("data").getJSONArray("categories");

                    List<Category> categories = new ArrayList<>();
                    for (int i = 0; i < categoriesArray.length(); i++) {
                        JSONObject categoryObject = categoriesArray.getJSONObject(i);
                        categories.add(new Category(categoryObject));
                    }

                    return categories;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting categories", e);
                    throw new RuntimeException("Error getting categories", e);
                }
            });
        }
    }

    public static class Posts {
        private Bloggrs bloggrs;

        public Posts(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<Post> getPost(String postId) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/blogs/" + this.bloggrs.blogId + "/posts/" + postId)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject postObject = jsonObject.getJSONObject("data").getJSONObject("post");

                    return new Post(postObject);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting post", e);
                    throw new RuntimeException("Error getting post", e);
                }
            });
        }

        public CompletableFuture<List<Post>> getPosts(Map<String, String> options) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(SERVER_URL + "/blogs/" + this.bloggrs.blogId + "/posts").newBuilder();
                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                    }

                    Request request = new okhttp3.Request.Builder()
                            .url(urlBuilder.build())
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray postsArray = jsonObject.getJSONObject("data").getJSONArray("posts");

                    List<Post> posts = new ArrayList<>();
                    for (int i = 0; i < postsArray.length(); i++) {
                        JSONObject postObject = postsArray.getJSONObject(i);
                        posts.add(new Post(postObject));
                    }

                    return posts;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting posts", e);
                    throw new RuntimeException("Error getting posts", e);
                }
            });
        }

        public CompletableFuture<Boolean> likePostHandler(String postId, String action) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{}");
                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/blogs/" + this.bloggrs.blogId + "/posts/" + postId + "/" + action)
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    String message = jsonObject.getString("message");

                    return "success".equals(message);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling post like", e);
                    throw new RuntimeException("Error handling post like", e);
                }
            });
        }
    }

    public class Auth {
        private Bloggrs bloggrs;

        public Auth(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<Map<String, Object>> getAuth() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/auth")
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    JSONObject userObject = dataObject.getJSONObject("user");
                    String token = dataObject.getString("token");

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREF_TOKEN, token);
                    editor.putString(PREF_USER_ID, userObject.getString("id"));
                    editor.apply();

                    Map<String, Object> result = new HashMap<>();
                    result.put("user", new User(userObject));
                    result.put("token", token);

                    return result;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting auth", e);
                    throw new RuntimeException("Error getting auth", e);
                }
            });
        }

        public String getUserId() {
            return prefs.getString(PREF_USER_ID, null);
        }
    }

    public class Pages {
        private Bloggrs bloggrs;

        public Pages(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<List<Page>> getPages(Map<String, String> options) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(SERVER_URL + "/blogs/" + this.bloggrs.blogId + "/pages").newBuilder();
                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                    }

                    Request request = new okhttp3.Request.Builder()
                            .url(urlBuilder.build())
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray pagesArray = jsonObject.getJSONObject("data").getJSONArray("pages");

                    List<Page> pages = new ArrayList<>();
                    for (int i = 0; i < pagesArray.length(); i++) {
                        JSONObject pageObject = pagesArray.getJSONObject(i);
                        pages.add(new Page(pageObject));
                    }

                    return pages;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting pages", e);
                    throw new RuntimeException("Error getting pages", e);
                }
            });
        }

        public CompletableFuture<Page> getPageBySlug(String slug) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/blogs/" + this.bloggrs.blogId + "/pages/" + slug)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject pageObject = jsonObject.getJSONObject("data").getJSONObject("page");

                    return new Page(pageObject);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting page by slug", e);
                    throw new RuntimeException("Error getting page by slug", e);
                }
            });
        }
    }

    public class General {
        private Bloggrs bloggrs;

        public General(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<Map<String, Object>> getBlogHeaderWidgetData() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/blogs/" + blogId + "/header-widget-data")
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject dataObject = jsonObject.getJSONObject("data");

                    Map<String, Object> result = new HashMap<>();
                    result.put("blogName", dataObject.optString("blogName"));
                    result.put("blogDescription", dataObject.optString("blogDescription"));
                    result.put("blogLogoUrl", dataObject.optString("blogLogoUrl"));

                    return result;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting blog header widget data", e);
                    throw new RuntimeException("Error getting blog header widget data", e);
                }
            });
        }
    }

    public static class BlogContacts {
        private Bloggrs bloggrs;

        public BlogContacts(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<BlogContact> createBlogContact(Map<String, String> contactData) {
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    JSONObject jsonBody = new JSONObject(contactData);
                    jsonBody.put("BlogId", this.bloggrs.blogId);

                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"), jsonBody.toString());

                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/blogcontacts")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject blogContactObject = jsonObject.getJSONObject("data").getJSONObject("blogcontact");

                    return new BlogContact(blogContactObject);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating blog contact", e);
                    throw new RuntimeException("Error creating blog contact", e);
                }
            });
        }
    }

    public static class PostComments {
        private Bloggrs bloggrs;

        public PostComments(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<PostComment> createPostComment(String postId, String content) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("PostId", postId);
                    jsonBody.put("content", content);

                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"), jsonBody.toString());

                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/postcomments")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject commentObject = jsonObject.getJSONObject("data").getJSONObject("postcomment");

                    return new PostComment(commentObject);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating post comment", e);
                    throw new RuntimeException("Error creating post comment", e);
                }
            });
        }
    }

    public static class Tags {
        private Bloggrs bloggrs;

        public Tags(Bloggrs bloggrs) {
            this.bloggrs = bloggrs;
        }

        public CompletableFuture<List<Tag>> getTags() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Request request = new okhttp3.Request.Builder()
                            .url(SERVER_URL + "/tags")
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray tagsArray = jsonObject.getJSONObject("data").getJSONArray("tags");

                    List<Tag> tags = new ArrayList<>();
                    for (int i = 0; i < tagsArray.length(); i++) {
                        JSONObject tagObject = tagsArray.getJSONObject(i);
                        tags.add(new Tag(tagObject));
                    }

                    return tags;
                } catch (Exception e) {
                    Log.e(TAG, "Error getting tags", e);
                    throw new RuntimeException("Error getting tags", e);
                }
            });
        }
    }

    // Helper classes for data models
    public static class Category {
        public int id;
        public String name;
        public String slug;
        public Date createdAt;
        public Date updatedAt;

        public Category(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.name = jsonObject.getString("name");
            this.slug = jsonObject.getString("slug");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
        }
    }
    public static class Meta {
        public int likesCount;
        public int commentsCount;
        public boolean liked;
        public String contentText;

        public Meta(JSONObject jsonObject) throws JSONException {
            this.likesCount = jsonObject.getInt("likes_count");
            this.commentsCount = jsonObject.getInt("comments_count");
            this.liked = jsonObject.getBoolean("liked");
            this.contentText = jsonObject.getString("content_text");
        }
    }

    public static class Post {
        public int id;
        public String title;
        public String slug;
        public String htmlContent;
        public String status;
        public Date createdAt;
        public Date updatedAt;
        public Date publishedAt;
        public int blogId;
        public int userId;
        public Date scheduledAt;
        public String language;
        public String locale;
        public int popularity;
        public boolean isDeleted;

        public Post(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.title = jsonObject.getString("title");
            this.slug = jsonObject.getString("slug");
            this.htmlContent = jsonObject.getString("html_content");
            this.status = jsonObject.getString("status");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
            this.publishedAt = parseDate(jsonObject.optString("publishedAt"));
            this.blogId = jsonObject.getInt("BlogId");
            this.userId = jsonObject.getInt("UserId");
            this.scheduledAt = parseDate(jsonObject.optString("scheduledAt"));
            this.language = jsonObject.optString("language");
            this.locale = jsonObject.optString("locale");
            this.popularity = jsonObject.optInt("popularity");
            this.isDeleted = jsonObject.optBoolean("isDeleted");


        }
    }

    public static class User {
        public int id;
        public String email;
        public String firstName;
        public String lastName;
        public boolean isGuest;
        public Date createdAt;
        public Date updatedAt;

        public User(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.email = jsonObject.getString("email");
            this.firstName = jsonObject.getString("first_name");
            this.lastName = jsonObject.getString("last_name");
            this.isGuest = jsonObject.getBoolean("isGuest");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
        }
    }

    public static class Page {
        public int id;
        public String name;
        public String slug;
        public String craftjsJsonState;
        public Date createdAt;
        public Date updatedAt;
        public int blogId;
        public int userId;

        public Page(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.name = jsonObject.getString("name");
            this.slug = jsonObject.getString("slug");
            this.craftjsJsonState = jsonObject.optString("craftjs_json_state");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
            this.blogId = jsonObject.getInt("BlogId");
            this.userId = jsonObject.getInt("UserId");
        }
    }

    public static class PostComment {
        public int id;
        public String content;
        public Date createdAt;
        public Date updatedAt;
        public int postId;
        public int userId;
        public Integer parentId;
        public boolean approved;
        public int reputationScore;

        public PostComment(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.content = jsonObject.getString("content");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
            this.postId = jsonObject.getInt("postId");
            this.userId = jsonObject.getInt("userId");
            this.parentId = jsonObject.has("parentId") ? jsonObject.getInt("parentId") : null;
            this.approved = jsonObject.getBoolean("approved");
            this.reputationScore = jsonObject.getInt("reputationScore");
        }
    }

    public static class BlogContact {
        public int id;
        public String firstName;
        public String lastName;
        public String email;
        public String content;
        public Date createdAt;
        public Date updatedAt;
        public int blogId;

        public BlogContact(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.firstName = jsonObject.getString("first_name");
            this.lastName = jsonObject.getString("last_name");
            this.email = jsonObject.getString("email");
            this.content = jsonObject.getString("content");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
            this.blogId = jsonObject.getInt("BlogId");
        }
    }

    public static class Tag {
        public int id;
        public String name;
        public Date createdAt;
        public Date updatedAt;

        public Tag(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getInt("id");
            this.name = jsonObject.getString("name");
            this.createdAt = parseDate(jsonObject.optString("createdAt"));
            this.updatedAt = parseDate(jsonObject.optString("updatedAt"));
        }
    }

    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            return format.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            return null;
        }
    }
}