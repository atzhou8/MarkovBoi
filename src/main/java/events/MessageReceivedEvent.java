package events;

import bot.Bot;
import markov.MarkovUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MessageReceivedEvent extends ListenerAdapter {
    private static int count = 0;

    @Override
    public void onMessageReceived(net.dv8tion.jda.core.events.message.MessageReceivedEvent event) {
        try {
            if (!event.getAuthor().isBot()) {
                Connection connection = DriverManager.getConnection(MarkovUtils.URL);
                Bot.setConnection(connection);
                String id = event.getAuthor().getId();
                Message message = event.getMessage();

                if (Bot.readMessage(id, message)) {
                    count++;
                }
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage(e.getMessage());
        }
    }
}
