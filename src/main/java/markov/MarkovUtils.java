package markov;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class MarkovUtils {
    private static Random random = new Random();

    private static final String[] PUNCT_PAUSE = {",", ";", ":", "(", ")", "\""};
    private static final String[] PUNCT_END = {".", "!", "?"};

    /* Removes capital letters from beginning of sentence
     * and splits into words and punctuation.
     */
    public static List<String> clean(String string) {
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

    public static String removeUrl(String str) {
        String regularExpression = "(((http|ftp|https):\\/\\/)?[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?)";
        return str.replaceAll(regularExpression, "");
    }

    /* Checks if a string is a punctuation mark that ends a sentence */
    public static boolean isEndPunct(String s) {
        return ArrayUtils.contains(PUNCT_END, s);
    }

    /* Checks if a string is a punctuation mark that does not end a sentence. */
    public static boolean isPausePunct(String s) {
        return ArrayUtils.contains(PUNCT_PAUSE, s);
    }

    /* Checks if a string is a punctuation mark */
    public static boolean isPunct(String s) {
        return isEndPunct(s) || isPausePunct(s);
    }

    /* Helper method to find an initial state given a starting word*/
    public static MarkovKey getStartFromString(MarkovChain mc, String s) {
        List<MarkovKey> keyList = new ArrayList();
        s = s.toLowerCase();

        for (MarkovKey k: mc.getAllKeys()) {
            if (k.getFirst().equals(s)) {
                keyList.add(k);
            }
        }

        if (keyList.size() == 0) {
            throw new IllegalArgumentException("Not enough data for starting string. Please try something else!");
        } else {
            return keyList.get(random.nextInt(keyList.size()));
        }
    }

    /* Helper method for correct grammar during sentence construction */
    public static String grammarHelper(MarkovChain mc, String s) {
        s = mc.getCapitalizeNext() ? capitalize(s) : s;
        if (isEndPunct(s)) {
            mc.setCapitalizeNext(true);
        } else if (isPausePunct(s)) {
            mc.setCapitalizeNext(false);
        } else {
            s = " " + s;
            mc.setCapitalizeNext(false);
        }

        return s;
    }

    /* Helper method to convert MarkovKey to a string */
    public static String keyToStringHelper(MarkovChain mc, MarkovKey k, int length) {
        String[] s = new String[k.getOrder()];
        k.getWords().toArray(s);
        StringBuilder sb = new StringBuilder();

        String next;
        int count = 0;
        while (count < Math.min(length, s.length)) {
            next = s[count];
            sb.append(grammarHelper(mc, next));
            count++;
        }

        return sb.toString();
    }

    /* Gets the next key to use during sentence construction */
    public static MarkovKey nextKeyHelper(MarkovKey k, String next) {
        k.getWords().addLast(next);
        k.getWords().removeFirst();

        return k;
    }

    /* Capitalizes first letter of a string */
    public static String capitalize(String s) {
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase()
                    + s.substring(1);
        } else {
            return s.toUpperCase();
        }
    }

    /* Combines the data from two Markov Chains */
    public static MarkovChain combineChains(MarkovChain mc1, MarkovChain mc2) {
        if (mc1.getOrder() == mc2.getOrder()) {
            MarkovChain minMC = (mc1.getSize() < mc2.getSize()) ? mc1 : mc2;
            MarkovChain maxMC = (minMC == mc1) ? mc2 : mc1;

            for (MarkovKey key : minMC.getAllKeys()) {
                MarkovMatrixRow<String> maxCounts = maxMC.getTransitionMatrix().get(key);
                MarkovMatrixRow<String> minCounts = minMC.getTransitionMatrix().get(key);

                if (maxMC.containsKey(key)) {
                    for (String s : minCounts.keySet()) {
                        maxCounts.add(s, minCounts.getKeyCount(s));
                    }
                } else {
                    maxMC.addRowToMatrix(key, minCounts);
                }
            }

            return maxMC;
        } else {
            throw new IllegalArgumentException("Markov chains must be of the same order!");
        }
    }
}
