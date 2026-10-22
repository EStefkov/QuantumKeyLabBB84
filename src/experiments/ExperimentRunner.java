package experiments;

import simulation.BB84Simulator;
import simulation.SimulationConfig;
import simulation.SimulationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExperimentRunner {

    private final List<ExperimentRunRecord> runRecords =
            new ArrayList<>();

    private static final int[] SIZES = {
            200, 1000, 5000
    };

    private static final double[] EVE_PROBABILITIES = {
            0.0, 0.25, 0.5, 1.0
    };

    private static final double[] NOISE_PROBABILITIES = {
            0.0, 0.01, 0.03
    };

    private static final int RUNS_PER_COMBINATION = 100;

    public List<ExperimentRunRecord> getRunRecords() {
        return new ArrayList<>(runRecords);
    }

    public List<ExperimentSummary> runAll() {

        runRecords.clear();

        List<ExperimentSummary> summaries = new ArrayList<>();
        BB84Simulator simulator = new BB84Simulator();

        long seed = 20261015L;
        long totalStart = System.nanoTime();

        printHeader();

        for (int n : SIZES) {
            for (double eve : EVE_PROBABILITIES) {
                for (double noise : NOISE_PROBABILITIES) {

                    long combinationStart = System.nanoTime();

                    List<Double> qberValues = new ArrayList<>();

                    int accepted = 0;
                    int rejected = 0;
                    int insufficient = 0;

                    for (int run = 0;
                         run < RUNS_PER_COMBINATION;
                         run++) {

                        long currentSeed = seed++;

                        SimulationConfig config = new SimulationConfig(
                                n,
                                eve,
                                noise,
                                currentSeed
                        );

                        long runStart = System.nanoTime();

                        SimulationResult result = simulator.run(config);

                        long runDurationNs =
                                System.nanoTime() - runStart;

                        runRecords.add(new ExperimentRunRecord(
                                n,
                                eve,
                                noise,
                                run + 1,
                                currentSeed,
                                result,
                                runDurationNs
                        ));

                        String decision = result.getDecision();

                        if ("accept".equalsIgnoreCase(decision)) {
                            accepted++;
                        } else if ("reject".equalsIgnoreCase(decision)) {
                            rejected++;
                        } else if ("insufficient_data".equalsIgnoreCase(decision)) {
                            insufficient++;
                        }

                        if (Double.isFinite(result.getQberPercent())) {
                            qberValues.add(result.getQberPercent());
                        }
                    }

                    double averageQber =
                            calculateAverage(qberValues);

                    double standardDeviation =
                            calculateStandardDeviation(
                                    qberValues,
                                    averageQber
                            );

                    double acceptRate =
                            percentage(accepted, RUNS_PER_COMBINATION);

                    double rejectRate =
                            percentage(rejected, RUNS_PER_COMBINATION);

                    double insufficientRate =
                            percentage(insufficient, RUNS_PER_COMBINATION);

                    double detectionRate = eve > 0.0
                            ? rejectRate
                            : Double.NaN;

                    double falseRejectionRate = eve == 0.0
                            ? rejectRate
                            : Double.NaN;

                    long durationMs =
                            (System.nanoTime() - combinationStart)
                                    / 1_000_000;

                    summaries.add(new ExperimentSummary(
                            n,
                            eve,
                            noise,
                            RUNS_PER_COMBINATION,
                            averageQber,
                            standardDeviation,
                            rejectRate,
                            insufficientRate,
                            detectionRate,
                            falseRejectionRate,
                            durationMs
                    ));

                    printResult(
                            n,
                            eve,
                            noise,
                            averageQber,
                            standardDeviation,
                            acceptRate,
                            rejectRate,
                            insufficientRate,
                            detectionRate,
                            durationMs
                    );
                }
            }
        }

        long totalDurationNs = System.nanoTime() - totalStart;

        printSummary(
                summaries.size(),
                runRecords.size(),
                totalDurationNs
        );

        return summaries;
    }

    private void printHeader() {

        System.out.println();
        System.out.println("==========================================================================");
        System.out.println("                  BB84 - ЕКСПЕРИМЕНТАЛНИ РЕЗУЛТАТИ");
        System.out.println("==========================================================================");

        System.out.printf(
                Locale.US,
                "%-7s %-6s %-7s %-11s %-10s %-10s %-10s %-12s %-10s%n",
                "N",
                "Eve",
                "Noise",
                "Avg QBER",
                "StdDev",
                "Accept",
                "Reject",
                "Insufficient",
                "Time(ms)"
        );

        System.out.println(
                "--------------------------------------------------------------------------"
        );
    }

    private void printResult(
            int n,
            double eve,
            double noise,
            double averageQber,
            double standardDeviation,
            double acceptRate,
            double rejectRate,
            double insufficientRate,
            double detectionRate,
            long durationMs
    ) {

        System.out.printf(
                Locale.US,
                "%-7d %-6.2f %-7.2f %-11s %-10s %-10s %-10s %-12s %-10d%n",
                n,
                eve,
                noise,
                formatPercent(averageQber),
                formatPercent(standardDeviation),
                formatPercent(acceptRate),
                formatPercent(rejectRate),
                formatPercent(insufficientRate),
                durationMs
        );
    }

    private void printSummary(
            int combinations,
            int totalRuns,
            long totalDurationNs
    ) {

        System.out.println();
        System.out.println("==========================================================================");
        System.out.println("                       ОБОБЩЕНИЕ НА ЕКСПЕРИМЕНТА");
        System.out.println("==========================================================================");

        System.out.printf(
                "Общо комбинации параметри : %d%n",
                combinations
        );

        System.out.printf(
                "Общо изпълнени симулации  : %d%n",
                totalRuns
        );

        System.out.printf(
                Locale.US,
                "Общо време                 : %.3f секунди%n",
                totalDurationNs / 1_000_000_000.0
        );

        System.out.println("==========================================================================");
    }

    private double calculateAverage(List<Double> values) {

        if (values.isEmpty()) {
            return Double.NaN;
        }

        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    private double calculateStandardDeviation(
            List<Double> values,
            double average
    ) {

        if (values.size() < 2) {
            return Double.NaN;
        }

        double sumSquaredDifferences = 0.0;

        for (double value : values) {
            double difference = value - average;
            sumSquaredDifferences += difference * difference;
        }

        // Извадково стандартно отклонение.
        return Math.sqrt(
                sumSquaredDifferences / (values.size() - 1)
        );
    }

    private double percentage(int count, int total) {
        return total == 0
                ? 0.0
                : (double) count / total * 100.0;
    }

    private String formatPercent(double value) {

        if (!Double.isFinite(value)) {
            return "N/A";
        }

        return String.format(Locale.US, "%.2f%%", value);
    }
}
