package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import ui.GUI;

public class Bot extends ListenerAdapter {

	private static GUI gui;
	private static Commands commands = new Commands();
	private final int builderColor = 13761099;
	private static boolean running;

	public static void main(String[] args) throws SQLException {
		gui = new GUI();
		gui.addFrameContent();

		gui.start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!running) {
						setupBot();
						running = true;
						gui.textArea.append("Bot is running...\n");
					} else {
						gui.textArea.append("Bot already running...\n");
					}
				} catch (LoginException | IOException e) {
					gui.textArea.append("Bot not running...\n");
					e.printStackTrace();
				}
			}
		});

		gui.close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
	}

	private static void loadProperties() {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("properties.ini"));
			Ref.discord_token = properties.getProperty("discord_token");
			Ref.bot_snowflake_id = properties.getProperty("bot_snowflake_id");
			Ref.imgur_client = properties.getProperty("imgur_client");
			Ref.imgur_token = properties.getProperty("imgur_token");
			Ref.youtube_token = properties.getProperty("youtube_token");
			Ref.omdb_token = properties.getProperty("omdb_token");
			gui.textArea.append("Loaded properties file.\n");
		} catch (Exception e) {
			gui.textArea.append("Error loading properties file.\n");
		}
	}

	private static void setupBot() throws LoginException, IOException {
		loadProperties();
		System.out.println(Ref.discord_token);
		JDA jda = JDABuilder.createDefault(Ref.discord_token).setBulkDeleteSplittingEnabled(false).build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.getPresence().setActivity(Activity.playing(Ref.prefix + "help"));
		jda.addEventListener(new Bot());
		commands.setupDatabase("database.db");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {

		User user = e.getAuthor();
		Member member = e.getMember();
		MessageChannel msgCha = e.getChannel();
		Message msg = e.getMessage();
		String raw = msg.getContentRaw();
		String[] msgSplit = raw.split("\\s+");
		String command = msgSplit[0];
		String rmComm = "";
		for (int i = 1; i < msgSplit.length; i++) {
			rmComm += msgSplit[i] + " ";
		}
		commands.getEmbedBuilder().clear();
		commands.getEmbedBuilder().setColor(builderColor);

		if (msgCha.getType().equals(ChannelType.PRIVATE)) {
			if (command.equals(Ref.prefix + "help")) {
				commands.help(msgSplit, user, gui);
			}
			return;
		}

		switch (command) {
		case Ref.prefix + "help":
			commands.help(msgSplit, user, gui);
			break;
		case Ref.prefix + "meme":
			commands.randomFaceOrMeme("memes", raw, user, msgCha, gui);
			break;
		case Ref.prefix + "quote":
			commands.quote(msg, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "oof":
			commands.playSoundEffect("oof.mp3", raw, user, member, e, gui);
			break;
		case Ref.prefix + "yeet":
			commands.playSoundEffect("yeet.mp3", raw, user, member, e, gui);
			break;
		case Ref.prefix + "seinfeld":
			commands.playSoundEffect("seinfeld.mp3", raw, user, member, e, gui);
			break;
		case Ref.prefix + "skip":
			commands.skip(raw, user, msgCha, e, gui);
			break;
		case Ref.prefix + "stop":
			commands.stop(raw, user, msgCha, e, gui);
			break;
		case Ref.prefix + "queue":
			commands.queue(raw, user, msgCha, e, gui);
			break;
		case Ref.prefix + "clear":
			commands.clear(raw, user, msgCha, e, gui);
			break;
		case Ref.prefix + "cleanup":
			commands.cleanup(raw, msgCha, user, gui);
			break;
		case Ref.prefix + "scores":
			commands.scores(raw, user, msgCha, gui);
			break;
		case Ref.prefix + "dank":
			commands.dank(raw, user, msgCha, gui);
			break;
		case Ref.prefix + "sub":
			commands.sub(msgSplit, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "unsub":
			commands.unsub(msgSplit, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "sublist":
			commands.sublist(raw, user, msgCha, gui);
			break;
		case Ref.prefix + "videos":
			commands.videos(raw, user, msgCha, gui);
			break;
		case Ref.prefix + "volume":
			commands.volume(raw, msgSplit, user, msgCha, e, gui);
			break;
		case Ref.prefix + "play":
			commands.play(msgSplit, raw, user, msgCha, member, e, gui);
			break;
		case Ref.prefix + "team":
			commands.team(msgSplit, e, user, msgCha, gui);
			break;
		case Ref.prefix + "tts":
			commands.tts(raw, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "addmov":
			commands.addMovie(rmComm, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "schmov":
			commands.searchMovie(rmComm, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "rmvmov":
			commands.removeMovie(rmComm, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "movlist":
			commands.getMovieList(raw, user, msgCha, gui);
			break;
		case Ref.prefix + "watch":
			commands.watch(rmComm, raw, user, msgCha, gui);
			break;
		case Ref.prefix + "unwatch":
			commands.unwatch(rmComm, raw, user, msgCha, gui);
			break;
		default:
			break;
		}
	}

	@Override
	public void onTextChannelUpdateNSFW(TextChannelUpdateNSFWEvent e) {
		if (e.getNewValue().booleanValue() == true) {
			EmbedBuilder builder = commands.getEmbedBuilder();
			builder.clear();
			builder.setTitle("Channel NSFW?? Badalhocos! HEHEHE");
			e.getChannel().sendMessage(builder.build()).queue();
			gui.textArea.append("Channel está agora NSFW! \n");
			builder.clear();
		}
	}

	@Override
	public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
		if (!gui.boxOne.isSelected()) {
			if (!e.getMember().getUser().isBot()) {
				try {
					Objects.requireNonNull(e.getGuild().getDefaultChannel())
							.sendMessage("Xau, " + e.getMember().getUser().getAsMention()).queue();
					gui.textArea.append(e.getMember().getUser().getName() + " left the voice channel. \n");
				} catch (NullPointerException e1) {
					gui.textArea.append("OnVoiceLeaveEvent -> Guild does not have a default channel.");
				}
			}
		}
	}

	@Override
	public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
		if (!gui.boxOne.isSelected()) {
			if (!e.getMember().getUser().isBot()) {
				try {
					Objects.requireNonNull(e.getGuild().getDefaultChannel())
							.sendMessage("Boas, " + e.getMember().getUser().getAsMention()).queue();
					gui.textArea.append(e.getMember().getUser().getName() + " joined the voice channel. \n");
				} catch (NullPointerException e1) {
					gui.textArea.append("OnVoiceJoinEvent -> Guild does not have a default channel.");
				}
			}
		}
	}

	@Override
	public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent e) {
		if (!gui.boxFour.isSelected()) {
			Calendar cal = Calendar.getInstance();
			String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
					+ cal.get(Calendar.SECOND);
			int month = cal.get(Calendar.MONTH) + 1;
			String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + month + "/" + cal.get(Calendar.YEAR);
			if (!e.getUser().isBot()) {
				if (e.getNewOnlineStatus() == OnlineStatus.ONLINE || e.getNewOnlineStatus() == OnlineStatus.OFFLINE) {
					gui.textArea.append(e.getUser().getName() + " is now "
							+ e.getNewOnlineStatus().toString().toLowerCase() + " at " + time + " in " + date + ".\n");
				}
			}
		}
	}

}