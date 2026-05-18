package com.example.lucincin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class register extends AppCompatActivity {
    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(register.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(register.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterRequest request = new RegisterRequest(name, email, phone, password);
                RetrofitClient.getApiService().registerUser(request).enqueue(new Callback<UserResponse>() {

                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        // Thành công HTTP 201 hoặc 200
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse userResponse = response.body();

                            if ("success".equals(userResponse.getStatus())) {
                                Toast.makeText(register.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();

                                // Đóng màn hình Register, hệ thống sẽ tự lùi về màn hình Login trước đó
                                finish();
                            } else {
                                Toast.makeText(register.this, "Lỗi: " + userResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        // Lỗi 409: Theo Swagger là "Email or phone already exists"
                        else if (response.code() == 409) {
                            Toast.makeText(register.this, "Email hoặc Số điện thoại đã được sử dụng!", Toast.LENGTH_SHORT).show();
                        }
                        // Lỗi 400: Thiếu trường bắt buộc
                        else if (response.code() == 400) {
                            Toast.makeText(register.this, "Dữ liệu không hợp lệ (Lỗi 400)!", Toast.LENGTH_SHORT).show();
                        }
                        // Các lỗi Server khác
                        else {
                            Toast.makeText(register.this, "Đăng ký thất bại. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Toast.makeText(register.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}