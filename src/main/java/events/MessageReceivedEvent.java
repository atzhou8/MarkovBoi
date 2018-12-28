package events;

import bot.Bot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageReceivedEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(net.dv8tion.jda.core.events.message.MessageReceivedEvent event) {
        if(!event.getAuthor().isBot()
                && event.getMessage().getContentStripped().split(" ").length >= 3
                && Character.isLetter(event.getMessage().getContentStripped().charAt(0))) {
            String id = event.getAuthor().getId();
            Message message = event.getMessage();

            Bot.readMessage(id, message);
            Bot.save();
            System.out.println("Read new message!");
            System.out.println("Stripped:" + message.getContentStripped());
            System.out.println("Raw:" + message.getContentRaw());
            System.out.println("Display" + message.getContentDisplay());
        }
    }
}
