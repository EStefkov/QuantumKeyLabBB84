
package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationResult {

    private final int nSent;
    private final int nSifted;
    private final int nTest;
    private final int testErrors;
    private final double qberPercent;
    private final double siftedPercent;
    private final int candidateBits;
    private final String decision;
    private final List<BitTrace> trace;

    public SimulationResult(
            int nSent,
            int nSifted,
            int nTest,
            int testErrors,
            double qberPercent,
            double siftedPercent,
            int candidateBits,
            String decision
    ) {
        this(
                nSent, nSifted, nTest, testErrors,
                qberPercent, siftedPercent, candidateBits,
                decision, Collections.emptyList()
        );
    }

    public SimulationResult(
            int nSent,
            int nSifted,
            int nTest,
            int testErrors,
            double qberPercent,
            double siftedPercent,
            int candidateBits,
            String decision,
            List<BitTrace> trace
    ) {
        this.nSent = nSent;
        this.nSifted = nSifted;
        this.nTest = nTest;
        this.testErrors = testErrors;
        this.qberPercent = qberPercent;
        this.siftedPercent = siftedPercent;
        this.candidateBits = candidateBits;
        this.decision = decision;

        this.trace = Collections.unmodifiableList(
                new ArrayList<>(trace)
        );
    }

    public int getNSent() {
        return nSent;
    }

    public int getNSifted() {
        return nSifted;
    }

    public int getNTest() {
        return nTest;
    }

    public int getTestErrors() {
        return testErrors;
    }

    public double getQberPercent() {
        return qberPercent;
    }

    public double getSiftedPercent() {
        return siftedPercent;
    }

    public int getCandidateBits() {
        return candidateBits;
    }

    public String getDecision() {
        return decision;
    }

    public List<BitTrace> getTrace() {
        return trace;
    }
}
