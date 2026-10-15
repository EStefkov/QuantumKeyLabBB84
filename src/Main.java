
package app;

import entities.Alice;
import entities.Bob;
import entities.Eve;
import quantum.QuantumState;
import protocol.BB84Protocol;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        Alice alice = new Alice();
        Eve eve = new Eve();
        Bob bob = new Bob();

        // 1. Alice генерира битове, бази и квантови състояния
        List<Integer> aliceBits = alice.generateBits(8);
        List<String> aliceBases = alice.generateBases(8);

        List<QuantumState> aliceStates =
                alice.encodeBits(aliceBits, aliceBases);

        // 2. Eve прихваща състоянията и ги изпраща отново
        List<QuantumState> statesToBob =
                eve.interceptAndResend(aliceStates);

        // 3. Bob избира свои бази и измерва състоянията
        List<String> bobBases = bob.generateBases(8);

        List<Integer> bobBits =
                bob.measureStates(statesToBob, bobBases);

        // 4. Alice и Bob запазват позициите със съвпадащи бази
        BB84Protocol protocol = new BB84Protocol();

        List<Integer> matchingIndexes = protocol.siftKey(
                aliceBases,
                bobBases
        );

        List<Integer> aliceKey = protocol.extractKey(
                aliceBits,
                matchingIndexes
        );

        List<Integer> bobKey = protocol.extractKey(
                bobBits,
                matchingIndexes
        );

        // 5. Показваме резултатите от предаването
        System.out.println("===== BB84: ПРЕДАВАНЕ =====");
        System.out.println("Alice bits:       " + aliceBits);
        System.out.println("Alice bases:      " + aliceBases);
        System.out.println("Alice states:     " + aliceStates);

        System.out.println("Eve bases:        " + eve.getLastBases());
        System.out.println("Eve measured:     " + eve.getLastMeasuredBits());
        System.out.println("States to Bob:    " + statesToBob);

        System.out.println("Bob bases:        " + bobBases);
        System.out.println("Bob bits:         " + bobBits);

        System.out.println("\n===== BB84: SIFTED KEY =====");
        System.out.println("Matching indexes: " + matchingIndexes);
        System.out.println("Alice key:        " + aliceKey);
        System.out.println("Bob key:          " + bobKey);

        // 6. Избираме битове за публична проверка
        int sampleSize = Math.min(3, aliceKey.size());

        if (sampleSize == 0) {
            System.out.println(
                    "\nНяма достатъчно битове за проверка."
            );
            return;
        }

        List<Integer> sampleIndexes =
                protocol.selectSampleIndexes(
                        aliceKey.size(),
                        sampleSize
                );

        // 7. Изчисляваме QBER върху тестовата извадка
        double errorRate = protocol.calculateErrorRate(
                aliceKey,
                bobKey,
                sampleIndexes
        );

        List<Integer> aliceSample =
                protocol.extractKey(aliceKey, sampleIndexes);

        List<Integer> bobSample =
                protocol.extractKey(bobKey, sampleIndexes);

        System.out.println("\n===== BB84: ПРОВЕРКА =====");
        System.out.println("Sample indexes:   " + sampleIndexes);
        System.out.println("Alice sample:     " + aliceSample);
        System.out.println("Bob sample:       " + bobSample);

        System.out.printf(
                "QBER:              %.2f%%%n",
                errorRate * 100
        );

        // 8. Премахваме публично разкритите тестови битове
        List<Integer> finalAliceKey =
                protocol.removeSampledBits(
                        aliceKey,
                        sampleIndexes
                );

        List<Integer> finalBobKey =
                protocol.removeSampledBits(
                        bobKey,
                        sampleIndexes
                );

        System.out.println("\n===== BB84: КЛЮЧ СЛЕД ПРОВЕРКАТА =====");
        System.out.println("Alice final key:  " + finalAliceKey);
        System.out.println("Bob final key:    " + finalBobKey);

        // 9. Учебна индикация за наличие на грешки
        if (errorRate > 0) {
            System.out.println(
                    "\nОткрити са грешки! Възможно е да има подслушване."
            );
        } else {
            System.out.println(
                    "\nВ тестовата извадка не са открити грешки."
            );
        }

        // Допълнителна проверка дали останалите ключове съвпадат
        System.out.println(
                "Останалите ключове съвпадат: "
                        + finalAliceKey.equals(finalBobKey)
        );
    }
}
