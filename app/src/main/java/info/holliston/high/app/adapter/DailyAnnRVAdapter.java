package info.holliston.high.app.adapter;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
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

    public DailyAnnRVAdapter(MainActivity ma){
        super(ma, R.layout.rv_row_dailyann, false);
    }

    /**
     * Adds data to the data row viewholder
     *
     * @param va           the viewholder containing the data
     * @param article      the article of data to be shown
     */
    @Override
    protected void setDataRow(RecyclerView.ViewHolder va, Article article, int position) {
                RVAdapter.ViewHolderArticle vha =
                        (RVAdapter.ViewHolderArticle) va;

        vha.text1.setText(article.getTitle());
    }

    /**
     * Identifies row headers, and inserts the headers into the list of items. In this case,
     * data will be grouped by week of the year (this week, next week, etc)
     *
     * @param list    the data to be sorted, in List form
     * @return        a list of headers and data rows
     */
    @Override
    protected List<Object> sortIntoGroups(List<Article> list) {

        //data will be grouped by week of the year
        int currentWeek = -1;
        List<Object> tempList = new ArrayList<>();

        // if this is set as trimmable, if it is now after 2pm, and if the first row has today's
        // data, then skip the first row
        if (isTrimmable && needsTrimming(list)) {
            list.remove(0);
        }
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

                String headerString;
                if (artWeek == thisWeek) {
                    headerString = getContext().getString(R.string.this_week);
                    tempList.add(headerString);
                } else if ((artWeek == thisWeek-1)) {
                    headerString = getContext().getString(R.string.last_week);
                    tempList.add(headerString);
                } else if ((artWeek == thisWeek-2)) {
                    headerString = getContext().getString(R.string.earlier);
                    tempList.add(headerString);
                }
                currentWeek = artWeek;
            }
            tempList.add(article);
        }
        return tempList;
    }
}

