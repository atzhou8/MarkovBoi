package commands;

import bot.Bot;
import markov.MarkovUtils;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import threads.ReadChannelThread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Starts a thread in which all messages in the channel are read and inserted into the db. */
public class ReadCommand extends Command {

    public static Set<ReadChannelThread> threadSet = new HashSet<>();

    public ReadCommand() {
        commandName = "read";
        helpMessage = "Reads all of the messages in the channel you use it in. Do not use more than once per channel" +
                      "(This command may take a few minutes)";
        usageMessage = getPrefix() + commandName;
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        Channel channel = event.getTextChannel();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(MarkovUtils.URL);
            List<Member> memberList = event.getGuild().getMembers();
            for (Member member: memberList) {
                Bot.createNewChain(member.getUser().getId(), connection);
            }
        } catch (SQLException e) {
            ((TextChannel) channel).sendMessage(e.getMessage());
        } finally {
            try{
                connection.close();
            } catch (SQLException e) {}
        }

        ReadChannelThread readChannelThread = new ReadChannelThread(event, channel);
        threadSet.add(readChannelThread);
        readChannelThread.start();
    }
}
