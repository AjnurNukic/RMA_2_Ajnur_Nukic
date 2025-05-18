package com.example.rma_2_ajnur_nukic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;   // ovo je tvoja “glavna” Activity
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChange;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ovdje eksplicitno kažemo „AppCompatActivity” da ga razriješi:
        super.setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChange         = findViewById(R.id.btn_change_password);

        btnChange.setOnClickListener(v -> attemptChange());
    }

    private void attemptChange() {
        String current = etCurrentPassword.getText().toString().trim();
        String neu     = etNewPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(current) || TextUtils.isEmpty(neu) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "Popunite sva polja!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!neu.equals(confirm)) {
            Toast.makeText(this, "Nova lozinka i potvrda se ne poklapaju!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (neu.length() < 6) {
            Toast.makeText(this, "Šifra mora imati bar 6 karaktera!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-autentikacija
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), current);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Sada možemo da update-ujemo lozinku
                            user.updatePassword(neu)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> updateTask) {
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(ChangePasswordActivity.this,
                                                        "Lozinka uspešno promjenjena!",
                                                        Toast.LENGTH_LONG).show();
                                                finish();
                                            } else {
                                                Toast.makeText(ChangePasswordActivity.this,
                                                        "Greška pri promjeni: " +
                                                                updateTask.getException().getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Pogrešna trenutna lozinka!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
