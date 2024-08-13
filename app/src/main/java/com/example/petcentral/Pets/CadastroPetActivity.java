package com.example.petcentral.Pets;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.databinding.ActivityCadastroPetBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Esta atividade irá cadastrar o pet no Firestore
 * recebendo dados como Data,nome,especie,sexo e raça
 */

public class CadastroPetActivity extends AppCompatActivity {

    private ActivityCadastroPetBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private MaterialAutoCompleteTextView autoCompleteTextViewEspecie, autoCompleteTextViewRaca, autoCompleteTextViewSexo;
    private ArrayList<String> idEspecieSelecionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCadastroPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        autoCompleteTextViewEspecie = binding.autoCompleteEspecie;
        autoCompleteTextViewRaca = binding.autoCompleteRaca;
        autoCompleteTextViewSexo = binding.autoCompleteSexo;


        inicializarAutoCompleteTextViewLocal(autoCompleteTextViewSexo, R.array.SexoArray);
        carregarEspecie();
        isEspecieSelecionada();
        clickListeners();



        autoCompleteTextViewEspecie.setOnItemClickListener((parent, view, position, id) -> {
            binding.autoCompleteEspecie.setError(null);
            autoCompleteTextViewRaca.setText("",false);
            autoCompleteTextViewRaca.setEnabled(true);
            carregarRaca(autoCompleteTextViewRaca,idEspecieSelecionados.get(position));
        });


    }

    private void clickListeners() {
        binding.btnCadastrar.setOnClickListener(v -> validarCampos());
        binding.editData.setOnClickListener(v -> startDatePicker());

        autoCompleteTextViewEspecie.setOnClickListener(v ->{
            autoCompleteTextViewRaca.setText("",false);
            binding.menuEspecie.setError(null);
        });
        autoCompleteTextViewRaca.setOnClickListener(v -> binding.menuRaca.setError(null));
        autoCompleteTextViewSexo.setOnClickListener(v -> binding.containerSexo.setError(null));

    }

    private void validarCampos() {
        String nome = Objects.requireNonNull(binding.editNome.getText()).toString().trim();
        String especie = binding.autoCompleteEspecie.getText().toString().trim();
        String raca = binding.autoCompleteRaca.getText().toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = Objects.requireNonNull(binding.editData.getText()).toString().trim();

        if (nome.isEmpty() && especie.isEmpty() && raca.isEmpty() && sexo.isEmpty() && dataNascimento.isEmpty()) {
            mostrarSnackbar("Campos obrigatórios estão vazios. Por favor, preencha todos os campos para continuar.");
            return;
        }
        if (nome.isEmpty()) {
            binding.containerNome.setError("Campo obrigatório");
            return;
        }
        if (especie.isEmpty()) {
            binding.menuEspecie.setError("Campo obrigatório");
            return;
        }
        if (raca.isEmpty()) {
            binding.menuRaca.setError("Campo obrigatório");
            return;
        }
        if (sexo.isEmpty()) {
            binding.containerSexo.setError("Campo obrigatório");
            return;
        }
        if (dataNascimento.isEmpty()) {
            binding.editData.setError("Campo obrigatório");
            return;
        }
        binding.autoCompleteEspecie.setText(especie);
        binding.autoCompleteRaca.setText(raca);
        binding.autoCompleteSexo.setText(sexo);
        binding.editData.setText(dataNascimento);
        salvarFirebase(nome, especie, raca, sexo, dataNascimento);
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
            binding.editData.setText(string);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

    private void inicializarAutoCompleteTextViewLocal(MaterialAutoCompleteTextView autoCompleteTextView, int arrayResourceId) {
        String[] opcoes = getResources().getStringArray(arrayResourceId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcoes);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void inicializarAutoComplete(MaterialAutoCompleteTextView autoCompleteTextView, List<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void isEspecieSelecionada(){
        if (binding.autoCompleteEspecie.getText().toString().isEmpty()){
            binding.autoCompleteRaca.setText("Escolha uma especie primeiro!",false);
            binding.autoCompleteRaca.setEnabled(false);
        }
    }

    private void carregarEspecie(){
        db.collection("especies").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> lista = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String especie = dc.getString("nome");
                            String idEspecie = dc.getId();
                            lista.add(especie);
                            idEspecieSelecionados.add(idEspecie);
                        }
                        inicializarAutoComplete(autoCompleteTextViewEspecie,lista);
                    }
                });
    }

    private void carregarRaca(MaterialAutoCompleteTextView autoCompleteTextView, String idEspecie){
        db.collection("especies").document(idEspecie).collection("racas")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> listaRacas = new ArrayList<>();
                    if (queryDocumentSnapshots != null){
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()){
                            String raca = dc.getString("nome");
                            listaRacas.add(raca);
                        }
                        inicializarAutoComplete(autoCompleteTextView,listaRacas);
                    }
                });
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

    private void salvarFirebase(String nome, String especie, String raca, String sexo, String dataNascimento) {
        Timestamp timestamp = converterParaTimestamp(dataNascimento);

        Map<String,Object> pet = new HashMap<>();
        pet.put("nome",nome);
        pet.put("especie",especie);
        pet.put("raca",raca);
        pet.put("sexo",sexo);
        pet.put("dataNascimento",timestamp);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid()).collection("pets").add(pet);
        mostrarSnackbar("Pet cadastrado com sucesso!");
        startActivity(new Intent(CadastroPetActivity.this, MainActivity.class));
    }
}