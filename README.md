# Quantum Key Lab: BB84 Simulation

A Java teaching simulator for the BB84 quantum key distribution protocol. It models Alice, Bob, optional intercept-and-resend eavesdropping by Eve, basis sifting, a public test sample, QBER evaluation, and repeated parameter experiments.

> This is an educational simulation of BB84 measurement rules. It does not send photons or implement a production cryptographic protocol.

## Requirements

- Java Development Kit (JDK) 11 or newer
- Python 3.9 or newer to regenerate the plots
- Python packages `pandas` and `matplotlib` to regenerate the plots

The Java simulator has no third-party Java dependencies. The repository does not currently pin Python package versions.

## Project layout

```text
src/
  Main.java                    Runs the batch experiment and exports its run records
  quantum/QuantumState.java    Four BB84 states in the Z and X bases
  simulation/                  Configuration, simulator, result, and bit trace
  experiments/                 Batch experiments, summaries, and CSV export
  test/java/BB84SimulatorTest.java  Dependency-free regression test runner
bb84_experiment_runs.csv       Per-run experiment data
test_journal.csv              Expected and observed results for the automated checks
BB84_Project_Report.docx       Three-page project report with methods, results, and limitations
visualize_results.py           Builds grouped statistics and PNG plots
bb84_graphs/                   Generated plots and grouped summary CSV
```

## Compile and run the experiments

Run these commands from the project root directory:

```bash
mkdir -p out
javac -d out \
  src/quantum/QuantumState.java \
  src/simulation/SimulationConfig.java \
  src/simulation/SimulationResult.java \
  src/simulation/BitTrace.java \
  src/simulation/BB84Simulator.java \
  src/experiments/ExperimentRunRecord.java \
  src/experiments/ExperimentSummary.java \
  src/experiments/ExperimentRunner.java \
  src/experiments/CsvExporter.java \
  src/Main.java
java -cp out Main
```

The batch runner performs 100 runs for each combination of:

- Transmissions `N`: 200, 1000, 5000
- Eve interception probability `f`: 0, 0.25, 0.5, 1
- Independent Bob bit-flip noise: 0, 0.01, 0.03

That produces 36 combinations and 3,600 runs. Each run uses a distinct seed. Running `Main` writes or replaces `bb84_experiment_runs.csv` in the current directory and prints combination-level statistics to the terminal.

## Run the automated checks

Compile the simulator and test runner, then run the test class:

```bash
javac -d out \
  src/quantum/QuantumState.java \
  src/simulation/SimulationConfig.java \
  src/simulation/SimulationResult.java \
  src/simulation/BitTrace.java \
  src/simulation/BB84Simulator.java \
  src/test/java/BB84SimulatorTest.java
java -cp out BB84SimulatorTest
```

The runner currently contains 18 checks for input validation, probability boundaries, repeatable traces, sifting, test sample rounding, candidate exclusion, ideal-channel QBER, certain-noise QBER, and insufficient data. Each run writes or replaces `test_journal.csv` in the current directory with the scenarios, expected results, observed results, and statuses. The runner prints one PASS line per check and exits with an error if a check fails. The tests use only the JDK and do not require JUnit.

## Regenerate graphs and summary

Install the plotting dependencies if needed:

```bash
python3 -m pip install pandas matplotlib
```

Then, from the project root:

```bash
python3 visualize_results.py
```

The script reads `bb84_experiment_runs.csv` and writes `bb84_graphs/bb84_summary.csv` plus three PNGs: mean QBER by Eve probability and noise, rejection rate by Eve probability, and mean execution time by transmission size. Execution time is measured per simulation run with Java's monotonic `System.nanoTime()` timer; the graph summarizes those recorded run times.

## Model and calculations

1. Alice independently chooses a random bit and a random Z or X basis for each transmission.
2. Eve intercepts each state with probability `f`. If she intercepts, she chooses a random basis, measures, and prepares a new state from her result and basis.
3. Bob chooses a random basis and measures. If his basis differs from the prepared state's basis, the result is an independent random bit.
4. The configured noise probability independently flips Bob's measured bit.
5. Alice and Bob keep positions where their bases match. The simulator randomly selects `ceil(20% of sifted bits)` as a test sample and compares those bits.
6. `QBER (%) = test errors / test sample size * 100`. The educational rule rejects a run when QBER is greater than 8%. Empty sifted material is marked `insufficient_data`; the result stores QBER as `NaN` when no test sample can be measured. The current CSV exporter writes this as `NaN`; the exam brief asks for non-applicable values to be labeled `N/A`.
7. Candidate material is `sifted bits - test bits`. Test bits are excluded from candidate positions in the detailed trace.

The CSV's `DurationNs` is the elapsed time for one simulator call, not a separately warmed-up benchmark. Use care when interpreting short-run timings.

## Output fields

`bb84_experiment_runs.csv` contains one row per run:

| Column | Meaning |
| --- | --- |
| `N` | Number of simulated transmissions |
| `EveProbability` | Probability that Eve intercepts a transmission |
| `NoiseProbability` | Probability of a Bob bit flip from the noise model |
| `RunNumber`, `Seed` | Run index within the combination and its reproducibility seed |
| `SiftedBits`, `SiftedPercent` | Count and percentage of matched-basis positions |
| `TestBits`, `TestErrors`, `QBERPercent` | Test sample size, observed errors, and QBER |
| `CandidateBits` | Sifted bits remaining after removing the test sample |
| `Decision` | `accept`, `reject`, or `insufficient_data` under the educational rule |
| `DurationNs` | Elapsed nanoseconds for the simulation call |

## Security and interpretation limits

- This model is for learning and does not prove security of a key.
- Candidate bits are not a finished key: the implementation does not perform error correction, privacy amplification, or authentication of the classical channel.
- The independent bit-flip noise model is simplified and does not represent every physical channel effect.
- A low QBER in a small test sample is not a precise estimate of the channel error rate.
- Seeds make the educational simulations repeatable; the seeded `java.util.Random` generator is not suitable for cryptographic secrets or secure key generation.

## Current project scope

The current source implements the command-line simulator, batch experiment workflow, dependency-free automated checks, a test journal, and a three-page project report. It does not yet include a graphical interface or presentation. These should be completed separately for the exam submission.
