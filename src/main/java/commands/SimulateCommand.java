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
                "!sim by itself will generate from the master chain \n\n" +
                "Default ids are: \n" +
                "marx\n" +
                "plato\n" +
                "master";
    }


    @Override
    public void executeCommand(MessageReceivedEvent event, String[] args) {
        try {
            String id = "master";
            String start = "";
            String name = "everyone";
            String url = null;
            MarkovChain markovChain = Bot.getMasterChain();
            System.out.println("Length of command is " + args.length);
            switch (args.length) {
                case 3:
                    start = args[2];
                    System.out.println("Case 3");
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

                    if(!Bot.getStoredIDs().contains(id)) {
                        event.getChannel().sendMessage("ID not found. Using master chain for generation").queue();
                        name = "everyone";
                        markovChain = Bot.getMasterChain();
                    } else {
                        markovChain = Bot.getChainForID(id);
                    }
                    System.out.println("Case 2");
                    break;
            }
            System.out.println("ID: " + id);
            System.out.println("Start: " + start );
            System.out.println("Name: " + name);
            System.out.println("URL: " + url);

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED)
                    .setThumbnail(url)
                    .setTitle("Simulated text for " + name + ":")
                    .setDescription(markovChain.simulate());

            event.getChannel().sendMessage(eb.build()).queue();
        } catch (IllegalArgumentException e) {
            sendUsageMessage(event);
        }
    }

}
