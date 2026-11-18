import experiments.CsvExporter;
import experiments.ExperimentRunner;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            javax.swing.SwingUtilities.invokeLater(() -> new ui.QuantumKeyLab().setVisible(true));
            return;
        }
        if (!"--batch".equals(args[0])) {
            throw new IllegalArgumentException("Usage: Main [--batch]");
        }
        ExperimentRunner runner = new ExperimentRunner();

        runner.runAll();

        CsvExporter exporter = new CsvExporter();

        var path = exporter.exportRuns(
                runner.getRunRecords(),
                "bb84_experiment_runs.csv"
        );

        System.out.println();
        System.out.println("CSV файлът е записан в:");
        System.out.println(path);
    }
}
