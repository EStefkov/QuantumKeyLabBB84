
package entities;

import random.RandomGenerator;
import quantum.QuantumState;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Bob {

    private final RandomGenerator randomGenerator;

    public Bob() {
        this.randomGenerator = new RandomGenerator(
                new Timestamp(System.currentTimeMillis())
        );
    }

    // Генерира една база: Z или X
    public String generateBase() {
        return randomGenerator.generateBit() == 0
                ? "Z"
                : "X";
    }

    // Генерира списък от бази
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

    // Измерва едно квантово състояние
    public int measureState(QuantumState state, String basis) {

        if (state == null) {
            throw new IllegalArgumentException(
                    "Квантовото състояние не може да бъде null."
            );
        }

        if (!"Z".equals(basis) && !"X".equals(basis)) {
            throw new IllegalArgumentException(
                    "Базата трябва да бъде Z или X."
            );
        }

        // Измерване в Z база
        if (basis.equals("Z")) {
            return switch (state) {
                case ZERO_Z, ZERO_X ->
                        state == QuantumState.ZERO_Z ? 0
                                : randomGenerator.generateBit();

                case ONE_Z, ONE_X ->
                        state == QuantumState.ONE_Z ? 1
                                : randomGenerator.generateBit();
            };
        }

        // Измерване в X база
        return switch (state) {
            case ZERO_X -> 0;
            case ONE_X -> 1;
            case ZERO_Z, ONE_Z -> randomGenerator.generateBit();
        };
    }

    // Измерва списък от квантови състояния
    public List<Integer> measureStates(
            List<QuantumState> states,
            List<String> bases) {

        if (states == null || bases == null) {
            throw new IllegalArgumentException(
                    "Списъците не могат да бъдат null."
            );
        }

        if (states.size() != bases.size()) {
            throw new IllegalArgumentException(
                    "Броят на състоянията и базите трябва да съвпада."
            );
        }

        List<Integer> measuredBits = new ArrayList<>();

        for (int i = 0; i < states.size(); i++) {
            measuredBits.add(
                    measureState(states.get(i), bases.get(i))
            );
        }

        return measuredBits;
    }
}
