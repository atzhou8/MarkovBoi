package markov;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Deque;

public class MarkovKey implements Serializable {
    private final Deque<String> words;
    private static final long serialVersionUID = 1L;

    public MarkovKey(Deque words) {
        this.words = words;
    }

    public int getOrder() {
        return words.size();
    }

    public Deque<String> getWords() {
        return words;
    }

    /* Exposes the first word of the key */
    public String getFirst() {
        return words.getFirst();
    }

    /* Exposes last word of the key */
    public String getLast() {
        return words.getLast();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        MarkovKey markovKey = (MarkovKey) obj;
        if (markovKey.getOrder() != getOrder()) {
            return false;
        }

        String[] arr1 = new String[getOrder()];
        String[] arr2 = new String[getOrder()];
        this.getWords().toArray(arr1);
        markovKey.getWords().toArray(arr2);

        return Arrays.equals(arr1, arr2);
    }

    @Override
    public int hashCode() {
        int c = 0;
        for (String s: words) {
            c += s.hashCode();
        }

        return c + 13 * 37;
    }
}
