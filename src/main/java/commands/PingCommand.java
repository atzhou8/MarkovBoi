package commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/** Pings the bot. If everything is working correctly, the bot should send message "Pong!" in the channel where this is used.*/
public class PingCommand extends Command {

    public PingCommand() {
        commandName = "ping";
        helpMessage = "Pongs you.";
        usageMessage = getPrefix() + commandName;
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Pong!").queue();
    }
}
