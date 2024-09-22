package com.example.petcentral.Alergias;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityAdicionarAlergiaBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class AdicionarAlergiaActivity extends AppCompatActivity {

    private ActivityAdicionarAlergiaBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdicionarAlergiaBinding.inflate(getLayoutInflater());
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

    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());

        binding.editTextDataAplicacao.setOnClickListener(v -> startDatePicker());

        binding.btnRegistrar.setOnClickListener(v -> validarCampos());

        binding.btnCancelar.setOnClickListener(v -> finish());

    }

    // Validações
    private void validarCampos(){
        String nomeAlergia = binding.editNome.getText().toString().trim();
        String dataDiagnostico = binding.editTextDataAplicacao.getText().toString().trim();
        String anotacoes = binding.editTextAnotacoes.getText().toString().trim();

        if (nomeAlergia.isEmpty()){
            binding.containerNome.setError("Campo obrigatório");
            return;
        }

        if (dataDiagnostico.isEmpty()){
            binding.InputLayoutDataAplicacao.setError("Campo obrigatório");
            return;
        }

        anotacoes = anotacoes.isEmpty() ? null : anotacoes;

        salvarFirebase(nomeAlergia,dataDiagnostico,anotacoes);
    }

    //Salvar firebase
    private void salvarFirebase(String alergiaString, String dataDiagnotico, String anotacoes){
        String idPet = getIntent().getStringExtra("idPet");

        Timestamp timestamp = converterParaTimestamp(dataDiagnotico);

        HashMap<String,Object> alergia = new HashMap<>();

        alergia.put("alergia", alergiaString);
        alergia.put("dataDiagnostico", timestamp);
        alergia.put("anotacoes", anotacoes);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet).collection("alergias").add(alergia)
                .addOnSuccessListener(documentReference -> {
                    Intent intent = new Intent(this, ViewAlergiasActivity.class);
                    intent.putExtra("idPet", idPet);
                    startActivity(intent);
                });
    }

    // ---------------- Utilitários -----------------
    private Timestamp converterParaTimestamp(String dataStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = dateFormat.parse(dataStr);
            if (parsedDate != null) {
                return new Timestamp(parsedDate);
            } else {
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startDatePicker() {
        binding.InputLayoutDataAplicacao.setError(null);
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String string = dateFormat.format(new Date(selection));
            binding.editTextDataAplicacao.setText(string);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

}