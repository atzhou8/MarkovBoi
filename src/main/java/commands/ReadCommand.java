package commands;

import bot.Bot;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ReadCommand extends Command {

    public ReadCommand() {
        commandName = "read";
        helpMessage = "Reads all of the messages in the channel you use it in.";
        usageMessage = getPrefix() + commandName;
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        event.getChannel();
        Channel channel = event.getTextChannel();

        int count = 0;
        for (Message message: ((TextChannel) channel).getIterableHistory().cache(false)) {
            if (!message.getAuthor().isBot()) {
                Bot.readMessage(message.getAuthor().getId(), message);
                count++;
            }
        }
        Bot.save();

        event.getChannel().sendMessage("Successfully read " + count + " messages!").queue();
    }
}
