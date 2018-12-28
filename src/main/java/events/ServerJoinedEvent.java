package events;

import bot.Bot;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public class ServerJoinedEvent extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("Joined new server!");
        Bot.addNewChain(event.getGuild().getId());

        List<Member> memberList = event.getGuild().getMembers();
        for (Member member: memberList) {
            Bot.addNewChain(member.getUser().getId());
        }
    }
}
