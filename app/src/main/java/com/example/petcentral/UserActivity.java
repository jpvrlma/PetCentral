package com.example.petcentral;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.databinding.ActivityUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserActivity extends AppCompatActivity {

    private ActivityUserBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clickListeners();
        getUsuario();

    }

    //Método para pegar os dados do usuário do Firestore e atualizar a interface de usuário
    private void getUsuario() {
        String userID = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(userID).addSnapshotListener(this, (value, error) -> {
            if (error != null){
                Log.e("ERRO FIRESTORE DATA", error.getMessage());
                return;
            } else {
                if (value != null && value.exists()) {
                    DocumentSnapshot documentSnapshot = value;
                    binding.textNome.setText(documentSnapshot.getString("nome"));
                    binding.textEmail.setText(documentSnapshot.getString("email"));
                } else {
                    Log.d("Firestore", "Documento não encontrado.");
                }
            }
        });
    }

    private void clickListeners() {
        binding.cardConfiguracoes.setOnClickListener(v-> startActivity(new Intent(this, SettingsActivity.class)));
        binding.cardConfiguracoes.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }
    }


