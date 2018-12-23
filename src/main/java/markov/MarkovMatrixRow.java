package markov;

import java.util.*;

public class MarkovMatrixRow {
    private Map<String, Integer> samples;
    private Map<String, Double> probabilities;
    private Random random = new Random();
    private static final String TOTAL_KEY = "";


    public MarkovMatrixRow(){
        samples = new HashMap<>();
        probabilities = new HashMap<>();
        samples.put(TOTAL_KEY, 0);
    }

    public void add(String string) {
        /* update samples */
        samples.replace(TOTAL_KEY, numSamples() + 1);
        if (samples.containsKey(string)) {
            samples.replace(string, getNextWordSamples(string) + 1);
        } else {
            samples.put(string, 1);
        }

        /* update probabilities based on samples */
        probabilities.put(string, (double) samples.get(string) / numSamples());
        updateProbabilities();
    }

    public void updateProbabilities() {
        for (String s : keySet()) {
            probabilities.put(s, (double) getNextWordSamples(s) / numSamples());
        }
    }

    public String nextWord() {
        Map<String, Double> ranges = uniformRanges();
        double r = random.nextDouble();


        String curr = null;
        for (String s: keySet()) {
            curr = s;
            if (ranges.get(curr) >= r)
                break;
        }
        return curr;
    }

    /* sums up the next state probabilities so that we can simulate using the uniform dist. */
    private Map<String, Double> uniformRanges() {
        Map<String, Double> uProbabilities = new TreeMap<>();
        double cumulation = 0;
        for (String s : keySet()) {
            cumulation += getNextWordProbabilities(s);
            uProbabilities.put(s, cumulation);
        }

        return uProbabilities;
    }

    public boolean contains(String string) {
        return samples.containsKey(string);
    }

    public int getNextWordSamples(String string) {
        return samples.get(string);
    }

    public double getNextWordProbabilities(String string) {
        return probabilities.get(string);
    }

    public Set<String> keySet() {
        return probabilities.keySet();
    }

    public int numStates() {
        return probabilities.size() - 1;
    }

    public int numSamples() {
        return samples.get(TOTAL_KEY);
    }


}
