package com.example.clock.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.clock.R;
import com.example.clock.model.Ingredient;
import com.example.clock.model.Recipe;
import com.example.clock.adapter.RecipeAdapter;
import com.example.clock.adapter.SearchTextAdapter;
import com.example.clock.ui.animator.HomeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private HomeAnimation animation;

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private ArrayList<Recipe> list = new ArrayList<>();

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int page = 0;

    private EditText searchBar;
    private String keyword = "";





    //ui 전용
    private View circle;
    private LinearLayout home, filter, option;
    private View[] items;


    //상단 검색 리스트
    private View searchResultPanel;
    private RecyclerView searchResultList;
    private SearchTextAdapter searchAdapter;
    private ArrayList<Recipe> searchResults = new ArrayList<>();

    // 래시피 등록 버튼
    private ImageView btnRegister;



    // 🔵 필터 패널
    private LinearLayout filterContainer;

    // 🔵 최근 업데이트
    private RadioGroup filterRecent;
    private RadioButton recent_1day, recent_7day, recent_30day;

    // 🟩 조리 시간
    private RadioGroup filterTime;
    private RadioButton time_10, time_30, time_60, time_over;

    // 🟧 난이도
    private RadioGroup filterDifficulty;
    private RadioButton diff_easy, diff_medium, diff_hard;

    // 🟪 인분
    private RadioGroup filterServing;
    private RadioButton serve_1, serve_2, serve_3, serve_4;

    // 적용 버튼
    private Button btnApplyFilter;

    boolean isSubmitting = false;


    //http 연결 변수
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        //검색 바 전용
        searchBar = findViewById(R.id.searchBar);

        //리 사이클 뷰 전용
        recyclerView = findViewById(R.id.recipeRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, list);
        recyclerView.setAdapter(adapter);

        //필터 바 연결
        filterContainer = findViewById(R.id.filterContainer);
        // 최근 업데이트
        filterRecent = findViewById(R.id.filterRecent);
        recent_1day = findViewById(R.id.recent_1day);
        recent_7day = findViewById(R.id.recent_7day);
        recent_30day = findViewById(R.id.recent_30day);

// 조리 시간
        filterTime = findViewById(R.id.filterTime);
        time_10 = findViewById(R.id.time_10);
        time_30 = findViewById(R.id.time_30);
        time_60 = findViewById(R.id.time_60);
        time_over = findViewById(R.id.time_over);

// 난이도
        filterDifficulty = findViewById(R.id.filterDifficulty);
        diff_easy = findViewById(R.id.diff_easy);
        diff_medium = findViewById(R.id.diff_medium);
        diff_hard = findViewById(R.id.diff_hard);

// 인분
        filterServing = findViewById(R.id.filterServing);
        serve_1 = findViewById(R.id.serve_1);
        serve_2 = findViewById(R.id.serve_2);
        serve_3 = findViewById(R.id.serve_3);
        serve_4 = findViewById(R.id.serve_4);

