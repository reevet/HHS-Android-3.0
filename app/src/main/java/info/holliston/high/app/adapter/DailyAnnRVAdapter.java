package info.holliston.high.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

/**
 * A class to manage Daily Announcements data in an expandable RecyclerView
 * NOTE: This class extends RVAdpater, not ExpRVAdapter, because the News view does not have
 * expand/collapse functionality; it sends the full article to a new fragment instead.
 *
 * @author Tom Reeve
 */
public class DailyAnnRVAdapter extends RVAdapter {

    //==============================================================================================
    // region Constructor
    //==============================================================================================

    /**
     * Constructor
     * @param ma    the MainActivity of the app
     */
    public DailyAnnRVAdapter(MainActivity ma){
        super(ma, R.layout.rv_row_dailyann, false);
    }

    // endregion
    //==============================================================================================
    // region Data organizing
    //==============================================================================================

    /**
     * Adds data to the data row viewholder
     *
     * @param va           the viewholder containing the data
     * @param article      the article of data to be shown
     */
    @Override
    protected void setDataRow(RecyclerView.ViewHolder va, Article article, int position) {
        // casts the viewholder as a ViewHolderArticle
        RVAdapter.ViewHolderArticle vha = (RVAdapter.ViewHolderArticle) va;

        // fills and shows the header view, if available
        String headerString = headers.get(position);
        if (headerString != null) {
            vha.headerTextview.setText(headerString);
            vha.headerFrame.setVisibility(View.VISIBLE);
        } else {
            vha.headerFrame.setVisibility(View.GONE);
        }

        // fills the article title
        vha.text1.setText(article.getTitle());
    }

    /**
     * Identifies row headers, and inserts the headers into the list of items. In this case,
     * data will be grouped by week of the year (this week, next week, etc)
     *
     * @param list    the data to be sorted, in List form
     */
    @Override
    protected void createHeaders(List<Article> list) {

        //data will be grouped by week of the year
        int currentWeek = -1;

        //cycle through the articles
        for (Article article : list){

            Date articleDate = article.getDate();
            Calendar artCal = Calendar.getInstance();
            artCal.setTime(articleDate);
            int artWeek = artCal.get(Calendar.WEEK_OF_YEAR);

            // if a different week is found, then create a new header row for that new week
            if (currentWeek != artWeek) {
                Calendar todaycal = Calendar.getInstance();
                todaycal.setTime(new Date());
                int thisWeek = todaycal.get(Calendar.WEEK_OF_YEAR);

                if (artWeek == thisWeek) {
                    headers.add(getContext().getString(R.string.this_week));
                } else if ((artWeek == thisWeek-1)) {
                    headers.add(getContext().getString(R.string.last_week));
                } else if ((artWeek == thisWeek-2)) {
                    headers.add(getContext().getString(R.string.earlier));
                } else {
                    headers.add("");
                }
                currentWeek = artWeek;
            } else {
                headers.add(null);
            }
        }
    }
}

