package com.example.clock;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.CountDownTimer;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterRecipe extends Activity {


    ImageView ImageVew;//이미지 버튼 추가용

    EditText TitleEdt, DescriptionEdt;

    //재료 구분 레이어
    LinearLayout CategoryContainer;
    Button AddCategoryBtn;

    //요리 순서 레이어
    LinearLayout StepContainer;
    Button AddStepBtn;

    int stepCount = 0;





    TextView PortionTvew, CookingTimeTvew, DifficultyTvew;
    Button PortionBtn, CookingTimeBtn, DifficultyBtn;
    Button RegisterRecipe;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_recipe);

        //타이틀 이미지 추가 버튼과 타이틀 이미지 뷰
        ImageVew =findViewById(R.id.ImageVew);



        //제목
        TitleEdt = findViewById(R.id.TitleEdt);

        //설명
        DescriptionEdt = findViewById(R.id.DescriptionEdt);

        //재료 정보
        CategoryContainer = findViewById(R.id.CategoryContainer);
        AddCategoryBtn = findViewById(R.id.AddCategoryBtn);

        //요리 순서
        StepContainer = findViewById(R.id.StepContainer);
        AddStepBtn = findViewById(R.id.AddStepBtn);


        //리스트 뷰 모음
        PortionTvew = findViewById(R.id.PortionTvew);
        PortionBtn = findViewById(R.id.PortionBtn);

        CookingTimeTvew = findViewById(R.id.CookingTimeTvew);
        CookingTimeBtn = findViewById(R.id.CookingTimeBtn);

        DifficultyTvew = findViewById(R.id.DifficultyTvew);
        DifficultyBtn = findViewById(R.id.DifficultyBtn);
        ////

        RegisterRecipe = findViewById(R.id.RegisterRecipe);



        String[] portionItem = {"1인분","2인분","3인분","4인분","5인분","6인분 이상"};
        String[] timeItem = {"5분 이내","10분 이내","15분 이내","20분 이내","30분 이내","60분 이내","1시간 이내","2시간 이내","2시간 이상"};
        String[] difficultyItem = {"쉬움", "보통", "어려움", "매우 어려움"};


        //이미지 선택 버튼
        ImageVew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        AddCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategory();
            }
        });


        //요리 순서 리스너
        addStep();
        AddStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStep();
            }
        });

        //인분 선택 버튼
        PortionBtn.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인원");
            builder.setItems(portionItem, (dialog,which)->{
                String selected = portionItem[which];
                PortionTvew.setText(selected);
            });
            builder.show();
        });

        CookingTimeBtn.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인원");
            builder.setItems(timeItem, (dialog,which)->{
                String selected = timeItem[which];
                CookingTimeTvew.setText(selected);
            });
            builder.show();
        });

        DifficultyBtn.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인원");
            builder.setItems(difficultyItem, (dialog,which)->{
                String selected = difficultyItem[which];
                DifficultyTvew.setText(selected);
            });
            builder.show();
        });


        RegisterRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateRecipe();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            // Step 이미지 선택
            if (requestCode == PICK_STEP_IMAGE && currentStepImageView != null) {
                Uri imageUri = data.getData();
                currentStepImageView.setImageURI(imageUri);
                return;
            }

            // 기존 요리 대표 이미지 기능은 여기에 넣으면 됨
            if (requestCode == PICK_IMAGE) {
                Uri imageUri = data.getData();
                ImageVew.setImageURI(imageUri);
            }
        }
    }
    //이미지 선택 함수
    private static final int PICK_IMAGE =100;
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void addCategory() {
        View categoryView = getLayoutInflater().inflate(R.layout.category_item, null);

        Button addIngBtn = categoryView.findViewById(R.id.AddIngredientBtn);
        Button deleteCatBtn = categoryView.findViewById(R.id.DeleteCategoryBtn);
        LinearLayout ingredientContainer = categoryView.findViewById(R.id.IngredientContainer);

        // 재료 추가 버튼
        addIngBtn.setOnClickListener(v -> addIngredientRow(ingredientContainer));

        // 🔥 카테고리 삭제 버튼
        deleteCatBtn.setOnClickListener(v -> CategoryContainer.removeView(categoryView));

        // 화면에 추가
        CategoryContainer.addView(categoryView);
    }



    //재료 레이어 추가 함수
    private void addIngredientRow(LinearLayout parent) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        // 재료명
        EditText name = new EditText(this);
        name.setHint("재료");
        name.setBackground(getDrawable(R.drawable.text_bg));
        name.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 3));

        // 양
        EditText amount = new EditText(this);
        amount.setHint("양");
        amount.setBackground(getDrawable(R.drawable.text_bg));
        amount.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));

        // 🔥 삭제 버튼
        Button deleteBtn = new Button(this);
        deleteBtn.setText("X");
        deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT));

        deleteBtn.setOnClickListener(v -> parent.removeView(row));

        // row 에 아이템 추가
        row.addView(name);
        row.addView(amount);
        row.addView(deleteBtn);

        parent.addView(row);
    }


    //요리 순서 함수
    private static final int PICK_STEP_IMAGE = 2000;
    private ImageView currentStepImageView = null;

    private void addStep() {
        stepCount++;

        View stepView = getLayoutInflater().inflate(R.layout.step_item, null);

        TextView stepTitle = stepView.findViewById(R.id.StepTitle);
        Button deleteStepBtn = stepView.findViewById(R.id.DeleteStepBtn);

        EditText descEdt = stepView.findViewById(R.id.StepDescriptionEdt);
        ImageView stepImage = stepView.findViewById(R.id.StepImageView);
        Button stepImageBtn = stepView.findViewById(R.id.StepImageBtn);

        stepTitle.setText("순서 " + stepCount);

        // 사진 선택 버튼
        stepImageBtn.setOnClickListener(v -> {
            currentStepImageView = stepImage;
            openStepImagePicker();
        });

        // 삭제 버튼 (1개 이상 유지)
        deleteStepBtn.setOnClickListener(v -> {
            if (StepContainer.getChildCount() > 1) {
                StepContainer.removeView(stepView);
                updateStepNumbers();
            } else {
                Toast.makeText(this, "최소 1개의 요리 순서는 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        StepContainer.addView(stepView);
    }
    private void updateStepNumbers() {
        int count = StepContainer.getChildCount();
        stepCount = count;

        for (int i = 0; i < count; i++) {
            View stepView = StepContainer.getChildAt(i);
            TextView title = stepView.findViewById(R.id.StepTitle);
            title.setText("순서 " + (i + 1));
        }
    }
    private void openStepImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_STEP_IMAGE);
    }


    private void CreateRecipe(){

        // 1) 기본 정보 수집
        String title = TitleEdt.getText().toString().trim();
        String description = DescriptionEdt.getText().toString().trim();
        String portion = PortionTvew.getText().toString().trim();
        String cookingTime = CookingTimeTvew.getText().toString().trim();
        String difficulty = DifficultyTvew.getText().toString().trim();

        // 기본값 체크
        if (title.isEmpty() || description.isEmpty() || portion.isEmpty() ||
                cookingTime.isEmpty() || difficulty.isEmpty()) {

            Toast.makeText(this, "제목 / 설명 / 인원 / 시간 / 난이도는 필수입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) 재료 데이터 검사
        JSONArray categoryArray = new JSONArray();

        for (int i = 0; i < CategoryContainer.getChildCount(); i++) {
            View categoryView = CategoryContainer.getChildAt(i);

            EditText categoryNameEdt = categoryView.findViewById(R.id.CategoryName);
            String categoryName = categoryNameEdt.getText().toString().trim();

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "재료 구분 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            LinearLayout ingredientContainer = categoryView.findViewById(R.id.IngredientContainer);

            // 재료 목록
            JSONArray ingredientArray = new JSONArray();

            for (int j = 0; j < ingredientContainer.getChildCount(); j++) {
                View ingRow = ingredientContainer.getChildAt(j);

                EditText ingNameEdt = ingRow.findViewById(R.id.IngName);
                EditText ingAmountEdt = ingRow.findViewById(R.id.IngAmount);

                String ingName = ingNameEdt.getText().toString().trim();
                String ingAmount = ingAmountEdt.getText().toString().trim();

                if (ingName.isEmpty() || ingAmount.isEmpty()) {
                    Toast.makeText(this, "모든 재료의 이름과 양을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject ingObj = new JSONObject();
                try {
                    ingObj.put("name", ingName);
                    ingObj.put("amount", ingAmount);
                } catch (Exception ignored) {}

                ingredientArray.put(ingObj);
            }

            // 재료 카테고리 구성
            JSONObject categoryObj = new JSONObject();
            try {
                categoryObj.put("category", categoryName);
                categoryObj.put("ingredients", ingredientArray);
            } catch (Exception ignored) {}

            categoryArray.put(categoryObj);
        }


        // 3) 요리 순서 검사
        JSONArray stepArray = new JSONArray();

        for (int i = 0; i < StepContainer.getChildCount(); i++) {

            View stepView = StepContainer.getChildAt(i);

            EditText stepEdt = stepView.findViewById(R.id.StepDescriptionEdt);
            String stepText = stepEdt.getText().toString().trim();

            if (stepText.isEmpty()) {
                Toast.makeText(this, "모든 요리 순서 설명을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject stepObj = new JSONObject();
            try {
                stepObj.put("order", i+1);
                stepObj.put("text", stepText);
            } catch (Exception ignored) {}

            stepArray.put(stepObj);
        }


        // 4) 최종 JSON 구성
        JSONObject finalJson = new JSONObject();
        try {
            finalJson.put("title", title);
            finalJson.put("description", description);
            finalJson.put("portion", portion);
            finalJson.put("cookingTime", cookingTime);
            finalJson.put("difficulty", difficulty);
            finalJson.put("ingredients", categoryArray);
            finalJson.put("steps", stepArray);

        } catch (Exception e){ e.printStackTrace(); }

        // 5) 서버 전송
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                finalJson.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://avocadoteam.n-e.kr/api/RegisterRecipe")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("Server", "Response: " + response.body().string());
                finish();
            }
        });
    }


}
