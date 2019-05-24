package markov;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestMarkovChain {

//    @Test
//    public void testCleanString() {
//        String messageA = "Good morning John";
//        String[] cleanedMessageA = {"good", "morning", "John"};
//
//        String messageB = "It's Tuesday!";
//        String[] cleanedMessageB = {"it's", "Tuesday", "!"};
//
//        String messageC = "And today I would like to talk to you about China.";
//        String[] cleanedMessageC = {"and", "today", "I", "would", "like", "to", "talk",
//                                    "to", "you", "about", "China", "."};
//
//        String messageD = "Didn't you know? Ophelia's favorite country is Great https://github.com/azhou314/MarkovBot.";
//        String[] cleanedMessageD = {"didn't", "you", "know", "?", "ophelia's", "favorite", "country",
//                                    "is", "Great", "."};
//
//
//        assertArrayEquals(cleanedMessageA, MarkovUtils.clean(messageA).toArray());
//        assertArrayEquals(cleanedMessageB, MarkovUtils.clean(messageB).toArray());
//        assertArrayEquals(cleanedMessageC, MarkovUtils.clean(messageC).toArray());
//        assertArrayEquals(cleanedMessageD, MarkovUtils.clean(messageD).toArray());
//
//    }
//

    @Test
    public void testAdd() {
        MarkovChain mc = new MarkovChain(2, "0");
        mc.readFile("data/marx.txt");
    }

//    @Test
//    public void testSimulate() {
//        MarkovChain mc = new MarkovChain(2, "0");
//        mc.readFile("data/plato.txt");
//        mc.readFile("data/marx.txt");
//        System.out.println(mc.simulate("Bourgeois"));
//    }
//
//    @Test
//    public void testDuplicates() {
//        MarkovChain mc = new MarkovChain(2, "0");
//        mc.readString("Boop boop boop");
//        System.out.println(mc.simulate());
//    }
}
