package threads;

import bot.Bot;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ReadChannelThread extends Thread implements Runnable {
    private MessageReceivedEvent event;
    private Channel channel;

    public ReadChannelThread(MessageReceivedEvent event, Channel channel) {
        this.event = event;
        this.channel = channel;
    }

    @Override
    public void run() {
        int count = 0;
        for (Message message: ((TextChannel) channel).getIterableHistory().cache(false)) {
            if (!message.getAuthor().isBot()) {
                Bot.readMessage(message.getAuthor().getId(), message);
                count++;
            }
        }

        new SaveThread().run();
        event.getChannel().sendMessage("Successfully read " + count + " messages!").queue();
    }
}
