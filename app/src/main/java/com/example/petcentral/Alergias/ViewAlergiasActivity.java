package com.example.petcentral.Alergias;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcentral.Adapters.viewAlergiasAdapter;
import com.example.petcentral.Adapters.viewExamesAdapter;
import com.example.petcentral.Exames.AdicionarExameActivity;
import com.example.petcentral.Interfaces.DeleteInterface;
import com.example.petcentral.Objetos.Alergias;
import com.example.petcentral.Objetos.Exames;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.Pets.MainPetActivity;
import com.example.petcentral.Pets.editPetActivity;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityViewAlergiasBinding;
import com.example.petcentral.databinding.RecyclerAlergiasBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class ViewAlergiasActivity extends AppCompatActivity implements DeleteInterface {

    private ActivityViewAlergiasBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private viewAlergiasAdapter alergiasAdapter;
    private ArrayList<Alergias> alergiasArrayList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityViewAlergiasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        clickListeners();
        carregarDadosPet();

        recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        alergiasArrayList = new ArrayList<>();
        alergiasAdapter = new viewAlergiasAdapter(alergiasArrayList, this, this);
        recyclerView.setAdapter(alergiasAdapter);

        exibirRecycler();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    //Cliques
    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainPetActivity.class);
            String idPet = getIntent().getStringExtra("idPet");
            intent.putExtra("idPet", idPet);
            startActivity(intent);
        });

        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, editPetActivity.class);
            intent.putExtra("idPet", getIntent().getStringExtra("idPet"));
            startActivity(intent);
        });

        binding.btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdicionarAlergiaActivity.class);
            intent.putExtra("idPet", getIntent().getStringExtra("idPet"));
            startActivity(intent);
        });

    }

    //Carregar dados do pet
    public void carregarDadosPet() {
        String idPet = getIntent().getStringExtra("idPet");
        db.collection("usuarios").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("pets").document(Objects.requireNonNull(idPet)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        binding.textNome.setText(Objects.requireNonNull(pet).getNome());
                        binding.textEspecie.setText(pet.getEspecie() + " - " + pet.getSexo());
                        binding.textRaca.setText(pet.getRaca());
                        if (pet.getDataNascimento() != null) {
                            Date dataNascimento = pet.getDataNascimento().toDate();
                            String idade = calcularIdadeFormatada(dataNascimento);
                            binding.textIdade.setText(idade);
                        }
                        String urlFotoPerfil = pet.getFotoPerfil();

                        if (urlFotoPerfil != null){
                            Glide.with(this)
                                    .load(urlFotoPerfil)
                                    .into(binding.petImg);
                        }
                    }
                });
    }

    //Recycler view
    private void exibirRecycler() {
        String idPet = getIntent().getStringExtra("idPet");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("alergias").addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ERRO FIRESTORE", error.getMessage());
                        return;
                    }
                    alergiasArrayList.clear();
                    for (DocumentSnapshot dc : value) {
                        Alergias alergias = dc.toObject(Alergias.class);
                        alergias.setId(dc.getId());
                        alergiasArrayList.add(alergias);
                    }
                    alergiasAdapter.notifyDataSetChanged();
                });
    }



    // ------------------- Utilitários -------------------------
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

    @Override
    public void onDeleteClick(int position) {
        Alergias alergias = alergiasArrayList.get(position);
        String idAlergia = alergias.getId();
        String idPet = getIntent().getStringExtra("idPet");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("alergias").document(idAlergia)
                .delete()
                .addOnSuccessListener(unused -> {
                    alergiasAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Alergia excluída com sucesso!", Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public void onSelectClick(int position) {
        Alergias alergias = alergiasArrayList.get(position);
        String idAlergia = alergias.getId();
        String idPet = getIntent().getStringExtra("idPet");
        Intent intent = new Intent(this, EditAlergiasActivity.class);
        intent.putExtra("idPet",idPet);
        intent.putExtra("idAlergia",idAlergia);
        startActivity(intent);
    }
}