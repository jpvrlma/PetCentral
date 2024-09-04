package com.example.petcentral.Pets;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditPetBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class editPetActivity extends AppCompatActivity {

    private ActivityEditPetBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String idPet, idEspecie, idRaca;
    private MaterialAutoCompleteTextView autoCompleteEspecie, autoCompleteRaca, autoCompleteSexo;
    private final ArrayList<String> idEspecieSelecionados = new ArrayList<>();
    private final ArrayList<String> idRacaSelecionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        autoCompleteEspecie = binding.autoCompleteEspecie;
        autoCompleteRaca = binding.autoCompleteRaca;
        autoCompleteSexo = binding.autoCompleteSexo;

        inicializarAutoCompleteTextViewLocal(autoCompleteSexo, R.array.SexoArray);
        carregarEspecie();


        autoCompleteSexo.setOnItemClickListener((parent, view, position, id) -> binding.autoCompleteSexo.setError(null));

        autoCompleteEspecie.setOnClickListener(v -> {
            autoCompleteRaca.setText("", false);
            autoCompleteRaca.setEnabled(true);
        });

        autoCompleteEspecie.setOnItemClickListener((parent, view, position, id) -> {
            binding.autoCompleteEspecie.setError(null);
            autoCompleteRaca.setText("", false);
            autoCompleteRaca.setEnabled(true);
            idEspecie = idEspecieSelecionados.get(position);
            carregarRaca(autoCompleteRaca, idEspecie);
        });

        autoCompleteRaca.setOnItemClickListener((parent, view, position, id) -> {
            binding.autoCompleteRaca.setError(null);
            idRaca = idRacaSelecionados.get(position);
        });

        idPet = getIntent().getStringExtra("idPet");

        clickListeners();
        getPet();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void clickListeners() {
        binding.editData.setOnClickListener(v -> startDatePicker());
        binding.btnSalvar.setOnClickListener(v -> validarCampos());
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnCancelar.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        binding.btnExcluir.setOnClickListener(v -> deletarPetESubcolecoes());

    }

    private void carregarEspecie() {
        db.collection("especies").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idEspecie = dc.getId();
                            idEspecieSelecionados.add(idEspecie);
                        }
                        inicializarAutoComplete(autoCompleteEspecie, idEspecieSelecionados);
                    }
                });
    }

    private void carregarRaca(MaterialAutoCompleteTextView autoCompleteTextView, String idEspecie) {
        idRacaSelecionados.clear();
        db.collection("especies").document(idEspecie).collection("racas")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idRaca = dc.getId();
                            idRacaSelecionados.add(idRaca);
                        }
                        inicializarAutoComplete(autoCompleteTextView, idRacaSelecionados);
                    }
                });
    }

    private void getPet() {
        binding.main.setVisibility(View.GONE);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        if (pet != null) {
                            binding.editNome.setText(pet.getNome());
                            binding.autoCompleteEspecie.setText(pet.getEspecie(), false);
                            binding.autoCompleteRaca.setText(pet.getRaca(), false);
                            binding.autoCompleteSexo.setText(pet.getSexo(), false);

                            Date dataNascimento = pet.getDataNascimento().toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String dataFormatada = sdf.format(dataNascimento);
                            binding.editData.setText(dataFormatada);

                            idEspecie = pet.getEspecie();
                            idRaca = pet.getRaca();

                            if (!autoCompleteEspecie.getText().toString().isEmpty()) {
                                carregarRaca(autoCompleteRaca, idEspecie);
                            }
                            binding.main.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Erro ao carregar pet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void validarCampos() {
        String nome = binding.editNome.getText().toString().trim();
        String especie = binding.autoCompleteEspecie.getText().toString().trim();
        String raca = binding.autoCompleteRaca.getText().toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = binding.editData.getText().toString().trim();

        if (nome.isEmpty() && especie.isEmpty() && raca.isEmpty() && sexo.isEmpty() && dataNascimento.isEmpty()) {
            mostrarSnackbar("Campos obrigatórios estão vazios. Por favor, preencha todos os campos para continuar.");
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
        binding.autoCompleteEspecie.setText(especie,false);
        binding.autoCompleteRaca.setText(raca,false);
        binding.autoCompleteSexo.setText(sexo,false);
        binding.editData.setText(dataNascimento);
        updatePet(nome, idEspecie, idRaca, sexo, dataNascimento);
        startActivity(new Intent(this, MainActivity.class));
    }

    private void updatePet(String nome, String idEspecie, String idRaca, String sexo, String dataNascimento) {
        Timestamp timestamp = converterParaTimestamp(dataNascimento);
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .update("nome", nome, "especie", idEspecie, "raca", idRaca, "sexo", sexo, "dataNascimento", timestamp);
    }

    private void deletarPetESubcolecoes() {
        String idUser = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(idUser)
                .collection("pets").document(idPet)
                .collection("vacinas").get().addOnSuccessListener(vacinasSnapshot -> {
                    if (vacinasSnapshot != null && !vacinasSnapshot.isEmpty()) {
                        for (DocumentSnapshot dcVac : vacinasSnapshot.getDocuments()) {
                            String idVacina = dcVac.getId();
                            deletarDoses(idUser, idPet, idVacina);
                        }
                    } else {
                        deletarPets();
                    }
                });
    }

    private void deletarDoses(String userId, String idPet, String idVacina) {
        db.collection("usuarios").document(userId)
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").get().addOnSuccessListener(dosesSnapshot -> {
                    if (dosesSnapshot != null && !dosesSnapshot.isEmpty()) {
                        for (DocumentSnapshot dc : dosesSnapshot.getDocuments()) {
                            dc.getReference().delete();
                        }
                    }
                    deletarVacina(userId, idPet, idVacina);
                });
    }

    private void deletarVacina(String userId, String idPet, String idVacina) {
        db.collection("usuarios").document(userId)
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina).delete().addOnSuccessListener(aVoid -> {
                    verificarVacinasRestantes(userId, idPet);
                });
    }

    private void verificarVacinasRestantes(String userId, String idPet) {
        db.collection("usuarios").document(userId)
                .collection("pets").document(idPet)
                .collection("vacinas").get().addOnSuccessListener(vacinasSnapshot -> {
                    if (vacinasSnapshot == null || vacinasSnapshot.isEmpty()) {
                        deletarPets();
                    }
                });
    }

    private void deletarPets() {
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet).delete().addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(this, MainActivity.class));
                });
    }



    private void mostrarSnackbar(String mensagem) {
        Snackbar.make(binding.getRoot(), mensagem, Snackbar.LENGTH_SHORT)
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

    private void startDatePicker() {
        binding.containerData.setError(null);
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

}