package com.example.clock.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.clock.R;

public class RecipeDetailActivity extends AppCompatActivity {

    ImageView ivImage;
    TextView tvTitle, tvDescription, tvPortion, tvTime, tvDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ivImage = findViewById(R.id.detail_ivImage);
        tvTitle = findViewById(R.id.detail_tvTitle);
        tvDescription = findViewById(R.id.detail_tvDescription);
        tvPortion = findViewById(R.id.detail_tvPortion);
        tvTime = findViewById(R.id.detail_tvTime);
        tvDifficulty = findViewById(R.id.detail_tvDifficulty);

        // ─────────────────────────────
        // 🔥 MainActivity에서 전달받은 값
        // ─────────────────────────────
        int id = getIntent().getIntExtra("id", -1);
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("description");
        String img = getIntent().getStringExtra("imageUrl");
        String portion = getIntent().getStringExtra("portion");
        String time = getIntent().getStringExtra("cookingTime");
        String difficulty = getIntent().getStringExtra("difficulty");

        // ─────────────────────────────
        // UI에 출력
        // ─────────────────────────────
        tvTitle.setText(title);
        tvDescription.setText(desc);
        tvPortion.setText(portion);
        tvTime.setText(time);
        tvDifficulty.setText(difficulty);

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivImage);
    }
}
