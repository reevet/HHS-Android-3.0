package info.holliston.high.app.datamodel;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

/**
 * The basic chunk of feed data.  This is the basis for the whole application
 *
 * @author Tom Reeve
 */

@Entity
public class Article {

    //id is used entirely by the database, and is never set based on feed data
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "url")
    private String url;

    @NonNull
    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "details")
    private String details;

    @ColumnInfo(name = "type")
    private String type;

    // the key is a variation of the article's URL, used for unique identification
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "key")
    private String key = "";

    @ColumnInfo(name = "imgSrc")
    private String imgSrc;

    // endregion
    //==============================================================================================
    // region String values
    //==============================================================================================

    //types
    public static final String SCHEDULES = "schedules";
    public static final String EVENTS = "events";
    public static final String LUNCH = "lunch";
    public static final String DAILY_ANN = "dailyann";
    public static final String NEWS = "news";

    // endregion
    //==============================================================================================
    // region Constructor and Key
    //==============================================================================================

    /**
     * Constructor for an article
     *
     * @param title    the title of the post, or summary of the event
     * @param url      the url to the post or event
     * @param date     the post's publish date, or the event's start date
     * @param details  the post's content, or the event's description
     * @param imgSrc   the url to the article's image
     * @param type     the data type (see "types" above)
     */
    public Article(String title, String url, @NonNull Date date, String details, String imgSrc, String type)
    {
        this.title = title == null ? "" : title;
        this.url = url == null ? "" : url;
        this.date = date;
        this.details = details == null ? "" : details;
        this.imgSrc = imgSrc == null ? "" : imgSrc;
        this.type = type == null ? "" : type;
        this.key = makeKey(url, title, date);
    }

    /**
     * Makes a unique key for each article, based on the article's url (preferably)
     * or the article's date and title.
     *
     * @param url    the url to use
     * @param title  the title to use
     * @param date   the date to use
     * @return       the generated key
     */
    private static String makeKey(String url, String title, Date date) {

        //The key is simply the referring URL, safely stored
        String tempKey;
        if ((url != null) && (!url.equals(""))) {
            tempKey = url;
        } else {
            tempKey = title + date.toString();
        }
        tempKey = tempKey.replace("/", "");
        tempKey = tempKey.replace("&", "");
        tempKey = tempKey.replace("?", "");
        //Log.d("Item","New key: "+key);

        return tempKey;
    }

    // endregion
    //==============================================================================================
    // region Convertors
    //==============================================================================================

    /**
     * Converters to help Room convert dates as longs
     */
    public static class Converters {
        @TypeConverter
        @SuppressWarnings("unused")
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }

    /**
     * Takes an articleList and sets the value of each article to the provided type.
     * Typically used after parsing.
     *
     * @param type          the type to set (schedules, news, etc)
     * @param articleList   the list of articles to update
     * @return              the same list, with type updated
     */
    static List<Article> addTypeToList(String type, List<Article> articleList) {
        // cycles through the articles
        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            // creates a new article with that type
            article.setType(type);
            // swaps the new article in for the old one
            articleList.set(i, article);
        }
        return articleList;
    }

    // endregion
    //==============================================================================================
    // region Accessors
    //==============================================================================================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    @SuppressWarnings("unused")
    public void setUrl(String url) {
        this.url = url;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    @SuppressWarnings("unused")
    private void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
