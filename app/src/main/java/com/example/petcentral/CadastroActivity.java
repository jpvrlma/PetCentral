package com.example.petcentral;


import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.databinding.ActivityCadastroBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    AutoCompleteTextView autoCompleteTextViewSexo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clickListeners();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //Método para configurar o clique dos botões da atividade
    private void clickListeners() {
        binding.btncadastrar.setOnClickListener(v -> validarCampos());
    }

    //Método para verificar se o formato do email digitado é valido
    private boolean isEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Método para exibir uma snackbar ao usuário informando que os campos estão vazios ou inválidos
    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    //Método para validar os campos preenchidos
    private void validarCampos() {
        String nome = binding.editNome.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String senha = binding.editSenha.getText().toString().trim();

        if (nome.isEmpty() && email.isEmpty() && senha.isEmpty()) {
            mostrarSnackbar(getString(R.string.snack_Vazio));
        } else if (nome.isEmpty()) {
            binding.editNome.setError(getString(R.string.err_set));
        } else if (email.isEmpty()) {
            binding.editEmail.setError(getString(R.string.err_set));
        } else if (!isEmailValido(email)) {
            binding.editEmail.setError(getString(R.string.snack_emailInvalido));
        } else if (senha.isEmpty()) {
            binding.editSenha.setError(getString(R.string.err_set));
        } else if (senha.length() < 6) {
            binding.editSenha.setError(getString(R.string.snack_senhaInvalida));
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            cadastrarFirebase(nome, email, senha);
        }
    }

    //Método para realizar o cadastro no Firebase
    private void cadastrarFirebase(String nome, String email, String senha) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Usuario user = new Usuario(nome, email);
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            db.collection("Usuarios").document(userId).set(user);
                            binding.progressBar.setVisibility(View.GONE);
                            mostrarSnackbar("Cadastro realizado com sucesso!");
                        }
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        errosCadastro(task.getException());
                    }
                });
    }

    //Método para exibir possiveis erros de cadastro
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

}