package com.example.petcentral.Pets;

import android.os.Bundle;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class editPetActivity extends AppCompatActivity {

    private ActivityEditPetBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String petId;
    private MaterialAutoCompleteTextView autoCompleteEspecie, autoCompleteRaca, autoCompleteSexo;
    private ArrayList<String> idEspecieSelecionados = new ArrayList<>();

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

        inicializarAutoCompleteTextViewLocal(autoCompleteSexo,R.array.SexoArray);
        carregarEspecie();
        isEspecieSelecionada();

        autoCompleteSexo.setOnItemClickListener((parent, view, position, id) -> binding.autoCompleteSexo.setError(null));

        autoCompleteEspecie.setOnClickListener(v->{
            autoCompleteRaca.setText("",false);
            autoCompleteRaca.setEnabled(true);
        });

        autoCompleteEspecie.setOnItemClickListener((parent, view, position, id) -> {
            binding.autoCompleteEspecie.setError(null);
            autoCompleteRaca.setText("",false);
            autoCompleteRaca.setEnabled(true);
            carregarRaca(autoCompleteRaca,idEspecieSelecionados.get(position));
        });

        autoCompleteRaca.setOnItemClickListener((parent, view, position, id) -> binding.autoCompleteRaca.setError(null));

        petId = getIntent().getStringExtra("petId");

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
                        inicializarAutoComplete(autoCompleteEspecie,lista);
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

    private void isEspecieSelecionada(){
        if (binding.autoCompleteEspecie.getText().toString().isEmpty()){
            binding.autoCompleteRaca.setText("Escolha uma especie primeiro!",false);
            binding.autoCompleteRaca.setEnabled(false);
        }
    }

    private void getPet() {
        binding.main.setVisibility(View.GONE);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(petId).get()
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

                            binding.main.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Erro ao carregar pet", Toast.LENGTH_SHORT).show();
                    }
                });
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

}