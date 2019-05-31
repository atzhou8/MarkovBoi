package commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;


/** Command that gives the number of servers this bot is in */
public class GetGuildsCommand extends Command{

    public GetGuildsCommand() {
        commandName = "guilds";
    }


    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("This bot is in " + event.getJDA().getGuildCache().size() + " servers!").queue();
    }
}
