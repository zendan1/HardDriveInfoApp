package com.example.harddriveinfoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harddriveinfoapp.adapters.TierDriveAdapter;
import com.example.harddriveinfoapp.models.TierEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TierListFragment extends Fragment {

    private RecyclerView rvTierList;
    private TextView tvEmptyTierList;
    private Button btnAddToTierList;

    // Адаптер и список, которые будем передавать в RecyclerView
    private TierDriveAdapter tierAdapter;
    private List<TierEntry> tierEntries = new ArrayList<>();

    // Экземпляр Firestore
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // "Надуваем" макет fragment_tier_list.xml
        View root = inflater.inflate(R.layout.fragment_tier_list, container, false);

        // Находим вёрстку по ID
        rvTierList = root.findViewById(R.id.rvTierList);
        tvEmptyTierList = root.findViewById(R.id.tvEmptyTierList);
        btnAddToTierList = root.findViewById(R.id.btnAddToTierList);

        // Инициализируем Firestore
        firestore = FirebaseFirestore.getInstance();

        // Настраиваем RecyclerView:
        rvTierList.setLayoutManager(new LinearLayoutManager(requireContext()));
        tierAdapter = new TierDriveAdapter(requireContext(), tierEntries);
        rvTierList.setAdapter(tierAdapter);

        // Подписываем кнопку "Добавить в Tier-лист" на открытие нашего диалога
        btnAddToTierList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Показываем DialogFragment, в котором пользователь выберет диск и Tier
                AddDriveDialogFragment dialog = new AddDriveDialogFragment();
                // Используем parentFragmentManager, чтобы диалог не завершил родительский фрагмент
                dialog.show(requireActivity().getSupportFragmentManager(), "ADD_DRIVE_DIALOG");
            }
        });

        // Подписка на изменения коллекции "tier_list" в Firestore
        loadTierEntriesFromFirestore();

        return root;
    }

    /**
     * Подписываемся на real-time обновления коллекции "tier_list".
     * При каждом изменении (добавлении или удалении документа) пересоздаём локальный список tierEntries,
     * затем вызвать notifyDataSetChanged() у адаптера. А также показываем/скрываем текст "Empty".
     */
    private void loadTierEntriesFromFirestore() {
        firestore.collection("tier_list")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Если произошла ошибка чтения, просто логируем и выходим
                            Log.e("TierListFragment", "Ошибка чтения Tier-листа", error);
                            return;
                        }

                        // Очищаем текущий список
                        tierEntries.clear();

                        if (snapshots != null && !snapshots.isEmpty()) {
                            // Пробегаемся по каждому документу в snapshots
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                TierEntry entry = doc.toObject(TierEntry.class);
                                if (entry != null) {
                                    // Важно: сохраняем ID документа, чтобы адаптер мог удалять именно этот документ
                                    entry.setDocumentId(doc.getId());
                                    tierEntries.add(entry);
                                }
                            }
                        }

                        // Проверяем, есть ли хотя бы один элемент
                        if (tierEntries.isEmpty()) {
                            // Если пусто — показываем TextView с сообщением и скрываем RecyclerView
                            tvEmptyTierList.setVisibility(View.VISIBLE);
                            rvTierList.setVisibility(View.GONE);
                        } else {
                            // Если есть записи — скрываем TextView и показываем RecyclerView
                            tvEmptyTierList.setVisibility(View.GONE);
                            rvTierList.setVisibility(View.VISIBLE);
                        }

                        // Говорим адаптеру перерисовать всё
                        tierAdapter.notifyDataSetChanged();
                    }
                });
    }
}
