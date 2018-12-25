package markov;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class MarkovChain {
    private Map<MarkovKey, MarkovMatrixRow<String>> transitionMatrix;
    private MarkovMatrixRow<MarkovKey> initialDist;
    private Set<String> wordsSeen;
    private int order;
    private boolean capitalizeNext;
    private Random random;
    private long ID;

    private static final String[] PUNCT_PAUSE = {",", ";", ":", "(", ")", "\""};
    private static final String[] PUNCT_END = {".", "!", "?"};
    private static final int GENERATION_LENGTH_MIN = 5;
    private static final int GENERATION_LENGTH_VARIANCE = 5;

    public MarkovChain(int order, long ID) {
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

    public void readFile(String fileLocation) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(fileLocation)), StandardCharsets.UTF_8);
            readString(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readString(String s) {
        String[] cleanedString = clean(s);
        boolean isInitial = true;
        for (int pos = 0; pos < cleanedString.length - order; pos++) {
            MarkovKey key = new MarkovKey(Arrays.copyOfRange(cleanedString, pos, pos + order));

            wordsSeen.addAll(Arrays.asList(key.getWords()));

            if (isInitial && !isPunct(key.getWords()[0])) {
                initialDist.add(key);
                isInitial = false;
            }


            if (!transitionMatrix.containsKey(key)) {
                MarkovMatrixRow<String> row = new MarkovMatrixRow();
                String next = cleanedString[pos + order];
                row.add(next);
                transitionMatrix.put(key, row);
            } else {
                MarkovMatrixRow<String> row = transitionMatrix.get(key);
                String next = cleanedString[pos + order];
                row.add(next);
            }

            if (isEndPunct(key.getWords()[order - 1])) {
                isInitial = true;
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
                .split("\\p{javaWhitespace}+|(?=[\\p{P}&&[^'-]])|(?<=[\\p{P}&&[^'-]])");

    }

    public String simulate() {
        int length = randLength();
        MarkovKey start = randStart();

        return simulate(length, start);
    }

    public String simulate(int length) {
        MarkovKey start = randStart();
        return simulate(length, start);
    }

    public String simulate(String s) {
        int length = randLength();
        MarkovKey start = getStartFromString(s);
        return simulate(length, start);
    }

    private MarkovKey getStartFromString(String s) {
        List<MarkovKey> keyList = new ArrayList();
        s = s.toLowerCase();

        for (MarkovKey k: getTransitionMatrix().keySet()) {
            if(k.getFirst().equals(s)) {
                keyList.add(k);
            }
        }

        if (keyList.size() == 0) {
            throw new IllegalArgumentException("Not enough data for starting string. Please try something else!");
        } else {
            return keyList.get(random.nextInt(keyList.size()));
        }
    }

    private MarkovKey randStart() {
        return initialDist.nextKey();
    }

    private int randLength() {
        return random.nextInt(GENERATION_LENGTH_VARIANCE) + GENERATION_LENGTH_MIN;
    }


    public String simulate(int length, MarkovKey initial) {
        if (length > initial.getOrder()) {
            MarkovKey curr = initial;
            StringBuilder sb = new StringBuilder(keyToStringHelper(initial, length));
            String next = "";
            while (length - curr.getOrder() > 0 ||
                    (length - curr.getOrder() <= 0&& !isEndPunct(next))) {
                MarkovMatrixRow<String> nextRow = getTransitionMatrix().get(curr);
                if (nextRow == null) {
                    break;
                }

                next = nextRow.nextKey();
                sb.append(grammarHelper(next));
                curr = nextKeyHelper(curr, next);
                length--;
            }

            return sb.toString();
        } else {
            throw new IllegalArgumentException("Length too small!");
        }
    }

    private String grammarHelper(String s) {
        s = capitalizeNext ? capitalize(s) : s;
        if (isEndPunct(s)) {
            capitalizeNext = true;
        } else if (isPausePunct(s)) {
            capitalizeNext = false;
        } else {
            s = " " + s;
            capitalizeNext = false;
        }

        return s;
    }

    private String keyToStringHelper(MarkovKey k, int length) {
        capitalizeNext = true;
        String s[] = k.getWords();
        StringBuilder sb = new StringBuilder();

        String next;
        int count = 0;
        while (count < Math.min(length, s.length)) {
            next = s[count];
            sb.append(grammarHelper(next));
            count++;
        }

        return sb.toString();
    }

    private MarkovKey nextKeyHelper(MarkovKey k, String next) {
        if (k.getOrder() == 1) {
            return new MarkovKey(new String[]{next});
        } else {
            String[] newArray = new String[order];
            System.arraycopy(k.getWords(), 1, newArray, 0, order - 1);
            newArray[order-1] = next;
            return new MarkovKey(newArray);
        }
    }

    private String capitalize(String s) {
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase()
                    + s.substring(1);
        } else {
            return s.toUpperCase();
        }
    }


    private boolean isEndPunct(String s) {
        return ArrayUtils.contains(PUNCT_END, s);
    }

    private boolean isPausePunct(String s) {
        return ArrayUtils.contains(PUNCT_PAUSE, s);
    }

    private boolean isPunct(String s) {
        return isEndPunct(s) || isPausePunct(s);
    }


    public Map<MarkovKey, MarkovMatrixRow<String>> getTransitionMatrix() {
        return transitionMatrix;
    }

    public long getID() {
        return ID;
    }
}


