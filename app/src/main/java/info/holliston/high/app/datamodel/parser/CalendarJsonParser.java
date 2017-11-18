package info.holliston.high.app.datamodel.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

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
    private static final String parserNameEntry = "items";
    private static final String parserNameTitle = "summary";
    private static final String parserNameLink = "htmlLink";
    private static final String parserNameDate = "start";
    private static final String parserNameDetails = "description";

    public List<Article> parse(InputStream in) {
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

    private List<Article> readFeed(String input) throws XmlPullParserException, IOException {
        List<Article> articleList = new ArrayList<>();
        try {
            JSONObject calObj = new JSONObject(input);
            JSONArray items = calObj.getJSONArray(parserNameEntry);
            for (int i = 0; i < items.length(); i++) {
                JSONObject event = items.getJSONObject(i);
                articleList.add(getArticle(event));
            }
        } catch (Exception e) {
            Log.d("CalJsonParser error: ", e.getMessage());
        }
        return articleList;

    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Article getArticle(JSONObject event) {

        String title;
        String details;
        String link;
        String date;

        title = readTitle(event);
        details = readSummary(event);
        link = readLink(event);
        date = readDate(event);

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

        return new Article(title, link, dateDate, details, "", "");
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
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return summary;

    }
}

