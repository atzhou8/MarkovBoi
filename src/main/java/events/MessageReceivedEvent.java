package events;

import bot.Bot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import threads.SaveThread;

public class MessageReceivedEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(net.dv8tion.jda.core.events.message.MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            String id = event.getAuthor().getId();
            Message message = event.getMessage();

            Bot.readMessage(id, message);
            new SaveThread().run();
        }
    }
}
