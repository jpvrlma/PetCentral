package com.example.petcentral.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import java.util.Objects;

/**
 * Esta atividade é responsável por fazer o login do usuário no aplicativo
 * Irá validar as informações passados no clique do botao de Login
 * ou irá redirecionar para a tela de cadastro ou recuperação de senha dependendo da escolha do usuário
 */

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void clickListeners() {
        binding.btnLogin.setOnClickListener(v -> loginFirebase());
        binding.textCriar.setOnClickListener(v -> startActivity(new Intent(this, CadastroActivity.class)));
        binding.textEsqueci.setOnClickListener(v -> startActivity(new Intent(this, ResetActivity.class)));
    }

    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    private boolean isEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void errosLogin(Exception exception) {
        String erro;
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            erro = "Email ou senha incorretos, tente novamente";
        } else {
            erro = "Algo saiu mal";
        }
        mostrarSnackbar(erro);
    }

    private void loginFirebase() {
        final String email = Objects.requireNonNull(binding.editEmail.getText()).toString();
        final String senha = Objects.requireNonNull(binding.editSenha.getText()).toString();

        if (email.isEmpty()) {
            mostrarSnackbar("Por favor insira seu email.");
            binding.editEmail.requestFocus();
            return;
        }
        if (!isEmailValido(email)) {
            mostrarSnackbar("Formato de Email inválido");
            binding.editEmail.requestFocus();
            return;
        }
        if (senha.isEmpty()) {
            mostrarSnackbar("Por favor insira sua senha");
            binding.editSenha.requestFocus();
            return;
        }
        if (senha.length() < 6) {
            mostrarSnackbar("A senha deve ter no mínimo 6 caracteres");
            binding.editSenha.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                binding.progressBar.setVisibility(View.GONE);
                startActivity(new Intent(this, MainActivity.class));
            } else {
                binding.progressBar.setVisibility(View.GONE);
                Exception exception = task.getException();
                errosLogin(exception);
            }
        });
    }
}