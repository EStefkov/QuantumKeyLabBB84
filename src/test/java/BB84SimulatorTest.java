import simulation.BB84Simulator;
import simulation.BitTrace;
import simulation.SimulationConfig;
import simulation.SimulationResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Dependency-free regression checks for the BB84 simulator. */
public final class BB84SimulatorTest {

    private static int passed;
    private static final List<String[]> journalRows = new ArrayList<>();

    private BB84SimulatorTest() { }

    public static void main(String[] args) {
        run("rejects zero transmissions", () -> expectIllegalArgument(
                () -> new SimulationConfig(0, 0, 0, 1)));
        run("rejects negative transmissions", () -> expectIllegalArgument(
                () -> new SimulationConfig(-1, 0, 0, 1)));
        run("rejects Eve probability below zero", () -> expectIllegalArgument(
                () -> new SimulationConfig(10, -0.01, 0, 1)));
        run("rejects Eve probability above one", () -> expectIllegalArgument(
                () -> new SimulationConfig(10, 1.01, 0, 1)));
        run("rejects NaN Eve probability", () -> expectIllegalArgument(
                () -> new SimulationConfig(10, Double.NaN, 0, 1)));
        run("rejects noise probability below zero", () -> expectIllegalArgument(
                () -> new SimulationConfig(10, 0, -0.01, 1)));
        run("rejects noise probability above one", () -> expectIllegalArgument(
                () -> new SimulationConfig(10, 0, 1.01, 1)));
        run("rejects NaN noise probability", () -> expectIllegalArgument(
                () -> new SimulationConfig(10, 0, Double.NaN, 1)));
        run("accepts probability boundary values", BB84SimulatorTest::testProbabilityBoundaries);
        run("same configuration and seed reproduce trace", BB84SimulatorTest::testRepeatableSeed);
        run("trace contains one record per transmission", BB84SimulatorTest::testTraceLength);
        run("only equal-basis positions are sifted", BB84SimulatorTest::testSifting);
        run("no Eve and no noise gives zero QBER", BB84SimulatorTest::testIdealChannel);
        run("test sample size is rounded upward", BB84SimulatorTest::testSampleRounding);
        run("test positions are excluded from candidate material", BB84SimulatorTest::testCandidateExclusion);
        run("noise probability one gives 100 percent QBER", BB84SimulatorTest::testCertainNoise);
        run("empty sifted set returns insufficient_data and NaN QBER", BB84SimulatorTest::testEmptySiftedSet);
        run("null configuration is rejected", BB84SimulatorTest::testNullConfiguration);

        writeJournal();
        System.out.println("PASS: " + passed + " tests");
    }

    private static void testProbabilityBoundaries() {
        new SimulationConfig(1, 0, 0, 2);
        new SimulationConfig(1, 1, 1, 3);
    }

    private static void testRepeatableSeed() {
        SimulationConfig config = new SimulationConfig(500, 0.5, 0.01, 987654321L);
        SimulationResult first = new BB84Simulator().run(config);
        SimulationResult second = new BB84Simulator().run(config);
        check(first.getTrace().size() == second.getTrace().size(), "trace sizes differ");
        check(first.getNSifted() == second.getNSifted(), "sifted counts differ");
        check(first.getNTest() == second.getNTest(), "test sample sizes differ");
        check(first.getTestErrors() == second.getTestErrors(), "test error counts differ");
        check(first.getCandidateBits() == second.getCandidateBits(), "candidate counts differ");
        check(first.getDecision().equals(second.getDecision()), "decisions differ");
        for (int i = 0; i < first.getTrace().size(); i++) {
            check(traceLine(first.getTrace().get(i)).equals(traceLine(second.getTrace().get(i))),
                    "trace differs at index " + i);
        }
    }

    private static void testTraceLength() {
        SimulationResult result = run(123, 0, 0, 5);
        check(result.getTrace().size() == 123, "expected 123 trace entries");
    }

    private static void testSifting() {
        SimulationResult result = run(500, 0.5, 0, 6);
        int matched = 0;
        for (BitTrace bit : result.getTrace()) {
            if (bit.isBasesMatched()) {
                matched++;
            } else {
                check(!bit.isSelectedForTest(), "mismatched bases selected for testing");
                check(!bit.isCandidateBit(), "mismatched bases included as candidates");
            }
        }
        check(matched == result.getNSifted(), "sifted count does not match trace");
    }

    private static void testIdealChannel() {
        SimulationResult result = run(1000, 0, 0, 7);
        check(result.getNTest() > 0, "expected a nonempty test sample");
        check(result.getTestErrors() == 0, "ideal channel had test errors");
        check(result.getQberPercent() == 0.0, "ideal channel QBER was not zero");
    }

    private static void testSampleRounding() {
        SimulationResult result = run(37, 0, 0, 8);
        check(result.getNSifted() > 0, "expected sifted bits");
        int expected = (int) Math.ceil(result.getNSifted() * 0.20);
        check(result.getNTest() == expected, "test size was not rounded up");
    }

