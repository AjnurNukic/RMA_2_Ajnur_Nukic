package com.example.rma_2_ajnur_nukic;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_TARGET      = "targetWord";
    private static final String KEY_ROW         = "currentRow";
    private static final String KEY_COL         = "currentCol";
    private static final String KEY_GRID_LET    = "gridLetters";
    private static final String KEY_GRID_COLS   = "gridColors";
    private static final String KEY_KEY_LET     = "keyLetters";
    private static final String KEY_KEY_COL     = "keyColors";

    private final String[] fallbackWords = {"STOJA","TIGAR","BANJA","GUMAS","JABUK"};
    private FirebaseAuth     mAuth;
    private FirebaseFirestore db;
    private String           targetWord;
    private TextView[][]     cells       = new TextView[6][5];
    private int              currentRow  = 0;
    private int              currentCol  = 0;

    // map slovo->boja dugmeta
    private final Map<String,Integer> keyColors  = new HashMap<>();
    // map id->dugme, za restart/recolor
    private final Map<Integer,Button> keyButtons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // 1) Welcome
        TextView tv = findViewById(R.id.tv_welcome);
        FirebaseUser u = mAuth.getCurrentUser();
        if (u!=null && u.getEmail()!=null) {
            db.collection("users")
                    .whereEqualTo("email", u.getEmail())
                    .get()
                    .addOnSuccessListener(qs->{
                        if (!qs.isEmpty()) {
                            String name = qs.iterator().next().getString("name");
                            tv.setText(name!=null&& !name.isEmpty()
                                    ? "Dobro došli, "+name
                                    : "Dobro došli");
                        }
                    })
                    .addOnFailureListener(e->tv.setText("Dobro došli"));
        } else {
            tv.setText("Dobro došli");
        }

        // 2) Grid 6×5
        for(int r=0;r<6;r++){
            for(int c=0;c<5;c++){
                int id = getResources()
                        .getIdentifier("cell_"+r+"_"+c,
                                "id", getPackageName());
                cells[r][c] = findViewById(id);
                cells[r][c].setText("");
                cells[r][c].setBackgroundColor(Color.LTGRAY);
            }
        }

        // 3) Restore state if any
        if (savedInstanceState!=null) {
            targetWord = savedInstanceState.getString(KEY_TARGET);
            currentRow = savedInstanceState.getInt(KEY_ROW);
            currentCol = savedInstanceState.getInt(KEY_COL);
            String[] gl = savedInstanceState.getStringArray(KEY_GRID_LET);
            int[]    gc = savedInstanceState.getIntArray(KEY_GRID_COLS);
            if (gl!=null && gc!=null) {
                for(int i=0;i<gl.length;i++){
                    int rr=i/5, cc=i%5;
                    cells[rr][cc].setText(gl[i]);
                    cells[rr][cc].setBackgroundColor(gc[i]);
                }
            }
        }

        // 4) Pick word & load Firestore
        targetWord = targetWord!=null
                ? targetWord
                : fallbackWords[new Random().nextInt(fallbackWords.length)];
        loadWordFromFirestore();

        // 5) Bind keyboard
        bindAllKeys();

        // 6) Restore key colors if theme toggled
        if (savedInstanceState!=null) {
            ArrayList<String> kl = savedInstanceState
                    .getStringArrayList(KEY_KEY_LET);
            int[] kc = savedInstanceState.getIntArray(KEY_KEY_COL);
            if (kl!=null && kc!=null && kl.size()==kc.length) {
                for(int i=0;i<kl.size();i++){
                    updateKeyboardColor(kl.get(i), kc[i]);
                }
            }
        }

        // 7) Settings, DEL & ENTER
        findViewById(R.id.btn_settings)
                .setOnClickListener(this::showSettingsMenu);
        findViewById(R.id.btn_del)
                .setOnClickListener(v-> handleDel());
        findViewById(R.id.btn_enter)
                .setOnClickListener(v-> handleEnter());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out){
        super.onSaveInstanceState(out);
        // grid
        out.putString(KEY_TARGET, targetWord);
        out.putInt(KEY_ROW, currentRow);
        out.putInt(KEY_COL, currentCol);
        String[] gl = new String[30];
        int[] gc    = new int[30];
        for(int r=0;r<6;r++){
            for(int c=0;c<5;c++){
                int idx=r*5+c;
                gl[idx] = cells[r][c].getText().toString();
                gc[idx] = ((ColorDrawable)cells[r][c]
                        .getBackground()).getColor();
            }
        }
        out.putStringArray(KEY_GRID_LET, gl);
        out.putIntArray(KEY_GRID_COLS, gc);
        // keys
        ArrayList<String> kl = new ArrayList<>(keyColors.keySet());
        int[] kc = new int[kl.size()];
        for(int i=0;i<kl.size();i++){
            kc[i] = keyColors.get(kl.get(i));
        }
        out.putStringArrayList(KEY_KEY_LET, kl);
        out.putIntArray(KEY_KEY_COL, kc);
    }

    // --- UI helpers ---

    private void bindAllKeys(){
        int[] ids = {
                R.id.btn_a, R.id.btn_b, R.id.btn_c,
                R.id.btn_c_caron, R.id.btn_c_acute, R.id.btn_d,
                R.id.btn_dz, R.id.btn_dj, R.id.btn_e,
                R.id.btn_f, R.id.btn_g, R.id.btn_h,
                R.id.btn_i, R.id.btn_j, R.id.btn_k,
                R.id.btn_l, R.id.btn_lj, R.id.btn_m,
                R.id.btn_n, R.id.btn_nj, R.id.btn_o,
                R.id.btn_p, R.id.btn_r, R.id.btn_s,
                R.id.btn_s_caron, R.id.btn_t, R.id.btn_u,
                R.id.btn_v, R.id.btn_z, R.id.btn_z_caron
        };
        String[] letters = {
                "A","B","C","Č","Ć","D",
                "DŽ","Đ","E","F","G","H",
                "I","J","K","L","LJ","M",
                "N","NJ","O","P","R","S",
                "Š","T","U","V","Z","Ž"
        };
        for(int i=0;i<ids.length;i++){
            int id = ids[i];
            String l = letters[i];
            Button b = findViewById(id);
            keyButtons.put(id,b);
            b.setOnClickListener(v-> handleLetter(l));
        }
    }

    private void showSettingsMenu(View v){
        PopupMenu m = new PopupMenu(this,v);
        m.getMenuInflater()
                .inflate(R.menu.settings_menu, m.getMenu());
        m.setOnMenuItemClickListener(this::onSettingsItemSelected);
        m.show();
    }
    private boolean onSettingsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return true;

        } else if (id == R.id.action_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
            return true;

        } else if (id == R.id.action_toggle_dark_mode) {
            boolean night = AppCompatDelegate.getDefaultNightMode()
                    == AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(
                    night
                            ? AppCompatDelegate.MODE_NIGHT_NO
                            : AppCompatDelegate.MODE_NIGHT_YES
            );
            recreate();
            return true;

        } else if (id == R.id.action_restart) {
            restartGame();
            return true;

        } else if (id == R.id.action_hint) {
            Toast.makeText(this,
                    "Hint: kategorija je životinje",
                    Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }


    private void handleLetter(String l){
        if (currentRow<6 && currentCol<5) {
            cells[currentRow][currentCol].setText(l);
            currentCol++;
        }
    }
    private void handleDel(){
        if (currentCol>0) {
            currentCol--;
            cells[currentRow][currentCol].setText("");
        }
    }
    private void handleEnter(){
        if (currentCol==5) checkGuess();
        else Toast.makeText(this,
                "Unesite svih 5 slova!",
                Toast.LENGTH_SHORT).show();
    }

    private void checkGuess(){
        List<String> guess = new ArrayList<>();
        for(int i=0;i<5;i++){
            guess.add(cells[currentRow][i].getText().toString());
        }
        List<String> targetLetters =
                WordleUtils.splitToLetters(targetWord);
        List<Integer> res =
                WordleUtils.evaluateGuess(guess,targetLetters);

        for(int i=0;i<5;i++){
            int status = res.get(i);
            int color = status==2
                    ? Color.GREEN
                    : status==1
                    ? Color.YELLOW
                    : Color.GRAY;
            cells[currentRow][i].setBackgroundColor(color);
            updateKeyboardColor(guess.get(i), color);
        }

        boolean win = !res.contains(0) && !res.contains(1);
        if (win) {
            Toast.makeText(this,
                    "Pogodak! Riječ je: "+targetWord,
                    Toast.LENGTH_LONG).show();
            endGame();
        } else {
            currentRow++; currentCol=0;
            if (currentRow==6) {
                Toast.makeText(this,
                        "Izgubili ste! Riječ je: "+targetWord,
                        Toast.LENGTH_LONG).show();
                endGame();
            }
        }
    }

    private void updateKeyboardColor(String letter, int color){
        Integer old = keyColors.get(letter);
        if (old!=null){
            if (old==Color.GREEN) return;
            if (old==Color.YELLOW && color==Color.GRAY) return;
        }
        keyColors.put(letter,color);
        // bojimo dugme
        for(Button b:keyButtons.values()){
            if (b.getText().toString().equalsIgnoreCase(letter)){
                b.setBackgroundColor(color);
                break;
            }
        }
    }

    private void endGame(){
        for(Button b:keyButtons.values()) b.setEnabled(false);
    }

    private void restartGame(){
        currentRow=0; currentCol=0;
        // reset grid
        for(int r=0;r<6;r++){
            for(int c=0;c<5;c++){
                cells[r][c].setText("");
                cells[r][c].setBackgroundColor(Color.LTGRAY);
            }
        }
        loadWordFromFirestore();

        // clear previous key-colors map
        keyColors.clear();

        // default boja zavisno od teme
        boolean isNight =
                (getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;
        int bg = isNight
                ? ContextCompat.getColor(this, R.color.green_500)
                : ContextCompat.getColor(this, R.color.colorSecondaryDefault);
        int txt= ContextCompat.getColor(this, R.color.colorOnSecondaryDefault);

        for(Button b:keyButtons.values()){
            b.setEnabled(true);
            b.setBackgroundColor(bg);
            b.setTextColor(txt);
        }
        Toast.makeText(this,
                "Igra je restartovana!",
                Toast.LENGTH_SHORT).show();
    }

    private void loadWordFromFirestore(){
        db.collection("words")
                .get()
                .addOnSuccessListener(qs->{
                    List<String> w=new ArrayList<>();
                    for(QueryDocumentSnapshot d:qs){
                        String s=d.getString("word");
                        if (s!=null&&s.length()==5)
                            w.add(s.toUpperCase());
                    }
                    if(!w.isEmpty()){
                        targetWord = w.get(new Random().nextInt(w.size()));
                    } else {
                        Toast.makeText(this,
                                "Firestore prazna!",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e->
                        Toast.makeText(this,
                                "Greška: "+e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
