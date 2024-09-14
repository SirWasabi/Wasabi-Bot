package imgur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import main.Ref;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImgurRequest {	
	
	private ArrayList<String> subreddits = new ArrayList<String>();
	
	public ImgurRequest() {
		subreddits.add("dankmemes");
		subreddits.add("blackpeopletwitter");
		subreddits.add("memeeconomy");
		subreddits.add("me_irl");
		subreddits.add("195");
		subreddits.add("meirl");
		subreddits.add("surrealmemes");
		subreddits.add("deepfriedmemes");
		subreddits.add("wackytictacs");
		subreddits.add("bonehurtingjuice");
	}

	public String GET() throws IOException {
		OkHttpClient client = new OkHttpClient();
		int randomPage = ThreadLocalRandom.current().nextInt(0, 49 + 1);
		int randomReddit = ThreadLocalRandom.current().nextInt(0, subreddits.size() - 1);
		String url = "https://api.imgur.com/3/gallery/r/" + subreddits.get(randomReddit) + "/time/" + randomPage;
		Request request = new Request.Builder().url(url).addHeader("Authorization", "Client-ID " + Ref.imgur_client)
				.build();
		Call call = client.newCall(request);
		Response response = call.execute();
		String jsonData = response.body().string();
//		System.out.println(jsonData);
		response.close();
		return jsonData;
	}
	
	public String parseImgurLink(String jsonData) {
		int randomUrl = ThreadLocalRandom.current().nextInt(0, 59 + 1);
		JsonObject jobj = new JsonParser().parse(jsonData).getAsJsonObject();
		JsonArray array = jobj.get("data").getAsJsonArray();
		return array.get(randomUrl).getAsJsonObject().get("link").getAsString();
	}

}
