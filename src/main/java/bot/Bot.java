package bot;

import commands.MarkovCommand;
import commands.ReadCommand;
import commands.PingCommand;
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

    private static transient JDA jda;
    private static Map<String , MarkovChain> markovChainList;


    private static final String[] DEFAULT_DATA_ID_ARRAY = {"marx", "plato"};
    private static final String SAVE_LOCATION = "data/save.txt";

    public static void main(String[] args) {
        load();
        addCommands();
        addEvents();
    }

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
        }  catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

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
    }

    private static void addCommands() {
        jda.addEventListener(new PingCommand());
        jda.addEventListener(new ReadCommand());
        jda.addEventListener(new MarkovCommand());
    }

    private static void addEvents() {
        jda.addEventListener(new MessageReceivedEvent());
        jda.addEventListener(new ServerJoinedEvent());
    }

    private static void loadDefaultData() {
        MarkovChain mc;
        for (String id : DEFAULT_DATA_ID_ARRAY) {
            addNewChain(id);
            markovChainList.get(id).readFile(idToFileName(id));
        }

        addNewChain("master");
    }

    private static String idToFileName(String s) {
        return "data/" + s + ".txt";
    }

    public static Set<String> getStoredIDs() {
        return markovChainList.keySet();
    }

    public static void readMessage(String id, Message m) {
        String message = m.getContentStripped();

        getChainForID(id).readString(message);
        getMasterChain().readString(message);
    }

    public static MarkovChain getChainForID(String id) {
        if (!markovChainList.keySet().contains(id)) {
            addNewChain(id);
            System.out.println("ID not found! Creating new Markov Chain for new user");
        }

        return markovChainList.get(id);
    }

    public static MarkovChain getMasterChain() {
        return markovChainList.get("master");
    }

    public static void addNewChain(String id) {
        if (!markovChainList.keySet().contains(id)) {
            markovChainList.put(id, new MarkovChain(2, id));
        }
    }
}
