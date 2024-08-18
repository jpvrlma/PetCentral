package com.example.petcentral.Vacinas;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.Pets.MainPetActivity;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityViewVacinasBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewVacinasActivity extends AppCompatActivity {

    private ActivityViewVacinasBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityViewVacinasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(this,SelectVacinaActivity.class);
            String id = getIntent().getStringExtra("petId");
            String idEspecie = getIntent().getStringExtra("idEspecie");
            intent.putExtra("petId",id);
            intent.putExtra("idEspecie",idEspecie);
            startActivity(intent);
        });
    }

}