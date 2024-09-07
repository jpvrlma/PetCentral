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
        binding.btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            String idPet = getIntent().getStringExtra("idPet");
            intent.putExtra("idPet", idPet);
            startActivity(intent);
        });
        binding.btnEdit.setOnClickListener(v -> onEditClick());
        binding.cardVacina.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewVacinasActivity.class);
            String idPet = getIntent().getStringExtra("idPet");
            intent.putExtra("idPet", idPet);
            startActivity(intent);
        } );

    }
    private void onEditClick(){
        String idPet = getIntent().getStringExtra("idPet");
        Intent intent = new Intent(this, editPetActivity.class);
        intent.putExtra("idPet", idPet);
        startActivity(intent);
    }

    public void carregarDadosPet(){
        String idPet = getIntent().getStringExtra("idPet");
        db.collection("usuarios").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("pets").document(Objects.requireNonNull(idPet)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        binding.textNome.setText(Objects.requireNonNull(pet).getNome());
                        binding.textEspecie.setText(pet.getEspecie() + " - " + pet.getSexo());
                        binding.textRaca.setText(pet.getRaca());
                        if (pet.getDataNascimento() != null){
                            Date dataNascimento = pet.getDataNascimento().toDate();
                            String idade = calcularIdadeFormatada(dataNascimento);
                            binding.textIdade.setText(idade);
                        }
                    }
                });
    }

    public static String calcularIdadeFormatada(Date dataNascimento) {
        Calendar dataDeNascimentoCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        dataDeNascimentoCalendar.setTime(dataNascimento);
        Calendar dataAtualCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        int anos = dataAtualCalendar.get(Calendar.YEAR) - dataDeNascimentoCalendar.get(Calendar.YEAR);
        int meses = dataAtualCalendar.get(Calendar.MONTH) - dataDeNascimentoCalendar.get(Calendar.MONTH);

        if (meses < 0) {
            anos--;
            meses += 12;
        }
        StringBuilder idadeFormatada = new StringBuilder();
        if (anos > 0) {
            idadeFormatada.append(anos).append(anos == 1 ? " ano" : " anos");
        }
        if (meses > 0) {
            if (idadeFormatada.length() > 0) {
                idadeFormatada.append(" e ");
            }
            idadeFormatada.append(meses).append(meses == 1 ? " mês" : " meses");
        }
        return idadeFormatada.toString();
    }

}