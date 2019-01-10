package events;

import bot.Bot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import threads.SaveThread;

public class MessageReceivedEvent extends ListenerAdapter {
    private static int count = 0;
    private static final int saveFrequency = 20; //Bot will save every time it reads this many messages

    @Override
    public void onMessageReceived(net.dv8tion.jda.core.events.message.MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            String id = event.getAuthor().getId();
            Message message = event.getMessage();

            if (Bot.readMessage(id, message)) {
                count++;

                if (count >= saveFrequency) {
                    new SaveThread().run();
                    count = 0;
                }
            }
        }
    }
}
