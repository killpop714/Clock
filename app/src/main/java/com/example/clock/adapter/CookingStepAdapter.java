package com.example.clock.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clock.R;
import com.example.clock.model.CookingStep;

import java.util.ArrayList;

public class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CookingStep> list;
    private static final String BASE_URL = "https://avocadoteam.n-e.kr/api/";

    public CookingStepAdapter(Context context, ArrayList<CookingStep> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumView, titleView, descView;
        ImageView stepImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumView = itemView.findViewById(R.id.step_num);
            titleView = itemView.findViewById(R.id.step_title);
            descView = itemView.findViewById(R.id.step_desc);
            stepImageView = itemView.findViewById(R.id.step_image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cooking_step, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("포지션", "position: "+list.get(position));
        CookingStep item = list.get(position);

        holder.stepNumView.setText(String.valueOf(item.step));
        holder.titleView.setText(item.title);
        holder.descView.setText(item.description);

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Log.d("테스트","있음" + item.imageUrl);
            String fullUrl = BASE_URL + item.imageUrl;
            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_home_noimage)
                    .error(R.drawable.ic_home_noimage)
                    .into(holder.stepImageView);
        } else {
            Log.d("테스트","널" + item.imageUrl);
            holder.stepImageView.setImageResource(R.drawable.ic_home_noimage);
        }
    }

    @Override
    public int getItemCount() {
        Log.d("리스트 크기", "getItemCount: "+list.size());
        return list.size();
    }
}

