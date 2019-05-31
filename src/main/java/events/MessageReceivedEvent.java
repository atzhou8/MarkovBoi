package events;

import bot.Bot;
import commands.ReadCommand;
import markov.MarkovUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import threads.ReadChannelThread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MessageReceivedEvent extends ListenerAdapter {
    private static int count = 0;

    @Override
    public void onMessageReceived(net.dv8tion.jda.core.events.message.MessageReceivedEvent event) {
        try {
            if (!event.getAuthor().isBot()) {
                while (true) {
                    for (ReadChannelThread rc : ReadCommand.threadSet) {
                        if (!rc.isAlive()) {
                            ReadCommand.threadSet.remove(rc);
                        }
                    }

                    if (ReadCommand.threadSet.isEmpty()) {
                        break;
                    }
                }

                Connection connection = DriverManager.getConnection(MarkovUtils.URL);
                String id = event.getAuthor().getId();
                Message message = event.getMessage();
                if (!Bot.getStoredIDs().contains(id)) {
                    Bot.createNewChain(id, connection);
                }
                Bot.setConnection(connection);
                Bot.readMessage(id, message);
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage(e.getMessage());
        }
    }
}
