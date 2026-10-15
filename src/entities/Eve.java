
package entities;

import random.RandomGenerator;
import quantum.QuantumState;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Eve {

    private final RandomGenerator randomGenerator;

    private List<String> lastBases = new ArrayList<>();
    private List<Integer> lastMeasuredBits = new ArrayList<>();

    public Eve() {
        this.randomGenerator = new RandomGenerator(
                new Timestamp(System.currentTimeMillis())
        );
    }

    // Eve избира собствена база: Z или X
    public String generateBase() {
        return randomGenerator.generateBit() == 0
                ? "Z"
                : "X";
    }

    // Eve генерира бази за всички прихванати състояния
    public List<String> generateBases(int count) {
        if (count < 0) {
            throw new IllegalArgumentException(
                    "Броят на базите не може да бъде отрицателен."
            );
        }

        List<String> bases = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            bases.add(generateBase());
        }

        return bases;
    }

    // Eve измерва едно състояние със своя база
    public int measureState(QuantumState state, String basis) {

        if (state == null) {
            throw new IllegalArgumentException(
                    "Състоянието не може да бъде null."
            );
        }

        if (!"Z".equals(basis) && !"X".equals(basis)) {
            throw new IllegalArgumentException(
                    "Базата трябва да бъде Z или X."
            );
        }

        if (basis.equals("Z")) {
            return switch (state) {
                case ZERO_Z -> 0;
                case ONE_Z -> 1;
                case ZERO_X, ONE_X ->
                        randomGenerator.generateBit();
            };
        }

        return switch (state) {
            case ZERO_X -> 0;
            case ONE_X -> 1;
            case ZERO_Z, ONE_Z ->
                    randomGenerator.generateBit();
        };
    }

    // Преобразува измерения бит в ново състояние
    private QuantumState encodeBit(int bit, String basis) {
        if (basis.equals("Z")) {
            return bit == 0
                    ? QuantumState.ZERO_Z
                    : QuantumState.ONE_Z;
        }

        return bit == 0
                ? QuantumState.ZERO_X
                : QuantumState.ONE_X;
    }

    // Прихваща, измерва и изпраща отново състоянията
    public List<QuantumState> interceptAndResend(
            List<QuantumState> states) {

        if (states == null) {
            throw new IllegalArgumentException(
                    "Списъкът със състояния не може да бъде null."
            );
        }

        lastBases = generateBases(states.size());
        lastMeasuredBits = new ArrayList<>();

        List<QuantumState> resentStates = new ArrayList<>();

        for (int i = 0; i < states.size(); i++) {

            // 1. Eve измерва състоянието
            int bit = measureState(
                    states.get(i),
                    lastBases.get(i)
            );

            lastMeasuredBits.add(bit);

            // 2. Eve създава ново състояние
            QuantumState newState = encodeBit(
                    bit,
                    lastBases.get(i)
            );

            // 3. Eve изпраща новото състояние към Bob
            resentStates.add(newState);
        }

        return resentStates;
    }

    public List<String> getLastBases() {
        return new ArrayList<>(lastBases);
    }

    public List<Integer> getLastMeasuredBits() {
        return new ArrayList<>(lastMeasuredBits);
    }
}
