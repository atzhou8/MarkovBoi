package markov;

import java.util.*;

public class MarkovMatrixRow<K> {
    private Map<K, Integer> counts;
    private Map<K, Double> probabilities;
    private Random random = new Random();
    private int totalSamples;


    public MarkovMatrixRow(){
        counts = new HashMap<>();
        probabilities = new HashMap<>();
        totalSamples = 0;
    }

    public void add(K key) {
        /* update counts */
        totalSamples++;
        if (counts.containsKey(key)) {
            counts.replace(key, getNextWordSamples(key) + 1);
        } else {
            counts.put(key, 1);
        }

        /* update probabilities based on counts */
        probabilities.put(key, (double) counts.get(key) / getTotalSamples());
        updateProbabilities();
    }

    public void updateProbabilities() {
        for (K key : keySet()) {
            probabilities.put(key, (double) getNextWordSamples(key) / getTotalSamples());
        }
    }

    public K nextKey() {
        Map<K, Double> ranges = uniformRanges();
        double r = random.nextDouble();


        K curr = null;
        for (K entry : keySet()) {
            curr = entry;
            if (ranges.get(curr) >= r)
                break;
        }
        return curr;
    }

    /* sums up the next state probabilities so that we can simulate using the uniform dist. */
    private Map<K, Double> uniformRanges() {
        Map<K, Double> uProbabilities = new HashMap<>();
        double cumulation = 0;
        for (K key : keySet()) {
            cumulation += getNextWordProbabilities(key);
            uProbabilities.put(key, cumulation);
        }

        return uProbabilities;
    }

    public boolean contains(K key) {
        return counts.containsKey(key);
    }

    public int getNextWordSamples(K key) {
        return counts.get(key);
    }

    public double getNextWordProbabilities(K key) {
        return probabilities.get(key);
    }

    public Set<K> keySet() {
        return probabilities.keySet();
    }

    public int getTotalStates() {
        return probabilities.size() - 1;
    }

    public int getTotalSamples() {
        return totalSamples;
    }
}
