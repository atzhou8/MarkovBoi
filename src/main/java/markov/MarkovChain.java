package markov;

import com.sun.jmx.remote.internal.ArrayQueue;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MarkovChain {
    private GraphDatabaseService graphDatabaseService;
    private boolean capitalizeNext;
    private Random random;
    private String ID;

    private static final int GENERATION_LENGTH_MIN = 10;
    private static final int GENERATION_LENGTH_VARIANCE = 3;
    private static final int ORDER = 2;

    public MarkovChain(String ID, GraphDatabaseService dbs) {
        this.ID = ID;
        this.graphDatabaseService = dbs;
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
    public boolean readString(String s) {
        List<String> cleanedString = MarkovUtils.clean(s);

        if (cleanedString.size() <= 2
                || MarkovUtils.isPunct(Character.toString(cleanedString.get(0).charAt(0)))) {
            return false;
        } else {
            List<String> curr = new ArrayQueue<>(2);
            boolean isInitial = true;
            curr.addAll(cleanedString.subList(0, ORDER));
            MarkovKey key;
            String next;

            for (int pos = 0; pos < cleanedString.size() - ORDER; pos++) {
                key = curr;
                next = cleanedString.get(pos + ORDER);

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

            return true;
        }
    }

    private void addInitial(List words) {
        try ( Transaction tx = graphDatabaseService.beginTx())
        {
            Node node = graphDatabaseService.createNode();
            node.addLabel(Label.label(this.ID + "inital"));


            tx.success();
        }
    }

}
