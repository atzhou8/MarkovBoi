package markov;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MarkovChain implements Serializable {
    private Map<MarkovKey, MarkovMatrixRow<String>> transitionMatrix;
    private MarkovMatrixRow<MarkovKey> initialDist;
    private Set<String> wordsSeen;
    private int order;
    private boolean capitalizeNext;
    private Random random;
    private String ID;

    private static final String[] PUNCT_PAUSE = {",", ";", ":", "(", ")", "\""};
    private static final String[] PUNCT_END = {".", "!", "?"};
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
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileLocation));
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                readString(line);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Reads a string and adds data to chain */
    public void readString(String s) {
        List<String> cleanedString = clean(s);

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

            if (isInitial && !isPunct(key.getFirst())) {
                initialDist.add(key);
                isInitial = false;
            }

            if (isEndPunct(key.getLast())) {
                isInitial = true;
            }


            addKey(key, next);
            curr.add(next);
            curr.removeFirst();
        }
    }

    private void addKey(MarkovKey key, String next) {
        if (!transitionMatrix.containsKey(key)) {
            MarkovMatrixRow<String> row = new MarkovMatrixRow();
            row.add(next);
            transitionMatrix.put(key, row);
        } else {
            MarkovMatrixRow<String> row = transitionMatrix.get(key);
            row.add(next);
        }
    }


    /* Removes capital letters from beginning of sentence
     * and splits into words and punctuation.
     */
    public List<String> clean(String string) {
        string = removeUrl(string);
        StringTokenizer st = new StringTokenizer(string,
                "\r\n,.!;()\\?: ", true);

        int total = st.countTokens();
        List<String> cleaned = new ArrayList<>();
        boolean isInitial = true;

        for (int i = 0; i < total; i++) {
            String next = st.nextToken();
            if (!next.equals(" ")
                    && !next.equals("\n")
                    && !next.equals("\r")) {
                if (isEndPunct(next)) {
                    cleaned.add(next.toLowerCase());
                    isInitial = true;
                } else if (isInitial) {
                    cleaned.add(next.toLowerCase());
                    isInitial = false;
                } else {
                    cleaned.add(next);
                }
            }
        }

        return cleaned;
    }

    private String removeUrl(String commentstr)
    {
        String urlPattern = "(?:(?:http|https):\\/\\/)?([-a-zA-Z0-9.]{2,256}\\.[a-z]{2,4})\\b(?:\\/[-a-zA-Z0-9@:%_\\+.~#?&//=]*)?";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
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
        int length = randLength();
        MarkovKey start = getStartFromString(s);
        return simulate(length, start);
    }

    /* Simulate words given minimum length and initial distribution */
    private String simulate(int length, MarkovKey initial) {
        if (length > initial.getOrder()) {
            ArrayDeque initialDeque = (ArrayDeque) initial.getWords();
            MarkovKey curr = new MarkovKey(initialDeque.clone());
            StringBuilder sb = new StringBuilder(keyToStringHelper(initial, length));
            String next = "";

            while (length - curr.getOrder() > 0 ||
                    (length - curr.getOrder() <= 0 && !isEndPunct(next))) {
                MarkovMatrixRow<String> nextRow = getTransitionMatrix().get(curr);
                /* ends string construction upon reaching an end state or loop */
                if (nextRow == null) {
                    break;
                }
                if (length < -20 && !isEndPunct(next)) {
                    sb.append(".");
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

    /* Helper method to find an initial state given a starting word*/
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

    /* Randomly picks a state from the initial distribution*/
    private MarkovKey randStart() {
        return initialDist.nextKey();
    }

    /* Randomly gets a minimum length */
    private int randLength() {
        return random.nextInt(GENERATION_LENGTH_VARIANCE) + GENERATION_LENGTH_MIN;
    }

    /* Helper method for correct grammar during sentence construction */
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

    /* Helper method to convert MarkovKey to a string */
    private String keyToStringHelper(MarkovKey k, int length) {
        capitalizeNext = true;
        String[] s = new String[k.getOrder()];
        k.getWords().toArray(s);
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

    /* Gets the next key to use during sentence construction */
    private MarkovKey nextKeyHelper(MarkovKey k, String next) {
        k.getWords().addLast(next);
        k.getWords().removeFirst();

        return k;
    }

    /* Capitalizes first letter of a string */
    private String capitalize(String s) {
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase()
                    + s.substring(1);
        } else {
            return s.toUpperCase();
        }
    }

    /* Checks if a string is a punctuation mark that ends a sentence */
    private boolean isEndPunct(String s) {
        return ArrayUtils.contains(PUNCT_END, s);
    }

    /* Checks if a string is a punctuation mark that does not end a sentence. */
    private boolean isPausePunct(String s) {
        return ArrayUtils.contains(PUNCT_PAUSE, s);
    }

    /* Checks if a string is a punctuation mark */
    private boolean isPunct(String s) {
        return isEndPunct(s) || isPausePunct(s);
    }

    /* Returns transition matrix*/
    public Map<MarkovKey, MarkovMatrixRow<String>> getTransitionMatrix() {
        return transitionMatrix;
    }

    /* Returns id */
    public String getID() {
        return ID;
    }
}
