package info.holliston.high.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;

/**
 * Implementation of App Widget functionality.
 */
public class HHSWidget extends AppWidgetProvider {
    public static final String NOTIFICATION = "info.holliston.high.widget";

    //==============================================================================================
    // region Update
    //==============================================================================================

    /**
     * manages the updating of widgets
     *
     * @param context             the app context
     * @param appWidgetManager    the widget manager
     * @param appWidgetIds        widget ids, in case more than one exists
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Updates a single widget instance
     *
     * @param context             the app context
     * @param appWidgetManager    the widget manager
     * @param appWidgetId         widget id, in case more than one exists
     */
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object (assuming 1x3 for the initial widget)
        RemoteViews views = getRemoteViews(context, 180);

        // get the data and fill the views
        ArticleDao dao = ArticleDatabase.getInstance(context).articleDao();
        fillData(views, dao);

        //onClickListener
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);

        views.setOnClickPendingIntent(R.id.widget_all, pendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // endregion
    //==============================================================================================
    // region Data
    //==============================================================================================

    /**
     * Fills schedule data into the widget's views
     *
     * @param views  the set of views in the layout
     * @param dao    the data access object that retrieves the schedule article
     */
    void fillData(RemoteViews views, ArticleDao dao) {

        // gets the articles
        List<Article> list = dao.getArticlesAfter(today(), Article.SCHEDULES);
        if (list.size() > 0) {

            // take the first schedule
            Article article = list.get(0);

            // compare today to the first schedule date
            Calendar today = Calendar.getInstance();
            Calendar schedCal = Calendar.getInstance();
            schedCal.setTime(article.getDate());
            Boolean dayMatch = ((today.get(Calendar.DATE) == schedCal.get(Calendar.DATE))
                    && (today.get(Calendar.MONTH) == schedCal.get(Calendar.MONTH))
                    && (today.get(Calendar.YEAR) == schedCal.get(Calendar.YEAR)));

            // if the first schedule's date is today, and it's after 2pm, take the second schedule
            if ((list.size() > 1) && dayMatch
                    && (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >=14)) {
                article = list.get(1);
            }
            // fill the data into the views
            setTitles(article, views);
            setDates(article, views);
            setIcon(article, views);
        }
    }

    /**
     * Sets the title info into the views
     * @param article  the schedule data article
     * @param views    the views to be filled
     */
    void setTitles(Article article, RemoteViews views) {

        String title = article.getTitle();
        if (title.length() > 0) {

            // Full title (for 3+ column widgets)
            views.setTextViewText(R.id.row_text_secondary, title);

            // Short title (for 2 column widgets)
            String shortTitle = title;
            int colonIndex = title.indexOf(":");
            if (colonIndex >= 1) {
                shortTitle = title.substring(0, colonIndex);
            }
            views.setTextViewText(R.id.row_text_secondary_short, shortTitle);
        }
    }

    /**
     * Sets the date info into the views
     * @param article  the schedule data article
     * @param views    the views to be filled
     */
    void setDates(Article article, RemoteViews views) {

        Calendar schedCal = Calendar.getInstance();
        schedCal.setTime(article.getDate());

        // Full length date (for 3+ column widgets)
        String dateStringFull = String.format(Locale.US, "%1$tA, %1$tB %1$te ", schedCal);
        views.setTextViewText(R.id.row_text_primary, dateStringFull);

        // Short date (for 2 column widgets)
        String dateStringShort = String.format(Locale.US, "%1$ta, %1$tb %1$te ", schedCal);
        views.setTextViewText(R.id.row_text_primary_short, dateStringShort);

        // Super Short date (for 1 column widgets)
        String dateStringSuperShort = String.format(Locale.US, "%1$ta\n%1$tb %1$te", schedCal);
        views.setTextViewText(R.id.row_text_primary_supershort, dateStringSuperShort);
    }

    /**
     * Fills an icon image into the views
     *
     * @param article  the schedule data article
     * @param views    the views to be filled
     */
    private void setIcon(Article article, RemoteViews views) {
        int icon;

        char letter = "E".charAt(0);
        if (article.getTitle().length() >=0) {
            letter = article.getTitle().charAt(0);
        }
        // chooses an image
        switch (letter) {
            case 'A':
                icon = R.drawable.a_sm;
                break;
            case 'B':
                icon = R.drawable.b_sm;
                break;
            case 'C':
                icon = R.drawable.c_sm;
                break;
            case 'D':
                icon = R.drawable.d_sm;
                break;
            default:
                icon = R.drawable.star_sm;
                break;
        }

        views.setImageViewResource(R.id.row_icon, icon);
    }

    // endregion
    //==============================================================================================
    // region Resizing
    //==============================================================================================

    /**
     * Changes the layout based on the new widget size
     *
     * @param context           app context
     * @param appWidgetManager  widget manager
     * @param appWidgetId       id of the widget
     * @param newOptions        options of the newly sized widget
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // gets min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        // gets the views and data
        RemoteViews views = getRemoteViews(context, minWidth);
        ArticleDao dao = ArticleDatabase.getInstance(context).articleDao();

        // fills data into the views
        fillData(views, dao);

        // refresh
        appWidgetManager.updateAppWidget(appWidgetId, views);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Determines appropriate view based on row or column provided.
     *
     * @param minWidth   the minimum width for this view
     * @return           the set of views for this layout
     */
    private static RemoteViews getRemoteViews(Context context, int minWidth) {

        // First finds out rows and columns based on width provided.
        int columns = getCellsForSize(minWidth);

        // chooses a layout based on the column width
        switch (columns) {
            case 1:
                return new RemoteViews(context.getPackageName(), R.layout.hhswidget_1col);
            case 2:
                return new RemoteViews(context.getPackageName(), R.layout.hhswidget_2col);
            default:
                return new RemoteViews(context.getPackageName(), R.layout.hhswidget_3col);
        }
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    // endregion
    //==============================================================================================
    // region Helper
    //==============================================================================================

    /**
     * Gets a date object for today, with time filled in as 00:00:00
     *
     * @return date object, wth time set to 00:00:00
     */
    static Date today() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);

        return today.getTime();
    }
}

