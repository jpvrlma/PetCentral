package com.example.petcentral.Vacinas;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.DoseVacina;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditarDoseBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class EditarDoseActivity extends AppCompatActivity {

    private ActivityEditarDoseBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MaterialAutoCompleteTextView autoCompleteTextViewMarca;
    private final ArrayList<String> idMarcaSelecionados = new ArrayList<>();
    private int numeroDose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditarDoseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        autoCompleteTextViewMarca = binding.autoCompleteMarca;

        carregarMarcaAutoComplete();
        clickListeners();
        carregarDadosDose();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v-> finish());
        binding.btnCancelar.setOnClickListener(v-> finish());
        binding.btnRegistrar.setOnClickListener(v-> validarCampos());
        binding.btnExcluir.setOnClickListener(v -> mostrarAlertaExclusao());
        binding.editTextDataAplicacao.setOnClickListener(v-> startDatePicker());
    }

    private void pegarUltimaDataEAtualizar(){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .whereEqualTo("aplicada", true)
                .orderBy("numeroDose", Query.Direction.DESCENDING)
                .limit(1)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null){
                        DocumentSnapshot ultimoDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp ultimaAplicacao = ultimoDoc.getTimestamp("dataAplicacao");
                        adaptarDatas(ultimaAplicacao);

                    }
                });
    }
    private void carregarMarcaAutoComplete(){
        String idEspecie = getIntent().getStringExtra("idEspecie");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("especies").document(idEspecie)
                .collection("vacinas").document(idVacina)
                .collection("marcas").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null){
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()){
                            String idMarca = dc.getId();
                            idMarcaSelecionados.add(idMarca);
                        }
                    }
                    inicializarAutoComplete(autoCompleteTextViewMarca,idMarcaSelecionados);
                });
    }

    private void carregarDadosDose(){
            String idPet = getIntent().getStringExtra("idPet");
            String idVacina = getIntent().getStringExtra("idVacina");
            String idDose = getIntent().getStringExtra("idDose");

            db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .collection("pets").document(idPet)
                    .collection("vacinas").document(idVacina)
                    .collection("doses").document(idDose).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null){
                            DoseVacina doseVacina = documentSnapshot.toObject(DoseVacina.class);
                            if (doseVacina!= null){
                                binding.autoCompleteMarca.setText(doseVacina.getMarca(),false);
                                binding.editTextLote.setText(doseVacina.getLote());
                                binding.editTextAnotacoes.setText(doseVacina.getAnotacoes());
                                binding.editTextLocal.setText(doseVacina.getLocal());
                                binding.editTextNomeVeterinario.setText(doseVacina.getNomeVeterinario());
                                binding.tvNome.setText(idDose);
                                numeroDose = doseVacina.getNumeroDose();

                                if (doseVacina.getDataAplicacao() != null){
                                    Date dataAplicacao = doseVacina.getDataAplicacao().toDate();
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    String dataFormatada = sdf.format(dataAplicacao);
                                    binding.editTextDataAplicacao.setText(dataFormatada);
                                } else {
                                    binding.editTextDataAplicacao.setText("");
                                }

                                if (doseVacina.getNumeroDose() == 1){
                                    binding.switchAplicado.setVisibility(View.GONE);
                                    binding.btnExcluir.setVisibility(View.VISIBLE);
                                } else {
                                    binding.switchAplicado.setVisibility(View.VISIBLE);
                                    binding.btnExcluir.setVisibility(View.GONE);
                                }

                                if (doseVacina.isAplicada() == true){
                                    binding.switchAplicado.setChecked(true);
                                } else {
                                    binding.switchAplicado.setChecked(false);
                                }
                            } else {
                                mostrarSnackbar("Erro ao carregar dados da dose");
                            }
                        }
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

        if (!binding.switchAplicado.isChecked()){
            limparVacina();
            pegarUltimaDataEAtualizar();
            atualizarVacinaTimeline();
            return;
        }
        atualizarDose(marca,lote,dataAplicacao,anotacoes,local,nomeVeterinario);
        atualizarDosesFuturas(numeroDose,dataAplicacaoTimestamp);
        atualizarVacinaTimeline();
        verificarSeUltimaDoseEChamarGeracao();
    }

    private void limparVacina(){

        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        HashMap<String, Object> dose = new HashMap<>();
        dose.put("dataAplicacao", null);
        dose.put("anotacoes",null);
        dose.put("marca", null);
        dose.put("lote", null);
        dose.put("local", null);
        dose.put("nomeVeterinario", null);
        dose.put("aplicada",false);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").document(idDose).update(dose);
    }

    private void verificarSeUltimaDoseEChamarGeracao() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .orderBy("numeroDose", Query.Direction.DESCENDING)
                .limit(1)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot ultimaDose = queryDocumentSnapshots.getDocuments().get(0);
                        String ultimaDoseId = ultimaDose.getId();

                        if (idDose.equals(ultimaDoseId)) {
                            gerarProximaDose();
                        }
                    }
                });
    }

    private void gerarProximaDose(){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .orderBy("numeroDose",Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots != null){
                            DocumentSnapshot ultimaDose = queryDocumentSnapshots.getDocuments().get(0);
                            int numeroDose = ultimaDose.getLong("numeroDose").intValue();
                            Timestamp proximaDose = ultimaDose.getTimestamp("proximaDose");

                            criarNovaDose(proximaDose,numeroDose + 1);

                        }
                }).addOnFailureListener(e ->{
                    Log.d(TAG, "GERAR DOSE", e);
                });
    }

    private  void criarNovaDose(Timestamp proximaDose,int novoNumeroDose){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        HashMap<String, Object> dose = new HashMap<>();
        dose.put("dataAplicacao", null);
        dose.put("proximaDose", proximaDose);
        dose.put("anotacoes",null);
        dose.put("marca", null);
        dose.put("lote", null);
        dose.put("local", null);
        dose.put("nomeVeterinario", null);
        dose.put("aplicada",false);
        dose.put("numeroDose",novoNumeroDose);

        String doseId = "Dose " + (novoNumeroDose);
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").document(doseId).set(dose);
    }

    private void excluirTodasAsDosesEVacina() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }
                    db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                            .collection("pets").document(idPet)
                            .collection("vacinas").document(idVacina).delete()
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(EditarDoseActivity.this, ViewVacinasActivity.class);
                                intent.putExtra("idPet", idPet);
                                startActivity(intent);
                                finish();
                            });

                });
    }

    private void mostrarAlertaExclusao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmação de Exclusão")
                .setMessage("Você tem certeza de que deseja excluir todas as doses e o registro dessa vacina? Essa ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirTodasAsDosesEVacina();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void atualizarDose(String marca, String lote, String dataAplicacao, String anotacoes, String local, String nomeVeterinario){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        Timestamp timestamp = converterParaTimestamp(dataAplicacao);
        Timestamp proximaDose = calcularProximaDoseMeses(timestamp, 12);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").document(idDose)
                .update("marca",marca,"lote",lote,"dataAplicacao",timestamp,"proximaDose",proximaDose,"anotacoes",anotacoes
                        ,"local",local,"nomeVeterinario",nomeVeterinario,"aplicada",binding.switchAplicado.isChecked());
    }

    private void adaptarDatas(Timestamp ultimaData){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null){
                        Timestamp ultimaAplicacao = ultimaData;
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()){
                            DoseVacina doseVacina = dc.toObject(DoseVacina.class);
                            if (doseVacina != null && !doseVacina.isAplicada()){
                                ultimaAplicacao = calcularProximaDoseMeses(ultimaAplicacao, 12);
                                dc.getReference().update("proximaDose",ultimaAplicacao);
                            }
                    }
                    }
                });
    }

    private void atualizarDosesFuturas(int doseAtual,Timestamp dataAplicacao){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null){
                        Timestamp ultimaAplicacao = dataAplicacao;
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()){
                            DoseVacina doseVacina = dc.toObject(DoseVacina.class);
                            if (doseVacina != null && doseVacina.getNumeroDose() > doseAtual && !doseVacina.isAplicada()){
                                ultimaAplicacao = calcularProximaDoseMeses(ultimaAplicacao, 12);
                                dc.getReference().update("proximaDose",ultimaAplicacao);
                            }
                        }
                    }
                });
    }

    private void atualizarVacinaTimeline() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .whereEqualTo("aplicada", true)
                .orderBy("numeroDose", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot ultimoDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp ultimaAplicacao = ultimoDoc.getTimestamp("dataAplicacao");
                        Timestamp ultimaProximaDose = ultimoDoc.getTimestamp("proximaDose");
                        atualizarVacina(ultimaAplicacao, ultimaProximaDose);
                    } else{
                        mostrarSnackbar("Nenhuma dose aplicada");
                    }
                }).addOnFailureListener(e ->{
                    Log.d(TAG, "get failed with ", e);
                });
    }

    private void atualizarVacina(Timestamp dataAplicacao,Timestamp proximaDose){
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");


        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina).update("dataAplicacao",dataAplicacao,"proximaDose",proximaDose);
    }

    public Timestamp calcularProximaDoseMeses(Timestamp dataAplicacao, int intervaloMeses){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(dataAplicacao.toDate());
        calendar.add(Calendar.MONTH,intervaloMeses);
        return new Timestamp(calendar.getTime());
    }

    private void inicializarAutoComplete(MaterialAutoCompleteTextView autoCompleteTextView, List<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista);
        autoCompleteTextView.setAdapter(adapter);
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
}