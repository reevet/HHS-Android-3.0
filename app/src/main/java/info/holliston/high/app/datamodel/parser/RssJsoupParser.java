package info.holliston.high.app.datamodel.parser;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.datamodel.Article;

/**
 * Parses XML data from Google Sites announcement rss feed
 *
 * @author Tom Reeve
 */
public class RssJsoupParser {

    private static final String parserNameFeed = "feed";
    private static final String parserNameEntry = "entry";
    private static final String parserNameTitle = "title";
    private static final String parserNameLink = "link";
    private static final String parserNameDate = "published";
    private static final String parserNameDetails = "content";

    public static List<Article> parse(Document doc) {
        List<Article> articleList = new ArrayList<>();
        try {
            Document.OutputSettings settings = doc.outputSettings();

            settings.prettyPrint(false);
            settings.escapeMode(Entities.EscapeMode.extended);
            settings.charset("ASCII");

            Elements entries = doc.select(parserNameFeed + " " + parserNameEntry);

            for(Element entry : entries)
            {
                Elements titles = entry.select(parserNameTitle);
                String title = titles == null ? "" : titles.get(0).text();

                Elements dates = entry.select(parserNameDate);
                String dateStr = dates == null ? null : dates.get(0).text();
                Date date = parseDate(dateStr);

                Elements bodies = entry.select(parserNameDetails);
                String body = bodies == null ? "" : bodies.get(0).html(); //parseDetails(bodyStr);

                Elements links = entry.select(parserNameLink);
                String link = links == null ? "" : links.get(0).text();

                articleList.add(new Article(title, link, date, body, "", ""));
                //articleList.add(link.text());
            }
        } catch (Exception e) {
            Log.e( "ArticleParser"," error: " + e.toString());
        }
        //returns a result string to the sender. If satisfied, the sender can then
        //send a getAllArticles() request for the actual dat
        return articleList;
    }

    private static Date parseDate(String date) {
        Date dateDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSSZ", Locale.US);

        if (date != null) {
            if (date.length() == 24) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'", Locale.US);
            } else if (date.length() == 10) {
                format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            }
        }
        try {
            dateDate = format.parse(date);
        } catch (ParseException e) {
            dateDate = new Date();
            Log.d("parser", "Error making date");
        }
        return dateDate;
    }
}
