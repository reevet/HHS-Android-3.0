package info.holliston.high.app.datamodel;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.content.ContentValues;
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

    static final String TABLENAME = "article";
    //types
    public static final String SCHEDULES = "schedules";
    public static final String EVENTS = "events";
    public static final String LUNCH = "lunch";
    public static final String DAILY_ANN = "dailyann";
    public static final String NEWS = "news";

    //columns
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DETAILS = "details";
    private static final String COLUMN_IMGSRC = "imgSrc";
    private static final String COLUMN_TYPE = "type";

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

    /**
     * Converters to help dates store as longs
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
     * Generates an Article from a set of ContentValues values
     *
     * @param values  the ContentValues to be parsed
     */
    static Article fromContentValues(ContentValues values) {
        String title = values.getAsString(Article.COLUMN_TITLE);
        Date date = new Date(values.getAsLong(Article.COLUMN_DATE));
        String details = values.getAsString(Article.COLUMN_DETAILS);
        String url = values.getAsString(Article.COLUMN_URL);
        String imgSrc = values.getAsString(Article.COLUMN_IMGSRC);
        String type = values.getAsString(Article.COLUMN_TYPE);

        return new Article(title, url, date, details, imgSrc, type);
    }

    static List<Article> addTypeToList(String type, List<Article> articleList) {
        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            article.setType(type);
            articleList.set(i, article);
        }
        return articleList;
    }

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
