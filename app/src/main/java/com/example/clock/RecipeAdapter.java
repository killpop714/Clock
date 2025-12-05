package com.example.clock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clock.Recipe;

import java.util.ArrayList;


public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Recipe> list;

    // 🔥 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(Recipe item);
    }

    private OnItemClickListener listener;

    // 🔥 외부에서 listener 등록 가능하게 하기
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RecipeAdapter(Context context, ArrayList<Recipe> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = list.get(position);

        holder.title.setText(r.title);
        holder.point.setText("⭐ " + r.point);

        Glide.with(context)
                .load(r.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.image);

        // 🔥 클릭 이벤트 연결
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, point;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_iv);
            title = itemView.findViewById(R.id.item_tvTitle);
            point = itemView.findViewById(R.id.item_tvLike);
        }
    }
}
