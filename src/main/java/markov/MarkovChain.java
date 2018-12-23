package markov;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;


public class MarkovChain{
    private Map<String, MarkovMatrixRow> transitionMatrix;
    private Random rand;
    private long ID;

    private static final int GENERATION_MIN_LENGTH = 100;
    private static final int GENERATION_MAX_LENGTH = 15;

    public MarkovChain(long ID) {
        transitionMatrix = new HashMap<>();
        rand = new Random();
        this.ID = ID;
    }

    public void add(String s) {
        String[] cleanedString = clean(s);
        for (int pos = 0; pos < cleanedString.length - 1; pos++) {
            String word = cleanedString[pos];
            if (!transitionMatrix.containsKey(word)) {
                MarkovMatrixRow row = new MarkovMatrixRow();
                String next = cleanedString[pos + 1];
                row.add(next);
                transitionMatrix.put(word, row);
            } else {
                MarkovMatrixRow row = transitionMatrix.get(word);
                String next = cleanedString[pos + 1];
                row.add(next);
            }
        }
    }


    /* Removes capital letters from beginning of sentence
     * and splits into words and punctuation.
     */
    private String[] clean(String m) {
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

    public String simulate() {
        int length = randLength();
        String start = randStart();

        return simulate(length, start);
    }

    public String simulate(int length) {
        String start = randStart();
        return simulate(length, start);
    }

    public String simulate(String start) {
        int length = randLength();
        return simulate(length, start);
    }

    public String simulate(int length, String start) {
        int count = 1;
        StringBuilder s = new StringBuilder(start.substring(0, 1).toUpperCase()
                + start.substring(1)); //StringBuilder beginning with capitalized starting string
        String next = getTransitionMatrix().get(start).nextWord();

        while (count < length && next != null) {
            if (next.equals(".") || next.equals("?") || next.equals(";")
                    || next.equals("!") || next.equals(",")) {
                s.append(next);
            } else {
                s.append(" " + next);
            }


            MarkovMatrixRow row = getTransitionMatrix().get(next);
            if (row != null) {
                next = row.nextWord();
            } else {
                next = null;
            }
            count++;
        }
        return s.toString();
    }

    private String randStart() {
        List<String> keyList = new ArrayList();
        keyList.addAll(getTransitionMatrix().keySet());

        return keyList.get(rand.nextInt(keyList.size()));
    }

    private int randLength() {
        return rand.nextInt(GENERATION_MAX_LENGTH) + GENERATION_MIN_LENGTH;
    }


    public Map<String, MarkovMatrixRow> getTransitionMatrix() {
        return transitionMatrix;
    }

    public long getID() {
        return ID;
    }
}


