package experiments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Formats shared summaries; statistical calculations belong to ExperimentSummary. */
public final class SummaryExporter {
    private SummaryExporter() { }

    public static void write(List<ExperimentRunRecord> records, Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("N,f,noise,Category,Runs,MeanQBERPercent,StdQBERPercent,MeanSiftedPercent,MeanCandidateBits,RejectPercent,DetectionPercent,FalseRejectPercent,MeanDurationMs,StdDurationMs");
        for (ExperimentSummary summary : ExperimentSummary.summarize(records)) {
            lines.add(summary.getNSent() + "," + summary.getEveProbability() + ","
                    + summary.getNoiseProbability() + "," + summary.getCategory() + ","
                    + summary.getRuns() + "," + number(summary.getAverageQber()) + ","
                    + number(summary.getStandardDeviation()) + ","
                    + number(summary.getMeanSiftedPercent()) + ","
                    + number(summary.getMeanCandidateBits()) + ","
                    + number(summary.getRejectRate()) + "," + number(summary.getDetectionRate()) + ","
                    + number(summary.getFalseRejectionRate()) + ","
                    + number(summary.getMeanDurationMs()) + "," + number(summary.getStdDurationMs()));
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private static String number(double value) {
        return Double.isFinite(value) ? String.format(Locale.US, "%.6f", value) : "N/A";
    }
}
