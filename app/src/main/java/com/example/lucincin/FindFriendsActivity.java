package com.example.lucincin;

import android.Manifest;
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
        checkPermissionAndLoadContacts();
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
}