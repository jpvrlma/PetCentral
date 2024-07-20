package com.example.petcentral;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CadastroPetActivity extends AppCompatActivity {

    private ActivityCadastroPetBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    MaterialAutoCompleteTextView autoCompleteTextViewEspecie;
    MaterialAutoCompleteTextView autoCompleteTextViewRaca;
    MaterialAutoCompleteTextView autoCompleteTextViewSexo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCadastroPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        autoCompleteTextViewEspecie = binding.autoCompleteEspecie;
        autoCompleteTextViewRaca = binding.autoCompleteRaca;
        autoCompleteTextViewSexo = binding.autoCompleteSexo;

        //De acordo com o click do usuario atualizar o menu de raças
        autoCompleteTextViewEspecie.setOnItemClickListener((parent, view, position, id) -> setAdapter());


        inicializarAutoCompleteTextView(autoCompleteTextViewEspecie, R.array.EspecieArray);
        inicializarAutoCompleteTextView(autoCompleteTextViewSexo, R.array.SexoArray);

        clickListeners();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //Método para configurar o clique dos botões da atividade
    private void clickListeners() {
        binding.btnCadastrar.setOnClickListener(v -> validarCampos());
        binding.editData.setOnClickListener(v -> startDatePicker());
    }

    //Método para validar os campos preenchidos
    private void validarCampos() {
        String nome = binding.editNome.getText().toString().trim();
        String especie = binding.autoCompleteEspecie.getText().toString().trim();
        String raca = binding.autoCompleteRaca.getText().toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = binding.editData.getText().toString().trim();

        if (nome.isEmpty() && especie.isEmpty() && raca.isEmpty() && sexo.isEmpty() && dataNascimento.isEmpty()) {
            mostrarSnackbar(getString(R.string.snack_Vazio));
        } else if (nome.isEmpty()) {
            binding.editNome.setError(getString(R.string.err_set));
        } else if (especie.isEmpty()) {
            binding.textErrorEspecie.setVisibility(View.VISIBLE);
            binding.textErrorEspecie.setText(getString(R.string.err_set));
        } else if (raca.isEmpty()) {
            binding.textErrorRaca.setVisibility(View.VISIBLE);
            binding.textErrorRaca.setText(getString(R.string.err_set));
        } else if (sexo.isEmpty()) {
            binding.textErrorSexo.setVisibility(View.VISIBLE);
            binding.textErrorSexo.setText(getString(R.string.err_set));
        } else if (dataNascimento.isEmpty()) {
            binding.textErrorData.setVisibility(View.VISIBLE);
            binding.textErrorData.setText(getString(R.string.err_set));
        } else {
            binding.autoCompleteEspecie.setText(especie);
            binding.autoCompleteRaca.setText(raca);
            binding.autoCompleteSexo.setText(sexo);
            binding.editData.setText(sexo);
            binding.progressBar.setVisibility(View.VISIBLE);
            salvarFirebase(nome, especie, raca, sexo, dataNascimento);
        }
    }

    //Método para exibir uma snackbar ao usuário informando que os campos estão vazios ou inválidos
    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    //Método para abrir o DatePicker do Material
    private void startDatePicker() {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection));
            binding.editData.setText(date);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

    //Método para inicializar autoComplete (Dropdown)
    private void inicializarAutoCompleteTextView(MaterialAutoCompleteTextView autoCompleteTextView, int arrayResourceId) {
        String[] opcoes = getResources().getStringArray(arrayResourceId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcoes);
        autoCompleteTextView.setAdapter(adapter);
    }

    //Método para atualizar o menu de raças
    private void setAdapter() {
        String itemSelecionado = autoCompleteTextViewEspecie.getText().toString();
        switch (itemSelecionado) {
            case "Cachorro":
                inicializarAutoCompleteTextView(autoCompleteTextViewRaca, R.array.CachorroArray);
                binding.menuRaca.setHint(getString(R.string.pet_raca));
                break;
            case "Gato":
                inicializarAutoCompleteTextView(autoCompleteTextViewRaca, R.array.GatoArray);
                binding.menuRaca.setHint(getString(R.string.pet_raca));
                break;
            case "Hamster":
                inicializarAutoCompleteTextView(autoCompleteTextViewRaca, R.array.HamsterArray);
                binding.menuRaca.setHint(getString(R.string.pet_raca));
                break;
            case "Ave":
                inicializarAutoCompleteTextView(autoCompleteTextViewRaca, R.array.AveArray);
                binding.menuRaca.setHint(getString(R.string.pet_raca));
                break;
            case "Peixe":
                inicializarAutoCompleteTextView(autoCompleteTextViewRaca, R.array.PeixeArray);
                binding.menuRaca.setHint(getString(R.string.pet_raca));
                break;
        }
    }

    //Método para converter uma data em string para Timestamp
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

    //Método para salvar os dados no Firestore
    private void salvarFirebase(String nome, String especie, String raca, String sexo, String dataNascimento) {
        Timestamp timestamp = converterParaTimestamp(dataNascimento);
        Pet pet = new Pet(nome, especie, raca, sexo, timestamp);
        String userID = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(userID).collection("Pets").add(pet);
        binding.progressBar.setVisibility(View.GONE);
        mostrarSnackbar(getString(R.string.firebase_cadastro_sucesso));
    }
}