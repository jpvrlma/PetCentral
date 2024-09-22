package com.example.petcentral.Alergias;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.Alergias;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditAlergiasBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EditAlergiasActivity extends AppCompatActivity {

    private ActivityEditAlergiasBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding = ActivityEditAlergiasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clickListeners();
        carregarDadosAlergia();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Cliques
    private void clickListeners() {
        binding.btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewAlergiasActivity.class);
            intent.putExtra("idPet", getIntent().getStringExtra("idPet"));
            startActivity(intent);
        });

        binding.editTextDataAplicacao.setOnClickListener(v -> startDatePicker());

        binding.btnRegistrar.setOnClickListener(v -> validarCampos());

        binding.btnExcluir.setOnClickListener(v -> mostrarAlertaExclusao());
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

        updateFirebase(nomeAlergia,dataDiagnostico,anotacoes);
    }

    //CARREGAR DADOS EDICAO
    private void carregarDadosAlergia() {
        String idPet = getIntent().getStringExtra("idPet");
        String idAlergia = getIntent().getStringExtra("idAlergia");
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("alergias").document(idAlergia)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Alergias alergias = documentSnapshot.toObject(Alergias.class);
                        if (alergias != null) {
                            binding.editNome.setText(alergias.getAlergia());
                            binding.editTextAnotacoes.setText(alergias.getAnotacoes());

                            if (alergias.getDataDiagnostico()!=null){
                                Date data = alergias.getDataDiagnostico().toDate();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                String dataFormatada = dateFormat.format(data);
                                binding.editTextDataAplicacao.setText(dataFormatada);
                            }
                        }

                    }
                });

    }


    //Salvar ediçao firebase
    private void updateFirebase(String nomeAlergia, String dataDiagnostico, String anotacoes) {
        String idPet = getIntent().getStringExtra("idPet");
        String idAlergia = getIntent().getStringExtra("idAlergia");

        Timestamp timestamp = converterParaTimestamp(dataDiagnostico);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("alergias").document(idAlergia)
                .update("alergia",nomeAlergia,"dataDiagnostico",timestamp,"anotacoes",anotacoes)
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(EditAlergiasActivity.this, ViewAlergiasActivity.class);
                    intent.putExtra("idPet", idPet);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                });
    }

    private void mostrarAlertaExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmação de exclusão")
                .setMessage("Tem certeza que deseja excluir esta alergia?")
                .setPositiveButton("Sim", (dialog, which) -> excluirAlergia()).setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void excluirAlergia(){
        String idPet = getIntent().getStringExtra("idPet");
        String idAlergia = getIntent().getStringExtra("idAlergia");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("alergias").document(idAlergia)
                .delete()
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(EditAlergiasActivity.this, ViewAlergiasActivity.class);
                    intent.putExtra("idPet", idPet);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(this, "Alergia excluída com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();

                });

    }

    // -------- UTILITARIOS ----------------
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