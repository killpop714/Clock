package com.example.clock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clock.R;
import com.example.clock.model.Category;
import com.example.clock.model.Ingredient;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Category> categories;

    public CategoryAdapter(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        RecyclerView ingredientRecycler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            ingredientRecycler = itemView.findViewById(R.id.recycler_ingredient);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_category_block, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);

        holder.categoryName.setText(cat.name);

        // 이 카테고리 안에 들어갈 재료 리스트
        IngredientAdapter ingAdapter = new IngredientAdapter(context, cat.ingredients);
        holder.ingredientRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.ingredientRecycler.setAdapter(ingAdapter);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
