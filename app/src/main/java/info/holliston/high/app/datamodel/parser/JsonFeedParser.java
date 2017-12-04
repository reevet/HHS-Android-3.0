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

    //==============================================================================================
    // region Parsing loops
    //==============================================================================================

    /*
     * Main method that pulls the data from inputstream
     *
     * @param in  the inputstream
     * @return    the list of parsed articles
     */
    public List<Article> parse(InputStream in)  {
        List<Article> articleList = new ArrayList<>();

        // reads data from the inputstream
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;

        // adds each line of input into a string
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            // parses the generated data string
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

    /**
     * Finds individual items in the data string, and parses each one
     *
     * @param input  the data string to parse
     * @return       the lists of newly parsed articles
     */
    private List<Article> readFeed(String input) {
        List<Article> articleList = new ArrayList<>();

        try {
            // converts the data into a JSON object
            JSONObject calObj = new JSONObject(input);

            //gets the array of items
            JSONArray items = calObj.getJSONArray(parserNameEntry);
            // loops through the items
            for (int i = 0; i < items.length(); i++) {
                // parses each individual item
                JSONObject event = items.getJSONObject(i);
                // add the item to the list
                Article article = getArticle(event);
                if (article != null) {
                    articleList.add(getArticle(event));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articleList;
    }

    /**
     * Parses the contents of an item. If it encounters a title, summary, or link tag, hands them
     * of to their respective "read" methods for processing. Otherwise, skips the tag.
     *@param event   the JSON item/event to parse
     *@return        the Article object holding the data's values
     */
    private Article getArticle(JSONObject event) {
        String title;
        String details;
        String link;
        Date date;
        String imgSrc;

        // attempt to get each element of the article
        title = readTitle(event);
        details = readSummary(event);
        link = readLink(event);
        date = readDate(event);
        imgSrc = readImgSrc(event);

        if (date != null) {
            // return a new article. Type will be filled in later.
            return new Article(title, link, date, details, imgSrc, "");
        }
        return null;
    }

    // endregion
    //==============================================================================================
    // region Parsing article elements
    //==============================================================================================

    /**
     * Processes title tags in the feed.
     *
     * @param event the JSON item/event to parse
     * @return      the title of the item
     */
    private String readTitle(JSONObject event) {
        String title = "";
        try {
            title = event.getString(parserNameTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }

    /**
     * Processes link tags in the feed.
     *
     * @param event the JSON item/event to parse
     * @return      the link url of the item
     */
    private String readLink(JSONObject event) {
        String link = null;
        try {
            link = event.getString(parserNameLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return link;
    }

    /**
     * Processes start date in the feed.
     *
     * @param event the JSON item/event to parse
     * @return      the date of the item
     */
    private Date readDate(JSONObject event) {
        Date date = null;
        try {
            String dateStr = event.getString(parserNameDate);

            // creates the base expected date format
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

            // other formats, based on the strong length
            if (dateStr != null) {
                if (dateStr.length() == 24) {
                    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                } else if (dateStr.length() == 25) {
                    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SS:00", Locale.US);
                } else if (dateStr.length() == 10) {
                    format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                }
            }
            try {
                // creates a Date object based on the format
                date = format.parse(dateStr);
            } catch (ParseException e) {
                Log.d("parser", "Error making date");
            }


        } catch (Exception e) {
            Log.e("EventJsonParser", e.getMessage());
            Log.e("EventJsonParser","date not found in Json");
        }
        return date;
    }

    /**
     * Processes the description tags in the feed.
     *
     * @param event the JSON item/event to parse
     * @return      the details of the item
     */
    private String readSummary(JSONObject event) {
        String summary = "";
        try {
            summary = event.getString(parserNameDetails);

            // sets up JSoup parsing, to unencode special characters
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

    /**
     * Processes the img tags in the feed.
     *
     * @param event the JSON item/event to parse
     * @return      the img src of the item
     */
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
