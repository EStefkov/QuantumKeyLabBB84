
package entities;

import quantum.QuantumState;
import random.RandomGenerator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Alice {

    private final RandomGenerator randomGenerator;

    public Alice() {
        this.randomGenerator = new RandomGenerator(
                new Timestamp(System.currentTimeMillis())
        );
    }

    // Генерира един бит: 0 или 1
    public int generateBit() {
        return randomGenerator.generateBit();
    }

    // Генерира една база: Z или X
    public String generateBase() {
        return generateBit() == 0 ? "Z" : "X";
    }

    // Генерира списък от битове
    public List<Integer> generateBits(int count) {
        List<Integer> bits = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            bits.add(generateBit());
        }

        return bits;
    }

    // Генерира списък от бази
    public List<String> generateBases(int count) {
        List<String> bases = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            bases.add(generateBase());
        }

        return bases;
    }

    // Кодира един бит в квантово състояние
    public QuantumState encodeBit(int bit, String basis) {

        if (bit != 0 && bit != 1) {
            throw new IllegalArgumentException(
                    "Битът трябва да бъде 0 или 1."
            );
        }

        if (basis == null) {
            throw new IllegalArgumentException(
                    "Базата не може да бъде null."
            );
        }

        return switch (basis) {
            case "Z" -> bit == 0
                    ? QuantumState.ZERO_Z
                    : QuantumState.ONE_Z;

            case "X" -> bit == 0
                    ? QuantumState.ZERO_X
                    : QuantumState.ONE_X;

            default -> throw new IllegalArgumentException(
                    "Базата трябва да бъде Z или X."
            );
        };
    }

    // Кодира всички битове в квантови състояния
    public List<QuantumState> encodeBits(
            List<Integer> bits,
            List<String> bases) {

        if (bits.size() != bases.size()) {
            throw new IllegalArgumentException(
                    "Броят на битовете и базите трябва да съвпада."
            );
        }

        List<QuantumState> states = new ArrayList<>();

        for (int i = 0; i < bits.size(); i++) {
            states.add(encodeBit(bits.get(i), bases.get(i)));
        }

        return states;
    }
}
