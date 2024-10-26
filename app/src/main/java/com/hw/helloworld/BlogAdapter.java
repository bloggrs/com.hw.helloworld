package com.hw.helloworld;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private final Context context;
    private final List<Blog> blogs;

    public BlogAdapter(Context context, List<Blog> blogs) {
        this.context = context;
        this.blogs = blogs != null ? blogs : List.of(); // Prevent null list
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blog_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogs.get(position);
        holder.titleTextView.setText(blog.getName());
        holder.descriptionTextView.setText(blog.getDescription() != null ? blog.getDescription() : "No description available");

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, com.hw.helloworld.BloggrsBlogPage.MainActivity.class);
                intent.putExtra("siteValue", blog.getSlug());
                intent.putExtra("publicKey", blog.getPublicKey());
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("BlogAdapter", "Error starting activity", e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.blog_title);
            descriptionTextView = itemView.findViewById(R.id.blog_description);
        }
    }
}
