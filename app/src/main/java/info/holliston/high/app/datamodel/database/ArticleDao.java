package info.holliston.high.app.datamodel.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

/**
 * Interface for accessing the Android Room database
 *
 * @author Tom Reeve
 */
@Dao
public interface ArticleDao {

    // Insert methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Article article);

    //Query methods
    @Query("SELECT * FROM article WHERE type = :type ORDER BY date DESC")
    List<Article> getArticles(String type);

    @Query("SELECT * FROM article WHERE date >= :date AND type = :type ORDER BY date ASC")
    List<Article> getArticlesAfter(Date date, String type);

}
