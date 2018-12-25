package markov;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class TestMarkovChain {

    @Test
    public void testCleanString() {
        MarkovChain mc = new MarkovChain(1,0);

        String messageA = "Good morning John";
        String[] cleanedMessageA = {"good", "morning", "John"};

        String messageB = "It's Tuesday!";
        String[] cleanedMessageB = {"it's", "Tuesday", "!"};

        String messageC = "And today I would like to talk to you about China.";
        String[] cleanedMessageC = {"and", "today", "I", "would", "like", "to", "talk",
                                    "to", "you", "about", "China", "."};

        String messageD = "Didn't you know? Ophelia's favorite country is Great Britain.";
        String[] cleanedMessageD = {"didn't", "you", "know", "?", "ophelia's", "favorite", "country",
                                    "is", "Great", "Britain", "."};


        assertArrayEquals(cleanedMessageA, mc.clean(messageA));
        assertArrayEquals(cleanedMessageB, mc.clean(messageB));
        assertArrayEquals(cleanedMessageC, mc.clean(messageC));
        assertArrayEquals(cleanedMessageD, mc.clean(messageD));
    }


    @Test
    public void testAdd() {
        MarkovChain mc = new MarkovChain(1,0);
        mc.readString("Good morning John");
        Map<MarkovKey, MarkovMatrixRow<String>> tm = mc.getTransitionMatrix();

        for (MarkovKey key : tm.keySet()) {
            assertEquals(1, tm.get(key).getTotalSamples());
        }

        mc.readString("Good morning Hank");
        assertEquals(2, tm.get(new MarkovKey(new String[]{"good"})).getTotalSamples());
        assertEquals(1, tm.get(new MarkovKey(new String[]{"morning"})).getNextWordSamples("Hank"));
        assertEquals(0.5, tm.get(new MarkovKey(new String[]{"morning"})).getNextWordProbabilities("Hank"), 0);
        assertEquals(1, tm.get(new MarkovKey(new String[]{"morning"})).getNextWordSamples("John"));
    }

    @Test
    public void testSimulate() {
        MarkovChain mc = new MarkovChain(2,0);
        mc.readString("Good morning John");
//        assertEquals("Good morning John", mc.simulate(5, "good"));

        mc.readFile("plato.txt");
        mc.readFile("marx.txt");
        System.out.println(mc.simulate("Bourgeois"));

    }

}