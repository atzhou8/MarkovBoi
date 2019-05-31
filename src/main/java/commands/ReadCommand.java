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

public class ReadCommand extends Command {

    public static Set<ReadChannelThread> threadSet = new HashSet<>();

    public ReadCommand() {
        commandName = "read";
        helpMessage = "Reads all of the messages in the channel you use it in. (May take a few minutes)";
        usageMessage = getPrefix() + commandName;
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        Channel channel = event.getTextChannel();

        try {
            Connection connection = DriverManager.getConnection(MarkovUtils.URL);
            List<Member> memberList = event.getGuild().getMembers();
            for (Member member: memberList) {
                Bot.createNewChain(member.getUser().getId(), connection);
            }
            connection.close();
        } catch (SQLException e) {
            ((TextChannel) channel).sendMessage(e.getMessage());
        }

        ReadChannelThread readChannelThread = new ReadChannelThread(event, channel);
        threadSet.add(readChannelThread);
        readChannelThread.start();
    }
}
