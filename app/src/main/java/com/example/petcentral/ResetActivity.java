package com.example.petcentral;

import android.os.Bundle;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.databinding.ActivityResetBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private ActivityResetBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clickListeners();
        mAuth = FirebaseAuth.getInstance();
    }

    //Método para configurar o clique dos botões da atividade
    private void clickListeners() {
        binding.btnEnviar.setOnClickListener(v -> validarCampos());
    }

    //Método para verificar se o formato do email digitado é valido
    private boolean isEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Método para validar os campos de email
    private void validarCampos() {
        String email = binding.editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            mostrarSnackbar(getString(R.string.snack_Vazio));
        } else if (!isEmailValido(email)) {
            mostrarSnackbar(getString(R.string.snack_emailInvalido));
            binding.editEmail.requestFocus();
        } else {
            resetSenha();
        }
    }

    //Método para realizar o reset de senha no Firebase
    private void resetSenha() {
        String email = binding.editEmail.getText().toString();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                mostrarSnackbar(getString(R.string.snack_emailEnviado));
                binding.editEmail.setText("");
            } else {
                Exception exception = task.getException();
                mostrarSnackbar(exception.getMessage());
            }
        });
    }

    //Método para exibir uma snackbar ao usuário informando que os campos estão vazios ou inválidos
    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }
}