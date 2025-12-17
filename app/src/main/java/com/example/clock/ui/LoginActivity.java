package com.example.clock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clock.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class LoginActivity extends AppCompatActivity {

    EditText edtUser, edtPass;
    Button btnLogin;

    TextView txtGoSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoSignUp = findViewById(R.id.txtGoSignUp);

        txtGoSignUp.setOnClickListener(v->{
            Intent intent = new Intent(this,SignUpActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = edtUser.getText().toString();
        String password = edtPass.getText().toString();

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
        } catch (Exception e) {}

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://avocadoteam.n-e.kr/api/Login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("LOGIN", "body = " + body);

                try {
                    JSONObject res = new JSONObject(body);

                    if (res.getString("status").equals("success")) {

                        int userId = res.getInt("user_id");
                        String username = res.getString("username");

                        getSharedPreferences("user", MODE_PRIVATE)
                                .edit()
                                .putBoolean("isLogin", true)
                                .putInt("user_id", userId)
                                .putString("username", username)
                                .apply();

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        });

                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "아이디/비번 오류", Toast.LENGTH_SHORT).show()
                        );
                    }

                } catch (Exception e) {
                    Log.e("LOGIN", "parse error", e);
                }
            }


            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}

