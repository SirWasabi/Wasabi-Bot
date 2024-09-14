package lyrics;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class LyricsParser {

	private final String google = "https://www.google.pt/search?q=site:azlyrics.com+";
	public final String base = "https://www.azlyrics.com/lyrics/";
	private final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31";

	public String googleLyrics(String query) throws IOException {
		String website = google + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
		Document doc = Jsoup.connect(website).userAgent(userAgent).cookie("CONSENT", "YES+cb.20210720-07-p0.en+FX+410").ignoreContentType(true).ignoreHttpErrors(true).get();

		Elements e = doc.select("a");
		String link = null;

		for (Element el : e) {
			if (el.attr("href").contains(base)) {
				Pattern pattern = Pattern.compile("https(.*)html");
				Matcher matcher = pattern.matcher(el.attr("href"));
				if (matcher.find()) {
					link = matcher.group();
				}
				break;
			}
		}

		return link;
	}

	public String getLyrics(String format) throws IOException {
		if (format != null) {

			Document doc = Jsoup.connect(format).userAgent(userAgent).ignoreContentType(true).ignoreHttpErrors(true)
					.get();

			//System.out.println(doc.outputSettings().prettyPrint(true));
			Element e = doc.getElementsByClass("col-xs-12 col-lg-8 text-center").select("div").get(6);

			if (e != null && e.hasText()) {
				String lyrics = e.html();
				String clean = Jsoup.clean(lyrics, "", Whitelist.none().addTags("br", "p"), doc.outputSettings());
				return Jsoup.clean(clean, "", Whitelist.none(), doc.outputSettings().prettyPrint(false));
			} else {
				return "No lyrics found...";
			}
		} else {
			return "No lyrics found...";
		}
	}
}
