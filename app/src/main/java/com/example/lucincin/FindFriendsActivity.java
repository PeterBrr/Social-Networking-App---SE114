package com.example.lucincin;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindFriendsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int READ_CONTACTS_REQUEST_CODE = 100;
    private static final int CONTACT_LOADER = 1;

    private RecyclerView rvContacts;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends); // Gọi layout có chứa RecyclerView

        // 1. Ánh xạ và cài đặt RecyclerView
        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        contactAdapter = new ContactAdapter();
        rvContacts.setAdapter(contactAdapter);

        // 2. Bắt đầu kiểm tra quyền
        // checkPermissionAndLoadContacts(); // Uncomment if you want to load local contacts
        loadFriendsFromServer();
    }

    private void checkPermissionAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
        } else {
            // Đã có quyền -> Bật máy hút dữ liệu danh bạ
            LoaderManager.getInstance(this).initLoader(CONTACT_LOADER, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LoaderManager.getInstance(this).initLoader(CONTACT_LOADER, null, this);
            } else {
                Toast.makeText(this, "Không có quyền đọc danh bạ!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CONTACT_LOADER) {
            String[] SELECTED_FIELDS = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            };
            return new CursorLoader(this, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    SELECTED_FIELDS, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        }
        throw new IllegalArgumentException("Unknown loader id");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CONTACT_LOADER) {
            List<ContactItem> loadedContacts = new ArrayList<>();

            if (data != null && data.getCount() > 0) {
                while (data.moveToNext()) {
                    String phone = data.getString(1);
                    String name = data.getString(2);

                    // Bỏ qua nếu số điện thoại rỗng
                    if (phone != null && !phone.trim().isEmpty()) {
                        String finalName = (name != null) ? name : "Unknown";
                        loadedContacts.add(new ContactItem(finalName, phone));
                    }
                }
            }

            // Gửi toàn bộ dữ liệu vừa quét được cho Adapter vẽ lên màn hình
            contactAdapter.setContacts(loadedContacts);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Khi loader bị reset (ví dụ app bị thu nhỏ/đóng), xóa trắng danh sách
        contactAdapter.setContacts(new ArrayList<>());
    }

    private void loadFriendsFromServer() {
        // 1. Lấy ID người dùng đang đăng nhập
        SharedPreferences sharedPref = getSharedPreferences("LucincinApp", Context.MODE_PRIVATE);
        int myUserId = sharedPref.getInt("USER_ID", -1);

        if (myUserId == -1) {
            Toast.makeText(this, "Lỗi xác thực người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Gọi API
        RetrofitClient.getApiService().getUserFriends(myUserId).enqueue(new Callback<FriendsResponse>() {
            @Override
            public void onResponse(Call<FriendsResponse> call, Response<FriendsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FriendsResponse friendsResponse = response.body();

                    if ("success".equals(friendsResponse.getStatus())) {
                        List<User> serverFriends = friendsResponse.getFriends();

                        // 3. Chuyển đổi dữ liệu từ User sang ContactItem để dùng lại Adapter cũ
                        List<ContactItem> displayList = new ArrayList<>();
                        if (serverFriends != null) {
                            for (User u : serverFriends) {
                                // Lấy Tên và Số điện thoại (nếu có) từ User
                                String name = u.getName() != null ? u.getName() : "Người dùng ẩn danh";
                                String phone = u.getPhone() != null ? u.getPhone() : "Chưa cập nhật số điện thoại";

                                displayList.add(new ContactItem(name, phone));
                            }
                        }

                        // 4. Đẩy dữ liệu vào Adapter và vẽ lên RecyclerView
                        // Giả sử biến adapter của bạn tên là contactAdapter
                        contactAdapter.setContacts(displayList);

                        Toast.makeText(getApplicationContext(), "Đã tải " + friendsResponse.getCount() + " bạn bè!", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Lỗi: " + friendsResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Lỗi Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FriendsResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}