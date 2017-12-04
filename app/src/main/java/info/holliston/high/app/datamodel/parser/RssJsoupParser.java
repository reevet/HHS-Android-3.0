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

    // these are the values that are in the XML to indicate items
    // e.g. entryName = "entry", dateName = "published"
    private static final String parserNameFeed = "feed";
    private static final String parserNameEntry = "entry";
    private static final String parserNameTitle = "title";
    private static final String parserNameLink = "link";
    private static final String parserNameDate = "published";
    private static final String parserNameDetails = "content";

    //==============================================================================================
    // region Parsing loop
    //==============================================================================================

    /**
     * Main method that pulls the data from inputstream
     *
     * @param doc  the JSoup document to parse
     * @return     the list of parsed articles
     */
    public static List<Article> parse(Document doc) {
        List<Article> articleList = new ArrayList<>();
        try {
            // first, sets the doc to unescape any special characters
            Document.OutputSettings settings = doc.outputSettings();
            settings.prettyPrint(false);
            settings.escapeMode(Entities.EscapeMode.extended);
            settings.charset("ASCII");

            // selects the entry items in the doc
            Elements entries = doc.select(parserNameFeed + " " + parserNameEntry);

            // loops through the entries
            for(Element entry : entries) {
                //attempts to set the article values
                Elements titles = entry.select(parserNameTitle);
                String title = titles == null ? "" : titles.get(0).text();

                Elements dates = entry.select(parserNameDate);
                String dateStr = dates == null ? null : dates.get(0).text();
                Date date = parseDate(dateStr);

                Elements bodies = entry.select(parserNameDetails);
                String body = bodies == null ? "" : bodies.get(0).html(); //parseDetails(bodyStr);

                Elements links = entry.select(parserNameLink);
                String link = links == null ? "" : links.get(0).text();

                // adds the article to the list
                if (date != null) {
                    // creates a new article. Imgsrc is not needed for these, and type will be
                    // filled in later.
                    articleList.add(new Article(title, link, date, body, "", ""));
                }
            }
        } catch (Exception e) {
            Log.e( "ArticleParser"," error: " + e.toString());
        }
        // returns a result string to the sender. If satisfied, the sender can then
        // send a getAllArticles() request for the actual dat
        return articleList;
    }

    // endregion
    //==============================================================================================
    // region Parsing article elements
    //==============================================================================================
    /**
     * Processes date tags in the feed.
     *
     * @param date the date string
     * @return      the Date object it represents
     */
    private static Date parseDate(String date) {
        Date dateDate = null;
        // creates the base expected date format
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

        // other formats, based on the string length
        if (date != null) {
            if (date.length() == 24) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            } else if (date.length() == 10) {
                format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            }
        }
        try {
            // creates a Date object based on the format
            dateDate = format.parse(date);
        } catch (ParseException e) {
            Log.d("parser", "Error making date");
        }
        return dateDate;
    }
}
