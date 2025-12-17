package com.example.clock.ui.animator;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.example.clock.R;

public class HomeAnimation {

    private View root;
    //버튼 레이어
    private View home, filter, option;
    //컨테이너 레이어
    private  LinearLayout homeContainer, filterContainer, optionContainer;

    private View[] items;
    private View[] containerItem;

    private View circle;
    public HomeAnimation(View rootView){
        this.root = rootView;

        filterContainer = root.findViewById(R.id.filterContainer);
        optionContainer = root.findViewById(R.id.optionContainer);


        home = root.findViewById(R.id.navHome);
        filter = root.findViewById(R.id.navFilter);
        option = root.findViewById(R.id.navOption);

        items =new View[]{home, filter, option};
    }

    public void addCircle(View circle){
        this.circle = circle;
    }


    //검색바 애니메이션
    public void openSearchPanel(Context context, View searchResultPanel) {
        float screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // 하단 네비 영역 + 여유 공간 남기기
        float targetY = screenHeight * 0.01f;

        searchResultPanel.setVisibility(View.VISIBLE);
        searchResultPanel.animate()
                .translationY(targetY)//값 -해야 적당히 내려옴
                .setDuration(450)
                .start();
    }
    public void closeSearchPanel(View searchResultPanel) {
        searchResultPanel.animate()
                .translationY(-searchResultPanel.getHeight())
                .setDuration(450)
                .withEndAction(() -> searchResultPanel.setVisibility(View.GONE))
                .start();
    }



    public void openFilter(){
        filterContainer.animate()
                .translationX(0)
                .setDuration(250)
                .start();
    }

    public void closeFilter(){
        filterContainer.animate()
                .translationX(filterContainer.getWidth())
                .setDuration(250)
                .start();
    }

    public void openOption(){
        optionContainer.animate()
                .translationX(0)
                .setDuration(250)
                .start();
    }

    public void closeOption(){
        optionContainer.animate()
                .translationX(-optionContainer.getWidth())
                .setDuration(250)
                .start();
    }

    // 처음 위치 세팅 (즉시 이동)
    public void setCirclePosition(int index) {
        View item = items[index];

        item.post(() -> {
            float targetX = item.getX() + item.getWidth() / 2f - circle.getWidth() / 2f;
            circle.setTranslationX(targetX);
        });
    }
    // 클릭 시 동그라미 이동 (애니메이션)
    public void moveCircle(int index) {
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
}
