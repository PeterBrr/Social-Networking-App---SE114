package com.example.lucincin;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Địa chỉ gốc của API (Lưu ý: Luôn phải có dấu gạch chéo '/' ở cuối)
    private static final String BASE_URL = "http://blackntt.net:8111/";
    private static Retrofit retrofit = null;

    // Áp dụng mô hình Singleton để chỉ tạo Retrofit 1 lần duy nhất
    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Tự động dịch JSON
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
