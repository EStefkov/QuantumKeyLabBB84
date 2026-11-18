import experiments.ExperimentRunRecord;
import experiments.ExperimentSummary;
import experiments.SummaryExporter;
import simulation.SimulationResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Checks shared statistics against independently calculated examples and CSV output. */
public final class ExperimentSummaryTest {
    public static void main(String[] args) throws Exception {
        List<ExperimentRunRecord> records = Arrays.asList(
                record(0, 0, 0, "accept", 1_000_000),
                record(0, 0, 10, "reject", 3_000_000),
                record(1, .03, 25, "reject", 0));
        List<ExperimentSummary> summaries = ExperimentSummary.summarize(records);
        check(summaries.size() == 2, "group by Eve and noise");
        ExperimentSummary clean = summaries.get(0);
        near(clean.getAverageQber(), 5);
        near(clean.getStandardDeviation(), Math.sqrt(50));
        near(clean.getMeanDurationMs(), 2);
        near(clean.getStdDurationMs(), Math.sqrt(2));
        near(clean.getFalseRejectionRate(), 50);
        check(Double.isNaN(clean.getDetectionRate()), "detection is undefined without Eve");
        near(clean.getMeanSiftedPercent(), 50);
        near(clean.getMeanCandidateBits(), 40);
        ExperimentSummary attacked = summaries.get(1);
        near(attacked.getDetectionRate(), 100);
        check(Double.isNaN(attacked.getMeanDurationMs()), "unmeasured time is undefined");
        check(Double.isNaN(attacked.getStandardDeviation()), "one sample has no sample SD");
        check("eve_and_noise".equals(attacked.getCategory()), "attack category");
        check(ExperimentSummary.summarize(Collections.emptyList()).isEmpty(), "empty input");
        Path csv = Files.createTempFile("bb84-summary-check-", ".csv");
        try {
            SummaryExporter.write(records, csv);
            List<String> lines = Files.readAllLines(csv);
            check(lines.size() == 3, "one CSV row per configuration");
            String[] cleanRow = lines.get(1).split(",");
            near(Double.parseDouble(cleanRow[5]), clean.getAverageQber());
            check(cleanRow[10].equals("N/A"), "CSV N/A");
            near(Double.parseDouble(cleanRow[11]), clean.getFalseRejectionRate());
        } finally {
            Files.deleteIfExists(csv);
        }
        System.out.println("PASS: shared summaries, sample SD, timing, grouping and CSV consistency");
    }

    private static ExperimentRunRecord record(double eve, double noise, double qber,
                                               String decision, long duration) {
        SimulationResult result = new SimulationResult(100, 50, 10, (int) (qber / 10),
                qber, 50, 40, decision);
        return new ExperimentRunRecord(100, eve, noise, 1, 42, result, duration);
    }

    private static void near(double actual, double expected) {
        check(Math.abs(actual - expected) < 1e-6, "expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
