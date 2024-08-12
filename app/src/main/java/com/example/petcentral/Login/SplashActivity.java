package com.example.petcentral.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcentral.Pets.MainActivity;
import com.example.petcentral.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Primeira atividade do Aplicativo
 * Ela irá checar se o usuário esta logado ou não
 * e redirecionar para a tela de login ou para a tela principal
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Handler handler = new Handler();

        if (mAuth.getCurrentUser() != null) {
            handler.postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class)), 2000);
        } else {
            handler.postDelayed(() -> startActivity(new Intent(SplashActivity.this, LoginActivity.class)), 2000);
        }
    }
}