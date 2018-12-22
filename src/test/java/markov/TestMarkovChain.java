package markov;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.ReceivedMessage;
import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class TestMarkovChain {

    @Test
    public void testCleanString() {
        MarkovChain mc = new MarkovChain(0);

        String messageA = "Good morning John";
        String[] cleanedMessageA = {"good", "morning", "John"};

        String messageB = "It's Tuesday!";
        String[] cleanedMessageB = {"it's", "Tuesday", "!"};

        String messageC = "And today I would like to talk to you about China.";
        String[] cleanedMessageC = {"and", "today", "I", "would", "like", "to", "talk",
                                    "to", "you", "about", "China", "."};

        String messageD = "Didn't you know? My favorite country is Great Britain.";
        String[] cleanedMessageD = {"didn't", "you", "know", "?", "my", "favorite", "country",
                                    "is", "Great", "Britain", "."};



        assertArrayEquals(cleanedMessageA, mc.clean(messageA));
        assertArrayEquals(cleanedMessageB, mc.clean(messageB));
        assertArrayEquals(cleanedMessageC, mc.clean(messageC));
        assertArrayEquals(cleanedMessageD, mc.clean(messageD));
        }



}
