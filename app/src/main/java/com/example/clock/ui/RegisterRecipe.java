package com.example.clock.ui;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import static com.example.clock.data.RecipeRepository.PICK_IMAGE;
import static com.example.clock.data.RecipeRepository.PICK_STEP_IMAGE;
import static com.example.clock.data.RecipeRepository.currentStepImageView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.widget.Toast;

import com.example.clock.R;
import com.example.clock.data.RecipeRepository;

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



    RecipeRepository repo = new RecipeRepository(this);
    ImageView ImageVew;//이미지 버튼 추가용

    EditText TitleEdt, DescriptionEdt;

    //프리뷰 이미지
    Uri previewUri = null;

    //재료 구분 레이어
    LinearLayout CategoryContainer;
    Button AddCategoryBtn;

    //요리 순서 레이어
    LinearLayout StepContainer;
    Button AddStepBtn;





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

        //타이틀 이미지 선택 버튼
        ImageVew.setOnClickListener(v ->  {

                repo.openGallery();
        });



        //재료 정보 레이어 추가 버튼 리스너
        AddCategoryBtn.setOnClickListener(v->{
                repo.addCategory(CategoryContainer);
        });

        //요리 순서 레이어 추가 버튼 리스너
        repo.addStep(StepContainer);
        AddStepBtn.setOnClickListener(v->{
            repo.addStep(StepContainer);
        });



        //인분 버튼 리스너
        PortionBtn.setOnClickListener(v->{
            repo.PortionSelect(PortionTvew);
        });
        //요리 시간 버튼 리스너
        CookingTimeBtn.setOnClickListener(v->{
            repo.CookingTimeSelect(CookingTimeTvew);
        });
        //난이도 버튼 리스너
        DifficultyBtn.setOnClickListener(v->{
            repo.DifficultySelect(DifficultyTvew);
        });

        //레시피 제작 버튼
        RegisterRecipe.setOnClickListener(v -> {
            repo.CreateRecipe(TitleEdt, DescriptionEdt,
                    CategoryContainer, StepContainer,
                    PortionTvew, CookingTimeTvew, DifficultyTvew,
                    previewUri);
        });
    }

    //데이터 받아오는 오버라이드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            // Step 이미지 선택


            if (requestCode == PICK_STEP_IMAGE && currentStepImageView != null) {
                Uri image = data.getData();
                currentStepImageView.setImageURI(image);
                return;
            }

            // 기존 요리 대표 이미지 기능은 여기에 넣으면 됨
            if (requestCode == PICK_IMAGE) {
                previewUri = data.getData();
                ImageVew.setImageURI(previewUri);
            }
        }
    }
}
