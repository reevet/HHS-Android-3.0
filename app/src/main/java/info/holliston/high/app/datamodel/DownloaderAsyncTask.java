package info.holliston.high.app.datamodel;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;
import info.holliston.high.app.datamodel.parser.CalendarJsonParser;
import info.holliston.high.app.datamodel.parser.JsonFeedParser;
import info.holliston.high.app.datamodel.parser.RssJsoupParser;

/**
 * An asynchronous task for fetching server data. Each task gathers data from one source, stores
 * the data in the content provider, and then broadcasts that the data has been updated
 *
 * @author Tom Reeve
 */

public class DownloaderAsyncTask extends AsyncTask<Void, Void, Integer> {

    // standard receiver name
    public static final String APP_RECEIVER = "info.holliston.high.app";
    private String feedUrl;
    private WeakReference<Context> context;
    private String source = "";

    public DownloaderAsyncTask(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected Integer doInBackground(Void... params) {
       int count =0;
       if ((source.equals(Article.SCHEDULES)) || source.equals("")) {
           count += getArticlesFor(Article.SCHEDULES);
       }

       if ((source.equals(Article.NEWS)) || source.equals("")) {
           count += getArticlesFor(Article.NEWS);
       }

       if ((source.equals(Article.EVENTS)) || source.equals("")) {
            count += getArticlesFor(Article.EVENTS);
       }

       if ((source.equals(Article.DAILY_ANN)) || source.equals("")) {
            count += getArticlesFor(Article.DAILY_ANN);
       }

       if ((source.equals(Article.LUNCH)) || source.equals("")) {
            count += getArticlesFor(Article.LUNCH);

       }
       return count;
    }

    private int getArticlesFor(String source){
        int count = 0;
        try {

            List<Article> articleList = downloadArticlesFromNetwork(source);

            if ((articleList != null) && (articleList.size() > 0)) {
                articleList = Article.addTypeToList(source, articleList);

                for (Article article : articleList) {
                    ArticleDao dao = ArticleDatabase.getInstance(context.get()).articleDao();
                    dao.insert(article);
                    //context.getContentResolver().insert(uri, Article.ContentValuesFromArticle(article));
                    count++;
                }
            }
        }catch (NullPointerException ex) {
           Log.e("Downloader:", "Error retreiving articles for" + source);
        }
        return count;
    }

    @Override
    protected void onPostExecute(Integer count) {
        super.onPostExecute(count);
        // tell MainActivity that datasource has finished downloading
        Intent intent = new Intent(APP_RECEIVER);
        intent.putExtra("count", count);
        context.get().sendBroadcast(intent);
    }

    private List<Article> downloadArticlesFromNetwork(String source) {
        List<Article> returnArticles = null;

        switch (source) {
            case Article.SCHEDULES:
                feedUrl = context.get().getString(R.string.schedules_url);
                returnArticles = calDownload();
                break;
            case Article.LUNCH:
                feedUrl = context.get().getString(R.string.lunch_url);
                returnArticles = calDownload();
                break;
            case Article.EVENTS:
                feedUrl = context.get().getString(R.string.events_url);
                returnArticles = calDownload();
                break;
            case Article.DAILY_ANN:
                feedUrl = context.get().getString(R.string.dailyann_url);
                returnArticles = rssDownload();
                break;
            case Article.NEWS:
                feedUrl = context.get().getString(R.string.news_url);
                returnArticles = jsonDownload();
                break;
        }
        return returnArticles;
    }

    private List<Article> calDownload() {
        List<Article> returnArticles = null;
        String urlString = feedUrl;
        InputStream stream = null;

        try {
            // these JSON feeds require the addition of dates added to the feed URL
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00-05:00", Locale.US);
            String nowString = sdf.format(now);

            urlString = urlString + "&timeMin=" + nowString;
            stream = openInputStream(urlString);
//https://www.googleapis.com/calendar/v3/calendars/sulsp2f8e4npqtmdp469o8tmro%40group.calendar.google.com/events?key=AIzaSyAQa5V8a141kmmGrcb2LgpmyTvocrVPiDI&amp;maxResults=30&amp;orderBy=startTime&amp;singleEvents=true
            //download and fromContentValues
            CalendarJsonParser jsonParser = new CalendarJsonParser();
            returnArticles = jsonParser.parse(stream);
            Log.d("CalArticleDS", "Downloaded:" + returnArticles.size());

        } catch (Exception e) {
            Log.e("CalArticleDS", "Downloading error: " + e.toString());
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                //ignore
            }
        }

        return returnArticles;
    }

    private List<Article> jsonDownload() {
         List<Article> returnArticles = null;
        String urlString = feedUrl;
        InputStream stream = null;

        try {
            urlString += "?key=" + context.get().getString(R.string.api_key);
            urlString += "&fetchImages=true";
            stream = openInputStream(urlString);

            //download and fromContentValues
            JsonFeedParser jsonParser = new JsonFeedParser();
            returnArticles = jsonParser.parse(stream);
            Log.d("JsonArticleDS", "Downloaded: "+returnArticles.size());


        } catch (Exception e) {
            Log.e("JsonArticleDS", "Downloading error: " + e.toString());
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                //ignore
            }
        }
        return returnArticles;
    }

    private List<Article> rssDownload() {
        List<Article> returnArticles = null;
        String urlString = feedUrl;

        try {

            Document doc = Jsoup.connect(urlString).get();
            returnArticles = RssJsoupParser.parse(doc);
            Log.d("RssArticleDS", "Downloaded:" + returnArticles.size());

        } catch (Exception e) {
            Log.e("RssArticleDS", "Downloading error: " + e.toString());
        }

        return returnArticles;
    }

    private InputStream openInputStream(String urlString) throws IOException {
        java.net.URL url = new java.net.URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    public void setSource(String source) {
        this.source = source;
    }
}

