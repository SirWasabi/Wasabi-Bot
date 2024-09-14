package userInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ui.GUI;

public class TeamMaker {

	public void teamFiller(MessageReceivedEvent e, int numberWanted, EmbedBuilder builder, GUI gui) {
		List<Member> users = e.getGuild().getMembers();
		List<Member> mentioned = e.getMessage().getMentionedMembers();
		List<Member> online = new ArrayList<>();
		List<Member> teamFilling = new ArrayList<>();
		int numberCount = 0;
		Random rand = new Random();
		boolean b = false;

		for (Member user : users) {
			if (user.getOnlineStatus() == OnlineStatus.ONLINE && !user.getUser().isBot()) {
				online.add(user);
			}
		}

		if (online.size() < numberWanted) {
			builder.clear();
			builder.setColor(0x42f4d9);
			builder.setTitle("Not enough people online for that team.");
			e.getChannel().sendMessage(builder.build()).queue();
			gui.textArea.append(e.getMember().getEffectiveName() + " tried to create a team with " + numberWanted
					+ " players but failed.\n");
			return;
		}

		if (mentioned.size() == numberWanted) {
			builder.clear();
			builder.setColor(0x42f4d9);
			builder.setTitle("Team:");
			for (Member mention : mentioned) {
				builder.appendDescription(mention.getAsMention() + "\n");
			}
			e.getChannel().sendMessage(builder.build()).queue();
			gui.textArea.append(e.getMember().getEffectiveName() + " had a team of " + numberWanted
					+ " players already formed but used the command anyway.\n");
			return;
		}

		if (mentioned.size() > numberWanted) {
			online = new ArrayList<Member>(mentioned);
			b = true;
		}

		if (mentioned.isEmpty() || b) {
			while (numberCount < numberWanted) {
				int k = rand.nextInt(online.size());
				teamFilling.add(online.get(k));
				online.remove(k);
				numberCount++;
			}
			builder.clear();
			builder.setColor(0x42f4d9);
			builder.setTitle("Team:");
			for (Member teammate : teamFilling) {
				builder.appendDescription(teammate.getAsMention() + "\n");
			}
			e.getChannel().sendMessage(builder.build()).queue();
			gui.textArea.append(
					e.getMember().getEffectiveName() + " created a team with " + numberWanted + " players." + "\n");
		}

		if (!mentioned.isEmpty() && (mentioned.size() < online.size())) {
			numberCount = mentioned.size();
			for (Member mention : mentioned) {
				for (int l = 0; l < online.size(); l++) { // <--- If you modify a List, it invalidates any Iterator
															// objects
					if (online.get(l).equals(mention)) { // created from it. Don't use advanced loops
						online.remove(online.get(l)); // (for each / Iterator)
					}
				}
			}
			while (numberCount < numberWanted) {
				int k = rand.nextInt(online.size());
				teamFilling.add(online.get(k));
				online.remove(k);
				numberCount++;
			}
			builder.clear();
			builder.setColor(0x42f4d9);
			builder.setTitle("Team:");
			for (Member mention : mentioned) {
				builder.appendDescription(mention.getAsMention() + "\n");
			}
			for (Member teammate : teamFilling) {
				builder.appendDescription(teammate.getAsMention() + "\n");
			}
			e.getChannel().sendMessage(builder.build()).queue();
			gui.textArea.append(
					e.getMember().getEffectiveName() + " created a team with " + numberWanted + " players." + "\n");
		}
	}

}
