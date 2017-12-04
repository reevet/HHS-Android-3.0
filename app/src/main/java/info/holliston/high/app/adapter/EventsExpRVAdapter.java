package info.holliston.high.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

/**
 * A class to manage Events data in an expandable RecyclerView
 *
 * @author Tom Reeve
 */
public class EventsExpRVAdapter extends ExpandableRVAdapter {

    //==============================================================================================
    // region Constructor
    //==============================================================================================

    /**
     * Constructor
     *
     * @param ma The MainActivity of the app
     */
    public EventsExpRVAdapter(MainActivity ma){
        super(ma, R.layout.rv_row_events, false);
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
        ExpandableRVAdapter.ViewHolderArticleExpandable vha =
                (ExpandableRVAdapter.ViewHolderArticleExpandable) va;

        // if there is a header, fills and shows it
        String headerString = headers.get(position);
        if (headerString != null) {
            vha.headerTextview.setText(headerString);
            vha.headerFrame.setVisibility(View.VISIBLE);
        } else {
            vha.headerFrame.setVisibility(View.GONE);
        }

        // updates whether the row is expanded or not
        vha.updateItem(position);

        // fills the title
        vha.text1.setText(article.getTitle());

        // fills the date
        SimpleDateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        String dateString = df.format(article.getDate());
        if (dateString.equals("12:00 AM")) {
            dateString = getContext().getString(R.string.all_day);
        }
        vha.text2.setText(dateString);

        //fills the details
        String details = article.getDetails();
        if ((details == null) || (details.equals(""))) {
            vha.discoveryArrow.setVisibility(View.GONE);
            vha.cardView.setVisibility(View.GONE);
            vha.expandableLayout.setExpand(false);
        }
        vha.detailsView.setText(article.getDetails());
    }

    /**
     * Identifies row headers, and inserts the headers into the list of items. In this case, data
     * will be grouped by day (today, tomorrow, etc)
     *
     * @param list    the data to be sorted, in List form
     */
    @Override
    protected void createHeaders(List<Article> list) {

        // tracks the date of the current "group"
        int currentYear = -1;
        int currentMonth = -1;
        int currentDate = -1;

        //get today's calendar info
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(new Date());
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayMonth = todayCal.get(Calendar.MONTH);
        int todayDate = todayCal.get(Calendar.DATE);

        //cycle through the articles
        for (Article article : list) {

            Date articleDate = article.getDate();
             // get calendar info of an article
            Calendar artCal = Calendar.getInstance();
            artCal.setTime(articleDate);
            int artYear = artCal.get(Calendar.YEAR);
            int artMonth = artCal.get(Calendar.MONTH);
            int artDate = artCal.get(Calendar.DATE);

            // if dates don't match, it's time for a new group header
            if ((currentYear != artYear)
                    || (currentMonth != artMonth)
                    || (currentDate != artDate)) {

                if ((todayYear == artYear)
                        && (todayMonth == artMonth)
                        && (todayDate == artDate)) {
                    headers.add(getContext().getString(R.string.today));
                } else if ((todayYear == artYear)
                        && (todayMonth == artMonth)
                        && (todayDate == artDate - 1)) {
                    headers.add(getContext().getString(R.string.tomorrow));
                } else {
                    SimpleDateFormat hFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
                    headers.add(hFormat.format(articleDate));
                }

                currentYear = artYear;
                currentMonth = artMonth;
                currentDate = artDate;
            } else {
                headers.add(null);
            }
        }
    }
}

