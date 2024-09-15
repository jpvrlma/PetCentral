package com.example.petcentral.Exames;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.Pets.MainPetActivity;
import com.example.petcentral.Pets.editPetActivity;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditPetBinding;
import com.example.petcentral.databinding.ActivityViewExamesBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ViewExamesActivity extends AppCompatActivity {

    private ActivityViewExamesBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityViewExamesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        clickListeners();
        carregarDadosPet();

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
            startActivity(intent);
        });

        binding.btnEdit.setOnClickListener(v ->{
            Intent intent = new Intent(this, editPetActivity.class);
            startActivity(intent);
        });

        binding.btnCadastrar.setOnClickListener(v->{
            Intent intent = new Intent(this, AdicionarExameActivity.class);
            intent.putExtra("idPet", getIntent().getStringExtra("idPet"));
            startActivity(intent);
        });
    }

    //Carregamento de dados

    private void carregarDadosPet(){
        String idPet = getIntent().getStringExtra("idPet");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .get().addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()){
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        binding.textNome.setText(pet.getNome());
                        binding.textEspecie.setText(pet.getEspecie());
                        binding.textRaca.setText(pet.getRaca());
                        if (pet.getDataNascimento() != null){
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

                }).addOnFailureListener(e -> {
                    Log.e("Erro ao carregar ExamesActivity",e.getMessage());
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

}