package com.example.petcentral.Pets;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Adapters.petAdapter;
import com.example.petcentral.Interfaces.PetInterface;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.R;
import com.example.petcentral.Usuario.UserActivity;
import com.example.petcentral.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;


/**
 * Esta atividade irá exibir a lista de pets do usuário logado
 * Permitirá também ele acessar as atividades para cadastrar um
 * pet ou acessar as configurações do usuário
 */

public class MainActivity extends AppCompatActivity implements PetInterface {

    private ActivityMainBinding binding;
    private ArrayList<Pet> petArrayList;
    private com.example.petcentral.Adapters.petAdapter petAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clickListeners();
        saudacao();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        petArrayList = new ArrayList<>();
        petAdapter = new petAdapter(this, petArrayList, this);
        recyclerView.setAdapter(petAdapter);

        exibirRecycler();

    }

    //Cliques
    private void clickListeners() {
        binding.floatingActionButton.setOnClickListener(v -> startActivity(new Intent(this, CadastroPetActivity.class)));
        binding.btnSettings.setOnClickListener(v -> startActivity(new Intent(this, UserActivity.class)));
    }

    //Exibir o Recycler view de pets cadastrados
    private void exibirRecycler() {
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets")
                .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ERRO FIRESTORE", error.getMessage());
                        return;
                    }
                    petArrayList.clear();
                    for (QueryDocumentSnapshot dc : value) {
                        Pet pet = dc.toObject(Pet.class);
                        pet.setId(dc.getId());
                        petArrayList.add(pet);
                    }
                    petAdapter.notifyDataSetChanged();
                });
    }

    //Configurar o text view de saudacao para o usuário
    private void saudacao() {
        LocalTime horaAtual = LocalTime.now();

        if (horaAtual.isAfter(LocalTime.of(6, 0)) && horaAtual.isBefore(LocalTime.of(12, 1))) {
            binding.textDia.setText("Bom dia!");
        } else if (horaAtual.isAfter(LocalTime.of(12, 0)) && horaAtual.isBefore(LocalTime.of(18, 1))) {
            binding.textDia.setText("Boa tarde!");
        } else {
            binding.textDia.setText("Boa noite!");
        }
    }

    //Métodos da interface
    @Override
    public void onEditClick(int position) {
        Pet pet = petArrayList.get(position);
        Intent intent = new Intent(this, editPetActivity.class);
        String idPet = pet.getId();
        intent.putExtra("idPet", idPet);
        startActivity(intent);

    }

    @Override
    public void onClick(int position) {
        Pet pet = petArrayList.get(position);
        Intent intent = new Intent(this, MainPetActivity.class);
        String idPet = pet.getId();
        intent.putExtra("idPet", idPet);
        startActivity(intent);

    }

    //Evitar voltar para a tela de login
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}