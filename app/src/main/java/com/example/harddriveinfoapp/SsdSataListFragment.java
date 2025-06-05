package com.example.harddriveinfoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.example.harddriveinfoapp.adapters.DriveAdapter;
import com.example.harddriveinfoapp.models.Drive;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SsdSataListFragment extends Fragment {

    private static final String TAG = "SsdSataListFragment";

    private RecyclerView rvSsdSataList;
    private ProgressBar progressSsdSata;
    private DriveAdapter adapter;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: старт");

        View root = inflater.inflate(R.layout.fragment_ssd_sata_list, container, false);

        rvSsdSataList = root.findViewById(R.id.rvSsdSataList);
        progressSsdSata = root.findViewById(R.id.progressSsdSata);

        firestore = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadSsdSataDrives();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Проверяем: если нет залогина, кидаем на AuthFragment
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            Log.d(TAG, "onStart: пользователь не залогинен, переходим на authFragment");
            View view = getView();
            if (view != null) {
                Navigation.findNavController(view).navigate(R.id.authFragment);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new DriveAdapter(new ArrayList<>());
        rvSsdSataList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSsdSataList.setAdapter(adapter);

        rvSsdSataList.setVisibility(View.GONE);
        progressSsdSata.setVisibility(View.VISIBLE);
    }

    private void loadSsdSataDrives() {
        Log.d(TAG, "loadSsdSataDrives: запускаем запрос");
        firestore.collection("ssdSataDrives")
                .get()
                .addOnSuccessListener(this::onSsdSataDrivesLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailureListener: " + e.getMessage());
                    progressSsdSata.setVisibility(View.GONE);
                });
    }

    private void onSsdSataDrivesLoaded(QuerySnapshot querySnapshot) {
        int count = querySnapshot.getDocuments().size();
        Log.d(TAG, "onSsdSataDrivesLoaded: docs count = " + count);

        List<Drive> drives = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            String docId = doc.getId();
            Double priceD = doc.getDouble("price");
            double price = priceD != null ? priceD : 0.0;

            Drive drive = new Drive();
            drive.setId(docId);

            String name = doc.getString("name");
            if (name == null || name.isEmpty()) {
                name = docId;
            }
            drive.setName(name);
            drive.setPrice(price);

            if (doc.contains("imageUrl")) {
                drive.setImageUrl(doc.getString("imageUrl"));
            }
            if (doc.contains("productUrl")) {
                drive.setProductUrl(doc.getString("productUrl"));
            }
            if (doc.contains("type")) {
                drive.setType(doc.getString("type"));
            }
            if (doc.contains("specs")) {
                Object rawSpecs = doc.getData().get("specs");
                if (rawSpecs instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> specsMap = (Map<String, Object>) rawSpecs;
                    drive.setSpecs(specsMap);
                }
            }

            drives.add(drive);
        }

        Log.d(TAG, "onSsdSataDrivesLoaded: Prepared Drive list size = " + drives.size());
        adapter.setDriveList(drives);

        rvSsdSataList.setVisibility(View.VISIBLE);
        progressSsdSata.setVisibility(View.GONE);
    }
}
