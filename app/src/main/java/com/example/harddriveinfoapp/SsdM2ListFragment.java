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

public class SsdM2ListFragment extends Fragment {

    private static final String TAG = "SsdM2ListFragment";

    private RecyclerView rvSsdM2List;
    private ProgressBar progressSsdM2;
    private DriveAdapter adapter;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: старт");

        View root = inflater.inflate(R.layout.fragment_ssd_m2_list, container, false);

        rvSsdM2List = root.findViewById(R.id.rvSsdM2List);
        progressSsdM2 = root.findViewById(R.id.progressSsdM2);

        firestore = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadSsdM2Drives();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Проверяем авторизацию: если не залогинен, переходим на AuthFragment
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
        rvSsdM2List.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSsdM2List.setAdapter(adapter);

        rvSsdM2List.setVisibility(View.GONE);
        progressSsdM2.setVisibility(View.VISIBLE);
    }

    private void loadSsdM2Drives() {
        Log.d(TAG, "loadSsdM2Drives: запускаем запрос");
        firestore.collection("ssdM2Drives")
                .get()
                .addOnSuccessListener(this::onSsdM2DrivesLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailureListener: " + e.getMessage());
                    progressSsdM2.setVisibility(View.GONE);
                });
    }

    private void onSsdM2DrivesLoaded(QuerySnapshot querySnapshot) {
        int count = querySnapshot.getDocuments().size();
        Log.d(TAG, "onSsdM2DrivesLoaded: docs count = " + count);

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

        Log.d(TAG, "onSsdM2DrivesLoaded: Prepared Drive list size = " + drives.size());
        adapter.setDriveList(drives);

        rvSsdM2List.setVisibility(View.VISIBLE);
        progressSsdM2.setVisibility(View.GONE);
    }
}
