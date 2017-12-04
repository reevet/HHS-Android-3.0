package info.holliston.high.app.datamodel.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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
 * Parses article data from the Google Calendar API
 *
 * @author Tom Reeve
 */
public class CalendarJsonParser implements ArticleParser {

    // these are the values that are in the JSON to indicate items
    // e.g. entryName = "items", dateName = "startDate"
    private static final String parserNameEntry = "items";
    private static final String parserNameTitle = "summary";
    private static final String parserNameLink = "htmlLink";
    private static final String parserNameDate = "start";
    private static final String parserNameDetails = "description";

    //==============================================================================================
    // region Parsing loops
    //==============================================================================================

    /**
     * Main method that pulls the data from inputstream
     *
     * @param in  the inputstream
     * @return    the list of parsed articles
     */
    public List<Article> parse(InputStream in) {
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
            Log.e("CalJsonParser error: ", e.toString());
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
     * @throws IOException   possible network read error
     */
    private List<Article> readFeed(String input) throws IOException {
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
            Log.d("CalJsonParser error: ", e.getMessage());
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

        // attempt to get each element of the article
        title = readTitle(event);
        details = readSummary(event);
        link = readLink(event);
        date = readDate(event);

        if (date != null) {
            // return a new article. imgSrc does not apply to calendar events, and type will be
            // filled in later.
            return new Article(title, link, date, details, "", "");
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
            JSONObject startDateObj = event.getJSONObject(parserNameDate);

            // there may be more than one date. Loop through to find what we need
            Iterator<String> keys = startDateObj.keys();
            while (keys.hasNext()) {
                // loops to get the dynamic key
                String currentDynamicKey = keys.next();
                String dateStr = "";

                // gets the value of the dynamic key
                if (currentDynamicKey.contains("date")) {
                    dateStr = startDateObj.getString(currentDynamicKey);
                }

                // creates the base expected date format
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

                // other formats, based on the string length
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
                    break;
                } catch (ParseException e) {
                    Log.d("parser", "Error making date");
                }
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return summary;
    }
}

