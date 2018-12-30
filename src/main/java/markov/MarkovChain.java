package markov;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class MarkovChain implements Serializable {
    private Map<MarkovKey, MarkovMatrixRow<String>> transitionMatrix;
    private MarkovMatrixRow<MarkovKey> initialDist;
    private Set<String> wordsSeen;
    private int order;
    private boolean capitalizeNext;
    private Random random;
    private String ID;

    private static final int GENERATION_LENGTH_MIN = 8;
    private static final int GENERATION_LENGTH_VARIANCE = 3;
    private static final long serialVersionUID = 123123123123123123L;

    public MarkovChain(int order, String ID) {
        if (order < 1) {
            throw new IllegalArgumentException("Markov chain must be of at least order one!");
        }

        transitionMatrix = new HashMap<>();
        wordsSeen = new HashSet<>();
        initialDist = new MarkovMatrixRow<>();
        capitalizeNext = true;
        this.order = order;
        this.ID = ID;

        random = new Random();
    }

    /* Reads a file and adds data to chain */
    public void readFile(String fileLocation) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(fileLocation)), StandardCharsets.UTF_8);
            readString(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Reads a string and adds data to chain */
    public void readString(String s) {
        List<String> cleanedString = MarkovUtils.clean(s);

        if (cleanedString.size() <= order) {
            return;
        }

        Deque<String> curr = new ArrayDeque();
        boolean isInitial = true;
        curr.addAll(cleanedString.subList(0, order));
        MarkovKey key;
        String next;

        for (int pos = 0; pos < cleanedString.size() - order; pos++) {
            key = new MarkovKey(((ArrayDeque<String>) curr).clone());
            next = cleanedString.get(pos + order);

            if (isInitial && !MarkovUtils.isPunct(key.getFirst())) {
                initialDist.add(key);
                isInitial = false;
            }

            if (MarkovUtils.isEndPunct(key.getLast())) {
                isInitial = true;
            }


            addToMatrix(key, next);
            curr.add(next);
            curr.removeFirst();
        }
    }

    /* Helper method to data to the transition matrix*/
    private void addToMatrix(MarkovKey key, String next) {
        if (!transitionMatrix.containsKey(key)) {
            MarkovMatrixRow<String> row = new MarkovMatrixRow();
            row.add(next);
            transitionMatrix.put(key, row);
        } else {
            MarkovMatrixRow<String> row = transitionMatrix.get(key);
            row.add(next);
        }
    }


    /* Simulates words from chain */
    public String simulate() {
        int length = randLength();
        MarkovKey start = randStart();

        return simulate(length, start);
    }

    /* Simulates words from chain given a minimum length. */
    public String simulate(int length) {
        MarkovKey start = randStart();
        return simulate(length, start);
    }

    /* Simulates words from chain given a starting word. */
    public String simulate(String s) {
        if (s.equals("") || s.equals(null)) {
            return simulate();
        } else {
            MarkovKey start = MarkovUtils.getStartFromString(this, s);
            int length = randLength();
            return simulate(length, start);
        }
    }

    /* Simulate words given minimum length and initial distribution */
    private String simulate(int length, MarkovKey initial) {
        setCapitalizeNext(true);
        if (length > initial.getOrder()) {
            ArrayDeque initialDeque = (ArrayDeque) initial.getWords();
            MarkovKey curr = new MarkovKey(initialDeque.clone());
            StringBuilder sb = new StringBuilder(MarkovUtils.keyToStringHelper(this, initial, length));
            String next = "";

            while (length - curr.getOrder() > 0 ||
                    (length - curr.getOrder() <= 0 && !MarkovUtils.isEndPunct(next))) {
                MarkovMatrixRow<String> nextRow = getTransitionMatrix().get(curr);
                /* ends string construction upon reaching an end state or loop */
                if (nextRow == null) {
                    break;
                }
                if (length < -20 && !MarkovUtils.isEndPunct(next)) {
                    sb.append(".");
                    break;
                }

                next = nextRow.nextKey();
                sb.append(MarkovUtils.grammarHelper(this, next));
                curr = MarkovUtils.nextKeyHelper(curr, next);
                length--;
            }

            return sb.toString();
        } else {
            throw new IllegalArgumentException("Length too small!");
        }
    }

    /* Randomly picks a state from the initial distribution*/
    private MarkovKey randStart() {
        return initialDist.nextKey();
    }

    /* Randomly gets a minimum length */
    private int randLength() {
        return random.nextInt(GENERATION_LENGTH_VARIANCE) + GENERATION_LENGTH_MIN;
    }

    /* Returns transition matrix*/
    public Map<MarkovKey, MarkovMatrixRow<String>> getTransitionMatrix() {
        return transitionMatrix;
    }

    /* Returns id */
    public String getID() {
        return ID;
    }

    public Set<MarkovKey> getAllKeys() {
        return getTransitionMatrix().keySet();
    }

    public void setCapitalizeNext(boolean capitalizeNext) {
        this.capitalizeNext = capitalizeNext;
    }

    public boolean getCapitalizeNext() {
        return capitalizeNext;
    }
}
