package com.example.clock.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.clock.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.*;

public class RegisterRecipe extends Activity {

    // 이미지 선택 코드
    public static final int PICK_TITLE_IMAGE = 100;
    public static final int PICK_STEP_IMAGE = 200;

    // 대표 이미지
    Uri previewUri = null;

    // 스텝 이미지 리스트
    ArrayList<Uri> stepImages = new ArrayList<>();
    int currentStepIndex = -1;

    // UI 요소들
    ImageView titleImageView;
    EditText TitleEdt, DescriptionEdt;
    LinearLayout CategoryContainer, StepContainer;
    TextView PortionTvew, CookingTimeTvew, DifficultyTvew;

    String[] portionItem = {"1인분", "2인분", "3인분", "4인분 이상"};
    String[] cookingItem = {"10분 이내", "30분 이내", "1시간 이내", "1시간 이상"};
    String[] diffItem = {"쉬움", "보통", "어려움"};

    int userId = -1;
    String username = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_recipe);

        // UI 연결
        titleImageView = findViewById(R.id.ImageVew);
        TitleEdt = findViewById(R.id.TitleEdt);
        DescriptionEdt = findViewById(R.id.DescriptionEdt);

        CategoryContainer = findViewById(R.id.CategoryContainer);
        StepContainer = findViewById(R.id.StepContainer);

        PortionTvew = findViewById(R.id.PortionTvew);
        CookingTimeTvew = findViewById(R.id.CookingTimeTvew);
        DifficultyTvew = findViewById(R.id.DifficultyTvew);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        username = prefs.getString("username", "");

        // 처음 스텝 1개 생성
        addStep();

        // 버튼 이벤트들
        findViewById(R.id.AddCategoryBtn).setOnClickListener(v -> addCategory());
        findViewById(R.id.AddStepBtn).setOnClickListener(v -> addStep());
        findViewById(R.id.RegisterRecipe).setOnClickListener(v -> {
            if (!validateInputs()) return;
            sendRecipe();
        });

        // 이미지 선택
        titleImageView.setOnClickListener(v -> pickTitleImage());

        PortionTvew.setOnClickListener(v -> selectDialog("인원", portionItem, PortionTvew));
        CookingTimeTvew.setOnClickListener(v -> selectDialog("시간", cookingItem, CookingTimeTvew));
        DifficultyTvew.setOnClickListener(v -> selectDialog("난이도", diffItem, DifficultyTvew));
    }

    // ============================
    // 이미지 선택
    // ============================

    void pickTitleImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_TITLE_IMAGE);
    }

    void pickStepImage(int index) {
        currentStepIndex = index;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_STEP_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        Uri uri = data.getData();

        if (requestCode == PICK_TITLE_IMAGE) {
            previewUri = uri;
            titleImageView.setImageURI(uri);
        }

        if (requestCode == PICK_STEP_IMAGE && currentStepIndex != -1) {
            stepImages.set(currentStepIndex, uri);

            // UI 업데이트
            View stepView = StepContainer.getChildAt(currentStepIndex);
            ImageView img = stepView.findViewById(R.id.StepImageView);
            img.setImageURI(uri);

            currentStepIndex = -1;
        }
    }

    // ============================
    // 재료 카테고리 추가
    // ============================

    void addCategory() {
        View view = getLayoutInflater().inflate(R.layout.category_item, null);
        LinearLayout ingContainer = view.findViewById(R.id.IngredientContainer);

        Button addIng = view.findViewById(R.id.AddIngredientBtn);
        Button delete = view.findViewById(R.id.DeleteCategoryBtn);

        addIng.setOnClickListener(v -> addIngredientRow(ingContainer));
        delete.setOnClickListener(v -> CategoryContainer.removeView(view));

        CategoryContainer.addView(view);
    }

    void addIngredientRow(LinearLayout parent) {
        View row = getLayoutInflater().inflate(R.layout.ingredient_row, null);
        parent.addView(row);
    }

    // ============================
    // 스텝 추가
    // ============================

    void addStep() {
        int index = StepContainer.getChildCount();
        View view = getLayoutInflater().inflate(R.layout.step_item, null);

        TextView title = view.findViewById(R.id.StepTitle);
        ImageView img = view.findViewById(R.id.StepImageView);
        Button addImg = view.findViewById(R.id.StepImageBtn);
        Button delete = view.findViewById(R.id.DeleteStepBtn);

        title.setText("순서 " + (index + 1));

        stepImages.add(null);

        addImg.setOnClickListener(v -> pickStepImage(index));

        delete.setOnClickListener(v -> {
            if (StepContainer.getChildCount() > 1) {
                StepContainer.removeView(view);
                stepImages.remove(index);
                updateStepTitles();
            } else {
                Toast.makeText(this, "최소 1개 필요", Toast.LENGTH_SHORT).show();
            }
        });

        StepContainer.addView(view);
    }

    void updateStepTitles() {
        for (int i = 0; i < StepContainer.getChildCount(); i++) {
            View view = StepContainer.getChildAt(i);
            TextView title = view.findViewById(R.id.StepTitle);
            title.setText("순서 " + (i + 1));
        }
    }

    // ============================
    // 리스트 선택
    // ============================

    void selectDialog(String title, String[] items, TextView target) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(items, (d, w) -> target.setText(items[w]))
                .show();
    }

    // ============================
    // 서버 전송
    // ============================

    void sendRecipe() {
        try {
            String title = TitleEdt.getText().toString().trim();
            String desc = DescriptionEdt.getText().toString().trim();

            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("description", desc);
            data.put("portion", PortionTvew.getText().toString());
            data.put("cookingTime", CookingTimeTvew.getText().toString());
            data.put("difficulty", DifficultyTvew.getText().toString());

            data.put("creator", username);

            // 재료 JSON
            JSONArray categories = new JSONArray();

            for (int i = 0; i < CategoryContainer.getChildCount(); i++) {
                View v = CategoryContainer.getChildAt(i);

                EditText catNameEdt = v.findViewById(R.id.CategoryName);
                String catName = catNameEdt.getText().toString().trim();

                JSONObject catObj = new JSONObject();
                catObj.put("category", catName);
                catObj.put("order", i + 1);

                // 재료
                JSONArray ingArr = new JSONArray();
                LinearLayout ingContainer = v.findViewById(R.id.IngredientContainer);

                for (int j = 0; j < ingContainer.getChildCount(); j++) {
                    View row = ingContainer.getChildAt(j);

                    EditText name = row.findViewById(R.id.IngName);
                    EditText amount = row.findViewById(R.id.IngAmount);

                    JSONObject ingObj = new JSONObject();
                    ingObj.put("name", name.getText().toString());
                    ingObj.put("amount", amount.getText().toString());

                    ingArr.put(ingObj);
                }

                catObj.put("ingredients", ingArr);
                categories.put(catObj);
            }

            data.put("ingredients", categories);
            // 재료

            // 스텝 JSON
            JSONArray steps = new JSONArray();
            for (int i = 0; i < StepContainer.getChildCount(); i++) {
                View v = StepContainer.getChildAt(i);
                EditText stepText = v.findViewById(R.id.StepDescriptionEdt);

                JSONObject o = new JSONObject();
                o.put("step", i + 1);
                o.put("description", stepText.getText().toString());
                steps.put(o);
            }
            data.put("steps", steps);

            // Multipart 생성
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("data", data.toString());

            // 대표 이미지
            if (previewUri != null)
                builder.addFormDataPart("image", "recipe.jpg",
                        RequestBody.create(readBytes(previewUri), MediaType.parse("image/jpeg")));

            // 스텝 이미지
            for (int i = 0; i < stepImages.size(); i++) {
                Uri uri = stepImages.get(i);
                if (uri != null)
                    builder.addFormDataPart("stepImage_" + (i+1), "step_" + (i+1) + ".jpg",
                            RequestBody.create(readBytes(uri), MediaType.parse("image/jpeg")));
            }

            MultipartBody body = builder.build();

            Request req = new Request.Builder()
                    .url("https://avocadoteam.n-e.kr/api/RegisterRecipe")
                    .post(body)
                    .build();

            new OkHttpClient().newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("REGISTER", "Fail: " + e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d("REGISTER", response.body().string());
                    finish();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================
    // 이미지 전송용 byte 변환
    // ============================

    byte[] readBytes(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    boolean validateInputs() {

        // 제목
        if (TitleEdt.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 설명
        if (DescriptionEdt.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "설명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 인분
        if (PortionTvew.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "인분을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 조리시간
        if (CookingTimeTvew.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "조리 시간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 난이도
        if (DifficultyTvew.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "난이도를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 카테고리 최소 1개
        if (CategoryContainer.getChildCount() == 0) {
            Toast.makeText(this, "재료 카테고리를 1개 이상 추가해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 카테고리별 검증
        for (int i = 0; i < CategoryContainer.getChildCount(); i++) {
            View catView = CategoryContainer.getChildAt(i);

            EditText catNameEdt = catView.findViewById(R.id.CategoryName);
            if (catNameEdt.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "카테고리 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }

            LinearLayout ingContainer = catView.findViewById(R.id.IngredientContainer);

            if (ingContainer.getChildCount() == 0) {
                Toast.makeText(this, "각 카테고리에 재료를 추가해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }

            for (int j = 0; j < ingContainer.getChildCount(); j++) {
                View row = ingContainer.getChildAt(j);

                EditText ingName = row.findViewById(R.id.IngName);
                EditText ingAmount = row.findViewById(R.id.IngAmount);

                if (ingName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "재료 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ingAmount.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "재료 양을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        // 스텝 최소 1개
        if (StepContainer.getChildCount() == 0) {
            Toast.makeText(this, "조리 순서를 1개 이상 추가해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < StepContainer.getChildCount(); i++) {
            View stepView = StepContainer.getChildAt(i);

            EditText stepDesc = stepView.findViewById(R.id.StepDescriptionEdt);

            if (stepDesc.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "각 조리 순서에 설명을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }

            // 필요하면 이미지 필수도 넣기
            // ImageView img = stepView.findViewById(R.id.StepImageView);
            // if (stepImages.get(i) == null) { ... }
        }

        return true;
    }

}
