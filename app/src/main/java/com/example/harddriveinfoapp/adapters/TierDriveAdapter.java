package com.example.harddriveinfoapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.harddriveinfoapp.R;
import com.example.harddriveinfoapp.models.TierEntry;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TierDriveAdapter extends RecyclerView.Adapter<TierDriveAdapter.TierDriveViewHolder> {

    private final Context context;
    private final List<TierEntry> tierEntries;
    private final FirebaseFirestore firestore;

    public TierDriveAdapter(Context context, List<TierEntry> tierEntries) {
        this.context = context;
        this.tierEntries = tierEntries;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TierDriveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_tier_drive, parent, false);
        return new TierDriveViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TierDriveViewHolder holder, int position) {
        TierEntry entry = tierEntries.get(position);

        // === 1) Загрузка картинки диска через Glide ===
        String imageUrl = entry.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.ivTierDriveImage);
        } else {
            // Если URL пустой, показываем placeholder
            holder.ivTierDriveImage.setImageResource(R.drawable.placeholder);
        }

        // === 2) Установка текста "Название диска" и "Tier: X" ===
        holder.tvTierDriveName.setText(entry.getName());
        holder.tvTierLabel.setText("Tier: " + entry.getTier());

        // === 3) Обработка нажатия на кнопку "Удалить" ===
        holder.btnDeleteTier.setOnClickListener(v -> {
            String documentId = entry.getDocumentId();
            if (documentId == null || documentId.isEmpty()) {
                // Если вдруг нет ID — показываем сообщение и выходим
                android.widget.Toast.makeText(context,
                        "Не удалось найти ID записи для удаления",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Удаляем из коллекции "tier_list" документ с этим ID
            firestore.collection("tier_list")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // 3.1) Успешно удалили из Firestore — удаляем из локального списка
                        int removedPos = holder.getAdapterPosition();
                        if (removedPos != RecyclerView.NO_POSITION) {
                            tierEntries.remove(removedPos);
                            notifyItemRemoved(removedPos);
                        }
                        android.widget.Toast.makeText(context,
                                "Элемент удалён из Tier-листа",
                                android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(context,
                                "Ошибка при удалении: " + e.getMessage(),
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return tierEntries.size();
    }

    // ====================== ViewHolder ======================
    public static class TierDriveViewHolder extends RecyclerView.ViewHolder {

        ImageView ivTierDriveImage;
        TextView tvTierDriveName;
        TextView tvTierLabel;
        AppCompatImageButton btnDeleteTier;

        public TierDriveViewHolder(@NonNull View itemView) {
            super(itemView);

            ivTierDriveImage = itemView.findViewById(R.id.ivTierDriveImage);
            tvTierDriveName = itemView.findViewById(R.id.tvTierDriveName);
            tvTierLabel = itemView.findViewById(R.id.tvTierLabel);
            btnDeleteTier = itemView.findViewById(R.id.btnDeleteTier);
        }
    }
}
