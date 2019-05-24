package markov;

public class TestNeo4J {
    public static void main(String[] args) {
        Neo4JTest neo4JTest = new Neo4JTest();
        neo4JTest.readFile("data/marx.txt");
    }
}
