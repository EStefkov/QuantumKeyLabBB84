package experiments;

/** Required measurement grid; callers receive copies of the parameter arrays. */
public final class ExperimentPlan {
    public static final int RUNS_PER_COMBINATION = 100;
    public static final int WARMUP_RUNS = 100;

    private ExperimentPlan() { }

    public static int[] sizes() { return new int[]{200, 1000, 5000}; }
    public static double[] eveProbabilities() { return new double[]{0, .25, .5, 1}; }
    public static double[] noiseProbabilities() { return new double[]{0, .01, .03}; }
}
