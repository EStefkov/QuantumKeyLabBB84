
package experiments;

public class ExperimentSummary {

    private final int nSent;
    private final double eveProbability;
    private final double noiseProbability;
    private final int runs;

    private final double averageQber;
    private final double standardDeviation;
    private final double rejectRate;
    private final double insufficientRate;
    private final double detectionRate;
    private final double falseRejectionRate;

    private final long durationMs;

    public ExperimentSummary(
            int nSent,
            double eveProbability,
            double noiseProbability,
            int runs,
            double averageQber,
            double standardDeviation,
            double rejectRate,
            double insufficientRate,
            double detectionRate,
            double falseRejectionRate,
            long durationMs
    ) {
        this.nSent = nSent;
        this.eveProbability = eveProbability;
        this.noiseProbability = noiseProbability;
        this.runs = runs;
        this.averageQber = averageQber;
        this.standardDeviation = standardDeviation;
        this.rejectRate = rejectRate;
        this.insufficientRate = insufficientRate;
        this.detectionRate = detectionRate;
        this.falseRejectionRate = falseRejectionRate;
        this.durationMs = durationMs;
    }

    public int getNSent() {
        return nSent;
    }

    public double getEveProbability() {
        return eveProbability;
    }

    public double getNoiseProbability() {
        return noiseProbability;
    }

    public int getRuns() {
        return runs;
    }

    public double getAverageQber() {
        return averageQber;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getRejectRate() {
        return rejectRate;
    }

    public double getInsufficientRate() {
        return insufficientRate;
    }

    public double getDetectionRate() {
        return detectionRate;
    }

    public double getFalseRejectionRate() {
        return falseRejectionRate;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
