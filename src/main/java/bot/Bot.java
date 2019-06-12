package bot;

import commands.*;
//import events.ServerJoinedEvent;
import events.MessageReceivedEvent;
import markov.MarkovChain;
import markov.MarkovUtils;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;

import javax.security.auth.login.LoginException;
import java.sql.*;
import java.util.*;

public class Bot {

    private static JDA jda;
    private static Map<String, String> defaultDataPictures;
    private static Map<String, MarkovChain> chains;


    private static final String ID = Config.TOKEN;
    private static final String[] DEFAULT_DATA_ID_ARRAY = {"marx", "plato", "trump"};
    private static final String[] DEFAULT_DATA_URL_ARRAY =
            {"https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Karl_Marx_001.jpg/220px-Karl_Marx_001.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Plato_Silanion_Musei_Capitolini_MC1377.jpg/220px-Plato_Silanion_Musei_Capitolini_MC1377.jpg",
            "https://typeset-beta.imgix.net/lovelace/getty/113257835.jpg"};

    public static void main(String[] args) {
        initialize();
        System.out.println("Initialized!");
        initDataToDB();
        loadDataFromDB();
        System.out.println("Default data loaded!");
        loadPictures();
        addCommands();
        addEvents();
        System.out.println("Bot ready!");
    }

    /* Creates JDA instance and initializes member variables */
    private static void initialize() {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(ID)
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        jda.getPresence().setGame(Game.watching("Type !help to see commands!"));
        chains = new HashMap<>();
        defaultDataPictures = new HashMap<>();
    }

    /* Loads data into SQLite database */
    private static void initDataToDB() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(MarkovUtils.URL);
            connection.setAutoCommit(false);
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS idTable (id TEXT);");

            /* initialize chains for default data and read files */
            for (String id : DEFAULT_DATA_ID_ARRAY) {
                createNewChain(id, connection);
            }
            createNewChain("master", connection);

            /* initialize id table */
            for (String id : DEFAULT_DATA_ID_ARRAY) {
                chains.get(id).setConnection(connection);
                chains.get(id).readFile(idToFileName(id));
            }
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {}
        }
    }

    /* Loads ID's for all MC's stored in the SQLite database */
    private static void loadDataFromDB() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(MarkovUtils.URL);
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM idTable");
            connection.setAutoCommit(false);
            while (rs.next()) {
                createNewChain(rs.getString("id"), connection);
            }
            connection.commit();
        } catch (SQLException e) {

        } finally {
            try {
                connection.close();
            } catch (SQLException e) {}
        }
    }


    /* Loads all commands for bot */
    private static void addCommands() {
        Command ping = new PingCommand();
        Command read = new ReadCommand();
        Command sim = new SimulateCommand();
        Command help = new HelpCommand();
        Command guilds = new GetGuildsCommand();

        jda.addEventListener(ping);
        jda.addEventListener(read);
        jda.addEventListener(sim);
        jda.addEventListener(help);
        jda.addEventListener(guilds);

        HelpCommand.addCommand(ping);
        HelpCommand.addCommand(read);
        HelpCommand.addCommand(sim);
    }

    /* Loads all events for bot */
    private static void addEvents() {
        jda.addEventListener(new MessageReceivedEvent());
    }

    /* Loads image locations for default MC's */
    private static void loadPictures() {
        for (int i = 0; i < DEFAULT_DATA_ID_ARRAY.length; i++) {
            defaultDataPictures.put(DEFAULT_DATA_ID_ARRAY[i],
                    DEFAULT_DATA_URL_ARRAY[i]);
        }
    }

    /* Converts the id of one of the default text files to its file location */
    private static String idToFileName(String s) {
        return "data/" + s + ".txt";
    }

    /* Gets the picture for one of the default texts */
    public static String getDefaultPicture(String id) {
        return defaultDataPictures.get(id);
    }

    /* Gets the map containing all Markov Chains */
    public static Map<String, MarkovChain> getChains() {
        return chains;
    }

    /* Gets a list of all users that have a chain associated with them */
    public static Set<String> getStoredIDs() {
        return chains.keySet();
    }

    /* Gets the MarkovChain given the id of a user*/
    public static MarkovChain getChainForID(String id) {
        if (!chains.keySet().contains(id)) {
            return null;
        }

        return chains.get(id);
    }

    /* Gets the master Markov chain for all users */
    public static MarkovChain getMasterChain() {
        return chains.get("master");
    }

    /* Creates a new Markov chain */
    public static void createNewChain(String id, Connection connection) {
        if (!chains.keySet().contains(id)) {
            System.out.println("Creating Markov Chain for " + id);
            chains.put(id, new MarkovChain(id, connection));
            try{
                PreparedStatement stmt = connection.prepareStatement("INSERT OR IGNORE INTO idTable VALUES(?);");
                stmt.setString(1, id);
                stmt.execute();
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    /* Sets all bots to a given connection. Call before readMessage() */
    public static void setConnection(Connection connection) {
        for (MarkovChain mc: getChains().values()) {
            mc.setConnection(connection);
        }
    }

    /* Reads a Message and encodes the data into the relevant chains.
     * Returns true if message is processed */
    public static boolean readMessage(String id, Message m) {
        String message = m.getContentStripped();
        MarkovChain master = getMasterChain();
        MarkovChain chainForID = getChainForID(id);


        System.out.println("Reading message: " + message);
        if (chainForID == null) {
            return master.readString(message);
        } else {
            return master.readString(message) &&
                    chainForID.readString(message);
        }
    }
}
