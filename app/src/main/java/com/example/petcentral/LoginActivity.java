package com.example.petcentral;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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
        binding.btnLogin.setOnClickListener(v -> validarCampos());
        binding.textCriar.setOnClickListener(v -> startActivity(new Intent(this, CadastroActivity.class)));
        binding.textEsqueci.setOnClickListener(v -> startActivity(new Intent(this, ResetActivity.class)));
    }

    //Método para exibir uma snackbar ao usuário informando que os campos estão vazios ou inválidos
    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    //Método para validar os campos de email e senha
    private void validarCampos() {
        String email = binding.editEmail.getText().toString().trim();
        String senha = binding.editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            mostrarSnackbar(getString(R.string.snack_Vazio));
        } else if (!isEmailValido(email)) {
            mostrarSnackbar(getString(R.string.snack_emailInvalido));
            binding.editEmail.requestFocus();
        } else if (senha.length() < 6) {
            mostrarSnackbar(getString(R.string.snack_senhaInvalida));
            binding.editSenha.requestFocus();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            loginFirebase();
        }
    }

    //Método para verificar se o formato do email digitado é valido
    private boolean isEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Método para pegar o erro de login do firebase e informar o usuário
    private void errosLogin(Exception exception) {
        String erro;
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            erro = "Email ou senha incorretos, tente novamente";
        } else {
            erro = "Algo saiu mal";
        }
        mostrarSnackbar(erro);
    }

    ////Método para realizar o login no Firebase
    private void loginFirebase() {
        String email = binding.editEmail.getText().toString();
        String senha = binding.editSenha.getText().toString();

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
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