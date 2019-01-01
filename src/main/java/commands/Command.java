package commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;

public abstract class Command extends ListenerAdapter {

    private static final String PREFIX = "!";
    protected String commandName;
    protected String helpMessage;
    protected String usageMessage;

    public abstract void executeCommand(MessageReceivedEvent event, String[] args);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] commandArgs = getCommandArgs(event);
        if (containsCommand(commandArgs)) {
            System.out.println("Executing " + commandName);
            executeCommand(event, commandArgs);
            System.out.println("Command executed correctly!");
        }
    }

    public String getCommandName() {
        return commandName;
    }

    public String getUsageMessage() {
        return usageMessage;
    }

    public String getHelpMessage() {
        return helpMessage;
    }

    public String getPrefix() {
        return PREFIX;
    }

    protected String[] getCommandArgs(MessageReceivedEvent m) {
        return m.getMessage().getContentRaw().split(" ");
    }

    protected boolean containsCommand(String[] args) {
        return args[0].equals(PREFIX + commandName);
    }

    protected void sendUsageMessage(MessageReceivedEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN)
                .setTitle(commandName)
                .setDescription(getHelpMessage())
                .addField("Usage:", getUsageMessage(), false)
                .setFooter("() - Required, [] - Optional", null);

        event.getChannel().sendMessage(eb.build()).queue();
    }

}
