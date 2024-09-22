package com.example.petcentral.Exames;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityAdicionarExameBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class AdicionarExameActivity extends AppCompatActivity {

    private ActivityAdicionarExameBinding binding;
    private Uri fileUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdicionarExameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        clickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    //Cliques
    private void clickListeners(){
        binding.btnVoltar.setOnClickListener(v -> finish());

        binding.btnCancelar.setOnClickListener(v-> finish());

        binding.btnAdd.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view1 = LayoutInflater.from(this).inflate(R.layout.bottom_exames, null);
            bottomSheetDialog.setContentView(view1);
            bottomSheetDialog.show();

            TextView tvPdf = view1.findViewById(R.id.tv_pdf);
            TextView tvImagem = view1.findViewById(R.id.tv_img);

            tvPdf.setOnClickListener(v1 -> selecionarArquivoPDF());

            tvImagem.setOnClickListener(v2 -> selecionarImagem());
        });

        binding.editNome.setOnClickListener(v -> binding.containerNome.setError(null));

        binding.editTextDataAplicacao.setOnClickListener(v -> startDatePicker());

        binding.btnRegistrar.setOnClickListener(v -> validarCampos());
    }

    // Validações
    private void validarCampos(){
        String nome = binding.editNome.getText().toString().trim();
        String data = binding.editTextDataAplicacao.getText().toString().trim();
        String anotacoes = binding.editTextAnotacoes.getText().toString().trim();

        if (nome.isEmpty()){
            binding.containerNome.setError("Campo obrigatório");
            return;
        }

        if (data.isEmpty()){
            binding.InputLayoutDataAplicacao.setError("Campo obrigatório");
            return;
        }

        anotacoes = anotacoes.isEmpty() ? null : anotacoes;

        salvarFirebase(nome,data,anotacoes);
    }

    //Salvar firebase
    private void salvarFirebase(String nome, String data, String anotacoes){
        String idPet = getIntent().getStringExtra("idPet");

        Timestamp timestamp = converterParaTimestamp(data);

        HashMap<String,Object> exame = new HashMap<>();

        exame.put("nome", nome);
        exame.put("data", timestamp);
        exame.put("anotacoes", anotacoes);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet).collection("exames").add(exame)
                .addOnSuccessListener(documentReference -> {
                    String idExame = documentReference.getId();
                    uploadDeArquivoFirebase(fileUri,idExame,nome);
                });
    }

    //Upload de arquivos
    private void uploadDeArquivoFirebase(Uri uri,String idExame,String nome){
        String idPet = getIntent().getStringExtra("idPet");
        String extensao = getFileExtension(fileUri);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (uri != null){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("exames/" + idPet + "/" + nome + extensao );

            storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri  -> {
                String downloadUrl = downloadUri.toString();
                db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                        .collection("pets").document(idPet).collection("exames").document(idExame)
                        .update("arquivo", downloadUrl).addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(this, ViewExamesActivity.class);
                            intent.putExtra("idPet", idPet);
                            progressDialog.dismiss();
                            startActivity(intent);
                        });
            }));
        }
    }

    //Seleção de arquivos
    private void selecionarImagem(){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePicker.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    fileUri = result.getData().getData();
                    if (fileUri != null) {
                        String nome = getFileName(fileUri);
                        binding.tvArquivosSelecionados.setText(nome);
                        binding.tvArquivosSelecionados.setOnClickListener(v -> {
                            Log.d("ImagePicker", "Clique no arquivo selecionado: " + fileUri);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(fileUri, "image/*");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        });
                    } else {
                        Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    private void selecionarArquivoPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        filePicker.launch(intent);
    }

    private final ActivityResultLauncher<Intent> filePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    fileUri = result.getData().getData();
                    if (fileUri != null) {
                        String nome = getFileName(fileUri);
                        binding.tvArquivosSelecionados.setText(nome);
                        binding.tvArquivosSelecionados.setOnClickListener(v -> {
                            if (fileUri != null) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(fileUri, "application/pdf");
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Nenhum arquivo PDF selecionado", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
    //----------------- Utilitários -----------------
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileExtension(Uri uri) {
        String fileType = getContentResolver().getType(uri);
        if (fileType != null) {
            if (fileType.equals("application/pdf")) {
                return ".pdf";
            } else if (fileType.startsWith("image/")) {
                return ".jpg";
            }
        }
        return "";
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