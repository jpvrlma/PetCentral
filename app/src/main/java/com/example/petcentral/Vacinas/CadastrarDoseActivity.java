package com.example.petcentral.Vacinas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityCadastrarDoseBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class CadastrarDoseActivity extends AppCompatActivity {

    private ActivityCadastrarDoseBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MaterialAutoCompleteTextView autoCompleteTextViewMarca;
    private final ArrayList<String> idMarcaSelecionados = new ArrayList<>();
    private ArrayList<Long> intervaloDias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCadastrarDoseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        autoCompleteTextViewMarca = binding.autoCompleteMarca;

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        clickListeners();
        carregarInfoDaVacina();
        carregarMarcaAutoComplete();


        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void carregarInfoDaVacina() {
        String idVacina = getIntent().getStringExtra("idVacina");
        String idEspecie = getIntent().getStringExtra("idEspecie");

        db.collection("especies").document(idEspecie)
                .collection("vacinas").document(idVacina)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        String resumo = documentSnapshot.getString("resumo");
                        intervaloDias = (ArrayList<Long>) documentSnapshot.get("intervalos");
                        String nome = documentSnapshot.getString("nome");
                        binding.tvNome.setText(nome);
                        binding.tvResumo.setText(resumo);

                    }
                });
    }

    private void carregarMarcaAutoComplete() {
        String idEspecie = getIntent().getStringExtra("idEspecie");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("especies").document(idEspecie)
                .collection("vacinas").document(idVacina)
                .collection("marcas").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idMarca = dc.getId();
                            idMarcaSelecionados.add(idMarca);
                        }
                    }
                    inicializarAutoComplete(autoCompleteTextViewMarca, idMarcaSelecionados);
                });
    }

    private void validarCampos() {
        String marca = binding.autoCompleteMarca.getText().toString().trim();
        String lote = binding.editTextLote.getText().toString().trim();
        String dataAplicacao = binding.editTextDataAplicacao.getText().toString().trim();
        String anotacoes = binding.editTextAnotacoes.getText().toString().trim();
        String local = binding.editTextLocal.getText().toString().trim();
        String nomeVeterinario = binding.editTextNomeVeterinario.getText().toString().trim();

        if (marca.isEmpty() && dataAplicacao.isEmpty()) {
            mostrarSnackbar("Preencha todos os campos obrigatórios");
            binding.InputLayoutMarca.setError("Campo obrigatório");
            binding.InputLayoutDataAplicacao.setError("Campo obrigatório");
            return;
        }
        if (marca.isEmpty()) {
            binding.InputLayoutMarca.setError("Campo obrigatório");
            return;
        }
        if (dataAplicacao.isEmpty()) {
            binding.InputLayoutDataAplicacao.setError("Campo obrigatório");
        }

        Timestamp dataAplicacaoTimestamp = converterParaTimestamp(dataAplicacao);

        salvarVacinaFirebase(dataAplicacaoTimestamp);
        salvarDoseFirebase(dataAplicacaoTimestamp, anotacoes, marca, lote, local, nomeVeterinario);
    }


    private void salvarVacinaFirebase(Timestamp dataAplicacao) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String nome = getIntent().getStringExtra("nome");

        if (intervaloDias != null && !intervaloDias.isEmpty()) {
            Long primeiroIntervalo = intervaloDias.get(0);
            Timestamp proximaDose = calcularProximaDoseDias(dataAplicacao, primeiroIntervalo);

            HashMap<String, Object> vacina = new HashMap<>();
            vacina.put("dataAplicacao", dataAplicacao);
            vacina.put("proximaDose", proximaDose);
            vacina.put("nome",nome);

            db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .collection("pets").document(idPet)
                    .collection("vacinas").document(idVacina).set(vacina);
        }
    }


    private void salvarDoseFirebase(Timestamp dataAplicacao, String anotacoes, String marca, String lote, String local, String nomeVeterinario) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        if (intervaloDias != null && !intervaloDias.isEmpty()) {
            Long primeiroIntervalo = intervaloDias.get(0);
            Timestamp proximaDose = calcularProximaDoseDias(dataAplicacao, primeiroIntervalo);
            int numeroDose = 1;

            HashMap<String, Object> dose = new HashMap<>();
            dose.put("dataAplicacao", dataAplicacao);
            dose.put("proximaDose", proximaDose);
            dose.put("anotacoes", anotacoes);
            dose.put("marca", marca);
            dose.put("lote", lote);
            dose.put("local", local);
            dose.put("nomeVeterinario", nomeVeterinario);
            dose.put("numeroDose", numeroDose);
            dose.put("aplicada", true);

            db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .collection("pets").document(idPet)
                    .collection("vacinas").document(idVacina)
                    .collection("doses").document("Dose 1").set(dose)
                    .addOnSuccessListener(aVoid -> {
                        gerarDosesFuturas(dataAplicacao, new ArrayList<>(intervaloDias.subList(1, intervaloDias.size())));
                        Intent intent = new Intent(this, ViewVacinasActivity.class);
                        intent.putExtra("idPet", idPet);
                        startActivity(intent);
                        finish();
                    }).addOnFailureListener(e -> {
                        Log.e("CadastrarDoseActivity", "Erro ao salvar a dose: ", e);
                        mostrarSnackbar("Erro ao salvar a dose");
                    });
        }
    }

    public Timestamp calcularProximaDoseDias(Timestamp dataAplicacao, long intervaloDias) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(dataAplicacao.toDate());
        calendar.add(Calendar.DAY_OF_YEAR, (int) intervaloDias);
        return new Timestamp(calendar.getTime());
    }

    private void gerarDosesFuturas(Timestamp dataAplicacao, ArrayList<Long> intervaloDias) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        for (int i = 0; i < intervaloDias.size(); i++) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(dataAplicacao.toDate());
            calendar.add(Calendar.DAY_OF_YEAR, intervaloDias.get(i).intValue());
            Timestamp proximaDose = new Timestamp(calendar.getTime());
            int numeroDose = i + 2;

            HashMap<String, Object> dose = new HashMap<>();
            dose.put("dataAplicacao", null);
            dose.put("proximaDose", proximaDose);
            dose.put("anotacoes", null);
            dose.put("marca", null);
            dose.put("lote", null);
            dose.put("local", null);
            dose.put("nomeVeterinario", null);
            dose.put("aplicada", false);
            dose.put("numeroDose", numeroDose);

            String doseId = "Dose " + (i + 2);
            db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .collection("pets").document(idPet)
                    .collection("vacinas").document(idVacina)
                    .collection("doses").document(doseId).set(dose);

            dataAplicacao = proximaDose;
        }

    }


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

    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }


    private void startDatePicker() {
        binding.InputLayoutDataAplicacao.setError(null);
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String string = dateFormat.format(new Date(selection));
            binding.editTextDataAplicacao.setText(string);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

    private void inicializarAutoComplete(MaterialAutoCompleteTextView autoCompleteTextView, List<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void clickListeners() {
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.editTextDataAplicacao.setOnClickListener(v -> startDatePicker());
        binding.btnRegistrar.setOnClickListener(v -> validarCampos());
        binding.autoCompleteMarca.setOnClickListener(v -> binding.InputLayoutMarca.setError(null));
    }

}