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
        return str.replaceAll(regularExpression, "").replaceAll("\"", "");
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

    /* Helper method to convert state to a string */
    public static String stateToStringHelper(MarkovChain mc, Deque<String> s) {
        String[] str = new String[2];
        s.toArray(str);

        StringBuilder sb = new StringBuilder();
        String next;
        int count = 0;
        while (count < Math.min(2, str.length)) {
            next = str[count];
            sb.append(grammarHelper(mc, next));
            count++;
        }

        return sb.toString();
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

}
