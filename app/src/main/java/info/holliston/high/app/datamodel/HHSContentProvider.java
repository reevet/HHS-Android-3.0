package info.holliston.high.app.datamodel;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import info.holliston.high.app.datamodel.database.ArticleDatabase;


public class HHSContentProvider extends ContentProvider {

    private static final String PROVIDER_NAME = "info.holliston.high.app.datamodel.hhscontentprovider";

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, Article.SCHEDULES, 0);
        uriMatcher.addURI(PROVIDER_NAME, Article.EVENTS, 1);
        uriMatcher.addURI(PROVIDER_NAME, Article.LUNCH, 2);
        uriMatcher.addURI(PROVIDER_NAME, Article.DAILY_ANN, 3);
        uriMatcher.addURI(PROVIDER_NAME, Article.NEWS, 4);
        uriMatcher.addURI(PROVIDER_NAME, Article.SCHEDULES+"/limit/#", 100);
        uriMatcher.addURI(PROVIDER_NAME, Article.EVENTS+"/limit/#", 101);
        uriMatcher.addURI(PROVIDER_NAME, Article.LUNCH+"/limit/#", 102);
        uriMatcher.addURI(PROVIDER_NAME, Article.DAILY_ANN+"/limit/#", 103);
        uriMatcher.addURI(PROVIDER_NAME, Article.NEWS+"/limit/#", 104);
    }

    public HHSContentProvider() {
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final Context context = getContext();
        if (context == null) {
            return null;
        }
        final long id = ArticleDatabase.getInstance(context).articleDao()
                .insert(Article.fromContentValues(values));
        context.getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }



    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}


