package com.example.lucincin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvRegister, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, register.class);
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra không để trống
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Request
        LoginRequest request = new LoginRequest(email, password);

        // Gọi API Đăng nhập
        RetrofitClient.getApiService().loginUser(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {

                // Thành công HTTP 200
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();

                    if ("success".equals(userResponse.getStatus()) && userResponse.getUser() != null) {
                        User loggedInUser = userResponse.getUser();

                        // LƯU THÔNG TIN USER VÀO BỘ NHỚ ĐIỆN THOẠI (SharedPreferences)
                        SharedPreferences sharedPref = getSharedPreferences("LucincinApp", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("USER_ID", loggedInUser.getId());
                        editor.putString("USER_NAME", loggedInUser.getName());
                        editor.apply(); // Xác nhận lưu

                        Toast.makeText(login.this, "Chào mừng " + loggedInUser.getName() + "!", Toast.LENGTH_SHORT).show();

                        // Chuyển sang màn hình Feed (Bảng tin)
                        Intent intent = new Intent(login.this, feed.class);
                        startActivity(intent);
                        finish(); // Đóng Activity đăng nhập để không back lại được
                    } else {
                        Toast.makeText(login.this, "Lỗi: " + userResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                // Xử lý các mã lỗi theo đúng file Swagger
                else if (response.code() == 401) {
                    Toast.makeText(login.this, "Sai Email hoặc Mật khẩu!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 400) {
                    Toast.makeText(login.this, "Thiếu trường dữ liệu bắt buộc!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(login.this, "Lỗi Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(login.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}