package experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/** One immutable statistical summary, shared by the console, UI, charts and CSV. */
public final class ExperimentSummary {
    private final int nSent;
    private final double eveProbability;
    private final double noiseProbability;
    private final int runs;
    private final double averageQber;
    private final double standardDeviation;
    private final double rejectRate;
    private final double insufficientRate;
    private final double meanSiftedPercent;
    private final double meanCandidateBits;
    private final double meanDurationMs;
    private final double stdDurationMs;

    private ExperimentSummary(List<ExperimentRunRecord> records) {
        ExperimentRunRecord first = records.get(0);
        nSent = first.getNSent();
        eveProbability = first.getEveProbability();
        noiseProbability = first.getNoiseProbability();
        runs = records.size();
        Statistics qber = statistics(records, ExperimentRunRecord::getQberPercent);
        Statistics timing = statistics(records,
                record -> record.getDurationNs() > 0 ? record.getDurationNs() / 1e6 : Double.NaN);
        averageQber = qber.mean;
        standardDeviation = qber.standardDeviation;
        meanDurationMs = timing.mean;
        stdDurationMs = timing.standardDeviation;
        meanSiftedPercent = statistics(records, ExperimentRunRecord::getSiftedPercent).mean;
        meanCandidateBits = statistics(records, ExperimentRunRecord::getCandidateBits).mean;
        rejectRate = decisionRate(records, "reject");
        insufficientRate = decisionRate(records, "insufficient_data");
    }

    /** Groups by all channel parameters, preserving experiment order. */
    public static List<ExperimentSummary> summarize(List<ExperimentRunRecord> records) {
        Map<List<Number>, List<ExperimentRunRecord>> groups = new LinkedHashMap<>();
        for (ExperimentRunRecord record : records) {
            List<Number> key = Arrays.asList(record.getNSent(), record.getEveProbability(),
                    record.getNoiseProbability());
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(record);
        }
        List<ExperimentSummary> summaries = new ArrayList<>();
        for (List<ExperimentRunRecord> group : groups.values()) {
            summaries.add(new ExperimentSummary(group));
        }
        return summaries;
    }

    private static double decisionRate(List<ExperimentRunRecord> records, String decision) {
        long count = records.stream().filter(record -> decision.equals(record.getDecision())).count();
        return 100.0 * count / records.size();
    }

    /** Welford's algorithm: finite values only, sample standard deviation (n-1). */
    private static Statistics statistics(List<ExperimentRunRecord> records,
                                         ToDoubleFunction<ExperimentRunRecord> valueOf) {
        int count = 0;
        double mean = 0;
        double squaredDifferences = 0;
        for (ExperimentRunRecord record : records) {
            double value = valueOf.applyAsDouble(record);
            if (!Double.isFinite(value)) continue;
            count++;
            double delta = value - mean;
            mean += delta / count;
            squaredDifferences += delta * (value - mean);
        }
        return new Statistics(count == 0 ? Double.NaN : mean,
                count < 2 ? Double.NaN : Math.sqrt(squaredDifferences / (count - 1)));
    }

    private static final class Statistics {
        final double mean;
        final double standardDeviation;
        Statistics(double mean, double standardDeviation) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
        }
    }

    public int getNSent() { return nSent; }
    public double getEveProbability() { return eveProbability; }
    public double getNoiseProbability() { return noiseProbability; }
    public int getRuns() { return runs; }
    public double getAverageQber() { return averageQber; }
    public double getStandardDeviation() { return standardDeviation; }
    public double getRejectRate() { return rejectRate; }
    public double getInsufficientRate() { return insufficientRate; }
    public double getAcceptRate() { return 100 - rejectRate - insufficientRate; }
    public double getDetectionRate() { return eveProbability > 0 ? rejectRate : Double.NaN; }
    public double getFalseRejectionRate() { return eveProbability == 0 ? rejectRate : Double.NaN; }
    public double getMeanSiftedPercent() { return meanSiftedPercent; }
    public double getMeanCandidateBits() { return meanCandidateBits; }
    public double getMeanDurationMs() { return meanDurationMs; }
    public double getStdDurationMs() { return stdDurationMs; }
    public String getCategory() {
        if (eveProbability == 0) return noiseProbability == 0 ? "clean" : "noise_only";
        return noiseProbability == 0 ? "eve_only" : "eve_and_noise";
    }
}
