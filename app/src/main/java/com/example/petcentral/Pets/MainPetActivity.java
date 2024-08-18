package com.example.petcentral.Pets;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.Vacinas.ViewVacinasActivity;
import com.example.petcentral.databinding.ActivityMainPetBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class MainPetActivity extends AppCompatActivity {

    private ActivityMainPetBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idEspecie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        carregarDadosPet();
        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnEdit.setOnClickListener(v -> onEditClick());
        binding.cardVacina.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewVacinasActivity.class);
            String idPet = getIntent().getStringExtra("petId");
            intent.putExtra("petId", idPet);
            intent.putExtra("idEspecie", idEspecie);
            startActivity(intent);
        } );

    }
    private void onEditClick(){
        String idPet = getIntent().getStringExtra("petId");
        Intent intent = new Intent(this, editPetActivity.class);
        intent.putExtra("petId", idPet);
        startActivity(intent);
    }

    public void carregarDadosPet(){
        String idPet = getIntent().getStringExtra("petId");
        db.collection("usuarios").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("pets").document(Objects.requireNonNull(idPet)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        binding.textNome.setText(Objects.requireNonNull(pet).getNome());
                        binding.textEspecie.setText(pet.getEspecie() + " - " + pet.getSexo());
                        binding.textRaca.setText(pet.getRaca());
                        idEspecie = pet.getEspecie();
                        if (pet.getDataNascimento() != null){
                            Date dataNascimento = pet.getDataNascimento().toDate();
                            int idade = calcularIdade(dataNascimento);
                            binding.textIdade.setText("Idade : " + String.valueOf(idade));
                        }
                    }
                });
    }

    public static int calcularIdade(Date dataNascimento) {
        Calendar dataDeNascimentoCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        dataDeNascimentoCalendar.setTime(dataNascimento);

        Calendar dataAtualCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        int idade = dataAtualCalendar.get(Calendar.YEAR) - dataDeNascimentoCalendar.get(Calendar.YEAR);
        dataDeNascimentoCalendar.add(Calendar.YEAR, idade);
        if (dataAtualCalendar.before(dataDeNascimentoCalendar)) {
            idade--;
        }
        return idade;
    }
}