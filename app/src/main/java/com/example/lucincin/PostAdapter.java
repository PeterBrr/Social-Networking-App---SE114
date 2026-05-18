package com.example.lucincin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    // Constructor nhận vào danh sách bài đăng
    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp file giao diện item_post.xml cho từng phần tử
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        // Lấy bài đăng hiện tại theo vị trí (position)
        Post currentPost = postList.get(position);

        // Lấy tên tác giả (Kiểm tra null để tránh văng app nếu server lỗi thiếu author)
        String authorName;
        if (currentPost.getAuthor() != null && currentPost.getAuthor().getName() != null) {
            authorName = currentPost.getAuthor().getName();
        } else {
            authorName = "Người dùng ẩn danh";
        }

        // Gán dữ liệu vào các TextView
        holder.tvUserName.setText(authorName);
        holder.tvPostDate.setText(currentPost.getCreatedAt());
        holder.tvPostContent.setText(currentPost.getContent());

        // Sự kiện khi bấm vào Tên người dùng (tvUserName)
        holder.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy Context từ chính cái View đang được click
                Context context = v.getContext();

                // Tạo Intent để chuyển từ màn hình hiện tại (context) sang ProfileActivity
                Intent intent = new Intent(context, profile.class);

                // Truyền tên User sang ProfileActivity để hiển thị
                intent.putExtra("EXTRA_USERNAME", authorName);
                if (currentPost.getAuthor() != null) {
                    intent.putExtra("EXTRA_USER_ID", currentPost.getAuthor().getId());
                }

                // Bắt đầu chuyển màn hình
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                Context context = v.getContext();
                while (context instanceof android.content.ContextWrapper) {
                    if (context instanceof feed) {
                        ((feed) context).setCurrentItemPosition(currentPosition);
                        break;
                    }
                    context = ((android.content.ContextWrapper) context).getBaseContext();
                }

                // Trả về false để hệ thống TIẾP TỤC xử lý hiển thị Context Menu
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size(); // Trả về số lượng bài đăng
    }

    // Lớp ViewHolder giúp lưu trữ sẵn các View, tối ưu hiệu năng
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvPostDate, tvPostContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID từ file item_post.xml
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
        }
    }
}
