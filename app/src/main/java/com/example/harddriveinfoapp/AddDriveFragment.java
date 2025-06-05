package com.example.harddriveinfoapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.harddriveinfoapp.utils.Constants;
import com.example.harddriveinfoapp.utils.FirestoreHelper;
import com.google.firebase.firestore.DocumentReference;
import java.util.HashMap;
import java.util.Map;

public class AddDriveFragment extends Fragment {

    private EditText etName, etPrice, etImageUrl, etProductUrl, etSpecs;
    private Spinner spinnerType;
    private Button btnSave;
    private ProgressBar progressBar;
    private TextView tvError;

    private String[] types = {"HDD", "SSD_SATA", "SSD_M2"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_drive, container, false);

        etName = view.findViewById(R.id.etDriveName);
        etPrice = view.findViewById(R.id.etPrice);
        etImageUrl = view.findViewById(R.id.etImageUrl);
        etProductUrl = view.findViewById(R.id.etProductUrl);
        etSpecs = view.findViewById(R.id.etSpecs);
        spinnerType = view.findViewById(R.id.spinnerType);
        btnSave = view.findViewById(R.id.btnSaveDrive);
        progressBar = view.findViewById(R.id.progressAddDrive);
        tvError = view.findViewById(R.id.tvAddDriveError);

        // Настраиваем Spinner для типов
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        btnSave.setOnClickListener(v -> saveDrive());

        return view;
    }

    private void saveDrive() {
        tvError.setVisibility(View.GONE);
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String productUrl = etProductUrl.getText().toString().trim();
        String specsRaw = etSpecs.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите название");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Введите цену");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Некорректная цена");
            return;
        }
        if (TextUtils.isEmpty(imageUrl)) {
            etImageUrl.setError("Введите URL картинки");
            return;
        }
        // productUrl можно пустым, но можно проверить начальные http:// либо https://

        // Парсим specs: ожидаем строки вида "Key: Value", разделённые переносом строки
        Map<String, Object> specsMap = new HashMap<>();
        if (!TextUtils.isEmpty(specsRaw)) {
            String[] lines = specsRaw.split("\\r?\\n");
            for (String line : lines) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0].trim();
                    String val = parts[1].trim();
                    specsMap.put(key, val);
                }
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        FirestoreHelper.addDrive(name, type, specsMap, price, imageUrl, productUrl)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // После добавления можно вернуться к списку того типа, который добавили:
                        Toast.makeText(requireContext(), "Диск добавлен", Toast.LENGTH_SHORT).show();
                        int destId;
                        switch (type) {
                            case "HDD":
                                destId = R.id.hddListFragment;
                                break;
                            case "SSD_SATA":
                                destId = R.id.ssdSataListFragment;
                                break;
                            case "SSD_M2":
                                destId = R.id.ssdM2ListFragment;
                                break;
                            default:
                                destId = R.id.hddListFragment;
                        }
                        Navigation.findNavController(requireView()).navigate(destId);
                    } else {
                        tvError.setText("Ошибка: " + task.getException().getMessage());
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
    }
}
