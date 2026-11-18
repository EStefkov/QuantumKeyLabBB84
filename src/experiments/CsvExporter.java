
package experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvExporter {

    public Path exportRuns(
            List<ExperimentRunRecord> records,
            String filename
    ) throws IOException {

        Path path = Path.of(filename).toAbsolutePath();

        try (BufferedWriter writer = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8)) {

            writer.write(
                    "N,EveProbability,NoiseProbability,RunNumber,"
                            + "Seed,SiftedBits,SiftedPercent,TestBits,"
                            + "TestErrors,QBERPercent,CandidateBits,"
                            + "Decision,DurationNs"
            );
            writer.newLine();

            for (ExperimentRunRecord record : records) {
                writer.write(String.format(
                        java.util.Locale.US,
                        "%d,%.2f,%.2f,%d,%d,%d,%.4f,"
                                + "%d,%d,%s,%d,%s,%s",
                        record.getNSent(),
                        record.getEveProbability(),
                        record.getNoiseProbability(),
                        record.getRunNumber(),
                        record.getSeed(),
                        record.getNSifted(),
                        record.getSiftedPercent(),
                        record.getNTest(),
                        record.getTestErrors(),
                        Double.isFinite(record.getQberPercent())
                                ? String.format(java.util.Locale.US, "%.4f", record.getQberPercent()) : "N/A",
                        record.getCandidateBits(),
                        record.getDecision(),
                        record.getDurationNs() > 0 ? Long.toString(record.getDurationNs()) : "N/A"
                ));
                writer.newLine();
            }
        }

        return path;
    }
}
