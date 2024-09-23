package com.example.petcentral.Pets;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Adapters.petAdapter;
import com.example.petcentral.Adapters.proximasVacinasAdapter;
import com.example.petcentral.Interfaces.PetInterface;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.R;
import com.example.petcentral.Usuario.UserActivity;
import com.example.petcentral.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


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
    private proximasVacinasAdapter proximasVacinasAdapter;
    private ArrayList<Vacinas> vacinasArrayList;

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

        vacinasArrayList = new ArrayList<>();

        exibirRecycler();
        carregarPets();

    }

    //Cliques
    private void clickListeners() {
        binding.floatingActionButton.setOnClickListener(v -> startActivity(new Intent(this, CadastroPetActivity.class)));
        binding.btnSettings.setOnClickListener(v -> startActivity(new Intent(this, UserActivity.class)));
        binding.btnNotifi.setOnClickListener(v -> {
            exibirProximasVacinas();
        });
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

    private void exibirProximasVacinas(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.bottom_notifi, null);
        bottomSheetDialog.setContentView(view1);

        RecyclerView rv = view1.findViewById(R.id.recyclerViewProximas);
        rv.setLayoutManager(new LinearLayoutManager(this));

        proximasVacinasAdapter = new proximasVacinasAdapter(this, vacinasArrayList);
        rv.setAdapter(proximasVacinasAdapter);

        if (vacinasArrayList.isEmpty()) {
            TextView titulo = view1.findViewById(R.id.titulo);
            titulo.setText("Não há vacinas próximas");
        }

        bottomSheetDialog.setOnDismissListener(dialog -> {
            carregarPets();
        });

        bottomSheetDialog.show();
    }

    private void carregarPets(){
        vacinasArrayList.clear();
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()){
                        Log.e("ERRO FIRESTORE", task.getException().getMessage());
                        return;
                    }
                    for (QueryDocumentSnapshot petdc : task.getResult()){
                        String petId = petdc.getId();
                        carregarVacinasPorPet(petId);
                    }
                });
    }

    private void carregarVacinasPorPet(String petId){
        CollectionReference vacinasRef = db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(petId)
                .collection("vacinas");

        Date hoje = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(hoje);
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        Date tresDias = calendar.getTime();

        vacinasRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Log.e("ERRO FIRESTORE", task.getException().getMessage());
                return;
            }
            for (QueryDocumentSnapshot vacinasdc : task.getResult()){
                Vacinas vacinas = vacinasdc.toObject(Vacinas.class);
                vacinas.setId(vacinasdc.getId());

                Date proximaDose = vacinas.getProximaDose().toDate();
                if (proximaDose.after(hoje) && proximaDose.before(tresDias)){
                    vacinasArrayList.add(vacinas);
                }
            }
        });
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