package com.example.clock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

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


        //하단탭바 전용
        circle = findViewById(R.id.highlightCircle);
        home = findViewById(R.id.navHome);
        filter = findViewById(R.id.navFilter);
        option = findViewById(R.id.navOption);

        //상단 검색 리스트
        searchResultPanel = findViewById(R.id.searchResultPanel);
        searchResultList = findViewById(R.id.searchResultList);

        //래시피 등록 버튼
        btnRegister = findViewById(R.id.btnRegister);



        //검색 키워드
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);



        searchAdapter = new SearchTextAdapter(this, searchResults);
        searchResultList.setLayoutManager(lm);
        searchResultList.setAdapter(searchAdapter);


        // 첫 페이지 로딩
        loadPage(true);


        items = new View[]{home, filter, option};

        // 초기 위치: 홈
        setCirclePosition(0);

        // 클릭 이벤트 연결
        setListeners();

        // 무한 스크롤
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (!rv.canScrollVertically(1) && !isLoading && !isLastPage) {
                    loadPage(false);
                }
            }
        });

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);

            intent.putExtra("id", item.id);
            intent.putExtra("title", item.title);
            intent.putExtra("description", item.description);
            intent.putExtra("imageUrl", item.imageUrl);

            startActivity(intent);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyword = s.toString().trim();

                if (keyword.isEmpty()) {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                    closeSearchPanel();
                } else {
                    openSearchPanel();
                    loadSearchResults(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;

            if (actionId == EditorInfo.IME_ACTION_SEARCH || enterPressed) {
                performSearch();
                return true;
            }
            return false;
        });

        searchAdapter.setOnSuggestionClickListener(suggestion -> {
            searchBar.setText(suggestion.getTitle());
            searchBar.setSelection(searchBar.getText().length());
            keyword = suggestion.getTitle();
            performSearch();
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegisterRecipe.class);
                startActivity(intent);
            }
        });
    }


    private void performSearch() {
        keyword = searchBar.getText().toString().trim();
        page = 0;
        isLastPage = false;

        loadPage(true);
        closeSearchPanel();
    }

    private void loadPage(boolean clearFirst) {
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
                                    o.optInt("point")
                            );
                            list.add(r);
                        }
                        page++;
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (JSONException e) {
                    Log.e("JSON", "Parse error: " + e.getMessage());
                }

                isLoading = false;


            }
        });

        
    }
    private void setListeners() {

        for (int i = 0; i < items.length; i++) {
            int index = i;

            items[i].setOnClickListener(v -> {
                moveCircle(index);
                handleNavigation(index);
            });
        }
    }

    // 처음 위치 세팅 (즉시 이동)
    private void setCirclePosition(int index) {
        View item = items[index];

        item.post(() -> {
            float targetX = item.getX() + item.getWidth() / 2f - circle.getWidth() / 2f;
            circle.setTranslationX(targetX);
        });
    }

    private void handleNavigation(int index) {
        String message;
        switch (index) {
            case 0:
                message = "홈";
                break;
            case 1:
                message = "필터";
                break;
            default:
                message = "설정";
                break;
        }

        Toast.makeText(this, message + " 메뉴 준비 중", Toast.LENGTH_SHORT).show();
    }

    // 클릭 시 동그라미 이동 (애니메이션)
    private void moveCircle(int index) {
        View item = items[index];

        item.post(() -> {

            float targetX = item.getX() + item.getWidth() / 2f - circle.getWidth() / 2f;

            // 1) 먼저 작아지는 애니메이션
            circle.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(120)
                    .withEndAction(() -> {

                        // 2) 작아진 상태에서 이동
                        circle.animate()
                                .translationX(targetX)
                                .setDuration(200)
                                .withEndAction(() -> {

                                    // 3) 도착할 때 크게 튀어오름
                                    circle.animate()
                                            .scaleX(1.15f)
                                            .scaleY(1.15f)
                                            .setDuration(120)
                                            .withEndAction(() -> {

                                                // 4) 마지막 원래 크기 복귀
                                                circle.animate()
                                                        .scaleX(1f)
                                                        .scaleY(1f)
                                                        .setDuration(120)
                                                        .start();

                                            })
                                            .start();

                                })
                                .start();

                    })
                    .start();

        });
    }


    private void openSearchPanel() {
        searchResultPanel.setVisibility(View.VISIBLE);
        searchResultPanel.animate()
                .translationY(0)
                .setDuration(300)
                .start();
    }

    private void closeSearchPanel() {
        float target = searchResultPanel.getHeight() == 0
                ? -500f
                : -searchResultPanel.getHeight();
        searchResultPanel.animate()
                .translationY(target)
                .setDuration(300)
                .withEndAction(() -> searchResultPanel.setVisibility(View.GONE))
                .start();
    }

    private void loadSearchResults(String query) {

        searchResults.clear();

        String url = "https://avocadoteam.n-e.kr/api/HomeFetch?query=" + query;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    JSONArray arr = new JSONArray(response.body().string());

                    runOnUiThread(() -> {
                        for (int i = 0; i < arr.length(); i++) {

                            try{
                                JSONObject o = arr.getJSONObject(i);

                                // 🔥 자동완성은 title만 필요하므로 Recipe를 검색어 컨테이너로 사용
                                searchResults.add(new Recipe(
                                        o.getInt("id"),
                                        o.getString("title"), // 자동완성에 표시될 값
                                        "",
                                        "",
                                        0
                                ));
                            }catch(Exception ignore){}
                        }
                        searchAdapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {}
            }
        });
    }




}
