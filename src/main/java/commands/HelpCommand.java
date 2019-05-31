package commands;

import markov.MarkovUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** Sends an embedded message with a list of commmands and their usage. */
public class HelpCommand extends Command {

    private static Map<String, Command> commands = new HashMap();

    private static final String ERROR_MESSAGE = "Error in help message. Make sure you are using the right command name!";

    public HelpCommand() {
        commandName = "help";
        usageMessage = "";
    }

    public static void addCommand(Command command) {
        commands.put(command.getCommandName(), command);
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        switch (args.length) {
            case 1:
                EmbedBuilder eb = createEmbed();
                event.getChannel().sendMessage(eb.build()).queue();
                break;
            case 2:
                String key = args[1].toLowerCase();
                if (commands.containsKey(key)) {
                    commands.get(key).sendUsageMessage(event);
                } else {
                    event.getChannel().sendMessage(ERROR_MESSAGE).queue();
                }
            default:
                break;
        }
    }

    private EmbedBuilder createEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Commands for Markov Boi")
                .setColor(Color.BLUE);
        for (Command command: commands.values()) {
            eb.addField(MarkovUtils.capitalize(command.getCommandName()), command.getHelpMessage(), false);
        }
        eb.setFooter("Try " + getPrefix() + "help [command name] to get usage information for a command", null);

        return eb;
    }
}
