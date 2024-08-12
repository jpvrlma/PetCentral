package com.example.petcentral.Usuario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Login.LoginActivity;
import com.example.petcentral.databinding.ActivityUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Esta atividade irá exibir as configurações do usuário
 * permitirá o acesso a edição de dados do usuário
 * e o logout da sua conta
 */

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

    private void getUsuario() {
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .addSnapshotListener(this, (value, error) -> {
            if (error != null){
                Log.e("DATA NAO CARREGA", error.getMessage());
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
        binding.btnEdit.setOnClickListener(v-> startActivity(new Intent(this, editUserActivity.class)));
        binding.cardConfiguracoes.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        binding.cardLogout.setOnClickListener(v -> logout());
        }

        private void logout(){
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


