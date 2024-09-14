package youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Activities;
import com.google.api.services.youtube.YouTube.Subscriptions;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.common.collect.Lists;
import main.Ref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YoutubeAccount {

	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static YouTube youtube;
	private static YouTube.Search.List search;
	private static final long NUMBER_OF_CHANNELS_RETURNED = 1;
	private boolean auth;

	public YoutubeAccount(boolean auth) {
		this.auth = auth;
		if(auth) {
			List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
			try {
				Credential credential = Auth.authorize(scopes, "auth");
				youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
						.setApplicationName("youtube-cmdline-auth-sample").build();
			} catch (GoogleJsonResponseException e) {
				System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
						+ e.getDetails().getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("IOException: " + e.getMessage());
				e.printStackTrace();
			} catch (Throwable t) {
				System.err.println("Throwable: " + t.getMessage());
				t.printStackTrace();
			}
		} else {
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
				public void initialize(HttpRequest request) throws IOException {}
			}).setApplicationName("youtube-cmdline-search-sample").build();
		}
	}

	private List<SearchResult> searchChannel(String channelName, long maxResults) throws IOException {
		List<SearchResult> searchResultList = null;

		search = youtube.search().list("id,snippet");
		search.setKey(Ref.youtube_token);
		search.setType("channel");
		search.setFields("items(id,snippet/title,snippet/thumbnails/default/url)");
		search.setMaxResults(maxResults);
		search.setQ(channelName);
		SearchListResponse searchResponse = search.execute();
		searchResultList = searchResponse.getItems();

		return searchResultList;
	}

	public List<SearchResult> searchVideos(String name, long maxResults) throws IOException {
		List<SearchResult> searchResultList = null;

		search = youtube.search().list("id,snippet");
		search.setKey(Ref.youtube_token);
		search.setType("video");
		search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
		search.setMaxResults(maxResults);
		search.setQ(name);
		SearchListResponse searchResponse = search.execute();
		searchResultList = searchResponse.getItems();

		return searchResultList;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<YoutubeVideo> listNewVideos() {
		Date date = new Date();
		long yesterday = date.getTime() - TimeUnit.DAYS.toMillis(2);
		ArrayList<YoutubeVideo> data = new ArrayList<YoutubeVideo>();
		
		try {
			ArrayList<Subscription> subscriptions = (ArrayList<Subscription>) getSubscriptions(false);
			Activities activities = youtube.activities();
			
			for (Subscription sub : subscriptions) {
				YouTube.Activities.List activityList = activities.list("id,snippet,contentDetails");
				activityList.setChannelId(sub.getSnippet().getResourceId().getChannelId());
				activityList.setPublishedAfter(new DateTime(yesterday, 1));
				activityList.setMaxResults((long) 1);
				ActivityListResponse resp = activityList.execute();
				
				for (Activity act : resp.getItems()) {
					if(act.getSnippet().getType().equals("upload")) {
						YoutubeVideo video = new YoutubeVideo(act.getContentDetails().getUpload().getVideoId(), act.getSnippet().getTitle(), act.getSnippet().getChannelTitle());
						data.add(video);
					}
				}
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}

	public ArrayList<?> getSubscriptions(boolean onlyNames) {
		ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
		ArrayList<String> subsNames = new ArrayList<String>();
		String pageToken = null;
		try {
			Subscriptions subs = youtube.subscriptions();
			YouTube.Subscriptions.List subscribers;
			subscribers = subs.list("id,snippet").setMine(true).setMaxResults((long) 50);
			SubscriptionListResponse resp = subscribers.execute();

			for (Subscription sub : resp.getItems()) {
				subscriptions.add(sub);
				subsNames.add(sub.getSnippet().getTitle());
			}

			if (resp.getNextPageToken() != null)
				pageToken = resp.getNextPageToken();

			while (pageToken != null) {
				SubscriptionListResponse nextPage = subscribers.execute();

				for (Subscription sub : nextPage.getItems()) {
					subscriptions.add(sub);
					subsNames.add(sub.getSnippet().getTitle());
				}
				pageToken = nextPage.getNextPageToken();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (onlyNames == true) {
			return subsNames;
		}

		return subscriptions;
	}

	@SuppressWarnings("unchecked")
	public String[] unsubscribe(String channelName) {
		String[] info = new String[2];
		try {
			SearchResult result = searchChannel(channelName, NUMBER_OF_CHANNELS_RETURNED).get(0);

			ArrayList<Subscription> subscriptions = (ArrayList<Subscription>) getSubscriptions(false);

			for (Subscription sub : subscriptions) {
				if (sub.getSnippet().getTitle().equals(result.getSnippet().getTitle())) {
					info[0] = sub.getSnippet().getTitle();
					info[1] = result.getSnippet().getThumbnails().getDefault().getUrl();
					YouTube.Subscriptions.Delete subscriptionDelete = youtube.subscriptions().delete(sub.getId());
					subscriptionDelete.execute();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return info;
	}

	public String[] subscribe(String channelName) {
		String[] info = new String[2];
		String channelId;
		try {
			SearchResult result = searchChannel(channelName, NUMBER_OF_CHANNELS_RETURNED).get(0);
			channelId = result.getId().getChannelId();

			ResourceId resourceId = new ResourceId();
			resourceId.setChannelId(channelId);
			resourceId.setKind("youtube#channel");

			SubscriptionSnippet snippet = new SubscriptionSnippet();
			snippet.setResourceId(resourceId);

			Subscription subscription = new Subscription();
			subscription.setSnippet(snippet);

			YouTube.Subscriptions.Insert subscriptionInsert = youtube.subscriptions().insert("snippet,contentDetails",
					subscription);
			subscriptionInsert.execute();

			info[0] = result.getSnippet().getTitle();
			info[1] = result.getSnippet().getThumbnails().getDefault().getUrl();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return info;
	}

	public boolean isAuth() {
		return auth;
	}
}
