package com.example.harddriveinfoapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.harddriveinfoapp.models.Drive;
import com.example.harddriveinfoapp.models.TierEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Диалог “Добавить диск в Tier-лист”.
 *
 * Шаги работы:
 * 1) Загружает hddDrives → ssdSataDrives → ssdM2Drives
 * 2) Объединяет все записи в allDriveList
 * 3) Заполняет spinnerDrives строками вида "<name> (HDD)" или "<name> (SSD SATA)" и т.д.
 * 4) spinnerTier берёт из ресурса R.array.tier_values
 * 5) При нажатии “Добавить” создаёт объект TierEntry → кладёт поля в Map → добавляет в collection("tier_list")
 * 6) После успешного добавления показывает Toast и закрывает диалог, TierListFragment (если открыт) слушает реальные события
 */
public class AddDriveDialogFragment extends DialogFragment {

    private Spinner spinnerDrives;
    private Spinner spinnerTier;

    /** Список всех дисков (HDD + SSD SATA + SSD M.2) */
    private final List<Drive> allDriveList = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // «Накачиваем» layout нашего диалога
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_tier, null);

        spinnerDrives = dialogView.findViewById(R.id.spinnerDrives);
        spinnerTier   = dialogView.findViewById(R.id.spinnerTier);

        // Захватываем Context в локальную final-переменную
        final Context ctx = requireContext();

        // 1) Настраиваем spinnerTier из строкового массива R.array.tier_values
        ArrayAdapter<CharSequence> tierAdapter = ArrayAdapter.createFromResource(
                ctx,
                R.array.tier_values,
                android.R.layout.simple_spinner_item
        );
        tierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTier.setAdapter(tierAdapter);

        // 2) Постепенно загружаем документы из трёх коллекций Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // (a) Читаем "hddDrives"
        firestore.collection("hddDrives")
                .get()
                .addOnSuccessListener((QuerySnapshot hddSnapshot) -> {
                    for (QueryDocumentSnapshot doc : hddSnapshot) {
                        Drive d = doc.toObject(Drive.class);
                        if (d != null) {
                            d.setId(doc.getId());
                            d.setType("hdd");
                            allDriveList.add(d);
                        }
                    }
                    // (b) Читаем "ssdSataDrives"
                    firestore.collection("ssdSataDrives")
                            .get()
                            .addOnSuccessListener((QuerySnapshot sataSnapshot) -> {
                                for (QueryDocumentSnapshot doc : sataSnapshot) {
                                    Drive d = doc.toObject(Drive.class);
                                    if (d != null) {
                                        d.setId(doc.getId());
                                        d.setType("ssd sata");
                                        allDriveList.add(d);
                                    }
                                }
                                // (c) Читаем "ssdM2Drives"
                                firestore.collection("ssdM2Drives")
                                        .get()
                                        .addOnSuccessListener((QuerySnapshot m2Snapshot) -> {
                                            for (QueryDocumentSnapshot doc : m2Snapshot) {
                                                Drive d = doc.toObject(Drive.class);
                                                if (d != null) {
                                                    d.setId(doc.getId());
                                                    d.setType("ssd m2");
                                                    allDriveList.add(d);
                                                }
                                            }
                                            // Когда все три коллекции загружены → заполняем spinnerDrives
                                            setupDrivesSpinner(ctx);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ctx,
                                                    "Ошибка загрузки SSD M.2: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ctx,
                                        "Ошибка загрузки SSD SATA: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ctx,
                            "Ошибка загрузки HDD: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        // 3) Собираем и возвращаем AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Добавить диск в Tier-лист")
                .setView(dialogView)
                .setNegativeButton("Отмена", (dialog, which) -> {
                    // Просто закрываем диалог
                    dialog.dismiss();
                })
                .setPositiveButton("Добавить", (dialogInterface, which) -> {
                    int posDrive = spinnerDrives.getSelectedItemPosition();
                    int posTier  = spinnerTier.getSelectedItemPosition();

                    if (posDrive < 0 || posDrive >= allDriveList.size()) {
                        Toast.makeText(ctx,
                                "Выберите диск",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String chosenTier = (String) spinnerTier.getSelectedItem();
                    Drive selectedDrive = allDriveList.get(posDrive);

                    // Собираем объект TierEntry
                    TierEntry entry = new TierEntry();
                    entry.setDriveId(selectedDrive.getId());

                    String name = selectedDrive.getName();
                    if (name == null || name.trim().isEmpty()) {
                        name = selectedDrive.getId();
                    }
                    entry.setName(name);

                    entry.setType(selectedDrive.getType());
                    entry.setImageUrl(selectedDrive.getImageUrl());
                    entry.setTier(chosenTier);
                    entry.setPrice(selectedDrive.getPrice());

                    // Подготавливаем данные для Firestore
                    Map<String, Object> data = new HashMap<>();
                    data.put("driveId", entry.getDriveId());
                    data.put("name", entry.getName());
                    data.put("type", entry.getType());
                    data.put("imageUrl", entry.getImageUrl());
                    data.put("tier", entry.getTier());
                    data.put("price", entry.getPrice());

                    // Добавляем документ в коллекцию "tier_list"
                    FirebaseFirestore.getInstance()
                            .collection("tier_list")
                            .add(data)
                            .addOnSuccessListener(docRef -> {
                                // Используем сохранённый контекст ctx, чтобы не упасть, если фрагмент уже не прикреплён
                                Toast.makeText(ctx,
                                        "Диск добавлен в Tier-лист",
                                        Toast.LENGTH_SHORT).show();
                                // Гарантированно закрываем диалог:
                                AddDriveDialogFragment.this.dismiss();
                                // TierListFragment сам «услышит», что в коллекции "tier_list" появился новый документ,
                                // и обновит свой RecyclerView (реализовано через addSnapshotListener()).
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ctx,
                                        "Ошибка добавления: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                });

        return builder.create();
    }

    /**
     * Заполняет spinnerDrives списком "<имя> (HDD)" / "<имя> (SSD SATA)" / "<имя> (SSD M.2)".
     * Если field name == null или пусто, вместо него показываем идентификатор документа.
     */
    private void setupDrivesSpinner(@NonNull Context ctx) {
        List<String> drivesForSpinner = new ArrayList<>();

        for (Drive d : allDriveList) {
            String title = d.getName();
            if (title == null || title.trim().isEmpty()) {
                title = d.getId();
            }
            String upperType = d.getType().toUpperCase();
            drivesForSpinner.add(title + " (" + upperType + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ctx,
                android.R.layout.simple_spinner_item,
                drivesForSpinner
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrives.setAdapter(adapter);
    }
}
