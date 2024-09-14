package movies;

public class Media {

	public final static String NOT_FOUND = "https://www.publicdomainpictures.net/pictures/280000/velka/not-found-image-15383864787lu.jpg";
	
	private String title = "";
	private String year = "";
	private String rate = "";
	private String releaseDate = "";
	private String runtime = "";
	private String genre = "";
	private String director = "";
	private String writer = "";
	private String actors = "";
	private String plot = "";
	private String language = "";
	private String country = "";
	private String awards = "";
	private String poster = "";
	private String ratings = "";
	private String metascore = "";
	private String imdbRating = "";
	private String imdbVotes = "";
	private String imdbID = "";
	private String type = "";
	private String dvdRelease = "";
	private String totalSeasons = "";
	private String boxOffice = "";
	private String production = "";
	private String website = "";
	private String watched = "";
	private String watchTimes = "";
	
	public Media(String title, String year, String poster, String imdbID, String type) {
		this.title = title;
		this.year = year;
		this.poster = poster;
		this.imdbID = imdbID;
		this.type = type;
	}

	public Media(String title, String year, String rate, String releaseDate, String runtime, String genre,
			String director, String writer, String actors, String plot, String language, String country, String awards,
			String poster, String ratings, String metascore, String imdbRating, String imdbVotes, String imdbID,
			String type, String dvdRelease, String totalSeasons, String boxOffice, String production, String website,
			String watched, String watchTimes) {
		this.title = title.replace("'", "\'");
		this.year = year.replace("'", "\'");
		this.rate = rate.replace("'", "\'");
		this.releaseDate = releaseDate.replace("'", "\'");
		this.runtime = runtime.replace("'", "\'");
		this.genre = genre.replace("'", "\'");
		this.director = director.replace("'", "\'");
		this.writer = writer.replace("'", "\'");
		this.actors = actors.replace("'", "\'");
		this.plot = plot.replace("'", "\'");
		this.language = language.replace("'", "\'");
		this.country = country.replace("'", "\'");
		this.awards = awards.replace("'", "\'");
		this.poster = poster;
		this.ratings = ratings.replace("'", "\'");
		this.metascore = metascore.replace("'", "\'");
		this.imdbRating = imdbRating.replace("'", "\'");
		this.imdbVotes = imdbVotes.replace("'", "\'");
		this.imdbID = imdbID.replace("'", "\'");
		this.type = type.replace("'", "\'");
		this.dvdRelease = dvdRelease.replace("'", "\'");
		this.totalSeasons = totalSeasons.replace("'", "\'");
		this.boxOffice = boxOffice.replace("'", "\'");
		this.production = production.replace("'", "\'");
		this.website = website.replace("'", "\'");
		this.watched = watched.replace("'", "\'");
		this.watchTimes = watchTimes.replace("'", "\'");
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getRuntime() {
		return runtime;
	}

	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getPlot() {
		return plot;
	}

	public void setPlot(String plot) {
		this.plot = plot;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getAwards() {
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getRatings() {
		return ratings;
	}

	public void setRatings(String ratings) {
		this.ratings = ratings;
	}

	public String getMetascore() {
		return metascore;
	}

	public void setMetascore(String metascore) {
		this.metascore = metascore;
	}

	public String getImdbRating() {
		return imdbRating + "/10";
	}

	public void setImdbRating(String imdbRating) {
		this.imdbRating = imdbRating;
	}

	public String getImdbVotes() {
		return imdbVotes;
	}

	public void setImdbVotes(String imdbVotes) {
		this.imdbVotes = imdbVotes;
	}

	public String getImdbID() {
		return imdbID;
	}

	public void setImdbID(String imdbID) {
		this.imdbID = imdbID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDvdRelease() {
		return dvdRelease;
	}

	public void setDvdRelease(String dvdRelease) {
		this.dvdRelease = dvdRelease;
	}

	public String getTotalSeasons() {
		return totalSeasons;
	}

	public void setTotalSeasons(String totalSeasons) {
		this.totalSeasons = totalSeasons;
	}

	public String getBoxOffice() {
		return boxOffice;
	}

	public void setBoxOffice(String boxOffice) {
		this.boxOffice = boxOffice;
	}

	public String getProduction() {
		return production;
	}

	public void setProduction(String production) {
		this.production = production;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getWatched() {
		return watched;
	}

	public void setWatched(String watched) {
		this.watched = watched;
	}

	public String getWatchTimes() {
		return watchTimes;
	}

	public void setWatchTimes(String watchTimes) {
		this.watchTimes = watchTimes;
	}

	@Override
	public String toString() {
		return "\"" + title + "\", \"" + year + "\", \"" + rate + "\", \"" + releaseDate + "\", \"" + runtime + "\", \""
				+ genre + "\", \"" + director + "\", \"" + writer + "\", \"" + actors + "\", \"" + plot + "\", \""
				+ language + "\", \"" + country + "\", \"" + awards + "\", \"" + poster + "\", \"" + ratings + "\", \""
				+ metascore + "\", \"" + imdbRating + "\", \"" + imdbVotes + "\", \"" + imdbID + "\", \"" + type
				+ "\", \"" + dvdRelease + "\", \"" + totalSeasons + "\", \"" + boxOffice + "\", \"" + production
				+ "\", \"" + website + "\", \"" + watched + "\", \"" + watchTimes + "\"";
	}
}