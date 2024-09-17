package com.example.petcentral.Pets;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.petcentral.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private String idEspecie, idRaca;
    private Uri imageUri;

    private MaterialAutoCompleteTextView autoCompleteTextViewEspecie, autoCompleteTextViewRaca, autoCompleteTextViewSexo;
    private final ArrayList<String> idEspecieSelecionados = new ArrayList<>();
    private final ArrayList<String> idRacaSelecionados = new ArrayList<>();

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
            autoCompleteTextViewRaca.setText("", false);
            autoCompleteTextViewRaca.setEnabled(true);
            idEspecie = idEspecieSelecionados.get(position);
            carregarRaca(autoCompleteTextViewRaca, idEspecie);
        });
        autoCompleteTextViewRaca.setOnItemClickListener((parent, view, position, id) -> idRaca = idRacaSelecionados.get(position));
    }

    //Cliques
    private void clickListeners() {
        binding.btnCadastrar.setOnClickListener(v -> validarCampos());

        binding.editData.setOnClickListener(v -> startDatePicker());

        binding.btnVoltar.setOnClickListener(v -> finish());

        binding.petAvatar.setOnClickListener(v -> selecionarImagem());

        binding.editNome.setOnClickListener(v -> binding.containerNome.setError(null));
        autoCompleteTextViewRaca.setOnClickListener(v -> binding.menuRaca.setError(null));
        autoCompleteTextViewSexo.setOnClickListener(v -> binding.containerSexo.setError(null));
    }


    // ---------------- Metodos para salvar no firebase ---------------------
    private void validarCampos() {
        String nome = Objects.requireNonNull(binding.editNome.getText()).toString().trim();
        String especie = binding.autoCompleteEspecie.getText().toString().trim();
        String raca = binding.autoCompleteRaca.getText().toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = Objects.requireNonNull(binding.editData.getText()).toString().trim();

        if (nome.isEmpty() && especie.isEmpty() && raca.isEmpty() && sexo.isEmpty() && dataNascimento.isEmpty()) {
            mostrarSnackbar();
            return;
        }
        if (nome.isEmpty()) {
            binding.containerNome.setError("Campo obrigatório");
            binding.editNome.requestFocus();
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
            binding.containerData.setError("Campo obrigatório");
            return;
        }
        binding.autoCompleteEspecie.setText(especie);
        binding.autoCompleteRaca.setText(raca);
        binding.autoCompleteSexo.setText(sexo);
        binding.editData.setText(dataNascimento);
        salvarFirebase(nome, sexo, dataNascimento);
    }

    private void isEspecieSelecionada() {
        if (binding.autoCompleteEspecie.getText().toString().isEmpty()) {
            binding.autoCompleteRaca.setText("Escolha uma especie primeiro!", false);
            binding.autoCompleteRaca.setEnabled(false);
        }
    }

    private void salvarFirebase(String nome, String sexo, String dataNascimento) {
        Timestamp timestamp = converterParaTimestamp(dataNascimento);

        Map<String, Object> pet = new HashMap<>();
        pet.put("nome", nome);
        pet.put("especie", idEspecie);
        pet.put("raca", idRaca);
        pet.put("sexo", sexo);
        pet.put("dataNascimento", timestamp);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid()).collection("pets").add(pet)
                .addOnSuccessListener(documentReference -> {
                    if (documentReference != null){
                        String idPet = documentReference.getId();
                        if (imageUri != null){
                            UploadImagemFirebase(imageUri, idPet);
                        } else {
                            limparCampos();
                            startActivity(new Intent(CadastroPetActivity.this, MainActivity.class));
                        }

                    }
                });
    }

    // ------------------ Carregamento de menus Dropdown -----------------------
    private void carregarEspecie() {
        db.collection("especies").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idEspecie = dc.getId();
                            idEspecieSelecionados.add(idEspecie);
                        }
                        inicializarAutoComplete(autoCompleteTextViewEspecie, idEspecieSelecionados);
                    }
                });
    }

    private void carregarRaca(MaterialAutoCompleteTextView autoCompleteTextView, String idEspecie) {
        idRacaSelecionados.clear();
        db.collection("especies").document(idEspecie).collection("racas")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            idRaca = dc.getId();
                            idRacaSelecionados.add(idRaca);
                        }
                        inicializarAutoComplete(autoCompleteTextView, idRacaSelecionados);
                    }
                });
    }

    // ------------------- UPLOAD DA IMAGEM -----------------------
    private void selecionarImagem(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePicker.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.petAvatar.setImageURI(imageUri);
                }
            });

    private void UploadImagemFirebase(Uri imageUri,String idPet){
        if (imageUri != null){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("fotos_de_perfil_pet/" +  idPet + ".jpg");

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Carregando...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                        .collection("pets").document(idPet)
                        .update("fotoPerfil", downloadUrl).addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            startActivity(new Intent(CadastroPetActivity.this, MainActivity.class));
                        });
            }));
        }
    }

    private void limparCampos() {
        binding.editNome.setText("");
        binding.editData.setText("");
        autoCompleteTextViewEspecie.setText("", false);
        autoCompleteTextViewRaca.setText("", false);
        autoCompleteTextViewSexo.setText("", false);

        idEspecie = null;
        idRaca = null;
        imageUri = null;

        autoCompleteTextViewRaca.setEnabled(false);

        binding.petAvatar.setImageResource(R.drawable.paw_solid);
    }


    //------------ Utilitários ------------
    private void startDatePicker() {
        binding.containerData.setError(null);
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
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

    private void mostrarSnackbar() {
        Snackbar.make(binding.getRoot(), "Campos obrigatórios estão vazios. Por favor, preencha todos os campos para continuar.", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
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