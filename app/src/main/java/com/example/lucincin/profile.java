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

public class profile extends AppCompatActivity {
    private TextView tvGreeting;
    private EditText etName, etEmail, etAddress, etAvatarUrl, etDescription;
    private Button btnSave, btnLogout;

    private int myUserId;
    private String userPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvGreeting = findViewById(R.id.tvGreeting);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        etEmail.setEnabled(false);

        SharedPreferences sharedPref = getSharedPreferences("LucincinApp", Context.MODE_PRIVATE);
        myUserId = sharedPref.getInt("USER_ID", -1);

        if (myUserId != -1) {
            // Tải dữ liệu hồ sơ từ Server về
            loadUserProfile();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(profile.this, login.class);

                // Cờ (Flag) này giúp xóa toàn bộ các Activity đang lưu trong bộ nhớ
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadUserProfile() {
        RetrofitClient.getApiService().getUserProfile(myUserId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        // Đổ dữ liệu từ Server vào các ô giao diện
                        tvGreeting.setText(user.getName() + "!");
                        etName.setText(user.getName());
                        etEmail.setText(user.getEmail());
                        etAddress.setText(user.getAddress());
                        etAvatarUrl.setText(user.getAvatarUrl());
                        etDescription.setText(user.getDescription());

                        // Lưu lại số điện thoại để giữ nguyên nếu user không đổi số
                        userPhone = user.getPhone();
                    }
                } else {
                    Toast.makeText(profile.this, "Không thể tải thông tin hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(profile.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String avatarUrl = etAvatarUrl.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đóng gói dữ liệu vào đối tượng Request
        UpdateProfileRequest request = new UpdateProfileRequest(name, address, avatarUrl, description, userPhone);

        // Gọi API PATCH lên server
        RetrofitClient.getApiService().updateUserProfile(myUserId, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        Toast.makeText(profile.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();

                        // Cập nhật lại thanh tiêu đề Greeting
                        tvGreeting.setText(name + "!");
                    } else {
                        Toast.makeText(profile.this, "Lỗi: " + res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 409) {
                    Toast.makeText(profile.this, "Số điện thoại cập nhật đã tồn tại!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(profile.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(profile.this, "Lỗi kết nối hệ thống!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}