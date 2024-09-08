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

import com.example.petcentral.Adapters.dosesVacinaAdapter;
import com.example.petcentral.Interfaces.OnSelectInterface;
import com.example.petcentral.Objetos.DoseVacina;
import com.example.petcentral.databinding.ActivityViewDosesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * ESTA ATIVIDADE VAI EXIBIR AS DOSES DA VACINA SELECIONADA PELO USUARIO
 */
public class ViewDosesActivity extends AppCompatActivity implements OnSelectInterface {

    private ActivityViewDosesBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<DoseVacina> doseVacinaArrayList = new ArrayList<>();
    private dosesVacinaAdapter dosesVacinaAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityViewDosesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dosesVacinaAdapter = new dosesVacinaAdapter(this, doseVacinaArrayList, this);
        recyclerView.setAdapter(dosesVacinaAdapter);


        clickListeners();
        exibirRecycler();
        exibirDadosVacina();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Cliques
    private void clickListeners() {
        binding.btnVoltar.setOnClickListener(v -> finish());
    }

    // ------------------------ CARREGAMENTOS ------------------------
    private void exibirDadosVacina() {
        String idVacina = getIntent().getStringExtra("idVacina");
        String idEspecie = getIntent().getStringExtra("idEspecie");
        db.collection("especies").document(idEspecie)
                .collection("vacinas").document(idVacina).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        String nome = documentSnapshot.getString("nome");
                        String resumo = documentSnapshot.getString("resumo");
                        binding.nomeVac.setText(nome);
                        binding.tvResumo.setText(resumo);
                    }
                });
    }

    // RECYCLER VIEW DE DOSES DA VACINA SELECIONADA CADASTRADAS NO FIREBASE
    private void exibirRecycler() {
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").orderBy("numeroDose", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ERRO FIRESTORE", "Erro ao obter dados: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        doseVacinaArrayList.clear();
                        for (QueryDocumentSnapshot dc : value) {
                            DoseVacina doseVacina = dc.toObject(DoseVacina.class);
                            doseVacina.setId(dc.getId());
                            doseVacinaArrayList.add(doseVacina);
                        }
                        dosesVacinaAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ERRO FIRESTORE", "Nenhum dado encontrado.");
                    }
                });
    }

    // Métodos da interface
    @Override
    public void onSelectClick(int position) {

        DoseVacina doseVacina = doseVacinaArrayList.get(position);
        Intent intent = new Intent(this, EditarDoseActivity.class);
        String idPet = getIntent().getStringExtra("idPet");
        String idVacina = getIntent().getStringExtra("idVacina");
        String idEspecie = getIntent().getStringExtra("idEspecie");
        String idDose = doseVacina.getId();

        intent.putExtra("idPet", idPet);
        intent.putExtra("idVacina", idVacina);
        intent.putExtra("idEspecie", idEspecie);
        intent.putExtra("idDose", idDose);
        startActivity(intent);

    }
}