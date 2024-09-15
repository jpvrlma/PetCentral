package com.example.petcentral.Exames;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityAdicionarExameBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdicionarExameActivity extends AppCompatActivity {

    private ActivityAdicionarExameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdicionarExameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Cliques
    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());

        binding.btnCancelar.setOnClickListener(v-> finish());

        binding.btnAdd.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view1 = LayoutInflater.from(this).inflate(R.layout.bottom_exames, null);
            bottomSheetDialog.setContentView(view1);
            bottomSheetDialog.show();
        });
    }

}