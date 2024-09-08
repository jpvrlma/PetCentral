package com.example.petcentral.Login;


import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.Usuario;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityCadastroBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * Esta atividade irá cadastrar o usuário no Firebase Authentication e no Firestore
 */

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    //Cliques
    private void clickListeners() {
        binding.btncadastrar.setOnClickListener(v -> validarCampos());
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.editNome.setOnClickListener(v -> binding.containerNome.setError(null));
        binding.editEmail.setOnClickListener(v -> binding.containerEmail.setError(null));
        binding.editSenha.setOnClickListener(v -> binding.containerSenha.setError(null));
    }

    //Metodos para cadastro
    private void validarCampos() {
        final String nome = Objects.requireNonNull(binding.editNome.getText()).toString().trim();
        final String email = Objects.requireNonNull(binding.editEmail.getText()).toString().trim();
        final String senha = Objects.requireNonNull(binding.editSenha.getText()).toString().trim();

        if (nome.isEmpty() && email.isEmpty() && senha.isEmpty()) {
            mostrarSnackbar("Campos obrigatórios estão vazios. Por favor, preencha todos os campos para continuar.");
            return;
        }
        if (nome.isEmpty()) {
            binding.containerNome.setError("Campo obrigatório");
            binding.editNome.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            binding.containerEmail.setError("Campo obrigatório");
            binding.editEmail.requestFocus();
            return;
        }
        if (!isEmailValido(email)) {
            binding.containerEmail.setError("O email digitado é inválido. Por favor, digite um email válido.");
            binding.editEmail.requestFocus();
            return;
        }
        if (senha.isEmpty()) {
            binding.containerSenha.setError("Campo obrigatório");
            binding.editSenha.requestFocus();
            return;
        }
        if (senha.length() < 6) {
            binding.containerSenha.setError("Sua senha precisa ter pelo menos 6 caracteres.");
            binding.editSenha.requestFocus();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        cadastrarFirebase(nome, email, senha);
    }

    private void cadastrarFirebase(String nome, String email, String senha) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Usuario user = new Usuario(nome, email);
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            db.collection("usuarios").document(firebaseUser.getUid()).set(user);
                            binding.progressBar.setVisibility(View.GONE);
                            mostrarSnackbar("Cadastro realizado com sucesso!");
                        }
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        errosCadastro(task.getException());
                    }
                });
    }

    // -------- Utilitários --------
    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    private void errosCadastro(Exception exception) {
        String erro;
        if (exception instanceof FirebaseAuthUserCollisionException) {
            erro = "E-mail já registrado.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            erro = "A senha deve conter no mínimo 6 caracteres!";
        } else {
            erro = "Ops, Algo saiu mal";
        }
        mostrarSnackbar(erro);
    }

    private boolean isEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}