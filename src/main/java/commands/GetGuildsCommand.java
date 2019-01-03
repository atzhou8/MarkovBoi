package commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GetGuildsCommand extends Command{

    public GetGuildsCommand() {
        commandName = "guilds";
    }


    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("This bot is in " + event.getJDA().getGuildCache().size() + " servers!").queue();
    }
}
