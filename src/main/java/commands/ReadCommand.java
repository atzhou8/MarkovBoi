//package commands;
//
//import bot.Bot;
//import net.dv8tion.jda.core.entities.Channel;
//import net.dv8tion.jda.core.entities.Message;
//import net.dv8tion.jda.core.entities.TextChannel;
//import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
//import threads.ReadChannelThread;
//
//public class ReadCommand extends Command {
//
//    public ReadCommand() {
//        commandName = "read";
//        helpMessage = "Reads all of the messages in the channel you use it in. (May take a few minutes)";
//        usageMessage = getPrefix() + commandName;
//    }
//
//    @Override
//    public void executeCommand(MessageReceivedEvent event, String[] args) {
//        Channel channel = event.getTextChannel();
//        ReadChannelThread readChannelThread = new ReadChannelThread(event, channel);
//
//        readChannelThread.start();
//        Bot.save();
//    }
//}
