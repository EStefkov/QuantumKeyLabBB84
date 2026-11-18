package experiments;

import simulation.BB84Simulator;
import simulation.SimulationConfig;
import simulation.SimulationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ExperimentRunner {

    private final List<ExperimentRunRecord> runRecords =
            new ArrayList<>();

    public List<ExperimentRunRecord> getRunRecords() {
        return new ArrayList<>(runRecords);
    }

    public List<ExperimentSummary> runAll() {

        runRecords.clear();

        List<ExperimentSummary> summaries = new ArrayList<>();
        BB84Simulator simulator = new BB84Simulator();

        long seed =  new Random().nextLong();
        long totalStart = System.nanoTime();

        printHeader();

        for (int n : ExperimentPlan.sizes()) {
            for (double eve : ExperimentPlan.eveProbabilities()) {
                for (double noise : ExperimentPlan.noiseProbabilities()) {

                    // Equal warm-up work for every configuration, excluded from measurements.
                    for (int warm = 0; warm < ExperimentPlan.WARMUP_RUNS; warm++) {
                        simulator.run(new SimulationConfig(n, eve, noise, seed++));
                    }
                    long combinationStart = System.nanoTime();

                    int groupStart = runRecords.size();

                    for (int run = 0;
                         run < ExperimentPlan.RUNS_PER_COMBINATION;
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

                    }

                    ExperimentSummary summary = ExperimentSummary.summarize(
                            runRecords.subList(groupStart, runRecords.size())).get(0);
                    summaries.add(summary);
                    printResult(summary, (System.nanoTime() - combinationStart) / 1_000_000);

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

    private void printResult(ExperimentSummary summary, long durationMs) {
        System.out.printf(
                Locale.US,
                "%-7d %-6.2f %-7.2f %-11s %-10s %-10s %-10s %-12s %-10d%n",
                summary.getNSent(), summary.getEveProbability(), summary.getNoiseProbability(),
                formatPercent(summary.getAverageQber()), formatPercent(summary.getStandardDeviation()),
                formatPercent(summary.getAcceptRate()), formatPercent(summary.getRejectRate()),
                formatPercent(summary.getInsufficientRate()), durationMs);
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

    private String formatPercent(double value) {

        if (!Double.isFinite(value)) {
            return "N/A";
        }

        return String.format(Locale.US, "%.2f%%", value);
    }
}
