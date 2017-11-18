package info.holliston.high.app.datamodel.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.datamodel.Article;

/**
 * Parses article data from the Blogger API
 *
 * @author Tom Reeve.
 */
public class JsonFeedParser implements ArticleParser {
    // these are the values that are in the XML to indicate items
    // e.g. entryName = "entry", dateName = "startDate"
    private static final String parserNameEntry = "items";
    private static final String parserNameTitle = "title";
    private static final String parserNameLink = "selfLink";
    private static final String parserNameDate = "published";
    private static final String parserNameDetails = "content";

    /*
     * Main method that pulls the data to fromContentValues
     */
    public List<Article> parse(InputStream in)  {
        List<Article> articleList = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            articleList = readFeed(sb.toString());
        } catch (Exception e) {
            Log.e("ArticleParser",  "error: " + e.toString());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return articleList;
    }

    /*
     * Takes a fromContentValues object and extracts the info
     * Serially reads tags to find articles, and then
     * sends them out for processing
     */
    private List<Article> readFeed(String input) {
        List<Article> articleList = new ArrayList<>();

        try {
            JSONObject calObj = new JSONObject(input);

            JSONArray items = calObj.getJSONArray(parserNameEntry);
            for (int i = 0; i < items.length(); i++) {
                JSONObject event = items.getJSONObject(i);
                articleList.add(getArticle(event));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return articleList;
        //return entries;
    }
    private Article getArticle(JSONObject event) {
        String title;
        String details;
        String link;
        String date;
        String imgSrc;

        title = readTitle(event);
        details = readSummary(event);
        link = readLink(event);
        date = readDate(event);
        imgSrc = readImgSrc(event);

        Date dateDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSSZ", Locale.US);

        if (date != null) {
            if (date.length() == 24) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'", Locale.US);
            } else if (date.length() == 25) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss-SS:00", Locale.US);
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

        //the "null" below represents the Key, which we don't bother with for events
        return new Article(title, link, dateDate, details, imgSrc, "");
    }

    // Processes title tags in the feed.
    private String readTitle(JSONObject event) {
        String title = "";
        try {
            title = event.getString(parserNameTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(JSONObject event) {
        String link = null;
        try {
            link = event.getString(parserNameLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return link;
    }

    private String readDate(JSONObject event) {
        String date = "";
        try {
            JSONObject startDateObj = event.getJSONObject(parserNameDate);
            Iterator<String> keys = startDateObj.keys();
            while (keys.hasNext()) {
                // loop to get the dynamic key
                String currentDynamicKey = keys.next();

                // get the value of the dynamic key
                if (currentDynamicKey.contains("date")) {
                    date = startDateObj.getString(currentDynamicKey);
                }

            }
        } catch (Exception e) {
            try {
                date = event.getString(parserNameDate);
            } catch (Exception ex) {
                Log.e("EventJsonParser","date not found in Json");
            }
        }
        return date;
    }

    // Processes summary tags in the feed.
    private String readSummary(JSONObject event) {
        String summary = "";
        try {
            summary = event.getString(parserNameDetails);
            Document doc = Jsoup.parse(summary);

            Document.OutputSettings settings = doc.outputSettings();
            settings.prettyPrint(false);
            settings.escapeMode(Entities.EscapeMode.extended);
            settings.charset("ASCII");

            summary = doc.html();

        } catch (Exception e) {
            //e.printStackTrace();
        }
        return summary;

    }

    // Processes link tags in the feed.
    private String readImgSrc(JSONObject event) {
        String src = null;
        try {
            JSONArray images = event.getJSONArray("images");
            JSONObject image = images.getJSONObject(0);
            src = image.getString("url");
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return src;
    }
}
