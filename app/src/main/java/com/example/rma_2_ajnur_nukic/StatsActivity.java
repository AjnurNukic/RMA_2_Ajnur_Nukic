package com.example.rma_2_ajnur_nukic;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatsActivity extends AppCompatActivity {

    private TextView tvGamesPlayed, tvWins, tvStreak, tvMaxStreak;
    private TextView tvAttempt1, tvAttempt2, tvAttempt3, tvAttempt4, tvAttempt5, tvAttempt6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // osnovni statistički TextView-i
        tvGamesPlayed = findViewById(R.id.tvGamesPlayed);
        tvWins         = findViewById(R.id.tvWins);
        tvStreak       = findViewById(R.id.tvStreak);
        tvMaxStreak    = findViewById(R.id.tvMaxStreak);

        // TextView-i za broj pogodaka po pokušajima
        tvAttempt1 = findViewById(R.id.tvAttempt1);
        tvAttempt2 = findViewById(R.id.tvAttempt2);
        tvAttempt3 = findViewById(R.id.tvAttempt3);
        tvAttempt4 = findViewById(R.id.tvAttempt4);
        tvAttempt5 = findViewById(R.id.tvAttempt5);
        tvAttempt6 = findViewById(R.id.tvAttempt6);

        loadStats();
    }

    private void loadStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Korisnik nije prijavljen", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "Statistika nije pronađena", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = query.getDocuments().get(0);

                    // Osnovna statistika
                    long games    = doc.getLong("games")    != null ? doc.getLong("games")    : 0;
                    long wins     = doc.getLong("wins")     != null ? doc.getLong("wins")     : 0;
                    long streak   = doc.getLong("streak")   != null ? doc.getLong("streak")   : 0;
                    long maxStreak= doc.getLong("maxStreak")!= null ? doc.getLong("maxStreak"): 0;

                    tvGamesPlayed.setText("Odigrane igre: " + games);
                    tvWins.setText("Pobjede: " + wins);
                    tvStreak.setText("Trenutni niz: " + streak);
                    tvMaxStreak.setText("Najveći niz: " + maxStreak);

                    // Distribucija pogodaka po pokušajima
                    long attempt1 = doc.getLong("1") != null ? doc.getLong("1") : 0;
                    long attempt2 = doc.getLong("2") != null ? doc.getLong("2") : 0;
                    long attempt3 = doc.getLong("3") != null ? doc.getLong("3") : 0;
                    long attempt4 = doc.getLong("4") != null ? doc.getLong("4") : 0;
                    long attempt5 = doc.getLong("5") != null ? doc.getLong("5") : 0;
                    long attempt6 = doc.getLong("6") != null ? doc.getLong("6") : 0;

                    tvAttempt1.setText("Pogodaka u 1. pokušaju: " + attempt1);
                    tvAttempt2.setText("Pogodaka u 2. pokušaju: " + attempt2);
                    tvAttempt3.setText("Pogodaka u 3. pokušaju: " + attempt3);
                    tvAttempt4.setText("Pogodaka u 4. pokušaju: " + attempt4);
                    tvAttempt5.setText("Pogodaka u 5. pokušaju: " + attempt5);
                    tvAttempt6.setText("Pogodaka u 6. pokušaju: " + attempt6);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Greška pri čitanju statistike", Toast.LENGTH_SHORT).show();
                });
    }
}
