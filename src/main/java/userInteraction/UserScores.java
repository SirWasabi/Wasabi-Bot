package userInteraction;

import database.Database;
import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import Utils.Pair;

public class UserScores {

	Database db;
	String TABLE_NAME = "Scores";

	public UserScores(Database db) {
		this.db = db;
	}

	public void updateScore(User user, int points) {
		try {
			db.executeUpdate("UPDATE " + TABLE_NAME + " SET Score = Score + " + points + ", DiscordName = \""
					+ user.getName() + "\" WHERE SnowflakeID = " + user.getId() + ";");
		} catch (SQLException e) {
			try {
				db.executeUpdate("INSERT INTO " + TABLE_NAME + " VALUES (" + user.getId() + ", \"" + user.getName()
						+ "\", " + points + ");");
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
	}

	public ArrayList<Pair<String, Integer>> getSortedScoreboard() {
		try {
			ArrayList<Pair<String, Integer>> array = new ArrayList<>();
			ResultSet rs = db.executeQuery("SELECT DiscordName, Score FROM " + TABLE_NAME + " ORDER BY Score DESC;");
			while (rs.next()) {
				String name = rs.getString("DiscordName");
				Integer score = rs.getInt("Score");
				array.add(new Pair<String, Integer>(name, score));
			}
			return array;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
