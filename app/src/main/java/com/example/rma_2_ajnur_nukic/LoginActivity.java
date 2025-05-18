package com.example.rma_2_ajnur_nukic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicijalizujemo Firebase Auth i Firestore
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        editTextEmail    = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        Button btnLogin  = findViewById(R.id.buttonLogin);
        TextView linkReg = findViewById(R.id.textViewRegisterLink);

        // Prijava
        btnLogin.setOnClickListener(v -> loginUser());

        // Link ka registraciji
        linkReg.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Popunite oba polja!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Auth prijava
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Ako je autentikacija uspešna, proverimo da li u Firestore postoji user sa tim e‑mailom
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            Toast.makeText(this, "Neočekivana greška.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(queryTask -> {
                                    if (queryTask.isSuccessful()) {
                                        QuerySnapshot qs = queryTask.getResult();
                                        if (qs != null && !qs.isEmpty()) {
                                            // Postoji zapis u Firestore → možemo dalje
                                            Toast.makeText(this, "Prijava uspješna!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Nema korisnika u kolekciji users
                                            Toast.makeText(this,
                                                    "Korisnički podaci nisu pronađeni. Registrujte se!",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        // Greška pri čitanju iz baze
                                        Toast.makeText(this,
                                                "Greška pri pristupu bazi: " + queryTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        // Neuspešna autentikacija
                        Toast.makeText(this,
                                "Prijava neuspješna: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
