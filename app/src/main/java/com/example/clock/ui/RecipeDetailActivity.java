package com.example.clock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clock.R;
import com.example.clock.adapter.CategoryAdapter;
import com.example.clock.adapter.CookingStepAdapter;
import com.example.clock.model.Category;
import com.example.clock.model.CookingStep;
import com.example.clock.model.Ingredient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.clock.model.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {

    // API 전용, 이미지 전용 분리
    private static final String API_BASE_URL = "https://avocadoteam.n-e.kr/api";

    private int recipeId;
    private String title;
    private String desc;
    private String imageUrl;

    private ImageView imageView;
    private TextView titleView, descView;

    // 🔹 카테고리 + 단계용 리사이클러뷰
    private RecyclerView categoryRecycler;
    private RecyclerView stepRecycler;

    // 🔹 데이터
    private final ArrayList<Category> categoryList = new ArrayList<>();
    private final ArrayList<CookingStep> stepList = new ArrayList<>();

    // 🔹 어댑터
    private CategoryAdapter categoryAdapter;
    private CookingStepAdapter stepAdapter;

    private OkHttpClient client = new OkHttpClient();

    TextView creatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // 1) 인텐트에서 기본 정보 꺼내기
        Intent intent = getIntent();
        recipeId = intent.getIntExtra("recipe_id", -1);
        title = intent.getStringExtra("title");
        desc = intent.getStringExtra("desc");
        imageUrl = intent.getStringExtra("imageUrl");

        // 2) 기본 UI 세팅
        imageView = findViewById(R.id.detail_image);
        titleView = findViewById(R.id.detail_title);
        descView = findViewById(R.id.detail_desc);


        categoryRecycler = findViewById(R.id.recycler_category);
        stepRecycler = findViewById(R.id.recycler_step);

        TextView portionText = findViewById(R.id.detail_portion);
        TextView timeText = findViewById(R.id.detail_time);
        TextView difficultyText = findViewById(R.id.detail_difficulty);


        // 어댑터 생성
        categoryAdapter = new CategoryAdapter(this, categoryList);
        stepAdapter = new CookingStepAdapter(this, stepList);

        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryRecycler.setAdapter(categoryAdapter);

        stepRecycler.setLayoutManager(new LinearLayoutManager(this));
        stepRecycler.setAdapter(stepAdapter);

        creatorView = findViewById(R.id.detail_creator);


        // 상단 기본 정보
        titleView.setText(title);
        descView.setText(desc);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 이미지용 BASE_URL 사용
            String fullUrl = API_BASE_URL + "/" + (imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl);
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_home_noimage)
                    .error(R.drawable.ic_home_noimage)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_home_noimage);
        }

        String portion = intent.getStringExtra("portion");
        String cookingTime = intent.getStringExtra("cookingTime");
        String difficulty = intent.getStringExtra("difficulty");

        portionText.setText(portion);
        timeText.setText(cookingTime);
        difficultyText.setText(difficulty);

        if (portion.isEmpty()) {
            portionText.setVisibility(View.GONE);
        }

        if (cookingTime.isEmpty()) {
            timeText.setVisibility(View.GONE);
        }

        if (difficulty.isEmpty()) {
            difficultyText.setVisibility(View.GONE);
        }


        // 3) 서버에서 전체 데이터(카테고리+재료+단계) 받아오기
        if (recipeId != -1) {
            fetchRecipeDetail(recipeId);
        }
    }

    private void fetchRecipeDetail(int id) {
        HttpUrl url = HttpUrl.parse(API_BASE_URL + "/GetRecipeDetail")
                .newBuilder()
                .addQueryParameter("id", String.valueOf(id))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("DETAIL", "Response: " + body);

                try {
                    JSONObject obj = new JSONObject(body);

                    //크레이터
                    JSONObject recipeObj = obj.optJSONObject("recipe");
                    final String creator =
                            (recipeObj != null) ? recipeObj.optString("creator", "") : "";

                    runOnUiThread(() -> {
                        if (!creator.isEmpty()) {
                            creatorView.setText("작성자: " + creator);
                            creatorView.setVisibility(View.VISIBLE);
                        } else {
                            creatorView.setVisibility(View.GONE);
                        }
                    });

                    // 🔹 카테고리 + 재료 파싱
                    JSONArray catArr = obj.optJSONArray("categories");
                    categoryList.clear();

                    if (catArr != null) {
                        for (int i = 0; i < catArr.length(); i++) {
                            JSONObject c = catArr.getJSONObject(i);

                            Category category = new Category(
                                    i,                              // ✅ 임시 ID
                                    c.optString("name"),
                                    c.optInt("order")               // ✅ 서버 필드
                            );

                            JSONArray ingArr = c.optJSONArray("ingredients");
                            if (ingArr != null) {
                                for (int j = 0; j < ingArr.length(); j++) {
                                    JSONObject ing = ingArr.getJSONObject(j);

                                    category.ingredients.add(
                                            new Ingredient(
                                                    j,                          // ✅ 임시 ID
                                                    ing.optString("name"),
                                                    ing.optString("amount")
                                            )
                                    );
                                }
                            }

                            categoryList.add(category);
                        }
                    }

                    // 🔹 단계 파싱
                    JSONArray stepArr = obj.optJSONArray("steps");
                    stepList.clear();

                    if (stepArr != null) {
                        for (int i = 0; i < stepArr.length(); i++) {
                            JSONObject o = stepArr.getJSONObject(i);

                            stepList.add(new CookingStep(
                                    i,              // 임시 ID
                                    recipeId,
                                    o.optInt("step"),
                                    "",              // title 없음
                                    o.optString("description"),
                                    o.optString("imageUrl")
                            ));
                        }
                    }

                    runOnUiThread(() -> {
                        categoryAdapter.notifyDataSetChanged();
                        stepAdapter.notifyDataSetChanged();
                    });



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
