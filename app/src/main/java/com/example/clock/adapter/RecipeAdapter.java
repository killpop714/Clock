package com.example.clock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clock.model.Recipe;
import com.example.clock.R;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Recipe> list;

    // 서버 기본 주소
    private static final String BASE_URL = "https://avocadoteam.n-e.kr";

    public RecipeAdapter(Context context, ArrayList<Recipe> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe item = list.get(position);

        holder.title.setText(item.title);
        holder.desc.setText(item.description);

        // ============================
        // 🔥 이미지 URL 결합 + Glide 로딩
        // ============================
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {

            String fullUrl = BASE_URL + item.imageUrl;  // 예: /static/recipe/xxx.jpg

            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder)  // 로딩 중
                    .error(R.drawable.no_image)           // 실패 시
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.no_image);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.RecipeImage);
            title = itemView.findViewById(R.id.RecipeTitle);
            desc = itemView.findViewById(R.id.RecipeDesc);
        }
    }
}
