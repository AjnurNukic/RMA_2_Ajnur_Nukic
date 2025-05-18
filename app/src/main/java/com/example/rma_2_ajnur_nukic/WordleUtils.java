package com.example.rma_2_ajnur_nukic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordleUtils {

    /**
     * Split a word into Bosnian “letters”, counting NJ, LJ, DŽ as single letters.
     */
    public static List<String> splitToLetters(String word) {
        List<String> letters = new ArrayList<>();
        for (int i = 0; i < word.length(); ) {
            if (i + 1 < word.length()) {
                String pair = word.substring(i, i + 2).toUpperCase();
                if (pair.equals("NJ") || pair.equals("LJ") || pair.equals("DŽ")) {
                    letters.add(pair);
                    i += 2;
                    continue;
                }
            }
            letters.add(String.valueOf(word.charAt(i)).toUpperCase());
            i++;
        }
        return letters;
    }

    /** Count “letters” in a Bosnian word, considering digraphs. */
    public static int countBosnianLetters(String word) {
        return splitToLetters(word).size();
    }

    /**
     * Evaluate a guess against the target:
     *   2 = correct letter & position (green)
     *   1 = letter present but wrong position (yellow)
     *   0 = letter absent (gray)
     *
     * Ispravno raspoređuje “žuta” i “siva” slova koristeći frekvencijsku mapu.
     */
    public static List<Integer> evaluateGuess(List<String> guess, List<String> target) {
        int n = guess.size();
        List<Integer> result = new ArrayList<>(Collections.nCopies(n, 0));
        // prvo prođi zelene
        List<String> remaining = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (guess.get(i).equals(target.get(i))) {
                result.set(i, 2);
            } else {
                remaining.add(target.get(i));
            }
        }
        // onda žute/gray
        for (int i = 0; i < n; i++) {
            if (result.get(i) == 0) {
                String g = guess.get(i);
                if (remaining.contains(g)) {
                    result.set(i, 1);
                    remaining.remove(g);
                }
            }
        }
        return result;
    }

}
