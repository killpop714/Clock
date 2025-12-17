package com.example.clock.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.clock.R;
import com.example.clock.model.Recipe;
import com.example.clock.ui.RecipeDetailActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Recipe> list;

    // ⚠ 반드시 "/"로 끝나야 한다
    private static final String BASE_URL = "https://avocadoteam.n-e.kr/api/";

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
        String imageUrl = item.imageUrl;

        holder.bind(item);

        // ① 재활용 방지 위해 무조건 초기화
        Glide.with(holder.itemView.getContext()).clear(holder.recipeImage);
        holder.recipeImage.setImageDrawable(null);

        holder.loadingImage.setVisibility(View.VISIBLE);
        holder.recipeImage.setVisibility(View.INVISIBLE);





        // ② URL 정규화
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
            if (imageUrl.equals("null") || imageUrl.isEmpty()) {
                imageUrl = null;
            }
        }
        Log.d("테스트","이미지 Uri:" + imageUrl);

        // ③ 정상 URL만 Glide로 처리
        if (imageUrl == null || !imageUrl.startsWith("static/")) {

            Log.d("RecipeAdapter", "No Image URL → 기본 이미지 표시");

            holder.loadingImage.setVisibility(View.GONE);
            holder.recipeImage.setVisibility(View.VISIBLE);
            holder.recipeImage.setImageResource(R.drawable.ic_home_noimage);

            return;
        }

        // ④ 실제 URL 생성
        String fullUrl = BASE_URL + imageUrl;

        Log.d("RecipeAdapter", "Glide Load URL: " + fullUrl);

        // ⑤ Glide 요청
        Glide.with(holder.itemView.getContext())
                .load(fullUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {

                        Log.d("RecipeAdapter", "이미지 로딩 실패: " + e);

                        holder.loadingImage.setVisibility(View.GONE);
                        holder.recipeImage.setVisibility(View.VISIBLE);
                        holder.recipeImage.setImageResource(R.drawable.ic_home_noimage);

                        return true; // 실패 처리 우리가 함
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {

                        Log.d("RecipeAdapter", "이미지 로딩 성공!");

                        holder.loadingImage.setVisibility(View.GONE);
                        holder.recipeImage.setVisibility(View.VISIBLE);

                        return false; // Glide가 이미지 넣도록 허용
                    }
                })
                .into(holder.recipeImage);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView recipeImage;
        View loadingImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeImage = itemView.findViewById(R.id.RecipeImage);
            loadingImage = itemView.findViewById(R.id.LoadingImage);


        }

        public void bind(Recipe item) {
            // 🟢 클릭 시 디테일 창 열기
            itemView.setOnClickListener(v -> {

                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", item.id);
                Log.d("레시피 아이디", "bind: "+item.id);
                intent.putExtra("title", item.title);
                intent.putExtra("desc", item.description);
                intent.putExtra("imageUrl", item.imageUrl);

                intent.putExtra("portion",item.portion);
                intent.putExtra("cookingTime",item.cookingTime);
                intent.putExtra("difficulty",item.difficulty);

                context.startActivity(intent);
            });
        }
    }


}