// 적용 버튼
        btnApplyFilter = findViewById(R.id.btnApplyFilter);

        //하단탭바 전용
        circle = findViewById(R.id.highlightCircle);
        home = findViewById(R.id.navHome);
        filter = findViewById(R.id.navFilter);
        option = findViewById(R.id.navOption);
        View root = findViewById(android.R.id.content);
        animation = new HomeAnimation(root);
        animation.addCircle(circle);


        //상단 검색 리스트
        searchResultPanel = findViewById(R.id.searchResultPanel);
        searchResultList = findViewById(R.id.searchResultList);

        //래시피 등록 버튼
        btnRegister = findViewById(R.id.btnRegister);


        View rootView = getWindow().getDecorView().getRootView();




        //검색 키워드
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);



        searchAdapter = new SearchTextAdapter(this, searchResults);
        searchResultList.setLayoutManager(lm);
        searchResultList.setAdapter(searchAdapter);


        // 첫 페이지 로딩
        loadPage(true);


        //기초 애니메이션
        items = new View[]{home, filter, option};
        // 초기 위치: 홈
        animation.setCirclePosition(0);

        //홈버튼
        home.setOnClickListener(v ->{
            animation.moveCircle(0);
            animation.closeFilter();
            animation.closeOption();

        });

        //필터 컨테이너 버튼
        filter.setOnClickListener(v ->{
            animation.moveCircle(1);
            animation.openFilter();
            animation.closeOption();
        });

        //설정 컨테이너 버튼
        option.setOnClickListener(v->{
            animation.moveCircle(2);
            animation.openOption();
            animation.closeFilter();
        });

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("user", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);

        boolean isLogin = prefs.getBoolean("isLogin", false);

        if (!isLogin) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 무한 스크롤
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();

                // 스크롤이 아래로 내려가는 중 + 로딩 중 아니고 + 마지막 페이지 아니면
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                        // 마지막 근처에 도달하면 다음 페이지 요청
                        page++;
                        loadPage(false); // false면 기존 리스트 유지 후 뒤에 추가
                    }
                }
            }
        });


        btnApplyFilter.setOnClickListener(v -> {

            // 선택된 필터 가져오기
            Map<String, String> filters = getFilterParams();

            // 페이지 초기화
            page = 0;
            isLastPage = false;
            list.clear();
            adapter.notifyDataSetChanged();

            // 다시 API 호출
            loadPage(true, filters);

            // 패널 닫기 (있을 경우)
           animation.closeFilter();
           animation.moveCircle(0);

        });




