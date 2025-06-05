package com.example.harddriveinfoapp.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.harddriveinfoapp.R;
import com.example.harddriveinfoapp.models.Drive;

import java.util.ArrayList;
import java.util.List;

/**
 * Универсальный адаптер для отображения списка дисков (HDD, SSD SATA, SSD M.2).
 * При клике на элемент открывает DriveDetailFragment, передавая:
 *   - "driveId"      : ID документа (String)
 *   - "collection"   : имя коллекции в Firestore (String)
 */
public class DriveAdapter extends RecyclerView.Adapter<DriveAdapter.DriveViewHolder> {

    private List<Drive> drives = new ArrayList<>();

    public DriveAdapter(List<Drive> initialDrives) {
        if (initialDrives != null) {
            drives = initialDrives;
        }
    }

    @NonNull
    @Override
    public DriveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drive, parent, false);
        return new DriveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriveViewHolder holder, int position) {
        Drive drive = drives.get(position);

        // 1) Устанавливаем название (если name пуст, берём ID документа)
        String name = drive.getName();
        if (name == null || name.isEmpty()) {
            name = drive.getId();
        }
        holder.tvName.setText(name);

        // 2) Устанавливаем цену (приводим double к int, чтобы не было ".0")
        int priceInt = (int) drive.getPrice();
        holder.tvPrice.setText(priceInt + " руб");

        // 3) Загружаем изображение через Glide (или показываем плейсхолдер)
        String imageUrl = drive.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.ivDriveImage);
        } else {
            holder.ivDriveImage.setImageResource(R.drawable.ic_placeholder);
        }

        // 4) Обрабатываем клик по элементу. В лямбде уже вычисляем collection:
        holder.itemView.setOnClickListener(v -> {
            // Определяем, из какой коллекции читать документ
            String type = drive.getType();
            String collection = "hddDrives"; // значение по умолчанию

            if (type != null) {
                String t = type.trim().toLowerCase();
                if (t.contains("ssd sata")) {
                    collection = "ssdSataDrives";
                } else if (t.contains("ssd m2") || t.contains("ssdm2")) {
                    collection = "ssdM2Drives";
                }
            }

            // Собираем Bundle и навигируем в DriveDetailFragment
            Bundle bundle = new Bundle();
            bundle.putString("driveId", drive.getId());
            bundle.putString("collection", collection);
            Navigation.findNavController(v).navigate(R.id.driveDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return drives.size();
    }

    /**
     * Обновляет список дисков в адаптере. Вызывать после получения данных из Firestore.
     */
    public void setDriveList(List<Drive> newDrives) {
        drives = (newDrives != null) ? newDrives : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class DriveViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDriveImage;
        TextView tvName;
        TextView tvPrice;

        public DriveViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDriveImage = itemView.findViewById(R.id.ivDriveImage);
            tvName       = itemView.findViewById(R.id.tvDriveName);
            tvPrice      = itemView.findViewById(R.id.tvDrivePrice);
        }
    }
}
