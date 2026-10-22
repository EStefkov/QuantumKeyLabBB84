
package simulation;

import quantum.QuantumState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BB84Simulator {

    private static final double TEST_FRACTION = 0.20;
    private static final double QBER_THRESHOLD_PERCENT = 8.0;

    public SimulationResult run(SimulationConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "Конфигурацията не може да бъде null."
            );
        }

        Random random = new Random(config.getSeed());

        int nSent = config.getnSent();
        double eveProbability = config.getEveProbability();
        double noiseProbability = config.getNoiseProbability();

        List<Integer> aliceSiftedBits = new ArrayList<>();
        List<Integer> bobSiftedBits = new ArrayList<>();

        // Записи за всички изпратени състояния
        List<BitTrace> trace = new ArrayList<>();

        // Индекси на всички състояния с еднакви базиси
        List<Integer> siftedIndexes = new ArrayList<>();

        for (int i = 0; i < nSent; i++) {

            // Alice генерира бит и избира базис
            int aliceBit = random.nextBoolean() ? 1 : 0;
            String aliceBasis = random.nextBoolean() ? "X" : "Z";

            QuantumState state = encode(aliceBit, aliceBasis);

            // Eve може да прихване състоянието
            boolean eveIntercepted =
                    random.nextDouble() < eveProbability;

            String eveBasis = null;
            Integer eveBit = null;

            if (eveIntercepted) {
                eveBasis = random.nextBoolean() ? "X" : "Z";

                eveBit = measure(state, eveBasis, random);

                // Eve изпраща отново състояние според измерването си
                state = encode(eveBit, eveBasis);
            }

            // Bob избира базис и измерва
            String bobBasis = random.nextBoolean() ? "X" : "Z";
            int bobBit = measure(state, bobBasis, random);

            // Независим шум в канала
            if (random.nextDouble() < noiseProbability) {
                bobBit = 1 - bobBit;
            }

            boolean basesMatched = aliceBasis.equals(bobBasis);
            boolean error = aliceBit != bobBit;

            // Записваме информация за всяко изпратено състояние
            BitTrace bitTrace = new BitTrace(
                    i + 1,
                    aliceBit,
                    aliceBasis,
                    eveIntercepted,
                    eveBasis,
                    eveBit,
                    bobBasis,
                    bobBit,
                    basesMatched,
                    error
            );

            trace.add(bitTrace);

            // Пресяваме само състоянията с еднакви базиси
            if (basesMatched) {
                aliceSiftedBits.add(aliceBit);
                bobSiftedBits.add(bobBit);
                siftedIndexes.add(i);
            }
        }

        int nSifted = aliceSiftedBits.size();

        double siftedPercent =
                (double) nSifted / nSent * 100.0;

        if (nSifted == 0) {
            return new SimulationResult(
                    nSent, 0, 0, 0,
                    Double.NaN, siftedPercent,
                    0, "insufficient_data", trace
            );
        }

        // Избираме на случаен принцип 20% за тест
        int nTest = (int) Math.ceil(nSifted * TEST_FRACTION);

        List<Integer> shuffledIndexes = new ArrayList<>();

        for (int i = 0; i < nSifted; i++) {
            shuffledIndexes.add(i);
        }

        Collections.shuffle(shuffledIndexes, random);

        boolean[] selectedForTest = new boolean[nSent];

        int testErrors = 0;

        for (int i = 0; i < nTest; i++) {

            int siftedPosition = shuffledIndexes.get(i);
            int originalIndex = siftedIndexes.get(siftedPosition);

            selectedForTest[originalIndex] = true;
            trace.get(originalIndex).setSelectedForTest(true);

            if (!aliceSiftedBits.get(siftedPosition)
                    .equals(bobSiftedBits.get(siftedPosition))) {
                testErrors++;
            }
        }

        for (int i = 0; i < nSent; i++) {
            BitTrace bit = trace.get(i);

            if (bit.isBasesMatched() && !bit.isSelectedForTest()) {
                bit.setCandidateBit(true);
            }
        }

        // Отбелязваме тестовите позиции в подробния запис
        // Тук избраните позиции вече са известни.
        // BitTrace засега пази основните данни за всяко състояние.

        double qberPercent =
                (double) testErrors / nTest * 100.0;

        int candidateBits = nSifted - nTest;

        String decision;

        if (nTest == 0 || candidateBits <= 0) {
            decision = "insufficient_data";
        } else if (qberPercent > QBER_THRESHOLD_PERCENT) {
            decision = "reject";
        } else {
            decision = "accept";
        }

        return new SimulationResult(
                nSent,
                nSifted,
                nTest,
                testErrors,
                qberPercent,
                siftedPercent,
                candidateBits,
                decision,
                trace
        );
    }

    private QuantumState encode(int bit, String basis) {

        if ("Z".equals(basis)) {
            return bit == 0
                    ? QuantumState.ZERO_Z
                    : QuantumState.ONE_Z;
        }

        if ("X".equals(basis)) {
            return bit == 0
                    ? QuantumState.ZERO_X
                    : QuantumState.ONE_X;
        }

        throw new IllegalArgumentException(
                "Непознат базис: " + basis
        );
    }

    private int measure(
            QuantumState state,
            String measurementBasis,
            Random random
    ) {
        String stateBasis;
        int stateBit;

        switch (state) {
            case ZERO_Z:
                stateBasis = "Z";
                stateBit = 0;
                break;

            case ONE_Z:
                stateBasis = "Z";
                stateBit = 1;
                break;

            case ZERO_X:
                stateBasis = "X";
                stateBit = 0;
                break;

            case ONE_X:
                stateBasis = "X";
                stateBit = 1;
                break;

            default:
                throw new IllegalArgumentException(
                        "Непознато квантово състояние."
                );
        }

        if (stateBasis.equals(measurementBasis)) {
            return stateBit;
        }

        // При различни базиси измерването е случаен бит
        return random.nextBoolean() ? 1 : 0;
    }
}
