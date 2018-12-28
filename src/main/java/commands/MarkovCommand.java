package commands;

import bot.Bot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MarkovCommand extends Command {

    public MarkovCommand() {
        commandName = "generate";
    }


    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        try {
            switch (args.length) {
                case 1:
                    event.getChannel().sendMessage(Bot.getMasterChain().simulate()).queue();
                    break;
                case 2:
                    String id = args[1];

                    if(!Bot.getStoredIDs().contains(id)) {
                        id = "master";
                        event.getChannel().sendMessage("ID not found. Using generic chain for generation").queue();
                    }

                    event.getChannel().sendMessage(Bot.getChainForID(id).simulate()).queue();
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHelpMessage(event);
        }
    }
}
