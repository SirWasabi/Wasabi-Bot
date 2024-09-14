package music;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayerManager {

	private static PlayerManager INSTANCE;
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;

	public PlayerManager() {
		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}

	public void loadAndPlay(final TextChannel channel, Member member, final String trackUrl) {
		final GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(13761099);
		
		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				String trackID = track.getInfo().identifier;
				if(trackID.startsWith("sound/") && trackID.endsWith(".mp3")) {
					play(channel.getGuild(), member, musicManager, track);
				} else {
					builder.setTitle("Adding to queue: " + track.getInfo().title);
					channel.sendMessage(builder.build()).queue();
					play(channel.getGuild(), member, musicManager, track);
				}
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}
				
				builder.setTitle("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist "
						+ playlist.getName() + ")");
				channel.sendMessage(builder.build()).queue();

				play(channel.getGuild(), member, musicManager, firstTrack);
			}

			@Override
			public void noMatches() {
				builder.setTitle("Nothing found by " + trackUrl);
				channel.sendMessage(builder.build()).queue();
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				builder.setTitle("Could not play: " + exception.getMessage());
				channel.sendMessage(builder.build()).queue();
			}
		});
	}

	private void play(Guild guild, Member member, GuildMusicManager musicManager, AudioTrack track) {
		connectToVoiceChannel(guild.getAudioManager(), member);
		musicManager.scheduler.queue(track);
	}

	public void skipTrack(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.nextTrack();
		channel.sendMessage("Skipped to next track.").queue();
	}
	
	public void setVolume(TextChannel channel, int volume) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.player.setVolume(volume);
	}
	
	public BlockingQueue<AudioTrack> getQueue(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		return musicManager.scheduler.getQueue();
	}
	
	public AudioTrack getPlayingTrack(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		return musicManager.player.getPlayingTrack();
	}
	
	public void clearQueue(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.clearQueue();
	}
	
	public void stop(TextChannel channel) {
		AudioManager audioManager = channel.getGuild().getAudioManager();
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		if(musicManager.player.getPlayingTrack() != null) {
			musicManager.player.stopTrack();
		}
		musicManager.scheduler.clearQueue();
		audioManager.closeAudioConnection();
	}

	private static void connectToVoiceChannel(AudioManager audioManager, Member member) {
		if (!audioManager.isConnected()) {
			for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
				if(voiceChannel.getMembers().contains(member)) {
					audioManager.openAudioConnection(voiceChannel);
				}
			}
		}
	}

	public static synchronized PlayerManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlayerManager();
		}
		return INSTANCE;
	}

}
