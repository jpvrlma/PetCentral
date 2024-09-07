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
    private ArrayList<Long> intervaloDias = new ArrayList<>();
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
        carregarInfoDaVacina();
        carregarDadosDose();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void clickListeners() {
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.btnRegistrar.setOnClickListener(v -> validarCampos());
        binding.btnExcluir.setOnClickListener(v -> mostrarAlertaParaExclusao());
        binding.editTextDataAplicacao.setOnClickListener(v -> startDatePicker());
    }

    private void carregarInfoDaVacina() {
        String idVacina = getIntent().getStringExtra("idVacina");
        String idEspecie = getIntent().getStringExtra("idEspecie");

        db.collection("especies").document(idEspecie)
                .collection("vacinas").document(idVacina)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        intervaloDias = (ArrayList<Long>) documentSnapshot.get("intervalos");
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
                        ArrayList<String> nomeMarcas = new ArrayList<>();
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idMarca = dc.getId();
                            String nome = dc.getString("nome");
                            idMarcaSelecionados.add(idMarca);
                            nomeMarcas.add(nome);
                        }
                        inicializarAutoComplete(autoCompleteTextViewMarca, nomeMarcas);
                    }

                });
    }

    private void carregarDadosDose() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").document(idDose).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        DoseVacina doseVacina = documentSnapshot.toObject(DoseVacina.class);
                        if (doseVacina != null) {
                            binding.autoCompleteMarca.setText(doseVacina.getMarca(), false);
                            binding.editTextLote.setText(doseVacina.getLote());
                            binding.editTextAnotacoes.setText(doseVacina.getAnotacoes());
                            binding.editTextLocal.setText(doseVacina.getLocal());
                            binding.editTextNomeVeterinario.setText(doseVacina.getNomeVeterinario());
                            binding.tvNome.setText(idDose);
                            numeroDose = doseVacina.getNumeroDose();

                            if (doseVacina.getDataAplicacao() != null) {
                                Date dataAplicacao = doseVacina.getDataAplicacao().toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                String dataFormatada = sdf.format(dataAplicacao);
                                binding.editTextDataAplicacao.setText(dataFormatada);
                            } else {
                                binding.editTextDataAplicacao.setText("");
                            }

                            if (doseVacina.getNumeroDose() == 1) {
                                binding.switchAplicado.setVisibility(View.GONE);
                                binding.btnExcluir.setVisibility(View.VISIBLE);
                            } else {
                                binding.switchAplicado.setVisibility(View.VISIBLE);
                                binding.btnExcluir.setVisibility(View.GONE);
                            }

                            if (doseVacina.isAplicada() == true) {
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

        lote = lote.isEmpty() ? null : lote;
        anotacoes = anotacoes.isEmpty() ? null : anotacoes;
        local = local.isEmpty() ? null : local;
        nomeVeterinario = nomeVeterinario.isEmpty() ? null : nomeVeterinario;

        //Nao aplicado
        if (!binding.switchAplicado.isChecked()) {
            limparCamposVacina();
            limparDosesFuturas();
            pegarUltimaDataEAtualizarDosesFuturas();
            atualizarVacinaTimeline();
            return;
        }
        //Aplicado
        verificarUltimaDoseAplicada(marca, lote, dataAplicacao, anotacoes, local, nomeVeterinario);
        atualizarDosesFuturas(numeroDose, dataAplicacaoTimestamp);
        atualizarVacinaTimeline();
        verificarSeUltimaDoseEChamarGeracao();
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

    private void gerarProximaDose() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .orderBy("numeroDose", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        DocumentSnapshot ultimaDose = queryDocumentSnapshots.getDocuments().get(0);
                        int numeroDose = ultimaDose.getLong("numeroDose").intValue();
                        Timestamp proximaDose = ultimaDose.getTimestamp("proximaDose");

                        criarNovaDose(proximaDose, numeroDose + 1);

                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "GERAR DOSE", e);
                });
    }

    private void criarNovaDose(Timestamp proximaDose, int novoNumeroDose) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        HashMap<String, Object> dose = new HashMap<>();
        dose.put("dataAplicacao", null);
        dose.put("proximaDose", proximaDose);
        dose.put("anotacoes", null);
        dose.put("marca", null);
        dose.put("lote", null);
        dose.put("local", null);
        dose.put("nomeVeterinario", null);
        dose.put("aplicada", false);
        dose.put("numeroDose", novoNumeroDose);

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
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });

                });
    }

    private void mostrarAlertaParaExclusao() {
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

    private void atualizarDose(String marca, String lote, String dataAplicacao, String anotacoes, String local, String nomeVeterinario) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        Timestamp timestamp = converterParaTimestamp(dataAplicacao);
        Timestamp proximaDose = null;

        if (intervaloDias != null && !intervaloDias.isEmpty()) {
            long intervalo;
            if (numeroDose < intervaloDias.size()) {
                intervalo = intervaloDias.get(numeroDose - 1);
            } else {
                intervalo = intervaloDias.get(intervaloDias.size() - 1);
            }
            proximaDose = calcularProximaDoseDias(timestamp, intervalo);
        }

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").document(idDose)
                .update("marca", marca, "lote", lote, "dataAplicacao", timestamp, "proximaDose", proximaDose, "anotacoes", anotacoes
                        , "local", local, "nomeVeterinario", nomeVeterinario, "aplicada", binding.switchAplicado.isChecked());
    }


    private void atualizarDosesFuturas(int doseAtual, Timestamp dataAplicacao) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        Timestamp ultimaAplicacao = dataAplicacao;
                        int numeroDoseAtual = doseAtual;
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            DoseVacina doseVacina = dc.toObject(DoseVacina.class);
                            if (doseVacina != null && doseVacina.getNumeroDose() > doseAtual && !doseVacina.isAplicada()) {
                                numeroDoseAtual++;
                                long intervalo;

                                if (numeroDoseAtual <= intervaloDias.size()) {
                                    intervalo = intervaloDias.get(numeroDoseAtual - 1);
                                } else {
                                    intervalo = intervaloDias.get(intervaloDias.size() - 1);
                                }
                                System.out.println("intervaloDias: " + intervalo);
                                if (ultimaAplicacao != null) {
                                    ultimaAplicacao = calcularProximaDoseDias(ultimaAplicacao, intervalo);
                                    dc.getReference().update("proximaDose", ultimaAplicacao)
                                            .addOnFailureListener(e -> Log.e("AtualizarDosesFuturas", "Erro ao atualizar proximaDose", e));
                                }
                            }
                        }
                    }
                });
    }


    private void limparCamposVacina() {

        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        HashMap<String, Object> dose = new HashMap<>();
        dose.put("dataAplicacao", null);
        dose.put("anotacoes", null);
        dose.put("marca", null);
        dose.put("lote", null);
        dose.put("local", null);
        dose.put("nomeVeterinario", null);
        dose.put("aplicada", false);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").document(idDose).update(dose);

    }

    private void limparDosesFuturas() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idDose = getIntent().getStringExtra("idDose");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .orderBy("numeroDose", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        boolean limpar = false;
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String id = dc.getId();
                            if (id != null) {
                                // Se encontrar a dose que foi limpa, comece a limpar as subsequentes
                                System.out.println(id);
                                if (id.equals(idDose)) {
                                    limpar = true;
                                }
                                if (limpar) {
                                    // Limpa todas as doses futuras não aplicadas
                                    HashMap<String, Object> doseFutura = new HashMap<>();
                                    doseFutura.put("dataAplicacao", null);
                                    doseFutura.put("anotacoes", null);
                                    doseFutura.put("marca", null);
                                    doseFutura.put("lote", null);
                                    doseFutura.put("local", null);
                                    doseFutura.put("nomeVeterinario", null);
                                    doseFutura.put("aplicada", false);

                                    dc.getReference().update(doseFutura);
                                }
                            }
                        }
                    }
                });
    }

    //Com a data pegada no firebase da ultima dose atualiza as outras
    private void adaptarDatas(Timestamp ultimaData) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses")
                .orderBy("numeroDose", Query.Direction.ASCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        Timestamp ultimaAplicacao = ultimaData;
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            DoseVacina doseVacina = dc.toObject(DoseVacina.class);
                            if (doseVacina != null && !doseVacina.isAplicada()) {
                                long intervalo;
                                if (numeroDose < intervaloDias.size()) {
                                    intervalo = intervaloDias.get(numeroDose - 1);
                                } else{
                                    intervalo = intervaloDias.get(intervaloDias.size() - 1);
                                }
                                ultimaAplicacao = calcularProximaDoseDias(ultimaAplicacao, intervalo);
                                dc.getReference().update("proximaDose", ultimaAplicacao);
                                numeroDose++;
                            }
                        }
                    }
                });
    }


    //Pega ultima dose onde aplicada igual a true
    private void pegarUltimaDataEAtualizarDosesFuturas() {
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
                    if (queryDocumentSnapshots != null) {
                        DocumentSnapshot ultimoDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp ultimaAplicacao = ultimoDoc.getTimestamp("dataAplicacao");
                        int numeroDoseAtual = ultimoDoc.getLong("numeroDose").intValue();
                        System.out.println(numeroDoseAtual);
                        adaptarDatas(ultimaAplicacao);
                    }
                });
    }

    private void verificarUltimaDoseAplicada(String marca, String lote, String dataAplicacao, String anotacoes, String local, String nomeVeterinario){
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
                        int numeroDoseAtual = ultimoDoc.getLong("numeroDose").intValue();
                        if (numeroDoseAtual < numeroDose){
                            mostrarSnackbar("Cadastre a dose anterior primeiro");
                        }else {
                            atualizarDose(marca,lote,dataAplicacao,anotacoes,local,nomeVeterinario);
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
                        atualizarVacinaClass(ultimaAplicacao, ultimaProximaDose);
                    } else {
                        mostrarSnackbar("Nenhuma dose aplicada");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "get failed with ", e);
                });
    }

    private void atualizarVacinaClass(Timestamp dataAplicacao, Timestamp proximaDose) {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");


        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina).update("dataAplicacao", dataAplicacao, "proximaDose", proximaDose);
    }

    public Timestamp calcularProximaDoseDias(Timestamp dataAplicacao, long intervaloDias) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(dataAplicacao.toDate());
        calendar.add(Calendar.DAY_OF_YEAR, (int) intervaloDias);
        return new Timestamp(calendar.getTime());
    }

    public Timestamp calcularProximaDoseMeses(Timestamp dataAplicacao, int intervaloMeses) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(dataAplicacao.toDate());
        calendar.add(Calendar.MONTH, intervaloMeses);
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