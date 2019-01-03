package bot;

import commands.*;
import events.MessageReceivedEvent;
import events.ServerJoinedEvent;
import markov.MarkovChain;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.*;

public class Bot {

    private static JDA jda;
    private static Map<String, MarkovChain> chains;
    private static Map<String, String> defaultDataPictures;
    private static List<String> readMessages;

    private static final String[] DEFAULT_DATA_ID_ARRAY = {"marx", "plato", "trump"};
    private static final String[] DEFAULT_DATA_URL_ARRAY =
            {"https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Karl_Marx_001.jpg/220px-Karl_Marx_001.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Plato_Silanion_Musei_Capitolini_MC1377.jpg/220px-Plato_Silanion_Musei_Capitolini_MC1377.jpg",
            "https://typeset-beta.imgix.net/lovelace/getty/113257835.jpg"};
    private static final String SAVE_LOCATION = "data/save.txt";

    public static void main(String[] args) {
        load();
        addCommands();
        addEvents();
        loadPictures();
    }

    /* Loads Markov chains from save file if available. Otherwise, initialize chains from default data */
    private static void load() {
        File f = new File(SAVE_LOCATION);
        initialize();
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                BufferedInputStream bs = new BufferedInputStream(fs);
                ObjectInputStream os = new ObjectInputStream(bs);
                Map<String, MarkovChain> objects = (Map<String, MarkovChain>) os.readObject();
                chains =  objects;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
            loadDefaultData();
        } else {
            System.out.println("Save not found");
            createNewChain("master");
        }
    }

    private static void initialize() {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(Config.TOKEN)
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        jda.getPresence().setGame(Game.watching("Type !help to see commands!"));
        chains = new HashMap<>();
        defaultDataPictures = new HashMap<>();
        readMessages = new ArrayList();
    }

    private static void loadDefaultData() {
        for (String id : DEFAULT_DATA_ID_ARRAY) {
            createNewChain(id);
            chains.get(id).readFile(idToFileName(id));
        }

    }

    private static void addCommands() {
        Command ping = new PingCommand();
        Command read = new ReadCommand();
        Command sim = new SimulateCommand();
        Command help = new HelpCommand();

        jda.addEventListener(ping);
        jda.addEventListener(read);
        jda.addEventListener(sim);
        jda.addEventListener(help);

        HelpCommand.addCommand(ping);
        HelpCommand.addCommand(read);
        HelpCommand.addCommand(sim);
    }

    private static void addEvents() {
        jda.addEventListener(new MessageReceivedEvent());
        jda.addEventListener(new ServerJoinedEvent());
    }

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
            createNewChain(id);
            System.out.println("ID not found! Creating new Markov Chain for new user");
        }

        return chains.get(id);
    }

    /* Gets the master Markov chain for all users */
    public static MarkovChain getMasterChain() {
        return chains.get("master");
    }

    /* Creates a new Markov chain */
    public static void createNewChain(String id) {
        if (!chains.keySet().contains(id)) {
            chains.put(id, new MarkovChain(2, id));
        }
    }

    /* Reads a Message and encodes the data into the relevant chains */
    public static void readMessage(String id, Message m) {
        String messageID = m.getId();
        if (!readMessages.contains(messageID)) {
            String message = m.getContentStripped();

            getChainForID(id).readString(message);
            getMasterChain().readString(message);
            readMessages.add(messageID);
        } else {
            return;
        }
    }

    /* Saves all existing Markov chains */
    public static void save() {
        File f = new File(SAVE_LOCATION);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            BufferedOutputStream bs = new BufferedOutputStream(fs);
            ObjectOutputStream os = new ObjectOutputStream(bs);
            os.writeObject(chains);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}
