package threads;

import bot.Bot;
import commands.PingCommand;
import markov.MarkovChain;
import markov.MarkovUtils;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ReadChannelThread extends Thread implements Runnable {
    private MessageReceivedEvent event;
    private Channel channel;

    public ReadChannelThread(MessageReceivedEvent event, Channel channel) {
        this.event = event;
        this.channel = channel;
    }

    @Override
    public void run() {
        int count = 0;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(MarkovUtils.URL);
            connection.setAutoCommit(false);
            Bot.setConnection(connection);
            for (Message message : ((TextChannel) channel).getIterableHistory().cache(false)) {
                if (!message.getAuthor().isBot()
                        && Bot.readMessage(message.getAuthor().getId(), message)) {
                    count++;
                    if (count > 50000) {break;}
                }
            }
            connection.commit();
            event.getChannel().sendMessage("Successfully read " + count + " messages!").queue();
        } catch (SQLException e) {
            event.getChannel().sendMessage(e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {}
        }
    }
}
