package com.example.lucincin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class feed extends AppCompatActivity {
    private EditText etIdeaInput;
    private Button btnPost;
    private RecyclerView recyclerViewPosts;

    private List<Post> postList;
    private PostAdapter postAdapter;
    private int myUserId;
    private int mCurrentItemPosition = -1;

    public void setCurrentItemPosition(int position) {
        this.mCurrentItemPosition = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Ánh xạ View
        etIdeaInput = findViewById(R.id.etIdeaInput);
        btnPost = findViewById(R.id.btnPost);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);

        // 2. Lấy ID của người dùng hiện tại từ SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("LucincinApp", Context.MODE_PRIVATE);
        myUserId = sharedPref.getInt("USER_ID", -1);

        // 3. Cài đặt RecyclerView (Ban đầu là danh sách rỗng)
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);

        // 4. Tải dữ liệu bài viết từ Server
        loadPostsFromServer();

        // Đăng ký Context Menu cho RecyclerView
        registerForContextMenu(recyclerViewPosts);

        // Xử lý sự kiện khi bấm nút "Post"
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPost();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // Lấy ID của item được click

        if (id == R.id.option_menu_profile) {
            // Chuyển sang màn hình profile
            Intent intent = new Intent(feed.this, profile.class);
            startActivity(intent);
            return true; // Kết thúc xử lý tại đây

        } else if (id == R.id.option_menu_sbdate) {
            // Gọi hàm sắp xếp theo ngày
            sortPostsByDate();
            return true;

        } else if (id == R.id.option_menu_sbname) {
            // Gọi hàm sắp xếp theo tên
            sortPostsByName();
            return true;
        } else if (id == R.id.find_friends) {
            Intent intent = new Intent(feed.this, FindFriendsActivity.class);
            startActivity(intent);
        }

        // Nếu ID không khớp với bất kỳ case nào ở trên, trả về hàm mặc định của hệ thống
        return super.onOptionsItemSelected(item);
    }

    private void sortPostsByName() {
        if (postList == null || postList.isEmpty()) return;

        // Sử dụng Collections.sort với một Comparator tùy chỉnh
        Collections.sort(postList, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                // So sánh tên (không phân biệt hoa thường)
                return p1.getUserName().compareToIgnoreCase(p2.getUserName());
            }
        });

        // Cập nhật lại giao diện (RecyclerView)
        postAdapter.notifyDataSetChanged();
    }

    private void sortPostsByDate() {
        if (postList == null || postList.isEmpty()) return;

        // Định dạng ngày đang sử dụng trong Post của bạn (ví dụ: "dd/MM/yyyy")
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Collections.sort(postList, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                try {
                    Date date1 = format.parse(p1.getCreatedAt());
                    Date date2 = format.parse(p2.getCreatedAt());

                    // Để sắp xếp mới nhất lên đầu (Giảm dần), so sánh date2 với date1
                    // Nếu muốn cũ nhất lên đầu (Tăng dần), đổi thành: return date1.compareTo(date2);
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0; // Nếu lỗi parse, giữ nguyên vị trí
            }
        });

        // Cập nhật lại giao diện (RecyclerView)
        postAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.recyclerViewPosts) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.context_menu_detail) {
            if (mCurrentItemPosition != -1 && mCurrentItemPosition < postList.size()) {
                Post selectedPost = postList.get(mCurrentItemPosition);
                // Chuyển sang màn hình chi tiết/profile với dữ liệu
                Intent intent = new Intent(feed.this, profile.class);
                intent.putExtra("EXTRA_USERNAME", selectedPost.getUserName());
                intent.putExtra("EXTRA_POST_CONTENT", selectedPost.getContent());
                startActivity(intent);
            }
        }
        else if (id == R.id.context_menu_hide) {
            if (mCurrentItemPosition != -1 && mCurrentItemPosition < postList.size()) {
                postList.remove(mCurrentItemPosition);
                postAdapter.notifyItemRemoved(mCurrentItemPosition);
                mCurrentItemPosition = -1;
                Toast.makeText(this, "Đã ẩn bài viết", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }

    private void loadPostsFromServer() {
        RetrofitClient.getApiService().getAllPosts().enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Xóa danh sách cũ và nạp danh sách mới từ server
                    postList.clear();
                    postList.addAll(response.body().getData());

                    // Cập nhật giao diện
                    postAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(feed.this, "Không thể tải bảng tin!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostsResponse> call, Throwable t) {
                Toast.makeText(feed.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewPost() {
        String content = etIdeaInput.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (myUserId == -1) {
            Toast.makeText(this, "Lỗi: Không nhận diện được người dùng. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request với ID người dùng và nội dung
        CreatePostRequest request = new CreatePostRequest(myUserId, content);

        // Gọi API POST
        RetrofitClient.getApiService().createPost(request).enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(feed.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();

                    // Xóa trắng ô nhập
                    etIdeaInput.setText("");

                    // Tải lại bảng tin để thấy bài viết mới nhất
                    loadPostsFromServer();
                } else {
                    Toast.makeText(feed.this, "Đăng bài thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostsResponse> call, Throwable t) {
                Toast.makeText(feed.this, "Lỗi mạng khi đăng bài!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}