package com.example.harddriveinfoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;

import com.example.harddriveinfoapp.adapters.DriveAdapter;
import com.example.harddriveinfoapp.models.Drive;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HddListFragment extends Fragment {
    private RecyclerView rvHddList;
    private ProgressBar progressHdd;
    private DriveAdapter adapter;
    private FirebaseFirestore firestore;
    private Button btnLogout;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_hdd_list, container, false);

        btnLogout = root.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });
        rvHddList = root.findViewById(R.id.rvHddList);
        progressHdd = root.findViewById(R.id.progressHdd);
        firestore = FirebaseFirestore.getInstance();
        NavHostFragment navHostFragment =
                (NavHostFragment) requireActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        setupRecyclerView();
        loadHddDrives();
        return root;
    }
    private void setupRecyclerView() {
        adapter = new DriveAdapter(new ArrayList<>());
        rvHddList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHddList.setAdapter(adapter);
        rvHddList.setVisibility(View.GONE);
        progressHdd.setVisibility(View.VISIBLE);
    }
    private void loadHddDrives() {
        firestore.collection("hddDrives")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("HddListFragment", "Ошибка загрузки HDD: " + error.getMessage());
                            progressHdd.setVisibility(View.GONE);
                            return;
                        }
                        List<Drive> drives = new ArrayList<>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Drive drive = doc.toObject(Drive.class);
                                if (drive != null) {
                                    drive.setId(doc.getId());
                                    drives.add(drive);
                                }
                            }
                        }
                        adapter.setDriveList(drives);
                        if (!drives.isEmpty()) {
                            rvHddList.setVisibility(View.VISIBLE);
                            progressHdd.setVisibility(View.GONE);
                        } else {
                            rvHddList.setVisibility(View.GONE);
                            progressHdd.setVisibility(View.GONE);
                        }
                    }
                });
    }
    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        if (navController != null) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            navController.navigate(R.id.authFragment, null, navOptions);
        }
    }
}
