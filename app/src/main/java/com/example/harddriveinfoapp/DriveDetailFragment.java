package com.example.harddriveinfoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.harddriveinfoapp.models.Drive;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class DriveDetailFragment extends Fragment {

    private static final String TAG = "DriveDetailFragment";

    private ImageView ivDetailImage;
    private TextView tvDetailName;
    private TextView tvDetailPrice;
    private TextView tvDetailSpecs;
    private Button btnViewProduct;
    private TextView tvDetailError;
    private View cardSpecsContainer;

    private FirebaseFirestore firestore;
    private String driveId;
    private String collectionName; // имя коллекции, из которой будем читать документ

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_drive_detail, container, false);

        // Инициализируем View
        ivDetailImage      = root.findViewById(R.id.ivDetailImage);
        tvDetailName       = root.findViewById(R.id.tvDetailName);
        tvDetailPrice      = root.findViewById(R.id.tvDetailPrice);
        tvDetailSpecs      = root.findViewById(R.id.tvDetailSpecs);
        btnViewProduct     = root.findViewById(R.id.btnViewProduct);
        tvDetailError      = root.findViewById(R.id.tvDetailError);
        cardSpecsContainer = root.findViewById(R.id.cardSpecsContainer);

        // Firestore
        firestore = FirebaseFirestore.getInstance();

        // Читаем аргументы из Bundle
        if (getArguments() != null) {
            driveId        = getArguments().getString("driveId");
            collectionName = getArguments().getString("collection");
            Log.d(TAG, "onCreateView: driveId = " + driveId + ", collection = " + collectionName);
        }

        // Если collectionName не был передан (по какой-то причине), используем hddDrives по умолчанию
        if (collectionName == null || collectionName.isEmpty()) {
            collectionName = "hddDrives";
        }

        // Начинаем загрузку деталей
        loadDriveDetails();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Если пользователь разлогинен, возвращаем его на экран авторизации
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            Log.d(TAG, "onStart: не авторизован, переходим на authFragment");
            View view = getView();
            if (view != null) {
                Navigation.findNavController(view).navigate(R.id.authFragment);
            }
        }
    }

    /**
     * Загружает документ с ID = driveId из коллекции collectionName.
     */
    private void loadDriveDetails() {
        if (driveId == null || driveId.isEmpty()) {
            showError("Информация о диске не найдена");
            return;
        }

        firestore.collection(collectionName)
                .document(driveId)
                .get()
                .addOnSuccessListener(this::onDriveLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailureListener: " + e.getMessage());
                    showError("Ошибка загрузки: " + e.getMessage());
                });
    }

    /**
     * Выполняется при успешном получении документа из Firestore.
     */
    private void onDriveLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            showError("Информация о диске не найдена");
            return;
        }

        // Преобразуем в модель Drive
        Drive drive = doc.toObject(Drive.class);
        if (drive == null) {
            showError("Не удалось прочитать данные диска");
            return;
        }

        // 1) Название диска
        String name = drive.getName();
        if (name == null || name.isEmpty()) {
            name = doc.getId();
        }
        tvDetailName.setText(name);

        // 2) Цена диска (приводим double к int)
        int priceInt = (int) drive.getPrice();
        tvDetailPrice.setText("Цена: " + priceInt + " руб");

        // 3) Загружаем картинку через Glide (либо ставим плейсхолдер)
        String imageUrl = drive.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivDetailImage);
        } else {
            ivDetailImage.setImageResource(R.drawable.ic_placeholder);
        }

        // 4) Характеристики: берём из поля "specs" как Map<String,Object>
        Object rawSpecs = doc.getData().get("specs");
        if (rawSpecs instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> specsMap = (Map<String, Object>) rawSpecs;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : specsMap.entrySet()) {
                sb.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }
            tvDetailSpecs.setText(sb.toString().trim());
        } else {
            tvDetailSpecs.setText("Характеристики недоступны");
        }

        // 5) Кнопка "Посмотреть товар": открывает productUrl в браузере
        String productUrl = drive.getProductUrl();
        if (productUrl != null && !productUrl.isEmpty()) {
            btnViewProduct.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(productUrl));
                startActivity(browserIntent);
            });
        } else {
            btnViewProduct.setEnabled(false);
            btnViewProduct.setText("Ссылка отсутствует");
        }

        // Скрываем TextView с ошибкой и показываем содержимое
        tvDetailError.setVisibility(View.GONE);
        ivDetailImage.setVisibility(View.VISIBLE);
        tvDetailName.setVisibility(View.VISIBLE);
        tvDetailPrice.setVisibility(View.VISIBLE);
        tvDetailSpecs.setVisibility(View.VISIBLE);
        btnViewProduct.setVisibility(View.VISIBLE);
        cardSpecsContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Если произошла ошибка или документ не найден, прячем весь контент и показываем сообщение.
     */
    private void showError(String message) {
        ivDetailImage.setVisibility(View.GONE);
        tvDetailName.setVisibility(View.GONE);
        tvDetailPrice.setVisibility(View.GONE);
        tvDetailSpecs.setVisibility(View.GONE);
        btnViewProduct.setVisibility(View.GONE);
        cardSpecsContainer.setVisibility(View.GONE);

        tvDetailError.setVisibility(View.VISIBLE);
        tvDetailError.setText(message);
    }
}
