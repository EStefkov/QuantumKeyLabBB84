
package experiments;

import simulation.SimulationResult;

public class ExperimentRunRecord {

    private final int nSent;
    private final double eveProbability;
    private final double noiseProbability;
    private final int runNumber;
    private final long seed;
    private final int nSifted;
    private final double siftedPercent;
    private final int nTest;
    private final int testErrors;
    private final double qberPercent;
    private final int candidateBits;
    private final String decision;
    private final long durationNs;

    public ExperimentRunRecord(
            int nSent,
            double eveProbability,
            double noiseProbability,
            int runNumber,
            long seed,
            SimulationResult result,
            long durationNs
    ) {
        this.nSent = nSent;
        this.eveProbability = eveProbability;
        this.noiseProbability = noiseProbability;
        this.runNumber = runNumber;
        this.seed = seed;
        this.nSifted = result.getNSifted();
        this.siftedPercent = result.getSiftedPercent();
        this.nTest = result.getNTest();
        this.testErrors = result.getTestErrors();
        this.qberPercent = result.getQberPercent();
        this.candidateBits = result.getCandidateBits();
        this.decision = result.getDecision();
        this.durationNs = durationNs;
    }

    public int getNSent() { return nSent; }
    public double getEveProbability() { return eveProbability; }
    public double getNoiseProbability() { return noiseProbability; }
    public int getRunNumber() { return runNumber; }
    public long getSeed() { return seed; }
    public int getNSifted() { return nSifted; }
    public double getSiftedPercent() { return siftedPercent; }
    public int getNTest() { return nTest; }
    public int getTestErrors() { return testErrors; }
    public double getQberPercent() { return qberPercent; }
    public int getCandidateBits() { return candidateBits; }
    public String getDecision() { return decision; }
    public long getDurationNs() { return durationNs; }
}
