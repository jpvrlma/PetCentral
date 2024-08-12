package com.example.petcentral.Login;

import android.os.Bundle;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityResetBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/**
 * Esta atividade é responsável por fazer o reset de senha do usuário no aplicativo
 * Irá validar as informações passados no clique do botao de redefinição
 * e irá retornar um email para que ele possoa alterar sua senha.
 */

public class ResetActivity extends AppCompatActivity {

    private ActivityResetBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityResetBinding.inflate(getLayoutInflater());
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
        binding.btnEnviar.setOnClickListener(v -> resetSenha());
    }

    private boolean isEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void resetSenha() {
        final String email = Objects.requireNonNull(binding.editEmail.getText()).toString();

        if (email.isEmpty()) {
            binding.containerEmail.setError("Por favor preencha este campo");
            binding.editEmail.requestFocus();
            return;
        }
        if (!isEmailValido(email)){
            binding.containerEmail.setError("Email inválido ou não cadastrado");
            binding.editEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                mostrarSnackbar("O email digitado é inválido. Por favor, digite um email válido.");
                binding.editEmail.setText("");
            } else {
                Exception exception = task.getException();
                mostrarSnackbar(exception.getMessage());
            }
        });
    }

    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }
}