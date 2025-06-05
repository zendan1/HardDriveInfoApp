package com.example.harddriveinfoapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.FirebaseApp;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHost != null) {
            navController = navHost.getNavController();
        }
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (navController != null && bottomNav != null) {
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(
                            systemBarsInsets.left,
                            systemBarsInsets.top,
                            systemBarsInsets.right,
                            systemBarsInsets.bottom
                    );
                    return insets;
                }
        );
    }
}
