package com.example.petcentral;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.databinding.ActivityEditUserBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class editUserActivity extends AppCompatActivity {

    private ActivityEditUserBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private MaterialAutoCompleteTextView autoCompleteTextViewSexo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        autoCompleteTextViewSexo = binding.autoCompleteSexo;

        getUsuario();
        listaSexo();
        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validarCampos() {
        String nome = binding.editTextNome.getText().toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = binding.editTextNascimento.getText().toString().trim();
        if (nome.isEmpty()) {
            binding.editTextNome.setError("Campo obrigatório");
        } else {
            binding.autoCompleteSexo.setText(sexo);
            binding.editTextNascimento.setText(dataNascimento);
            atualizarUsuario(nome, sexo, dataNascimento);
        }

    }

    private void clickListeners() {
        binding.btnSalvar.setOnClickListener(v -> validarCampos());
        binding.editTextNascimento.setOnClickListener(v -> startDatePicker());
    }

    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    private void getUsuario() {
        String userID = mAuth.getCurrentUser().getUid();

        db.collection("Usuarios").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    binding.editTextNome.setText(document.getString("nome"));
                    binding.autoCompleteSexo.setText(document.getString("sexo"));
                    binding.editTextNascimento.setText(document.getString("dataNascimento"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERRO FIRESTORE DATA", e.getMessage());
            }
        });
    }

    private void atualizarUsuario(String nome, String sexo, String dataNascimento) {

        Timestamp timestamp = converterParaTimestamp(dataNascimento);

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Usuarios").document(userId)
                .update("nome", nome, "sexo", sexo, "dataNascimento", timestamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mostrarSnackbar("Atualizado com sucesso");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mostrarSnackbar("Algo saiu mal");
                    }
                });

    }

    private void listaSexo() {
        db.collection("Genero").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> lista = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            lista.add(snapshot.getString("nome"));
                            inicializarAutoComplete(autoCompleteTextViewSexo,lista);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mostrarSnackbar("Erro ao carregar");
                    }
                });
    }

    private void inicializarAutoComplete(MaterialAutoCompleteTextView autoCompleteTextView,List<String> lista){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,lista);
        autoCompleteTextViewSexo.setAdapter(adapter);
    }

    private void startDatePicker() {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma Data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection));
            binding.editTextNascimento.setText(date);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

    private Timestamp converterParaTimestamp(String dataStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dtNascimento = dateFormat.parse(dataStr);
            return new Timestamp(dtNascimento);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
