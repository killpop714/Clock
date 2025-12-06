package com.example.clock.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadingData extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //clinet 설정
        OkHttpClient client = new OkHttpClient();
        //서버 url
        String url = "https://avocadoteam.n-e.kr/api/Fetch";


        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Toast.makeText(LoadingData.this, "데이터를 불러오는데 실패했습니다.\n네트워크를 다시 확인해주세요.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String data = response.body().toString();
                if(response.isSuccessful()){

                    Log.d("Sever Response",data);
                }
                Log.d("Sever Response",data);

                Log.d("작동중","작동중");
            }
        });
    }
}
