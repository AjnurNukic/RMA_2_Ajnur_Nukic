package com.example.rma_2_ajnur_nukic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WordleUtilsTest {

    @Test
    public void testSplitToLetters() {
        assertEquals(Arrays.asList("NJ","I","V","A"),
                WordleUtils.splitToLetters("NJIVA"));
        assertEquals(Arrays.asList("LJ","E","T","O"),
                WordleUtils.splitToLetters("LJETO"));
        assertEquals(Arrays.asList("DŽ","A","B","A"),
                WordleUtils.splitToLetters("DŽABA"));
        assertEquals(Arrays.asList("A","B","C","Ć","D"),
                WordleUtils.splitToLetters("ABCĆD"));
    }

    @Test
    public void testCountBosnianLetters() {
        assertEquals(4, WordleUtils.countBosnianLetters("NJIVA"));
        assertEquals(4, WordleUtils.countBosnianLetters("LJETO"));
        assertEquals(4, WordleUtils.countBosnianLetters("DŽABA"));
        assertEquals(5, WordleUtils.countBosnianLetters("ABCDE"));
    }

    @Test
    public void testEvaluateGuess_AllCorrect() {
        List<Integer> result = WordleUtils.evaluateGuess(
                Arrays.asList("S","T","O","J","A"),
                Arrays.asList("S","T","O","J","A")
        );
        assertEquals(Arrays.asList(2, 2, 2, 2, 2), result);
    }

    @Test
    public void testEvaluateGuess_AllAbsent() {
        List<Integer> result = WordleUtils.evaluateGuess(
                Arrays.asList("X","Y","Z","U","V"),
                Arrays.asList("S","T","O","J","A")
        );
        assertEquals(Arrays.asList(0, 0, 0, 0, 0), result);
    }

    @Test
    public void testEvaluateGuess_Mixed() {
        List<Integer> result = WordleUtils.evaluateGuess(
                Arrays.asList("J","A","N","S","A"),
                Arrays.asList("B","A","N","J","A")
        );
        assertEquals(Arrays.asList(1, 2, 2, 0, 2), result);
    }

    @Test
    public void testEvaluateGuess_YellowLimit() {
        // PAPAK vs PATKA → [G,G,.,Y,Y] = [2,2,0,1,1]
        List<Integer> result = WordleUtils.evaluateGuess(
                Arrays.asList("P","A","P","A","K"),
                Arrays.asList("P","A","T","K","A")
        );
        assertEquals(Arrays.asList(2, 2, 0, 1, 1), result);
    }

    // Ako želiš da ostaviš test za case‑insensitive, prije toga u WordleUtils.evaluateGuess:
    // for svaki unos raditi .toUpperCase(Locale.ROOT)
    // pa onda ovaj test možeš vratiti:
    /*
    @Test
    public void testEvaluateGuess_CaseInsensitive() {
        List<Integer> result = WordleUtils.evaluateGuess(
                Arrays.asList("s","t","o","j","a"),
                Arrays.asList("S","T","O","J","A")
        );
        assertEquals(Arrays.asList(2,2,2,2,2), result);
    }
    */
}
