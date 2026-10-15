
package protocol;

import java.util.ArrayList;
import java.util.List;

public class BB84Protocol {

    // Връща индексите, на които базите съвпадат
    public List<Integer> siftKey(
            List<String> aliceBases,
            List<String> bobBases) {

        if (aliceBases == null || bobBases == null) {
            throw new IllegalArgumentException(
                    "Списъците с бази не могат да бъдат null."
            );
        }

        if (aliceBases.size() != bobBases.size()) {
            throw new IllegalArgumentException(
                    "Броят на базите трябва да съвпада."
            );
        }

        List<Integer> matchingIndexes = new ArrayList<>();

        for (int i = 0; i < aliceBases.size(); i++) {
            if (aliceBases.get(i).equals(bobBases.get(i))) {
                matchingIndexes.add(i);
            }
        }

        return matchingIndexes;
    }

    // Извлича битовете от посочените позиции
    public List<Integer> extractKey(
            List<Integer> bits,
            List<Integer> matchingIndexes) {

        if (bits == null || matchingIndexes == null) {
            throw new IllegalArgumentException(
                    "Списъците не могат да бъдат null."
            );
        }

        List<Integer> key = new ArrayList<>();

        for (int index : matchingIndexes) {
            if (index < 0 || index >= bits.size()) {
                throw new IllegalArgumentException(
                        "Невалиден индекс: " + index
                );
            }

            key.add(bits.get(index));
        }

        return key;
    }


    public List<Integer> selectSampleIndexes(int keySize, int sampleSize) {
        if (keySize < 0 || sampleSize < 0 || sampleSize > keySize) {
            throw new IllegalArgumentException(
                    "Невалиден размер на тестовата извадка."
            );
        }

        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < keySize; i++) {
            indexes.add(i);
        }

        // Разбъркване на индексите чрез Fisher-Yates
        random.RandomGenerator generator =
                new random.RandomGenerator(
                        new java.sql.Timestamp(System.currentTimeMillis())
                );

        for (int i = indexes.size() - 1; i > 0; i--) {
            int j = (int) Math.floorMod(
                    generator.generateRandomNum(), i + 1L
            );

            int temp = indexes.get(i);
            indexes.set(i, indexes.get(j));
            indexes.set(j, temp);
        }

        return new ArrayList<>(indexes.subList(0, sampleSize));
    }

    public double calculateErrorRate(
            List<Integer> aliceKey,
            List<Integer> bobKey,
            List<Integer> sampleIndexes) {

        if (aliceKey == null || bobKey == null || sampleIndexes == null) {
            throw new IllegalArgumentException(
                    "Списъците не могат да бъдат null."
            );
        }

        if (aliceKey.size() != bobKey.size()) {
            throw new IllegalArgumentException(
                    "Двата ключа трябва да имат еднаква дължина."
            );
        }

        if (sampleIndexes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Необходима е поне една позиция за проверка."
            );
        }

        int errors = 0;

        for (int index : sampleIndexes) {
            if (index < 0 || index >= aliceKey.size()) {
                throw new IllegalArgumentException(
                        "Невалиден индекс: " + index
                );
            }

            if (!aliceKey.get(index).equals(bobKey.get(index))) {
                errors++;
            }
        }

        return (double) errors / sampleIndexes.size();
    }

    public List<Integer> removeSampledBits(
            List<Integer> key,
            List<Integer> sampleIndexes) {

        if (key == null || sampleIndexes == null) {
            throw new IllegalArgumentException(
                    "Списъците не могат да бъдат null."
            );
        }

        List<Integer> remainingKey = new ArrayList<>(key);

        // Изтриваме отзад напред, за да не се променят
        // индексите на останалите елементи.
        List<Integer> sortedIndexes = new ArrayList<>(sampleIndexes);
        sortedIndexes.sort(java.util.Collections.reverseOrder());

        for (int index : sortedIndexes) {
            if (index < 0 || index >= remainingKey.size()) {
                throw new IllegalArgumentException(
                        "Невалиден индекс: " + index
                );
            }

            remainingKey.remove(index);
        }

        return remainingKey;
    }

}
