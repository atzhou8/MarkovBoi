package commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class Command extends ListenerAdapter {

    private static final String PREFIX = "!";
    public String commandName;
    public abstract void executeCommand(MessageReceivedEvent event, String[] args);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] commandArgs = getCommandArgs(event);
        if (containsCommand(commandArgs)){
            executeCommand(event, commandArgs);
            System.out.println("Executing " + commandName);
        }
    }

    protected String[] getCommandArgs(MessageReceivedEvent m) {
        return m.getMessage().getContentRaw().split(" ");
    }

    protected boolean containsCommand(String[] args) {
        return args[0].equals(PREFIX + commandName);
    }

}
