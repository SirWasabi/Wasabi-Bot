package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.Ref;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;

public class MovieRequest {

	private OkHttpClient client = new OkHttpClient();
	private String url = "http://www.omdbapi.com/?apikey=";

	public ArrayList<Media> searchMovie(String name) {
		ArrayList<Media> media = new ArrayList<>();
		String link = url + Ref.omdb_token + "&s=" + name;
		try {

			Request request = new Request.Builder().url(link).build();
			Response response = client.newCall(request).execute();
			String data = response.body().string();
			JsonObject obj = new JsonParser().parse(data).getAsJsonObject();
			System.out.println(obj);
			if (obj.get("Response").getAsString().equals("True")) {
				
				JsonArray search = obj.get("Search").getAsJsonArray();

				for (JsonElement mov : search) {
					JsonObject movie = mov.getAsJsonObject();
					String title = movie.get("Title").getAsString();
					String year = movie.get("Year").getAsString();
					String imdbID = movie.get("imdbID").getAsString();
					String type = movie.get("Type").getAsString();
					String poster;
					if (movie.get("Poster").getAsString().equals("N/A"))
						poster = Media.NOT_FOUND;
					else
						poster = movie.get("Poster").getAsString();
					
					media.add(new Media(title, year, poster, imdbID, type));
				}
			}

			response.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return media;
	}

	public String getMovieByID(String id) {
		String link = url + Ref.omdb_token + "&i=" + id;
		try {

			Request request = new Request.Builder().url(link).build();
			Response response = client.newCall(request).execute();
			String data = response.body().string();
			response.close();
			return data;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getMovieByTitle(String title) {
		String link = url + Ref.omdb_token + "&t=" + title;
		try {

			Request request = new Request.Builder().url(link).build();
			Response response = client.newCall(request).execute();
			String data = response.body().string();
			response.close();
			return data;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
