package com.example.clock.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clock.R;
import com.example.clock.model.Ingredient;

import java.util.ArrayList;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Ingredient> list;

    public IngredientAdapter(Context context, ArrayList<Ingredient> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, amountView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.ing_name);
            amountView = itemView.findViewById(R.id.ing_amount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient item = list.get(position);
        Log.d("포지션", "position: "+list.get(position));
        holder.nameView.setText(item.name);
        holder.amountView.setText(item.amount);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

