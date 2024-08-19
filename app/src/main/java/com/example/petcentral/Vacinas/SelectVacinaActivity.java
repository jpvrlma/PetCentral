package com.example.petcentral.Vacinas;

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

import com.example.petcentral.Adapters.vacinaAdapter;
import com.example.petcentral.Interfaces.selectVacinaInterface;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.databinding.ActivitySelectVacinaBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class SelectVacinaActivity extends AppCompatActivity implements selectVacinaInterface {

    private ActivitySelectVacinaBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private vacinaAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Vacinas> vacinasArrayList;
    private String idEspecie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySelectVacinaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vacinasArrayList = new ArrayList<>();
        adapter = new vacinaAdapter(this,vacinasArrayList,this);
        recyclerView.setAdapter(adapter);
        carregarDadosPet();
        clickListeners();
        exibirVacinas();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());
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


    private void exibirVacinas(){
        String idEspecie = getIntent().getStringExtra("idEspecie");
        System.out.println(idEspecie);
        db.collection("especies").document(idEspecie)
                .collection("vacinas").addSnapshotListener((value, error) -> {
                    if (error != null){
                        Log.e("ERRO FIRESTORE", error.getMessage());
                        return;
                    }
                    vacinasArrayList.clear();
                    for (QueryDocumentSnapshot dc : value){
                        Vacinas vacinas = dc.toObject(Vacinas.class);
                        vacinas.setId(dc.getId());
                        vacinasArrayList.add(vacinas);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onSelectClick(int position) {
        Vacinas vacinas = vacinasArrayList.get(position);
        String idVacina = vacinas.getId();
        String idPet = getIntent().getStringExtra("petId");
        Intent intent = new Intent(this, CadastrarDoseActivity.class);
        intent.putExtra("idVacina",idVacina);
        intent.putExtra("idPet",idPet);
        startActivity(intent);
    }
}