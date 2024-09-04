package com.example.petcentral.Vacinas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Adapters.timelineVacinaAdapter;
import com.example.petcentral.Interfaces.OnSelectInterface;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.Pets.MainPetActivity;
import com.example.petcentral.Pets.editPetActivity;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityViewVacinasBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class ViewVacinasActivity extends AppCompatActivity implements OnSelectInterface {

    private ActivityViewVacinasBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private timelineVacinaAdapter timelineVacinaAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Vacinas> vacinasArrayList;
    private String idEspecie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityViewVacinasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        carregarDadosPet();
        clickListeners();

        recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vacinasArrayList = new ArrayList<>();
        timelineVacinaAdapter = new timelineVacinaAdapter(this,vacinasArrayList,this);
        recyclerView.setAdapter(timelineVacinaAdapter);

        exibirRecycler();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void carregarDadosPet(){
        String idPet = getIntent().getStringExtra("idPet");
        db.collection("usuarios").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("pets").document(Objects.requireNonNull(idPet)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        binding.textNome.setText(Objects.requireNonNull(pet).getNome());
                        idEspecie = pet.getEspecie();
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


    private void exibirRecycler(){
        String idPet = getIntent().getStringExtra("idPet");
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").addSnapshotListener((value, error) -> {
                    if (error != null){
                        Log.d("Erro firestore",error.getMessage());
                        return;
                    }
                    vacinasArrayList.clear();
                    for (QueryDocumentSnapshot dc : value){
                        Vacinas vacinas = dc.toObject(Vacinas.class);
                        vacinas.setId(dc.getId());
                        vacinasArrayList.add(vacinas);
                    }
                    timelineVacinaAdapter.notifyDataSetChanged();
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

    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(this,SelectVacinaActivity.class);
            String idPet = getIntent().getStringExtra("idPet");
            intent.putExtra("idPet",idPet);
            startActivity(intent);
        });
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, editPetActivity.class);
            String idPet = getIntent().getStringExtra("idPet");
            intent.putExtra("idPet",idPet);
            startActivity(intent);
        });
    }

    @Override
    public void onSelectClick(int position) {
        Intent intent = new Intent(this, ViewDosesActivity.class);
        String idPet = getIntent().getStringExtra("idPet");
        Vacinas vacinas = vacinasArrayList.get(position);
        String idVacina = vacinas.getId();
        intent.putExtra("idEspecie",idEspecie);
        intent.putExtra("idPet",idPet);
        intent.putExtra("idVacina",idVacina);
        startActivity(intent);
    }
}