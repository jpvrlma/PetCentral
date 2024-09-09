package com.example.petcentral.Usuario;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditUserBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Esta atividade irá editar os dados usuário logado no aplicativo
 */

public class editUserActivity extends AppCompatActivity {

    private ActivityEditUserBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialAutoCompleteTextView autoCompleteTextViewSexo = binding.autoCompleteSexo;
        inicializarAutoCompleteTextViewLocal(autoCompleteTextViewSexo, R.array.GeneroArray);

        getUsuario();
        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Cliques
    private void clickListeners() {
        binding.btnSalvar.setOnClickListener(v -> validarCampos());

        binding.editTextNascimento.setOnClickListener(v -> startDatePicker());

        binding.backButton.setOnClickListener(v -> finish());

        binding.editTextNome.setOnClickListener(v -> binding.textInputLayoutNome.setError(null));

        binding.userFoto.setOnClickListener(v -> {
            selecionarImagem();
        });
    }

    //Carregar dados do usuário
    private void getUsuario() {
        binding.main.setVisibility(View.GONE);
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            binding.editTextNome.setText(document.getString("nome"));
                            binding.autoCompleteSexo.setText(document.getString("sexo"), false);
                            Timestamp timestamp = document.getTimestamp("dataNascimento");
                            if (timestamp != null) {
                                Date dataNascimento = timestamp.toDate();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                String dataFormatada = dateFormat.format(dataNascimento);
                                binding.editTextNascimento.setText(dataFormatada);
                            }
                        }
                        binding.main.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(e -> Log.e("ERRO FIRESTORE DATA", Objects.requireNonNull(e.getMessage())));
    }


    // --------------------- EDIÇAO DO USUARIO ----------------
    private void validarCampos() {
        String nome = Objects.requireNonNull(binding.editTextNome.getText()).toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = Objects.requireNonNull(binding.editTextNascimento.getText()).toString().trim();
        if (nome.isEmpty()) {
            binding.textInputLayoutNome.setError("Campo obrigatório");
        } else {
            binding.autoCompleteSexo.setText(sexo, false);
            binding.editTextNascimento.setText(dataNascimento);
            atualizarUsuario(nome, sexo, dataNascimento);
            UploadImagemFirebase(imageUri);
        }
    }

    private void atualizarUsuario(String nome, String sexo, String dataNascimento) {
        Timestamp timestamp = converterParaTimestamp(dataNascimento);
        db.collection("usuarios").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .update("nome", nome, "sexo", sexo, "dataNascimento", timestamp)
                .addOnSuccessListener(unused -> mostrarSnackbar("Atualizado com sucesso"))
                .addOnFailureListener(e -> mostrarSnackbar("Algo saiu mal"));
    }

    // ------------------- Upload de imagens do usuario ------------------

    private void selecionarImagem(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePicker.launch(intent);
    }

    private ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        binding.userFoto.setImageURI(imageUri);
                    }
            });

    private void UploadImagemFirebase(Uri imageUri){
        if (imageUri != null){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("fotos_de_perfil/" +  mAuth.getCurrentUser().getUid() + ".jpg");

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    db.collection("usuarios").document(mAuth.getCurrentUser().getUid()).update("fotoPerfil", downloadUrl);
            }));
        }
    }


    // ------------------- Utilitários --------------------
    private void inicializarAutoCompleteTextViewLocal(MaterialAutoCompleteTextView autoCompleteTextView, int arrayResourceId) {
        String[] opcoes = getResources().getStringArray(arrayResourceId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcoes);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    private void startDatePicker() {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String string = dateFormat.format(new Date(selection));
            binding.editTextNascimento.setText(string);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

    private Timestamp converterParaTimestamp(String dataStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = dateFormat.parse(dataStr);
            return new Timestamp(parsedDate);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
