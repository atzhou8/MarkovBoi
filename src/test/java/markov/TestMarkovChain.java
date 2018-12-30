package markov;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestMarkovChain {

    @Test
    public void testCleanString() {
        MarkovChain mc = new MarkovChain(1,"0");

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


//        assertArrayEquals(cleanedMessageA, mc.clean(messageA));
//        assertArrayEquals(cleanedMessageB, mc.clean(messageB));
//        assertArrayEquals(cleanedMessageC, mc.clean(messageC));
//        assertArrayEquals(cleanedMessageD, mc.clean(messageD));

        for(String s: MarkovUtils.clean("sure. i wrote it when i was a freshman, still works well. it stays on the registration page (https://sdb.admin.uw.edu/students/uwnetid/register.asp) and registers for classes as soon as it's told to. it will automatically drop any conflicting classes, so swapping quiz sections, lectures, or even entire classes is possible.\n")) {
            System.out.println(s);
        }

    }


    @Test
    public void testAdd() {
        MarkovChain mc = new MarkovChain(2, "0");
        mc.readFile("data/marx.txt");
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());
        System.out.println(mc.simulate());

    }
//
//    @Test
//    public void testSimulate() {
//        MarkovChain mc = new MarkovChain(2,"0");
//        mc.readFile("data/plato.txt");
//        mc.readFile("data/marx.txt");
////        System.out.println(mc.simulate("Bourgeois"));
//        }
//
//    @Test
//    public void testDuplicates() {
//        MarkovChain mc = new MarkovChain(2, "0");
//        mc.readString("Boop boop boop");
//        System.out.println(mc.simulate());
//    }
}