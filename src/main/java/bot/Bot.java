package bot;

import commands.PingCommand;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;

public class Bot {

    private static JDA jda;

    public static void main(String[] args) {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(Config.TOKEN)
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        addCommands();
    }

    private static void addCommands() {
        jda.addEventListener(new PingCommand());
    }
}
