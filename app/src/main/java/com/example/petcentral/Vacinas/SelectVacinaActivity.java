package com.example.petcentral.Vacinas;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Adapters.vacinaAdapter;
import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivitySelectVacinaBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SelectVacinaActivity extends AppCompatActivity {

    private ActivitySelectVacinaBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private vacinaAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Vacinas> vacinasArrayList;

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
        adapter = new vacinaAdapter(this,vacinasArrayList);
        recyclerView.setAdapter(adapter);

        exibirVacinas();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
}