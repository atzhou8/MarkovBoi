package commands;

import bot.Bot;
import markov.MarkovChain;
import markov.MarkovUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class SimulateCommand extends Command {

    public SimulateCommand() {
        commandName = "sim";
        usageMessage = "!sim (id) [starting word] \n\n" +
                "Default ids are: \n" +
                "trump\n" +
                "marx\n" +
                "plato\n" +
                "master\n\n" +
                "If you are seeing this message, it may be because your starting word does not exist.";
        helpMessage = "Generates text based on read data";
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        try {
            String id = "master";
            String start = "";
            String name = "everyone";
            String url = null;
            MarkovChain markovChain = Bot.getMasterChain();

            switch (args.length) {
                case 3:
                    start = args[2];
                case 2:
                    List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

                    if (mentionedMembers.size() == 1) {
                        User user = mentionedMembers.get(0).getUser();
                        id = user.getId();
                        url = user.getAvatarUrl();
                        name = user.getName();
                    } else {
                        id = args[1];
                        name = MarkovUtils.capitalize(id);
                        url = Bot.getDefaultPicture(id);
                    }

                    if (!Bot.getStoredIDs().contains(id)) {
                        event.getChannel().sendMessage("ID not found. Using master chain for generation").queue();
                        name = "everyone";
                        markovChain = Bot.getMasterChain();
                    } else {
                        markovChain = Bot.getChainForID(id);
                    }
                    break;
                default:
                    break;
            }

            EmbedBuilder eb = createEmbed(name, url, markovChain, start);
            event.getChannel().sendMessage(eb.build()).queue();
        } catch (IllegalArgumentException e) {
            sendUsageMessage(event);
        }
    }

    private EmbedBuilder createEmbed(String name, String url, MarkovChain markovChain, String start) {
        EmbedBuilder eb = new EmbedBuilder();
        if (start == null) {
            eb.setColor(Color.RED)
                    .setThumbnail(url)
                    .setTitle("Simulated text for " + name + ":")
                    .setDescription(markovChain.simulate(start));
        } else {
            eb.setColor(Color.RED)
                    .setThumbnail(url)
                    .setTitle("Simulated text for " + name + ":")
                    .setDescription(markovChain.simulate());

        }
        return eb;
    }
}
