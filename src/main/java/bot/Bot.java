package bot;

import commands.*;
import events.MessageReceivedEvent;
import events.ServerJoinedEvent;
import markov.MarkovChain;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.*;

public class Bot {

    private static JDA jda;
    private static Map<String, MarkovChain> markovChainList;
    private static Map<String, String> defaultDataPictures;

    private static final String[] DEFAULT_DATA_ID_ARRAY = {"marx", "plato"};
    private static final String[] DEFAULT_DATA_URL_ARRAY =
            {"https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Karl_Marx_001.jpg/220px-Karl_Marx_001.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Plato_Silanion_Musei_Capitolini_MC1377.jpg/220px-Plato_Silanion_Musei_Capitolini_MC1377.jpg"};
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
                markovChainList =  objects;
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
        } else {
            System.out.println("Save not found");
            loadDefaultData();
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

        markovChainList = new HashMap<>();
        defaultDataPictures = new HashMap<>();
    }

    private static void loadDefaultData() {
        for (String id : DEFAULT_DATA_ID_ARRAY) {
            createNewChain(id);
            markovChainList.get(id).readFile(idToFileName(id));
        }

        createNewChain("master");
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

    /* Gets a list of all users that have a chain associated with them */
    public static Set<String> getStoredIDs() {
        return markovChainList.keySet();
    }

    /* Gets the MarkovChain given the id of a user*/
    public static MarkovChain getChainForID(String id) {
        if (!markovChainList.keySet().contains(id)) {
            createNewChain(id);
            System.out.println("ID not found! Creating new Markov Chain for new user");
        }

        return markovChainList.get(id);
    }

    /* Gets the master Markov chain for all users */
    public static MarkovChain getMasterChain() {
        return markovChainList.get("master");
    }

    /* Creates a new Markov chain */
    public static void createNewChain(String id) {
        if (!markovChainList.keySet().contains(id)) {
            markovChainList.put(id, new MarkovChain(2, id));
        }
    }

    /* Reads a Message and encodes the data into the relevant chains */
    public static void readMessage(String id, Message m) {
        String message = m.getContentStripped();

        getChainForID(id).readString(message);
        getMasterChain().readString(message);
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
            os.writeObject(markovChainList);
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
