package com.example.petcentral.Exames;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.Objetos.Exames;
import com.example.petcentral.databinding.ActivityEditExamesBinding;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private void clickListeners() {
        binding.btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewExamesActivity.class);
            intent.putExtra("idPet", getIntent().getStringExtra("idPet"));
            startActivity(intent);
        });

        binding.btnRegistrar.setOnClickListener(v -> validarCampos());

        binding.editTextDataAplicacao.setOnClickListener(v -> startDatePicker());

        binding.btnExcluir.setOnClickListener(v -> mostrarAlertaExclusao());
    }

    // Validações
    private void validarCampos() {
        String nome = binding.editNome.getText().toString().trim();
        String data = binding.editTextDataAplicacao.getText().toString().trim();
        String anotacoes = binding.editTextAnotacoes.getText().toString().trim();

        if (nome.isEmpty()) {
            binding.containerNome.setError("Campo obrigatório");
            return;
        }

        if (data.isEmpty()) {
            binding.InputLayoutDataAplicacao.setError("Campo obrigatório");
            return;
        }

        anotacoes = anotacoes.isEmpty() ? null : anotacoes;

        UpdateFirebase(nome, data, anotacoes);
    }

    //Salvar ediçao firebase
    private void UpdateFirebase(String nome, String data, String anotacoes) {
        String idPet = getIntent().getStringExtra("idPet");
        String idExame = getIntent().getStringExtra("idExame");

        Timestamp timestamp = converterParaTimestamp(data);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("exames").document(idExame)
                .update("nome", nome, "data", timestamp, "anotacoes", anotacoes).addOnSuccessListener(unused -> {
                    Intent intent = new Intent(EditExamesActivity.this, ViewExamesActivity.class);
                    intent.putExtra("idPet", idPet);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    progressDialog.dismiss();
                    finish();
                });
    }

    // Exclusao
    private void mostrarAlertaExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmação de exclusão")
                .setMessage("Tem certeza que deseja excluir este exame?")
                .setPositiveButton("Sim", (dialog, which) -> excluirExame()).setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void excluirExame() {
        String idPet = getIntent().getStringExtra("idPet");
        String idExame = getIntent().getStringExtra("idExame");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("exames").document(idExame)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fileUrl = documentSnapshot.getString("arquivo");

                        if (fileUrl != null && !fileUrl.isEmpty()) {
                            excluirArquivoNoStorage(fileUrl);
                        }

                        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                                .collection("pets").document(idPet)
                                .collection("exames").document(idExame)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(EditExamesActivity.this, ViewExamesActivity.class);
                                    intent.putExtra("idPet", idPet);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    progressDialog.dismiss();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir exame: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void excluirArquivoNoStorage(String fileUrl) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
        fileRef.delete().addOnSuccessListener(aVoid -> Toast.makeText(this, "Arquivo excluído com sucesso", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Carregar dados do exame e exibir o nome do arquivo
    private void carregarDadosExame() {
        String idPet = getIntent().getStringExtra("idPet");
        String idExame = getIntent().getStringExtra("idExame");

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .collection("exames").document(idExame)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Exames exames = documentSnapshot.toObject(Exames.class);
                        if (exames != null) {
                            binding.editNome.setText(exames.getNome());
                            binding.editTextAnotacoes.setText(exames.getAnotacoes());

                            if (exames.getData() != null) {
                                Date data = exames.getData().toDate();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                String dataFormatada = dateFormat.format(data);
                                binding.editTextDataAplicacao.setText(dataFormatada);
                            }

                            fileUrl = exames.getArquivo();

                            if (fileUrl != null && !fileUrl.isEmpty()) {
                                String fileName = getFileNameFromUrl(fileUrl);
                                binding.tvArquivosSelecionados.setText(fileName);

                                binding.tvArquivosSelecionados.setOnClickListener(v -> abrirArquivo(fileUrl));
                            }
                        }
                    }
                });
    }

    //Utilitarios
    private String getFileNameFromUrl(String fileUrl) {
        if (fileUrl != null) {
            String decodedUrl = Uri.decode(fileUrl);

            String path = decodedUrl.substring(decodedUrl.lastIndexOf('/') + 1);

            int queryIndex = path.indexOf('?');
            if (queryIndex != -1) {
                path = path.substring(0, queryIndex);
            }

            return path;
        }
        return "Arquivo";
    }

    private String getMimeTypeFromExtension(String extension) {
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "*/*";
        }
    }

    private void abrirArquivo(String fileUrl) {
        String decodedUrl = Uri.decode(fileUrl);

        String extension = decodedUrl.substring(decodedUrl.lastIndexOf('.') + 1).split("\\?")[0];
        System.out.println(extension);

        String mimeType = getMimeTypeFromExtension(extension);
        System.out.println(mimeType);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(fileUrl), mimeType);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
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