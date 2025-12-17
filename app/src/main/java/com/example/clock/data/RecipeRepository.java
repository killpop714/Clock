package com.example.clock.data;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.clock.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecipeRepository {

    //기존 Activity 저장용 변수
    Activity activity;


    // 타이틀 이미지 상수 변수
    public static final int PICK_IMAGE =100;

    //요리 순서 상수 변수
    public static final int PICK_STEP_IMAGE = 2000;
    public static ArrayList<Uri> stepImageUris = new ArrayList<>();
    public static int currentStepIndex = -1;
    public static ImageView currentStepImageView = null;
    int stepCount = 0;


    // 리스트 선택 상수 변수
    final String[] portionItem = {"1인분","2인분","3인분","4인분 이상"};
    final String[] CookingTimeItem = {"10분 이내","30분 이내","1시간 이내","1시간 이상"};
    final String[] difficultyItem = {"쉬움", "보통", "어려움"};

    public RecipeRepository(Activity activity){
        this.activity = activity;
    }

    public void CreateRecipe(@NonNull EditText titleEdit, @NonNull EditText descriptionEdit,
                             LinearLayout categoryContainer, LinearLayout stepContainer,
                             @NonNull TextView portionText, @NonNull TextView cookingTimeText, @NonNull TextView difficultyText,
                             Uri preview){

        // 1) 기본 정보 수집
        String title = titleEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();
        String portion = portionText.getText().toString().trim();
        String cookingTime = cookingTimeText.getText().toString().trim();
        String difficulty = difficultyText.getText().toString().trim();

        // 기본값 체크
        if (title.isEmpty() || description.isEmpty() || portion.isEmpty() ||
                cookingTime.isEmpty() || difficulty.isEmpty()) {

            Toast.makeText(activity, "제목 / 설명 / 인원 / 시간 / 난이도는 필수입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) 재료 데이터 검사
        JSONArray categoryArray = new JSONArray();

        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            View categoryView = categoryContainer.getChildAt(i);

            EditText categoryNameEdt = categoryView.findViewById(R.id.CategoryName);
            String categoryName = categoryNameEdt.getText().toString().trim();

            if (categoryName.isEmpty()) {
                Toast.makeText(activity, "재료 구분 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(activity, "모든 재료의 이름과 양을 입력하세요.", Toast.LENGTH_SHORT).show();
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

        for (int i = 0; i < stepContainer.getChildCount(); i++) {

            View stepView = stepContainer.getChildAt(i);

            EditText stepEdt = stepView.findViewById(R.id.StepDescriptionEdt);
            String stepText = stepEdt.getText().toString().trim();

            if (stepText.isEmpty()) {
                Toast.makeText(activity, "모든 요리 순서 설명을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject stepObj = new JSONObject();
            try {
                stepObj.put("step", i+1);
                stepObj.put("description", stepText);
            } catch (Exception ignored) {}

            stepArray.put(stepObj);
        }


        // 4) 최종 JSON 구성
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("title", title);
            jsonData.put("description", description);
            jsonData.put("portion", portion);
            jsonData.put("cookingTime", cookingTime);
            jsonData.put("difficulty", difficulty);
            jsonData.put("ingredients", categoryArray);
            jsonData.put("steps", stepArray);


            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            // JSON은 파일말고 "문자열"로 넣기
            builder.addFormDataPart("data", jsonData.toString());

            // 이미지 있을 때
            if (preview != null) {
                InputStream is = activity.getContentResolver().openInputStream(preview);
                byte[] bytes = readBytes(is);

                builder.addFormDataPart(
                        "image",
                        "recipe.jpg",
                        RequestBody.create(bytes, MediaType.parse("image/jpeg"))
                );
            }

            MultipartBody body = builder.build();

            Request request = new Request.Builder()
                    .url("https://avocadoteam.n-e.kr/api/RegisterRecipe")
                    .post(body)
                    .build();

            // 5) 서버 전송
            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d("REGISTER", response.body().string());
                    activity.finish();
                }
            });

        } catch (Exception e){ e.printStackTrace(); }
    }


    //재료 카테고리 추가 함수
    public void addCategory(LinearLayout categoryContainer) {
        View categoryView = activity.getLayoutInflater().inflate(R.layout.category_item, null);

        Button addIngBtn = categoryView.findViewById(R.id.AddIngredientBtn);
        Button deleteCatBtn = categoryView.findViewById(R.id.DeleteCategoryBtn);
        LinearLayout ingredientContainer = categoryView.findViewById(R.id.IngredientContainer);

        // 재료 추가 버튼
        addIngBtn.setOnClickListener(v -> addIngredientRow(activity,ingredientContainer));

        // 🔥 카테고리 삭제 버튼
        deleteCatBtn.setOnClickListener(v -> categoryContainer.removeView(categoryView));

        // 화면에 추가
        categoryContainer.addView(categoryView);
    }
    //재료 레이어 추가 함수(카테고리의 부속품)
    private void addIngredientRow(Context context, LinearLayout parent) {

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);

        // 재료명
        EditText name = new EditText(context);
        name.setHint("재료");
        name.setBackground(ContextCompat.getDrawable(context,R.drawable.text_bg));
        name.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 3));

        // 양
        EditText amount = new EditText(context);
        amount.setHint("양");
        amount.setBackground(ContextCompat.getDrawable(context,R.drawable.text_bg));
        amount.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));

        // 🔥 삭제 버튼
        Button deleteBtn = new Button(context);
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

    public void addStep(LinearLayout stepContainer) {
        stepCount++;

        View stepView = activity.getLayoutInflater().inflate(R.layout.step_item, null);

        TextView stepTitle = stepView.findViewById(R.id.StepTitle);
        Button deleteStepBtn = stepView.findViewById(R.id.DeleteStepBtn);

        EditText descEdt = stepView.findViewById(R.id.StepDescriptionEdt);
        ImageView stepImage = stepView.findViewById(R.id.StepImageView);
        Button stepImageBtn = stepView.findViewById(R.id.StepImageBtn);

        stepTitle.setText("순서 " + stepCount);

        // 사진 선택 버튼
        stepImageBtn.setOnClickListener(v -> {
            currentStepImageView = stepImage;
            openStepImagePicker(activity);
        });

        // 삭제 버튼 (1개 이상 유지)
        deleteStepBtn.setOnClickListener(v -> {
            if (stepContainer.getChildCount() > 1) {
                stepContainer.removeView(stepView);
                updateStepNumbers(stepContainer);
            } else {
                Toast.makeText(activity, "최소 1개의 요리 순서는 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        stepContainer.addView(stepView);
    }
    private void updateStepNumbers(LinearLayout stepContainer) {
        int count = stepContainer.getChildCount();
        stepCount = count;

        for (int i = 0; i < count; i++) {
            View stepView = stepContainer.getChildAt(i);
            TextView title = stepView.findViewById(R.id.StepTitle);
            title.setText("순서 " + (i + 1));
        }
    }
    private void openStepImagePicker(Activity currentActivity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        currentActivity.startActivityForResult(intent, PICK_STEP_IMAGE);
    }



    //인분 선택 함수
    public void PortionSelect(TextView view){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("인원");
        builder.setItems(portionItem, (dialog,which)->{
            String selected = portionItem[which];
            view.setText(selected);
        });
        builder.show();
    }

    //요리 시간 선택 함수
    public void CookingTimeSelect(TextView view){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("인원");
        builder.setItems(CookingTimeItem, (dialog,which)->{
            String selected = CookingTimeItem[which];
            view.setText(selected);
        });
        builder.show();
    }

    //난이도 선택 함수
    public void DifficultySelect(TextView view){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("인원");
        builder.setItems(difficultyItem, (dialog,which)->{
            String selected = difficultyItem[which];
            view.setText(selected);
        });
        builder.show();
    }

    //타이틀 이미지 선택 함수
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }
}
