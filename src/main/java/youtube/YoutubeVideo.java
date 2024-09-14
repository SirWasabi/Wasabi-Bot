package youtube;

public class YoutubeVideo {

	private String id = null;
	private String title = null;
	private String author = null;
	private String description = null;
	private String publishedAt = null;
	private String thumbnail = null;

	public YoutubeVideo() {
		
	}
	
	public YoutubeVideo(String id, String title, String author) {
		this.id = id;
		this.title = title;
		this.author = author;
	}
	
	public YoutubeVideo(String id, String title, String author, String description, String publishedAt, String thumbnail) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.description = description;
		this.publishedAt = publishedAt;
		this.thumbnail = thumbnail;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public String getPublishedAt() {
		return publishedAt;
	}

	public String getThumbnail() {
		return thumbnail;
	}
	
	public String getFullLink() {
		return "https://www.youtube.com/watch?v=" + id;
	}
}