    private static void testCandidateExclusion() {
        SimulationResult result = run(300, 0.25, 0.01, 9);
        int candidateCount = 0;
        int testCount = 0;
        for (BitTrace bit : result.getTrace()) {
            if (bit.isSelectedForTest()) {
                testCount++;
                check(!bit.isCandidateBit(), "test position is also a candidate");
            }
            if (bit.isCandidateBit()) {
                candidateCount++;
                check(bit.isBasesMatched(), "candidate has mismatched bases");
                check(!bit.isSelectedForTest(), "candidate is in test sample");
            }
        }
        check(testCount == result.getNTest(), "trace test count differs from result");
        check(candidateCount == result.getCandidateBits(), "trace candidate count differs from result");
    }

    private static void testCertainNoise() {
        SimulationResult result = run(500, 0, 1, 10);
        check(result.getNTest() > 0, "expected a nonempty test sample");
        check(result.getTestErrors() == result.getNTest(), "not every test bit was flipped");
        check(result.getQberPercent() == 100.0, "expected QBER of 100 percent");
        check("reject".equals(result.getDecision()), "100 percent QBER was not rejected");
    }

    private static void testEmptySiftedSet() {
        BB84Simulator simulator = new BB84Simulator();
        SimulationResult empty = null;
        for (long seed = 0; seed < 10000; seed++) {
            SimulationResult candidate = simulator.run(new SimulationConfig(1, 0, 0, seed));
            if (candidate.getNSifted() == 0) {
                empty = candidate;
                break;
            }
        }
        check(empty != null, "could not find a one-bit run with no basis match");
        check("insufficient_data".equals(empty.getDecision()), "empty sifted set has wrong decision");
        check(empty.getNTest() == 0, "empty sifted set has a nonzero test size");
        check(Double.isNaN(empty.getQberPercent()), "empty test QBER should be NaN internally");
    }

    private static void testNullConfiguration() {
        expectIllegalArgument(() -> new BB84Simulator().run(null));
    }

    private static SimulationResult run(int n, double eve, double noise, long seed) {
        return new BB84Simulator().run(new SimulationConfig(n, eve, noise, seed));
    }

    private static String traceLine(BitTrace bit) {
        return bit.getIndex() + "|" + bit.getAliceBit() + "|" + bit.getAliceBasis()
                + "|" + bit.isEveIntercepted() + "|" + bit.getEveBasis() + "|"
                + bit.getEveBit() + "|" + bit.getBobBasis() + "|" + bit.getBobBit()
                + "|" + bit.isBasesMatched() + "|" + bit.isError() + "|"
                + bit.isSelectedForTest() + "|" + bit.isCandidateBit();
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            journalRows.add(new String[]{name, expectedResult(name), "All test assertions passed.", "PASS"});
            writeJournal();
            System.out.println("PASS: " + name);
        } catch (Throwable failure) {
            journalRows.add(new String[]{name, expectedResult(name),
                    "Assertion failed: " + String.valueOf(failure.getMessage()), "FAIL"});
            writeJournal();
            System.err.println("FAIL: " + name + " - " + failure.getMessage());
            throw failure;
        }
    }

    private static String expectedResult(String name) {
        switch (name) {
            case "rejects zero transmissions":
                return "N=0 is rejected with IllegalArgumentException.";
            case "rejects negative transmissions":
                return "Negative N is rejected with IllegalArgumentException.";
            case "rejects Eve probability below zero":
                return "Eve probability below 0 is rejected.";
            case "rejects Eve probability above one":
                return "Eve probability above 1 is rejected.";
            case "rejects NaN Eve probability":
                return "NaN Eve probability is rejected.";
            case "rejects noise probability below zero":
                return "Noise probability below 0 is rejected.";
            case "rejects noise probability above one":
                return "Noise probability above 1 is rejected.";
            case "rejects NaN noise probability":
                return "NaN noise probability is rejected.";
            case "accepts probability boundary values":
                return "Probability values 0 and 1 are accepted.";
            case "same configuration and seed reproduce trace":
                return "Same seed and configuration produce identical traces and summaries.";
            case "trace contains one record per transmission":
                return "Trace length equals N (123 entries).";
            case "only equal-basis positions are sifted":
                return "Only matched-basis positions are sifted; mismatches are neither test nor candidate bits.";
            case "no Eve and no noise gives zero QBER":
                return "A nonempty test sample has zero errors and QBER 0%.";
            case "test sample size is rounded upward":
                return "Test sample size equals ceil(20% of sifted bits).";
            case "test positions are excluded from candidate material":
                return "Test positions are excluded and candidate positions are sifted and untested.";
            case "noise probability one gives 100 percent QBER":
                return "Every test bit is wrong, QBER is 100%, and decision is reject.";
            case "empty sifted set returns insufficient_data and NaN QBER":
                return "An empty sifted set yields insufficient_data, zero test bits, and NaN QBER.";
            case "null configuration is rejected":
                return "Null configuration is rejected with IllegalArgumentException.";
            default:
                return "All assertions defined for this scenario pass.";
        }
    }

    private static void writeJournal() {
        Path path = Path.of("test_journal.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("test_id,scenario,expected_result,actual_result,status");
            writer.newLine();
            for (int i = 0; i < journalRows.size(); i++) {
                String[] row = journalRows.get(i);
                writer.write(csv("T" + String.format("%02d", i + 1)));
                for (String value : row) {
                    writer.write(",");
                    writer.write(csv(value));
                }
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write test_journal.csv", exception);
        }
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
