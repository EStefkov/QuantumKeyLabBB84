package simulation;

public class SimulationConfig {

    private final int nSent;
    private final double eveProbability;
    private final double noiseProbability;
    private final long seed;

    public SimulationConfig(int nSent, double eveProbability, double noiseProbability, long seed) {
        if(nSent <= 0) {
            throw new IllegalArgumentException("n Трябва да е положително число");
        }
        if (!Double.isFinite(eveProbability)
                || eveProbability < 0 || eveProbability > 1) {
            throw new IllegalArgumentException("eveProbability трябва да е число между 0 и 1");
        }
        if (!Double.isFinite(noiseProbability)
                || noiseProbability < 0 || noiseProbability > 1) {
            throw new IllegalArgumentException("noiseProbability трябва да е число между 0 и 1");
        }
        this.nSent = nSent;
        this.eveProbability = eveProbability;
        this.noiseProbability = noiseProbability;
        this.seed = seed;
    }

    public int getnSent() {
        return nSent;
    }

    public double getEveProbability() {
        return eveProbability;
    }

    public double getNoiseProbability() {
        return noiseProbability;
    }

    public long getSeed() {
        return seed;
    }

}
