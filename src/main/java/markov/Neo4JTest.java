package markov;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Neo4JTest {

    private static final String ID = "marx";


    public void readString(String string, Session session) {
        List<String> cleanedString = MarkovUtils.clean(string);
        if (cleanedString.size() <= 2
                || MarkovUtils.isPunct(Character.toString(cleanedString.get(0).charAt(0)))) {
            return;
        } else {
            boolean isInitial = true;
            Deque<String> curr = new ArrayDeque();
            curr.addAll(cleanedString.subList(0, 2));
            String next;

            for (int pos = 0; pos < cleanedString.size() - 2; pos++) {
                next = cleanedString.get(pos + 2);

                if (isInitial && !MarkovUtils.isPunct(curr.getFirst())) {
                    addInitial(curr, session);
                    isInitial = false;
                }

                if (MarkovUtils.isEndPunct(curr.getLast())) {
                    isInitial = true;
                }

                addRelation(curr, next, session);


                curr.add(next);
                curr.removeFirst();
            }
        }
    }

    public void addInitial(Deque<String> words, Session session) {
        String command = String.format("MERGE (p:Initial {ID: \"%1$s\"}) " +
                                       "MERGE (c:Parent {ID: \"%1$s\", word1: \"%2$s\", word2: \"%3$s\"}) " +
                                       "MERGE (p)-[r:SEEN]->(c) " +
                                       "ON CREATE SET r.num = 1 " +
                                       "ON MATCH SET r.num = r.num + 1",
                                        ID, words.getFirst(), words.getLast());
        session.run(command);
    }

    public void addRelation(Deque<String> words, String child, Session session) {
        String command = String.format("MERGE (p:Parent {ID: \"%1$s\", first: \"%2$s\", second: \"%3$s\"}) " +
                                       "MERGE (c:Child {ID: \"%1$s\", word: \"%4$s\"}) " +
                                       "MERGE (p)-[r:SEEN]->(c) " +
                                       "ON CREATE SET r.num = 1 " +
                                       "ON MATCH SET r.num = r.num + 1",
                                       ID, words.getFirst(), words.getLast(), child);

        session.run(command);
    }



    public void readFile(String fileLocation) {
        Driver driver = GraphDatabase.driver(
                "bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
        Session session = driver.session();
        try {
            String text = new String(Files.readAllBytes(Paths.get(fileLocation)), StandardCharsets.UTF_8);
            readString(text, session);
        } catch (IOException e) {
            e.printStackTrace();
        }
        session.close();
        driver.close();
    }

}
