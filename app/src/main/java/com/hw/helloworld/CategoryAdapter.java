package com.hw.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> categories;

    public CategoryAdapter(Context context, List<String> categories) {
        super(context, R.layout.category_item, categories);
        this.context = context;
        this.categories = categories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the layout for each category item
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.category_item, parent, false);
        }

        // Set the category name
        TextView categoryTextView = convertView.findViewById(R.id.categoryTextView);
        categoryTextView.setText(categories.get(position));

        return convertView;
    }
}
