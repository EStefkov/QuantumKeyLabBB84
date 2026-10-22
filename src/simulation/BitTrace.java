
package simulation;

public class BitTrace {

    private final int index;

    private final int aliceBit;
    private final String aliceBasis;

    private final boolean eveIntercepted;
    private final String eveBasis;
    private final Integer eveBit;

    private final String bobBasis;
    private final int bobBit;

    private final boolean basesMatched;
    private final boolean error;

    private boolean selectedForTest;
    private boolean candidateBit;

    public BitTrace(
            int index,
            int aliceBit,
            String aliceBasis,
            boolean eveIntercepted,
            String eveBasis,
            Integer eveBit,
            String bobBasis,
            int bobBit,
            boolean basesMatched,
            boolean error
    ) {
        this.index = index;
        this.aliceBit = aliceBit;
        this.aliceBasis = aliceBasis;
        this.eveIntercepted = eveIntercepted;
        this.eveBasis = eveBasis;
        this.eveBit = eveBit;
        this.bobBasis = bobBasis;
        this.bobBit = bobBit;
        this.basesMatched = basesMatched;
        this.error = error;
    }

    public int getIndex() {
        return index;
    }

    public int getAliceBit() {
        return aliceBit;
    }

    public String getAliceBasis() {
        return aliceBasis;
    }

    public boolean isEveIntercepted() {
        return eveIntercepted;
    }

    public String getEveBasis() {
        return eveBasis;
    }

    public Integer getEveBit() {
        return eveBit;
    }

    public String getBobBasis() {
        return bobBasis;
    }

    public int getBobBit() {
        return bobBit;
    }

    public boolean isBasesMatched() {
        return basesMatched;
    }

    public boolean isError() {
        return error;
    }

    public boolean isSelectedForTest() {
        return selectedForTest;
    }

    public void setSelectedForTest(boolean selectedForTest) {
        this.selectedForTest = selectedForTest;
    }

    public boolean isCandidateBit() {
        return candidateBit;
    }

    public void setCandidateBit(boolean candidateBit) {
        this.candidateBit = candidateBit;
    }
}
