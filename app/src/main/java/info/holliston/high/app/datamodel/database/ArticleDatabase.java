package info.holliston.high.app.datamodel.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import info.holliston.high.app.datamodel.Article;

@Database(version = 1, entities = {Article.class})
@TypeConverters({Article.Converters.class})
public abstract class ArticleDatabase extends RoomDatabase {

    abstract public ArticleDao articleDao();
    private static ArticleDatabase INSTANCE;

    public static ArticleDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), ArticleDatabase.class, "article")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }
}
