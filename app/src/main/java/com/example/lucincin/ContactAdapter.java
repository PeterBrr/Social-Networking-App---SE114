package com.example.lucincin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<ContactItem> contactList = new ArrayList<>();

    public void setContacts(List<ContactItem> contacts) {
        this.contactList.clear();
        this.contactList.addAll(contacts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        // Cài đặt dữ liệu
        ContactItem currentContact = contactList.get(position);
        holder.tvName.setText(currentContact.getName());
        holder.tvPhone.setText(currentContact.getPhone());

        // Reset lại trạng thái của nút Add (Rất quan trọng với RecyclerView để khi cuộn không bị lỗi hiển thị nhầm)
        holder.btnAdd.setText("Add");
        holder.btnAdd.setEnabled(true);
        holder.btnAdd.setAlpha(1.0f); // 1.0 là độ nét 100%

        // Xử lý sự kiện khi bấm nút Add
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int actualPosition = holder.getAdapterPosition();
                if (actualPosition != RecyclerView.NO_POSITION) {
                    ContactItem clickedContact = contactList.get(actualPosition);

                    // 1. Hiển thị thông báo (Hoặc viết logic gọi API kết bạn ở đây)
                    Toast.makeText(v.getContext(), "Đã gửi lời mời tới: " + clickedContact.getName(), Toast.LENGTH_SHORT).show();

                    // 2. Thay đổi giao diện nút để báo hiệu thành công
                    holder.btnAdd.setText("Added");
                    holder.btnAdd.setEnabled(false); // Khóa nút không cho bấm nữa
                    holder.btnAdd.setAlpha(0.5f);    // Làm mờ nút đi 50%
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        Button btnAdd; // Khai báo thêm nút Add

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            btnAdd = itemView.findViewById(R.id.btnAdd); // Ánh xạ ID
        }
    }
}