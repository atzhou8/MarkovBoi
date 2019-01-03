package markov;

import java.io.Serializable;
import java.util.*;

public class MarkovMatrixRow<K> implements Serializable {
    private Map<K, Integer> counts;
    private Map<K, Double> probabilities;
    private Random random = new Random();
    private int totalSamples;

    private static final long serialVersionUID = 2L;

    public MarkovMatrixRow() {
        counts = new HashMap<>();
        probabilities = new HashMap<>();
        totalSamples = 0;
    }

    public void add (K key, int count) {
        /* update counts */
        totalSamples += count;
        /* adds the key */
        if (counts.containsKey(key)) {
            counts.replace(key, getKeyCount(key) + count);
        } else {
            counts.put(key, count);
        }
    }

    public void add(K key) {
        add(key, 1);
    }

    public void updateProbabilities() {
        for (K key : keySet()) {
            probabilities.put(key, (double) getKeyCount(key) / getTotalSamples());
        }
    }

    public K nextKey() {
        updateProbabilities();
        Map<K, Double> ranges = uniformRanges();
        double r = random.nextDouble();

        K curr = null;
        for (K entry : keySet()) {
            curr = entry;
            if (ranges.get(curr) >= r) {
                break;
            }
        }
        return curr;
    }

    /* Sums up the next state probabilities so that we can simulate using the uniform dist.
     * from Random.nextDouble */
    private Map<K, Double> uniformRanges() {
        Map<K, Double> uProbabilities = new HashMap<>();
        double cumulation = 0;
        for (K key : keySet()) {
            cumulation += getKeyProbability(key);
            uProbabilities.put(key, cumulation);
        }

        return uProbabilities;
    }

    public boolean contains(K key) {
        return counts.containsKey(key);
    }

    public int getKeyCount(K key) {
        return counts.get(key);
    }

    public double getKeyProbability(K key) {
        return probabilities.get(key);
    }

    public Set<K> keySet() {
        return counts.keySet();
    }

    public int getTotalSamples() {
        return totalSamples;
    }
}
