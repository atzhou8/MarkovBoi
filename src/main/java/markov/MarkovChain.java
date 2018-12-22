package markov;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MarkovChain{
    private int size;
    private Map<String, Map<String, Integer>> transitionMatrix;
    private Random rand;
    private long ID;

    private static final String SAMPLE_SIZE_KEY = "";

    public MarkovChain(long ID) {
        size = 0;
        transitionMatrix = new HashMap<>();
        this.ID = ID;
    }

    public void add(String s) {
        String[] cleanedString = clean(s);
        for (int pos = 0; pos < cleanedString.length - 1; pos++) {
            String word = cleanedString[0];
            if (!transitionMatrix.containsKey(word)) {
                Map<String, Integer> list = new HashMap<>();
                String next = cleanedString[pos + 1];

                list.put(SAMPLE_SIZE_KEY, 1);
                list.put(next, 1);

                transitionMatrix.put(word, list);
            } else {
                Map<String, Integer> list = transitionMatrix.get(word);
                String next = cleanedString[pos + 1];

                list.replace(SAMPLE_SIZE_KEY, list.get(SAMPLE_SIZE_KEY) + 1);
                if (list.containsKey(next)) {
                    list.replace(next, list.get(next) + 1);
                } else {
                    list.put(next, 1);
                }
            }

        }

    }

    /* Removes capital letters from beginning of sentence
     * and splits into words and punctuation.
     */
    public String[] clean(String m) {
        StringBuilder s = new StringBuilder(m);
        boolean isLowerCase = true;
        char[] punctuation = {'!', '.', '?'};

        for (int i = 0; i < s.length(); i++) {
            if (ArrayUtils.contains(punctuation, s.charAt(i))) {
                isLowerCase = true;
            } else if(isLowerCase && s.charAt(i) != ' ') {
                s.setCharAt(i, Character.toLowerCase(s.charAt(i)));
                isLowerCase = false;
            }
        }

        return s.toString()
                .split("\\p{javaWhitespace}+|(?=[\\p{P}&&[^']])|(?<=[\\p{P}&&[^']])");

    }

}


