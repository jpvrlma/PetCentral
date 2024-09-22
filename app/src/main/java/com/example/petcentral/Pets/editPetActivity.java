package com.example.petcentral.Pets;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.petcentral.Exames.EditExamesActivity;
import com.example.petcentral.Exames.ViewExamesActivity;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityEditPetBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class editPetActivity extends AppCompatActivity {

    private ActivityEditPetBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String idPet, idEspecie, idRaca;
    private MaterialAutoCompleteTextView autoCompleteEspecie, autoCompleteRaca, autoCompleteSexo;
    private final ArrayList<String> idEspecieSelecionados = new ArrayList<>();
    private final ArrayList<String> idRacaSelecionados = new ArrayList<>();
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        autoCompleteEspecie = binding.autoCompleteEspecie;
        autoCompleteRaca = binding.autoCompleteRaca;
        autoCompleteSexo = binding.autoCompleteSexo;

        inicializarAutoCompleteTextViewLocal(autoCompleteSexo, R.array.SexoArray);
        carregarEspecie();

        autoCompleteSexo.setOnItemClickListener((parent, view, position, id) -> binding.autoCompleteSexo.setError(null));

        autoCompleteEspecie.setOnClickListener(v -> {
            autoCompleteRaca.setText("", false);
            autoCompleteRaca.setEnabled(true);
        });

        autoCompleteEspecie.setOnItemClickListener((parent, view, position, id) -> {
            binding.autoCompleteEspecie.setError(null);
            autoCompleteRaca.setText("", false);
            autoCompleteRaca.setEnabled(true);
            idEspecie = idEspecieSelecionados.get(position);
            carregarRaca(autoCompleteRaca, idEspecie);
        });

        autoCompleteRaca.setOnItemClickListener((parent, view, position, id) -> {
            binding.autoCompleteRaca.setError(null);
            idRaca = idRacaSelecionados.get(position);
        });

        idPet = getIntent().getStringExtra("idPet");

        clickListeners();
        getPet();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Cliques
    private void clickListeners() {
        binding.editData.setOnClickListener(v -> startDatePicker());

        binding.btnSalvar.setOnClickListener(v -> validarCampos());

        binding.btnVoltar.setOnClickListener(v -> finish());

        binding.btnCancelar.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        binding.btnExcluir.setOnClickListener(v -> mostrarAlerta());

        binding.btnUpload.setOnClickListener(v -> selecionarImagem());

    }

    // -------------------- Carregamento de menus -----------------------
    private void carregarEspecie() {
        db.collection("especies").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idEspecie = dc.getId();
                            idEspecieSelecionados.add(idEspecie);
                        }
                        inicializarAutoComplete(autoCompleteEspecie, idEspecieSelecionados);
                    }
                });
    }

    private void carregarRaca(MaterialAutoCompleteTextView autoCompleteTextView, String idEspecie) {
        idRacaSelecionados.clear();
        db.collection("especies").document(idEspecie).collection("racas")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                            String idRaca = dc.getId();
                            idRacaSelecionados.add(idRaca);
                        }
                        inicializarAutoComplete(autoCompleteTextView, idRacaSelecionados);
                    }
                });
    }

    //----------------- Carregar dados do pet -------------------
    private void getPet() {
        binding.main.setVisibility(View.GONE);

        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        if (pet != null) {
                            binding.editNome.setText(pet.getNome());
                            binding.autoCompleteEspecie.setText(pet.getEspecie(), false);
                            binding.autoCompleteRaca.setText(pet.getRaca(), false);
                            binding.autoCompleteSexo.setText(pet.getSexo(), false);

                            Date dataNascimento = pet.getDataNascimento().toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String dataFormatada = sdf.format(dataNascimento);
                            binding.editData.setText(dataFormatada);

                            idEspecie = pet.getEspecie();
                            idRaca = pet.getRaca();

                            if (!autoCompleteEspecie.getText().toString().isEmpty()) {
                                carregarRaca(autoCompleteRaca, idEspecie);
                            }

                            binding.main.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Erro ao carregar pet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //--------------------- Metodos para update de pet ---------------------
    private void validarCampos() {
        String nome = binding.editNome.getText().toString().trim();
        String especie = binding.autoCompleteEspecie.getText().toString().trim();
        String raca = binding.autoCompleteRaca.getText().toString().trim();
        String sexo = binding.autoCompleteSexo.getText().toString().trim();
        String dataNascimento = binding.editData.getText().toString().trim();

        if (nome.isEmpty() && especie.isEmpty() && raca.isEmpty() && sexo.isEmpty() && dataNascimento.isEmpty()) {
            mostrarSnackbar();
            return;
        }
        if (nome.isEmpty()) {
            binding.containerNome.setError("Campo obrigatório");
            binding.editNome.requestFocus();
            return;
        }
        if (especie.isEmpty()) {
            binding.menuEspecie.setError("Campo obrigatório");
            return;
        }
        if (raca.isEmpty()) {
            binding.menuRaca.setError("Campo obrigatório");
            return;
        }
        if (sexo.isEmpty()) {
            binding.containerSexo.setError("Campo obrigatório");
            return;
        }
        if (dataNascimento.isEmpty()) {
            binding.containerData.setError("Campo obrigatório");
            return;
        }
        binding.autoCompleteEspecie.setText(especie, false);
        binding.autoCompleteRaca.setText(raca, false);
        binding.autoCompleteSexo.setText(sexo, false);
        binding.editData.setText(dataNascimento);

        updatePet(nome, idEspecie, idRaca, sexo, dataNascimento);

    }

    private void updatePet(String nome, String idEspecie, String idRaca, String sexo, String dataNascimento) {
        Timestamp timestamp = converterParaTimestamp(dataNascimento);
        db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .collection("pets").document(idPet)
                .update("nome", nome, "especie", idEspecie, "raca", idRaca, "sexo", sexo, "dataNascimento", timestamp)
                .addOnSuccessListener(unused -> {
                    UploadImagemFirebase(imageUri, idPet);
                });
    }

    // ------------- Metodos de exclusao de pets,vacinas e doses -----------------

    private void mostrarAlerta() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmação de exclusão")
                .setMessage("Tem certeza que deseja excluir este pet?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    verificarVacinasDoPetAntesDeDeletar();
                }).setNegativeButton("Não", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void verificarVacinasDoPetAntesDeDeletar() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("vacinas").get().addOnSuccessListener(vacinasSnapshot -> {
                    if (vacinasSnapshot != null && !vacinasSnapshot.isEmpty()) {
                        // Se o pet tem vacinas, verificar se há doses
                        for (DocumentSnapshot vacinaDoc : vacinasSnapshot.getDocuments()) {
                            String idVacina = vacinaDoc.getId();
                            deletarDosesDaVacina(idUsuario, idPet, idVacina);
                        }
                    } else {
                        deletarPet();
                    }
                });
    }

    private void deletarDosesDaVacina(String idUsuario, String idPet, String idVacina) {
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina)
                .collection("doses").get().addOnSuccessListener(dosesSnapshot -> {
                    if (dosesSnapshot != null && !dosesSnapshot.isEmpty()) {
                        for (DocumentSnapshot doseDoc : dosesSnapshot.getDocuments()) {
                            doseDoc.getReference().delete();
                        }
                    }
                    deletarVacina(idUsuario, idPet, idVacina);
                });
    }

    private void deletarVacina(String idUsuario, String idPet, String idVacina) {
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("vacinas").document(idVacina).delete().addOnSuccessListener(aVoid -> verificarSeAindaHaVacinas(idUsuario, idPet));
    }

    private void verificarSeAindaHaVacinas(String idUsuario, String idPet) {
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("vacinas").get().addOnSuccessListener(vacinasSnapshot -> {
                    if (vacinasSnapshot == null || vacinasSnapshot.isEmpty()) {
                        deletarExames();
                    }
                });
    }

    private void deletarExames() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("exames").get().addOnSuccessListener(examesSnapshot -> {
                    if (examesSnapshot != null && !examesSnapshot.isEmpty()) {
                        for (DocumentSnapshot exameDoc : examesSnapshot.getDocuments()) {
                            String url = exameDoc.getString("arquivo");
                            excluirArquivoNoStorage(url);
                            exameDoc.getReference().delete();
                        }
                        verificarSeAindaHaExames();
                    } else {
                        deletarAlergias();
                    }
                });
    }

    private void verificarSeAindaHaExames() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("exames").get()
                .addOnSuccessListener(examesSnapshot -> {
                    if (examesSnapshot == null || examesSnapshot.isEmpty()) {
                        deletarAlergias();
                    }
                });
    }

    private void verificarSeAindaHaAlergias() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("alergias").get()
                .addOnSuccessListener(alergiasSnapshot -> {
                    if (alergiasSnapshot == null || alergiasSnapshot.isEmpty()) {
                        deletarPet();
                    }
                });
    }

    private void deletarAlergias(){
        String idUsuario = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .collection("alergias").get().addOnSuccessListener(alergiasSnapshot -> {
                    if (alergiasSnapshot != null && !alergiasSnapshot.isEmpty()) {
                        for (DocumentSnapshot alergiaDoc : alergiasSnapshot.getDocuments()) {
                            alergiaDoc.getReference().delete();
                        }
                        verificarSeAindaHaAlergias();
                    } else {
                        deletarPet();
                    }
                });
    }


    private void excluirArquivoNoStorage(String fileUrl) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
        fileRef.delete().addOnSuccessListener(aVoid -> Toast.makeText(this, "Arquivo excluído com sucesso", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deletarPet() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(idUsuario)
                .collection("pets").document(idPet)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fileUrl = documentSnapshot.getString("fotoPerfil");

                        if (fileUrl != null && !fileUrl.isEmpty()) {
                            excluirArquivoNoStorage(fileUrl);
                        }
                        documentSnapshot.getReference().delete();
                        Intent intent = new Intent(editPetActivity.this, ViewExamesActivity.class);
                        intent.putExtra("idPet", idPet);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();

                    }

                });
    }


    // ------------------- UPLOAD DA IMAGEM -----------------------
    private void selecionarImagem() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePicker.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.btnUpload.setImageURI(imageUri);
                }
            });

    private void UploadImagemFirebase(Uri imageUri, String idPet) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("fotos_de_perfil_pet/" + idPet + ".jpg");

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Carregando...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                db.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                        .collection("pets").document(idPet)
                        .update("fotoPerfil", downloadUrl).addOnSuccessListener(aVoid -> startActivity(new Intent(editPetActivity.this, MainActivity.class)))
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            limparCampos();
                            startActivity(new Intent(editPetActivity.this, MainActivity.class));
                        });
            }));
        } else {
            startActivity(new Intent(editPetActivity.this, MainActivity.class));
        }
    }

    private void limparCampos() {
        binding.editNome.setText("");
        binding.editData.setText("");
        autoCompleteEspecie.setText("", false);
        autoCompleteRaca.setText("", false);
        autoCompleteSexo.setText("", false);

        idEspecie = null;
        idRaca = null;
        imageUri = null;

        autoCompleteRaca.setEnabled(false);

        binding.btnUpload.setImageResource(R.drawable.paw_solid);
    }

    //-------------- Utilitários --------------
    private void mostrarSnackbar() {
        Snackbar.make(binding.getRoot(), "Campos obrigatórios estão vazios. Por favor, preencha todos os campos para continuar.", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.md_theme_primary))
                .setActionTextColor(getColor(R.color.md_theme_onPrimary))
                .show();
    }

    private Timestamp converterParaTimestamp(String dataStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = dateFormat.parse(dataStr);
            return new Timestamp(parsedDate);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startDatePicker() {
        binding.containerData.setError(null);
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione uma data")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String string = dateFormat.format(new Date(selection));
            binding.editData.setText(string);
        });
        materialDatePicker.show(getSupportFragmentManager(), "TAG");
    }

    private void inicializarAutoCompleteTextViewLocal(MaterialAutoCompleteTextView autoCompleteTextView, int arrayResourceId) {
        String[] opcoes = getResources().getStringArray(arrayResourceId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcoes);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void inicializarAutoComplete(MaterialAutoCompleteTextView autoCompleteTextView, List<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista);
        autoCompleteTextView.setAdapter(adapter);
    }
}