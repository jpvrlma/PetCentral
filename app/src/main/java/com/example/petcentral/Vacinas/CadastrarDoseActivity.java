package com.example.petcentral.Vacinas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petcentral.R;
import com.example.petcentral.databinding.ActivityCadastrarDoseBinding;

public class CadastrarDoseActivity extends AppCompatActivity {

    private ActivityCadastrarDoseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCadastrarDoseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clickListeners();


        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void clickListeners() {
        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnCancelar.setOnClickListener(v -> finish());
    }

}