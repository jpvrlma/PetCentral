package com.example.petcentral.Exames;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.Exames;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditExamesBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EditExamesActivity extends AppCompatActivity {

    private ActivityEditExamesBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String fileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditExamesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        clickListeners();
        carregarDadosExame();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Cliques
    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewExamesActivity.class);
            intent.putExtra("idPet", getIntent().getStringExtra("idPet"));
            startActivity(intent);
        });
    }

    //Carregar dados para edição

    // Carregar dados do exame e exibir o nome do arquivo
    private void carregarDadosExame(){
        String idPet = getIntent().getStringExtra("idPet");
        String idExame = getIntent().getStringExtra("idExame");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("exames").document(idExame)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        Exames exames = documentSnapshot.toObject(Exames.class);
                        if (exames != null){
                            binding.editNome.setText(exames.getNome());
                            binding.editTextAnotacoes.setText(exames.getAnotacoes());

                            if (exames.getData() != null){
                                Date data = exames.getData().toDate();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                String dataFormatada = dateFormat.format(data);
                                binding.editTextDataAplicacao.setText(dataFormatada);
                            }

                            fileUrl = exames.getArquivo();

                            if (fileUrl != null && !fileUrl.isEmpty()) {
                                String fileName = getFileNameFromUrl(fileUrl);
                                binding.tvArquivosSelecionados.setText(fileName);

                                binding.tvArquivosSelecionados.setOnClickListener(v -> {
                                    abrirArquivo(fileUrl);
                                });
                            }
                        }
                    }
                });
    }



    //Utilitarios
    private String getFileNameFromUrl(String fileUrl) {
        if (fileUrl != null) {
            int index = fileUrl.lastIndexOf('/');
            if (index != -1) {

                String fileNameWithExtension = fileUrl.substring(index + 1).split("\\?")[0];

                return fileNameWithExtension;
            }
        }
        return "Arquivo";
    }

    private void abrirArquivo(String fileUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(fileUrl), "*/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent chooser = Intent.createChooser(intent, "Abrir arquivo com");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, "Nenhum aplicativo disponível para abrir este arquivo", Toast.LENGTH_SHORT).show();
        }
    }

    private Timestamp converterParaTimestamp(String dataStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = dateFormat.parse(dataStr);
            if (parsedDate != null) {
                return new Timestamp(parsedDate);
            } else {
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startDatePicker() {
        binding.InputLayoutDataAplicacao.setError(null);
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String string = dateFormat.format(new Date(selection));
            binding.editTextDataAplicacao.setText(string);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }
}