//        searchBar.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//
//                keyword = searchBar.getText().toString().trim();
//
//                animation.openSearchPanel(getApplicationContext(), searchResultPanel); //패널 열기
//                loadSearchResults(keyword); //패널 검색
//                //searchResultList.scrollToPosition(searchResults.size() - 1);
//
//                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
////                    searchBar.dispatchKeyEvent(
////                            new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
////                    );
//                    Log.d("작동중","작동");
//                    page = 0;
//                    isLastPage = false;
//
//                    loadPage(true);  // 리스트 초기화 후 다시 로딩
//
//                    animation.closeSearchPanel(searchResultPanel);
//
//
//                    return true;
//                }else{
//                    return false;
//                }
//            }
//        });




        searchBar.addTextChangedListener(new TextWatcher() {


            @Override
            public void afterTextChanged(Editable editable) {
                animation.openSearchPanel(getApplicationContext(), searchResultPanel); //패널 열기
                loadSearchResults(editable.toString().trim()); //패널 검색\
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });
//
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                Log.d("작동중","작동");
                keyword = searchBar.getText().toString().trim();

                page = 0;
                isLastPage = false;
                loadPage(true); // 최종 검색

                animation.closeSearchPanel(searchResultPanel);
                return true;
            }
            Log.d("작동중","작동");
            return false;
        });


        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootView.getHeight();
            int keypadHeight = screenHeight - r.bottom;

            boolean isKeyboardOpen = keypadHeight > screenHeight * 0.15;

            if (!isKeyboardOpen) {
                // 🔽 키보드가 내려갔을 때
                animation.closeSearchPanel(searchResultPanel);
            }
        });



        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterRecipe.class);
                startActivity(intent);
            }
        });
    }


    private void loadPage(boolean clearFirst) {

        if(isLoading) return;
        isLoading = true;

        if (clearFirst) {
            list.clear();
            adapter.notifyDataSetChanged();
        }

        String baseUrl = "https://avocadoteam.n-e.kr/api/HomeFetch";
        String url = baseUrl + "?page=" + page;
        if (!keyword.isEmpty()) {
            url += "&query=" + keyword;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                Log.e("HTTP", "Fail: " + e.getMessage());
                isSubmitting = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("HTTP", "Response: " + body);

                try {
                    JSONArray arr = new JSONArray(body);

                    if (arr.length() == 0) {
                        // 더 이상 페이지 없음
                        isLastPage = true;
                    } else {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            Recipe r = new Recipe(
                                    o.optInt("id"),
                                    o.optString("title"),
                                    o.optString("description"),
                                    o.optString("imageUrl"),
                                    o.optInt("point"),

                                    o.optString("portion"),
                                    o.optString("cookingTime"),
                                    o.optString("difficulty")


                            );
                            list.add(r);
                        }
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (JSONException e) {
                    Log.e("JSON", "Parse error: " + e.getMessage());
                }

                isLoading = false;
                isSubmitting = false;


            }
        });
    }

    private void loadPage(boolean clear, Map<String, String> filters) {

        if (isLoading || isLastPage) return;
        isLoading = true;

        HttpUrl.Builder builder = HttpUrl.parse("https://avocadoteam.n-e.kr/api/HomeFilter")
                .newBuilder()
                .addQueryParameter("page", String.valueOf(page));

        // 🔥 필터 추가
        if (filters != null) {
            for (String key : filters.keySet()) {
                builder.addQueryParameter(key, filters.get(key));
            }
        }

        HttpUrl url = builder.build();

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                Log.d("HOME", "Response: " + body);

                try {
                    JSONArray arr = new JSONArray(body);

                    if (clear) list.clear();

                    if (arr.length() == 0) {
                        isLastPage = true;
                    } else {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            list.add(new Recipe(
                                    o.optInt("id"),
                                    o.optString("title"),
                                    o.optString("description"),
                                    o.optString("imageUrl"),
                                    o.optInt("point"),

                                    o.optString("portion"),
                                    o.optString("cookingTime"),
                                    o.optString("difficulty")
                            ));
                        }
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                isLoading = false;
            }
        });
    }




    private void loadSearchResults(String query) {

        Log.d("스트링",query);
        searchResults.clear();

        String url = "https://avocadoteam.n-e.kr/api/SearchRecipe?q=" + query;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("API","실패 : " +e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d("API","성공");
                try {
                    JSONArray arr = new JSONArray(response.body().string());

                    runOnUiThread(() -> {
                        for (int i = 0; i < arr.length(); i++) {
                            try {
                                JSONObject o = arr.getJSONObject(i);

                                // 🔥 자동완성은 title만 사용
                                searchResults.add(new Recipe(
                                        o.getInt("id"),
                                        o.getString("title"),
                                        "",
                                        "",
                                        0,
                                        "",
                                        "",
                                        ""

                                ));

                            } catch (Exception ignore) {}
                        }

                        searchAdapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {}
            }
        });
    }

    private Map<String, String> getFilterParams() {

        Map<String, String> params = new HashMap<>();

        // 🔵 최근 업데이트
        int recentId = filterRecent.getCheckedRadioButtonId();
        if (recentId == R.id.recent_1day) params.put("recent", "1");
        else if (recentId == R.id.recent_7day) params.put("recent", "7");
        else if (recentId == R.id.recent_30day) params.put("recent", "30");


        // 🟩 조리 시간
        int timeId = filterTime.getCheckedRadioButtonId();
        if (timeId == R.id.time_10) params.put("time", "10");
        else if (timeId == R.id.time_30) params.put("time", "30");
        else if (timeId == R.id.time_60) params.put("time", "60");
        else if (timeId == R.id.time_over) params.put("time", "over");

        // 🟧 난이도
        int diffId = filterDifficulty.getCheckedRadioButtonId();
        if (diffId == R.id.diff_easy) params.put("difficulty", "easy");
        else if (diffId == R.id.diff_medium) params.put("difficulty", "medium");

        // 인분
        int servingId = filterServing.getCheckedRadioButtonId();
        if (servingId == R.id.serve_1) params.put("serving", "1");
        else if (servingId == R.id.serve_2) params.put("serving", "2");
        else if (servingId == R.id.serve_3) params.put("serving", "3");
        else if (servingId == R.id.serve_4) params.put("serving", "4+");

        return params;
    }


}
