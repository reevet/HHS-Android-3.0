package info.holliston.high.app.adapter;

import android.support.v7.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

/**
 * A class to manage Schedules data in an expandable RecyclerView
 *
 * @author Tom Reeve
 */
public class SchedulesExpRVAdapter extends ExpandableRVAdapter {

    public SchedulesExpRVAdapter(MainActivity ma){
        super(ma, R.layout.rv_row_schedules, true);
    }

    /**
     * Adds data to the data row viewholder
     *
     * @param va           the viewholder containing the data
     * @param article      the article of data to be shown
     */
    @Override
    protected void setDataRow(RecyclerView.ViewHolder va, Article article, int position) {
        ExpandableRVAdapter.ViewHolderArticleExpandable vha =
                (ExpandableRVAdapter.ViewHolderArticleExpandable) va;

        vha.updateItem(position);

        SimpleDateFormat dfDay = new SimpleDateFormat("EEEE", Locale.US);
        String dayString = dfDay.format(article.getDate());

        SimpleDateFormat dfFull = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        String fullString = dfFull.format(article.getDate());

        vha.text1.setText(dayString);
        vha.text2.setText(fullString);
        String details = article.getDetails();
        vha.detailsView.setText(details);

        char initial = article.getTitle().charAt(0);

        switch (initial) {
            case 'A':
                vha.icon.setImageResource(R.drawable.a_sm);
                break;
            case 'B':
                vha.icon.setImageResource(R.drawable.b_sm);
                break;
            case 'C':
                vha.icon.setImageResource(R.drawable.c_sm);
                break;
            case 'D':
                vha.icon.setImageResource(R.drawable.d_sm);
                break;
            default:
                vha.icon.setImageResource(R.drawable.star_sm);
                break;
        }
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

        // track the current group
        int currentWeek = -1;
        List<Object> tempList = new ArrayList<>();

        // if trimmable and it's after 2pm and the data is today's data, skip the first row
        if (isTrimmable && needsTrimming(list)) {
            list.remove(0);
        }

        //cycle through the articles
        for (Article article : list) {

            //get the article's calendar info
            Date schedDate = article.getDate();
            Calendar schedCal = Calendar.getInstance();
            schedCal.setTime(schedDate);
            int schedWeek = schedCal.get(Calendar.WEEK_OF_YEAR);

            // if the article's week is not the current group's week, it's time for a new header
            if (currentWeek != schedWeek) {
                Calendar todaycal = Calendar.getInstance();
                todaycal.setTime(new Date());
                int thisWeek = todaycal.get(Calendar.WEEK_OF_YEAR);
                String headerString;

                if (schedWeek == thisWeek) {
                    headerString = getContext().getString(R.string.this_week);
                    tempList.add(headerString);
                } else if (schedWeek == thisWeek+1) {
                    headerString = getContext().getString(R.string.next_week);
                    tempList.add(headerString);
                }
                else if (schedWeek == thisWeek+2) {
                    headerString = getContext().getString(R.string.farther);
                    tempList.add(headerString);
                }
                currentWeek = schedWeek;
            }
            tempList.add(article);
        }
        return tempList;
    }
}

