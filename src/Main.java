import experiments.CsvExporter;
import experiments.ExperimentRunner;

public class Main {

    public static void main(String[] args) throws Exception {

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
