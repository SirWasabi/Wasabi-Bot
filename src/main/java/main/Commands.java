package main;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import Utils.Pair;
import database.Database;
import imgur.ImgurRequest;
import lyrics.LyricsParser;
import movies.Media;
import movies.MovieRequest;
import music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ui.GUI;
import userInteraction.TeamMaker;
import userInteraction.UserScores;
import youtube.YoutubeAccount;
import youtube.YoutubeVideo;

public class Commands {

	private EmbedBuilder builder = new EmbedBuilder();
	private LyricsParser parser = new LyricsParser();
	private PlayerManager manager = new PlayerManager();
	private Database db;
	private UserScores us;
	private YoutubeAccount youtube = new YoutubeAccount(false);
	private ImgurRequest imgur = new ImgurRequest();
	private final int MAX_RESULTS = 4;

	public void setupDatabase(String filename) {
		db = new Database();
		db.connectToDatabase(filename);
		us = new UserScores(db);
	}

	public void help(String[] msgSplit, User user, GUI gui) {
		if (msgSplit.length == 2) {
			try {
				ResultSet rs = db
						.executeQuery("SELECT Command, Description FROM Help WHERE Command = \"" + msgSplit[1] + "\";");
				if (rs.next()) {
					builder.setTitle(Ref.prefix + rs.getString(1));
					builder.setDescription("***" + rs.getString(2) + "***");
				} else {
					builder.setTitle(Ref.prefix + msgSplit[1] + " does not exist.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (msgSplit.length == 1) {
			try {
				ResultSet gen = db.executeQuery("SELECT * FROM Help WHERE Type = 'General' ORDER BY Command;");
				ResultSet player = db.executeQuery("SELECT * FROM Help WHERE Type = 'Player' ORDER BY Command;");
				ResultSet media = db.executeQuery("SELECT * FROM Help WHERE Type = 'Media' ORDER BY Command;");
				ResultSet youtube = db
						.executeQuery("SELECT * FROM Help WHERE Type = 'Youtube Account' ORDER BY Command;");
				ResultSet faces = db.executeQuery("SELECT * FROM Help WHERE Type = 'Faces' ORDER BY Command;");
				builder.setTitle("COMMANDS:");
				String s = "";
				while (gen.next()) {
					s += Ref.prefix + gen.getString(1) + "\n";
				}
				builder.addField("General", s, true);
				s = "";
				while (faces.next()) {
					s += Ref.prefix + faces.getString(1) + "\n";
				}
				builder.addField("Faces", s, true);
				s = "";
				while (player.next()) {
					s += Ref.prefix + player.getString(1) + "\n";
				}
				builder.addField("Player", s, true);
				s = "";
				while (media.next()) {
					s += Ref.prefix + media.getString(1) + "\n";
				}
				builder.addField("Movies/Shows", s, true);
				s = "";
				while (youtube.next()) {
					s += Ref.prefix + youtube.getString(1) + "\n";
				}
				builder.addField("Youtube", s, true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		builder.setThumbnail("https://i.gifer.com/33HU.gif");
		builder.setFooter("Created By Wasabi");
		user.openPrivateChannel().complete().sendMessage(builder.build()).queue();
		us.updateScore(user, 1);
		successUpdateGUI(gui, user, msgSplit[0]);
	}

	public void cleanup(String command, MessageChannel msgCha, User user, GUI gui) {
		List<Message> messages = msgCha.getHistory().retrievePast(100).complete();
		for (Message m : messages) {
			if (m.getContentRaw().startsWith(Ref.prefix) || m.getAuthor().getId().equals(Ref.bot_snowflake_id)) {
				msgCha.deleteMessageById(m.getId()).complete();
			}
		}
		us.updateScore(user, 1);
		successUpdateGUI(gui, user, command);
	}

	@SuppressWarnings("rawtypes")
	public void scores(String command, User user, MessageChannel msgCha, GUI gui) {
		ArrayList<Pair<String, Integer>> sorted = us.getSortedScoreboard();
		builder.setTitle("Scoreboard:");
		for (Pair pair : sorted) {
			builder.appendDescription(pair.fst + " - " + pair.snd + "\n");
		}
		msgCha.sendMessage(builder.build()).queue();
		successUpdateGUI(gui, user, command);
	}

	public void team(String[] msgSplit, MessageReceivedEvent e, User user, MessageChannel msgCha, GUI gui) {
		if (msgSplit.length > 1) {
			String s = msgSplit[1];
			int i;
			if (Character.isDigit(s.charAt(0))) {
				i = Integer.parseInt(s);
				new TeamMaker().teamFiller(e, i, builder, gui);
				us.updateScore(user, 1);
			} else {
				builder.setTitle("Format of command is: !!team __***number***__ __***mentions(optional)***__");
				msgCha.sendMessage(builder.build()).queue();
			}
		} else {
			builder.setTitle("Format of command is: !!team __***number***__ __***mentions(optional)***__");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void tts(String raw, String command, User user, MessageChannel msgCha, GUI gui) {
		if (!gui.boxFive.isSelected()) {
			StringBuilder ms = new StringBuilder(raw);
			ms.delete(0, 6);
			msgCha.sendMessage(ms).tts(true).queue();
			us.updateScore(user, 1);
			successUpdateGUI(gui, user, command);
		} else {
			builder.setTitle("Text To Speech is disabled.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void quote(Message msg, String command, User user, MessageChannel msgCha, GUI gui) {
		try {
			if (msg.getMentionedUsers().size() == 0) {
				ResultSet rs = db.executeQuery("SELECT Quote, Name, Date FROM Quotes ORDER BY random() LIMIT 1;");
				builder.setTitle(rs.getString(2) + " - " + rs.getInt(3));
				builder.setDescription("***" + rs.getString(1) + "***");
			} else {
				ResultSet rs = db.executeQuery(
						"SELECT Quote, Name, Date FROM " + "(SELECT Quote, Name, Date FROM Quotes WHERE SnowflakeID = "
								+ msg.getMentionedUsers().get(0).getId() + ") ORDER BY random() LIMIT 1;");
				if (rs.next()) {
					builder.setTitle(rs.getString(2) + " - " + rs.getInt(3));
					builder.setDescription("***" + rs.getString(1) + "***");
				} else {
					builder.setTitle("No quotes found by " + msg.getMentionedUsers().get(0).getName());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		msgCha.sendMessage(builder.build()).queue();
		us.updateScore(user, 1);
		successUpdateGUI(gui, user, command);
	}

	public void randomFaceOrMeme(String folderName, String command, User user, MessageChannel msgCha, GUI gui) {
		msgCha.sendFile(randomMeme(new File(System.getProperty("user.dir") + "/images/" + folderName))).queue();
		us.updateScore(user, 1);
		successUpdateGUI(gui, user, command);
	}

	public void sendTextAndFile(String text, String filename, String command, User user, MessageChannel msgCha,
			GUI gui) {
		builder.setTitle(text);
		msgCha.sendMessage(builder.build()).queue();
		msgCha.sendFile(new File("images/" + filename)).queue();
		us.updateScore(user, 1);
		successUpdateGUI(gui, user, command);
	}

	public void playSoundEffect(String filename, String command, User user, Member member, MessageReceivedEvent e, GUI gui) {
		manager.loadAndPlay(e.getTextChannel(), member, "sound/" + filename);
		us.updateScore(user, 1);
		successUpdateGUI(gui, user, command);
	}

	public void dank(String command, User user, MessageChannel msgCha, GUI gui) {
		try {
			String link = imgur.parseImgurLink(imgur.GET());
			msgCha.sendMessage(link).queue();
			us.updateScore(user, 1);
			successUpdateGUI(gui, user, command);
		} catch (IOException e) {
			System.out.println("Error: Imgur GET");
			e.printStackTrace();
		}
	}

	public void play(String[] msgSplit, String raw, User user, MessageChannel msgCha, Member member, MessageReceivedEvent e, GUI gui) {
		String youtubeLink = "https://www.youtube.com/watch?v=";
		if (!gui.boxTwo.isSelected()) {
			String video;
			if (msgSplit.length > 1) {
				video = raw.substring(7);
			} else {
				builder.setTitle("Tell me what to play.");
				msgCha.sendMessage(builder.build()).queue();
				return;
			}
			try {
				List<SearchResult> searchResultList = youtube.searchVideos(video, 1);
				if (searchResultList != null && searchResultList.size() > 0) {
					SearchResult result = searchResultList.get(0);
					if(member.getVoiceState().getChannel() == null) { 
						builder.setTitle("Connect to a voice channel first!");
						msgCha.sendMessage(builder.build()).queue();
					}
					manager.loadAndPlay(e.getTextChannel(), member, youtubeLink + result.getId().getVideoId());
					if (!gui.boxSix.isSelected()) {
						String songString = result.getSnippet().getTitle();
						String[] sp = songString.split("([\\[|(])");
						String complete = sp[0];
						builder.setTitle(sp[0]);
						builder.setThumbnail(result.getSnippet().getThumbnails().getDefault().getUrl());
						String lyricsURL = parser.googleLyrics(complete);
						String lyrics = parser.getLyrics(lyricsURL);
						if (lyrics.length() > 2048) {
							builder.setDescription(lyrics.substring(0, 2044) + "\n...");
							if (lyricsURL != null) {
								builder.addField("For More:", "[" + lyricsURL + "]" + "(" + lyricsURL + ")", false);
							}
							msgCha.sendMessage(builder.build()).queue();
						} else {
							builder.setDescription(lyrics + "\n");
							if (lyricsURL != null) {
								builder.addField("Lyrics:", "[" + lyricsURL + "]" + "(" + lyricsURL + ")", false);
							}
							msgCha.sendMessage(builder.build()).queue();
						}
					} else {
						builder.setTitle("Lyrics are disabled.");
						msgCha.sendMessage(builder.build()).queue();
					}
					gui.textArea.append(user.getName() + " used !!play: \n" + "\t - " + result.getSnippet().getTitle()
							+ ". \t ID: " + result.getId().getVideoId() + "\n");
					us.updateScore(user, 1);
				} else {
					builder.setTitle("Nothing found by that name! :(");
					msgCha.sendMessage(builder.build()).queue();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			builder.setTitle("Music player is unavailable.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void volume(String command, String[] msgSplit, User user, MessageChannel msgCha, MessageReceivedEvent e,
			GUI gui) {
		if (!gui.boxTwo.isSelected()) {
			if (!gui.boxThree.isSelected()) {
				if (msgSplit.length > 1) {
					int value;
					try {
						value = Integer.parseInt(msgSplit[1]);
					} catch (NumberFormatException e1) {
						builder.setTitle("I need a number for the volume!");
						msgCha.sendMessage(builder.build()).queue();
						return;
					}
					if(value > 200) {
						builder.setTitle("Max volume is 200!");
						msgCha.sendMessage(builder.build()).queue();
						return;
					} else if(value < 0) {
						value = 0;
					}
					manager.setVolume(e.getTextChannel(), value);
					gui.textArea.append(user.getName() + " set volume to " + value + ".\n");
					gui.textVolume.setText("Volume: " + value);
					if (value == 69) {
						builder.setTitle("hehehe 69. ;D");
						msgCha.sendMessage(builder.build()).queue();
						gui.textVolume.setText("Volume: " + value);
					}
					us.updateScore(user, 1);
				} else {
					builder.setTitle("Volume needs to be set to a value!");
					msgCha.sendMessage(builder.build()).queue();
					failureUpdateGUI(gui, user, command);
				}
			} else {
				builder.setTitle("Volume is locked.");
				msgCha.sendMessage("Volume is locked.").queue();
			}
		} else {
			builder.setTitle("Music player is unavailable.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void skip(String command, User user, MessageChannel msgCha, MessageReceivedEvent e, GUI gui) {
		if (!gui.boxTwo.isSelected()) {
			if (manager.getPlayingTrack(e.getTextChannel()) != null) {
				manager.skipTrack(e.getTextChannel());
				us.updateScore(user, 1);
				successUpdateGUI(gui, user, command);
			} else {
				builder.setTitle("Nothing to skip.");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
			}
		} else {
			builder.setTitle("Music player is unavailable.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void stop(String command, User user, MessageChannel msgCha, MessageReceivedEvent e, GUI gui) {
		if (!gui.boxTwo.isSelected()) {
			manager.stop(e.getTextChannel());
			us.updateScore(user, 1);
			successUpdateGUI(gui, user, command);
		} else {
			builder.setTitle("Music player is unavailable.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void queue(String command, User user, MessageChannel msgCha, MessageReceivedEvent e, GUI gui) {
		if (!gui.boxTwo.isSelected()) {
			if (manager.getPlayingTrack(e.getTextChannel()) != null) {
				int i = 1;
				builder.setTitle("QUEUE:");
				gui.textArea.append(user.getName() + " used !!queue and returned: \n");
				if (manager.getPlayingTrack(e.getTextChannel()) != null) {
					builder.setTitle((i + " - " + manager.getPlayingTrack(e.getTextChannel()).getInfo().title
							+ "   <---   NOW PLAYING"));
					gui.textArea.append("\t" + i + " - " + manager.getPlayingTrack(e.getTextChannel()).getInfo().title
							+ "<- Playing \n");
				}
				i++;
				if (manager.getQueue(e.getTextChannel()).size() != 0) {
					for (AudioTrack audio : manager.getQueue(e.getTextChannel())) {
						builder.appendDescription(i + " - " + audio.getInfo().title + "\n");
						gui.textArea.append("\t" + i + " - " + audio.getInfo().title + "\n");
						i++;
					}
				}
				msgCha.sendMessage(builder.build()).queue();
				us.updateScore(user, 1);
			} else {
				builder.setTitle("Queue is empty.");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
			}
		} else {
			builder.setTitle("Music player is unavailable.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void clear(String command, User user, MessageChannel msgCha, MessageReceivedEvent e, GUI gui) {
		if (!gui.boxTwo.isSelected()) {
			builder.setTitle("Queue cleared.");
			manager.clearQueue(e.getTextChannel());
			msgCha.sendMessage(builder.build()).queue();
			us.updateScore(user, 1);
			successUpdateGUI(gui, user, command);
		} else {
			builder.setTitle("Music player is unavailable.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void sub(String[] msgSplit, String raw, User user, MessageChannel msgCha, GUI gui) {
		if (youtube.isAuth()) {
			if (msgSplit.length > 1) {
				String channel = raw.substring(6);
				String[] info = youtube.subscribe(channel);
				if (info.length > 1) {
					builder.setTitle("Subbed to:");
					builder.setDescription(info[0]);
					builder.setThumbnail(info[1]);
					msgCha.sendMessage(builder.build()).queue();
					us.updateScore(user, 1);
					gui.textArea.append(user.getName() + " subbed to " + info[0] + ".\n");
				}
			} else {
				builder.setTitle("Name a channel to subscribe to.");
				msgCha.sendMessage(builder.build()).queue();
			}
		} else {
			builder.setTitle("Youtube account is not available.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void unsub(String[] msgSplit, String raw, User user, MessageChannel msgCha, GUI gui) {
		if (youtube.isAuth()) {
			if (msgSplit.length > 1) {
				String channel = raw.substring(8);
				String[] info = youtube.unsubscribe(channel);
				if (info.length > 1) {
					builder.setTitle("Unsubbed from:");
					builder.setDescription(info[0]);
					builder.setThumbnail(info[1]);
					msgCha.sendMessage(builder.build()).queue();
					us.updateScore(user, 1);
					gui.textArea.append(user.getName() + " unsubbed from " + info[0] + ".\n");
				}
			} else {
				builder.setTitle("Name a channel to unsubscribe to.");
				msgCha.sendMessage(builder.build()).queue();
			}
		} else {
			builder.setTitle("Youtube account is not available.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	@SuppressWarnings("unchecked")
	public void sublist(String command, User user, MessageChannel msgCha, GUI gui) {
		if (youtube.isAuth()) {
			ArrayList<String> subscriptions = (ArrayList<String>) youtube.getSubscriptions(true);
			if (!subscriptions.isEmpty()) {
				builder.setTitle("Subscriptions:");
				for (String sub : subscriptions) {
					builder.appendDescription(sub + "\n");
				}
				msgCha.sendMessage(builder.build()).queue();
				us.updateScore(user, 1);
				successUpdateGUI(gui, user, command);
			} else {
				builder.setTitle("No subscriptions.");
				msgCha.sendMessage(builder.build()).queue();
			}
		} else {
			builder.setTitle("Youtube account is not available.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void videos(String command, User user, MessageChannel msgCha, GUI gui) {
		if (youtube.isAuth()) {
			ArrayList<YoutubeVideo> videos = youtube.listNewVideos();
			if (videos.size() > 0) {
				builder.setTitle("New videos:");
				for (YoutubeVideo video : videos) {
					builder.appendDescription("[" + video.getAuthor() + " - " + video.getTitle() + "]" + "("
							+ video.getFullLink() + ")\n");
				}
				msgCha.sendMessage(builder.build()).queue();
				us.updateScore(user, 1);
				successUpdateGUI(gui, user, command);
			} else {
				builder.setTitle("No recent videos.");
				msgCha.sendMessage(builder.build()).queue();
			}
		} else {
			builder.setTitle("Youtube account is not available.");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void searchMovie(String query, String command, User user, MessageChannel msgCha, GUI gui) {
		ArrayList<Media> media = new ArrayList<>();
		media = new MovieRequest().searchMovie(query);
		if (media.size() == 0) {
			builder.setTitle("No results found");
			msgCha.sendMessage(builder.build()).queue();
		}
		for (int i = 0; i < media.size(); i++) {
			if (i < MAX_RESULTS) {
				builder.setTitle(media.get(i).getTitle());
				builder.setImage(media.get(i).getPoster());
				builder.addField("Year", media.get(i).getYear(), true);
				builder.addField("Type", media.get(i).getType(), true);
				builder.addField("IMDB ID", "[" + media.get(i).getImdbID().toUpperCase()
						+ "](https://www.imdb.com/title/" + media.get(i).getImdbID() + ")", true);
				msgCha.sendMessage(builder.build()).queue();
				builder.clear();
				builder.setColor(13761099);
				us.updateScore(user, 1);
				successUpdateGUI(gui, user, command);
			}
		}
	}

	public void addMovie(String rmComm, String command, User user, MessageChannel msgCha, GUI gui) {
		MovieRequest rq = new MovieRequest();
		Pattern p = Pattern.compile("^tt+[0-9]{5,}");
		Matcher m = p.matcher(rmComm);
		String data = "";
		if (m.find()) {
			data = rq.getMovieByID(m.group());
		} else {
			data = rq.getMovieByTitle(rmComm);
		}
		JsonObject obj = new JsonParser().parse(data).getAsJsonObject();
		if (obj.get("Response").getAsString().equals("True")) {
			JsonArray array = obj.get("Ratings").getAsJsonArray();
			String ratings = "";
			for (int i = 0; i < array.size(); i++) {
				JsonObject r = array.get(i).getAsJsonObject();
				if (i == array.size())
					ratings += r.get("Source").getAsString() + " - " + r.get("Value").getAsString();
				else
					ratings += r.get("Source").getAsString() + " - " + r.get("Value").getAsString() + ", ";
			}
			Media media = new Media(obj.get("Title").getAsString(), obj.get("Year").getAsString(),
					obj.get("Rated").getAsString(), obj.get("Released").getAsString(), obj.get("Runtime").getAsString(),
					obj.get("Genre").getAsString(), obj.get("Director").getAsString(), obj.get("Writer").getAsString(),
					obj.get("Actors").getAsString(), obj.get("Plot").getAsString(), obj.get("Language").getAsString(),
					obj.get("Country").getAsString(), obj.get("Awards").getAsString(), obj.get("Poster").getAsString(),
					ratings, obj.get("Metascore").getAsString(), obj.get("imdbRating").getAsString(),
					obj.get("imdbVotes").getAsString(), obj.get("imdbID").getAsString(), obj.get("Type").getAsString(),
					obj.get("DVD").getAsString(), "N/A", obj.get("BoxOffice").getAsString(),
					obj.get("Production").getAsString(), obj.get("Website").getAsString(), "NOT WATCHED", "0");
			if(obj.get("Poster").getAsString().equals("N/A")) {
				media.setPoster(Media.NOT_FOUND);
			}
			if (obj.get("Type").getAsString().equals("series")) {
				media.setDvdRelease("N/A");
				media.setTotalSeasons(obj.get("totalSeasons").getAsString());
				media.setBoxOffice("N/A");
				media.setProduction("N/A");
				media.setWebsite("N/A");
			}
			if (obj.get("Type").getAsString().equals("game")) {
				media.setRatings("N/A");
				media.setDvdRelease("N/A");
				media.setBoxOffice("N/A");
				media.setProduction("N/A");
				media.setWebsite("N/A");
			}
			try {
				ResultSet rs = db.executeQuery("SELECT * FROM Movies WHERE SnowflakeID = \"" + user.getId()
						+ "\" AND IMDB_ID = \"" + media.getImdbID() + "\";");
				if (!rs.next()) {
					db.executeUpdate("INSERT INTO Movies VALUES (\"" + user.getId() + "\", " + media.toString() + ");");
					builder.setTitle(user.getName() + " added:");
					builder.addField(media.getTitle(), media.getPlot(), false);
					builder.addField("Year", media.getYear(), true);
					builder.addField("Runtime", media.getRuntime(), true);
					builder.addField("IMDB",
							"[" + media.getImdbRating() + "](https://www.imdb.com/title/" + media.getImdbID() + ")",
							true);
					builder.addField("Director", media.getDirector().replace(",", "\n"), true);
					builder.addField("Actors", media.getActors().replace(",", "\n"), true);
					builder.addField("Genre", media.getGenre(), true);
					builder.addField("Ratings", media.getRatings().replace(",", "\n"), true);
					if (media.getType().equals("series")) {
						builder.addField("Seasons", media.getTotalSeasons(), true);
					}
					builder.addField("Type", media.getType(), true);
					if (media.getPoster() != null || media.getPoster() != "N/A") {
						builder.setImage(media.getPoster());
					}
					msgCha.sendMessage(builder.build()).queue();
					us.updateScore(user, 1);
					successUpdateGUI(gui, user, command);
				} else {
					builder.setTitle("Movie already in your list!");
					msgCha.sendMessage(builder.build()).queue();
					failureUpdateGUI(gui, user, command);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			builder.setTitle("Couldn't find anything by that title/ID");
			msgCha.sendMessage(builder.build()).queue();
		}
	}

	public void getMovieList(String command, User user, MessageChannel msgCha, GUI gui) {
		try {
			ResultSet rs = db
					.executeQuery("SELECT Title, Year, IMDB_ID, Watched, WatchTimes FROM Movies WHERE SnowflakeID = \""
							+ user.getId() + "\";");
			builder.setTitle(user.getName() + " movie list:");
			while (rs.next()) {
				if (!rs.getString(5).equals("0")) {
					builder.appendDescription(
							rs.getString(1) + " - " + rs.getString(2) + " - [IMDB](https://www.imdb.com/title/"
									+ rs.getString(3) + ") (" + rs.getString(4) + " x" + rs.getString(5) + ")\n");
				} else {
					builder.appendDescription(
							rs.getString(1) + " - " + rs.getString(2) + " - [IMDB](https://www.imdb.com/title/"
									+ rs.getString(3) + ") (" + rs.getString(4) + ")\n");
				}
			}
			if (builder.getDescriptionBuilder().length() == 0) {
				builder.setDescription("***List empty!***");
			}
			msgCha.sendMessage(builder.build()).queue();
			us.updateScore(user, 1);
			successUpdateGUI(gui, user, command);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeMovie(String rmComm, String command, User user, MessageChannel msgCha, GUI gui) {
		MovieRequest rq = new MovieRequest();
		Pattern p = Pattern.compile("^tt+[0-9]{5,}");
		Matcher m = p.matcher(rmComm);
		String data = "";
		JsonObject obj = null;
		if (m.find()) {
			data = m.group();
		} else {
			obj = new JsonParser().parse(rq.getMovieByTitle(rmComm)).getAsJsonObject();
			if (obj.get("Response").getAsString().equals("True")) {
				data = obj.get("imdbID").getAsString();
			} else {
				builder.setTitle("Couldn't find a movie in your list with that title");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
				return;
			}
		}
		if (!data.equals("")) {
			try {
				ResultSet rs = db
						.executeQuery("SELECT Title, IMDB_ID, Poster, Watched FROM Movies WHERE SnowflakeID = \""
								+ user.getId() + "\" AND IMDB_ID = \"" + data + "\";");
				if (rs.next()) {
					builder.setTitle("Deleted from " + user.getName() + " list:");
					builder.setDescription(rs.getString(1) + " - " + "[IMDB](https://www.imdb.com/title/"
							+ rs.getString(2) + ") - (" + rs.getString(4) + ")");
					builder.setImage(rs.getString(3));
					db.executeUpdate("DELETE FROM Movies WHERE SnowflakeID = \"" + user.getId() + "\" AND IMDB_ID = \""
							+ data + "\";");
					msgCha.sendMessage(builder.build()).queue();
					us.updateScore(user, 1);
					successUpdateGUI(gui, user, command);
				} else {
					builder.setTitle("Couldn't find a movie in your list with that Title/ID");
					msgCha.sendMessage(builder.build()).queue();
					failureUpdateGUI(gui, user, command);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			builder.setTitle("Couldn't find a movie in your list with that Title/ID");
			msgCha.sendMessage(builder.build()).queue();
			failureUpdateGUI(gui, user, command);
		}
	}

	public void watch(String rmComm, String command, User user, MessageChannel msgCha, GUI gui) {
		MovieRequest rq = new MovieRequest();
		Pattern p = Pattern.compile("^tt+[0-9]{5,}");
		Matcher m = p.matcher(rmComm);
		String data = "";
		JsonObject obj = null;
		if (m.find()) {
			data = m.group();
		} else {
			obj = new JsonParser().parse(rq.getMovieByTitle(rmComm)).getAsJsonObject();
			if (obj.get("Response").getAsString().equals("True")) {
				data = obj.get("imdbID").getAsString();
			} else {
				builder.setTitle("Couldn't find a movie in your list with that title");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
			}
		}
		try {
			ResultSet rs = db.executeQuery(
					"SELECT Title, IMDB_ID, Poster, Watched, WatchTimes FROM Movies WHERE SnowflakeID = \""
							+ user.getId() + "\" AND IMDB_ID = \"" + data + "\";");
			if (rs.next()) {
				db.executeUpdate(
						"UPDATE Movies SET Watched = 'WATCHED', WatchTimes = WatchTimes + 1 WHERE SnowflakeID = \""
								+ user.getId() + "\" AND IMDB_ID = \"" + data + "\";");
				builder.setTitle(user.getName().toUpperCase() + " WATCHED:");
				int watchTimes = Integer.parseInt(rs.getString(5)) + 1;
				builder.setDescription(rs.getString(1) + " - " + "[IMDB](https://www.imdb.com/title/" + rs.getString(2)
						+ ") (WATCHED x" + watchTimes + ")");
				builder.setImage(rs.getString(3));
				msgCha.sendMessage(builder.build()).queue();
				us.updateScore(user, 1);
				successUpdateGUI(gui, user, command);
			} else {
				builder.setTitle("Couldn't find a movie in your list with that title");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void unwatch(String rmComm, String command, User user, MessageChannel msgCha, GUI gui) {
		MovieRequest rq = new MovieRequest();
		Pattern p = Pattern.compile("^tt+[0-9]{5,}");
		Matcher m = p.matcher(rmComm);
		String data = "";
		JsonObject obj = null;
		if (m.find()) {
			data = m.group();
		} else {
			obj = new JsonParser().parse(rq.getMovieByTitle(rmComm)).getAsJsonObject();
			if (obj.get("Response").getAsString().equals("True")) {
				data = obj.get("imdbID").getAsString();
			} else {
				builder.setTitle("Couldn't find a movie in your list with that title");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
			}
		}
		try {
			ResultSet rs = db.executeQuery("SELECT Title, IMDB_ID, Poster, Watched FROM Movies WHERE SnowflakeID = \""
					+ user.getId() + "\" AND IMDB_ID = \"" + data + "\";");
			if (rs.next()) {
				if (rs.getString(4).equals("WATCHED")) {
					db.executeUpdate(
							"UPDATE Movies SET Watched = 'NOT WATCHED', WatchTimes = '0' WHERE SnowflakeID = \""
									+ user.getId() + "\" AND IMDB_ID = \"" + data + "\";");
					builder.setTitle(user.getName().toUpperCase() + " UNWATCHED:");
					builder.setDescription(rs.getString(1) + " - " + "[IMDB](https://www.imdb.com/title/"
							+ rs.getString(2) + ") (NOT WATCHED)");
					builder.setImage(rs.getString(3));
					msgCha.sendMessage(builder.build()).queue();
					us.updateScore(user, 1);
					successUpdateGUI(gui, user, command);
				} else {
					builder.setTitle("You have not seen " + rs.getString(1) + " yet");
					msgCha.sendMessage(builder.build()).queue();
					failureUpdateGUI(gui, user, command);
				}
			} else {
				builder.setTitle("Couldn't find a movie in your list with that title");
				msgCha.sendMessage(builder.build()).queue();
				failureUpdateGUI(gui, user, command);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void successUpdateGUI(GUI gui, User user, String command) {
		gui.textArea.append(user.getName() + " used " + command + " \n");
	}

	private void failureUpdateGUI(GUI gui, User user, String command) {
		gui.textArea.append(user.getName() + " used " + command + " but failed. \n");
	}

	private File randomMeme(File pasta) {
		File[] listOfFiles = pasta.listFiles();
		Random rand = new Random();
		assert listOfFiles != null : pasta.getName() + " may be empty";
		return listOfFiles[rand.nextInt(listOfFiles.length)];
	}

	public EmbedBuilder getEmbedBuilder() {
		return builder;
	}

}
