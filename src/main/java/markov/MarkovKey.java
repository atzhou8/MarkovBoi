package markov;

import java.util.Arrays;

public class MarkovKey {
    private final String[] words;

    public MarkovKey(String[] words) {
        this.words = words;
    }

    public int getOrder() {
        return words.length;
    }

    public String[] getWords() {
        return words;
    }

    /* exposes the first word of the key */
    public String getFirst() {
        return words[0];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        MarkovKey markovKey = (MarkovKey) obj;
        return Arrays.equals(this.getWords(), markovKey.getWords());
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
