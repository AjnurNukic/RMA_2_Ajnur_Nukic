package com.example.rma_2_ajnur_nukic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // inicijalizacija Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        editTextName     = findViewById(R.id.editTextName);
        editTextEmail    = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button btnReg    = findViewById(R.id.buttonRegister);

        btnReg.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name     = editTextName.getText().toString().trim();
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Popunite sva polja!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prvo provjerimo da li dokument s tim korisničkim imenom već postoji
        DocumentReference userDoc = db.collection("users").document(name);
        userDoc.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Već postoji korisnik s tim imenom
                Toast.makeText(this, "Korisnik sa ovim imenom već postoji!", Toast.LENGTH_SHORT).show();
            } else {
                // Nema takvog dokumenta, možemo kreirati Auth i bazu
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sastavimo mapu s podacima
                                Map<String,Object> userData = new HashMap<>();
                                // Sastavimo mapu s podacima + početna statistika
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("games", 0);
                                userData.put("wins", 0);
                                userData.put("streak", 0);
                                userData.put("maxStreak", 0);

                                // po potrebi možeš dodati i lozinku ili neki drugi podatak:
                                // userData.put("password", password);

                                // Spremamo pod mapom ID = name
                                db.collection("users")
                                        .document(name)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show();
                                            // Nakon uspješne registracije, preusmjeri na Login screen
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this,
                                                    "Greška pri čuvanju podataka: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });

                            } else {
                                Toast.makeText(this,
                                        "Greška pri registraciji: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this,
                    "Greška pri provjeri korisnika: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }
}
