package info.holliston.high.app.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

/**
 * A generic class for loading data into a RecyclerView.
 *
 * @author Tom Reeve
 */

public abstract class RVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;

    // The organized data. Data can be sorted into groups, and the group header (for example,
    // "Today's Events") can be inserted as its own separate row in the list.
    private List<Article> mArticleList = new ArrayList<>();

    List<String> headers = new ArrayList<>();

    // Whether or not the data can be trimmed based on time of day. For example, Schedules data
    // is usually marked isTrimmable = true so that, after 2pm, today's schedule is ignored
    // and tomorrow's is displayed instead.
    Boolean isTrimmable = false;

    // The relevant layout file for this fragment
    int rowLayout;

    RVAdapter(Context c, int rowLayout, Boolean isTrimmable) {
        this.context = c;
        this.rowLayout = rowLayout;
        this.isTrimmable = isTrimmable;
    }

    /**
     * Sets and groups the data for the fragment
     *
     * @param articleList  List of articles to display
     */
    public void setData(List<Article> articleList) {
        mArticleList = articleList;
        createHeaders(articleList);
    }

    /**
     * Sorts the cursor's data into groups, with each article is its own row.
     * Articles are also grouped, according to the data. This default method returns a list of
     * data ungrouped (no header rows), but subclasses can override to perform their own grouping
     * <p>
     * For example:
     * list[0] = "Today's events"
     * list[1] = Article1
     * list[2] = Article2
     * list[3] = "Tomorrow's events"
     * list[4] = Article 3
     * etc.
     *
     * @param articleList    the data to be sorted, in List form
     */
    protected void createHeaders(List<Article> articleList) {
    }

    /**
     * Creates the view for a row of data (not a header) in the recyclerview
     */
    protected RecyclerView.ViewHolder createRowView(ViewGroup parent) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(rowLayout, parent, false);

        return new RVAdapter.ViewHolderArticle(rowView);
    }

    /**
     * Fills data into a row of data (not a header) in the recyclerview. Because this is
     * data-specific, it must be overridden.
     * @param vh           the viewholder to hold the data pieces
     * @param article      the article of data to be shown
     */
    protected abstract void setDataRow(RecyclerView.ViewHolder vh,
                                                          Article article, int position);


    /**
     * Gets the type of data for a given row. Row types are determined by the mArticleList's
     * item's data type.
     *
     * @param position  the position (row number) in the recyclerview
     * @return          int 0 = empty data set, 1 = data Article, 2 = header
     */
    @Override
    public int getItemViewType(int position) {
        if((mArticleList == null) || (mArticleList.size() == 0)) {
            return 0;   //0 = one row for empty dataset
        }
        return 1;   //1 = type Article
    }

    /**
     * Gets the number of data items for display in the recyclerview. If the data set is empty,
     * it returns 1 so that one row of "No data" is displayed
     *
     * @return the number of rows of data (including header rows)
     */
    @Override
    public int getItemCount() {
        if ((mArticleList == null) || mArticleList.size() == 0) {
            return 1; //so that we can display one "empty list" row
        }
        return mArticleList.size();
    }

    /**
     * Sets the viewholder for a particular row in the recyclerview. The viewholder holds references
     * to the display elements (textviews, imageviews, etc). The type of viewholder is determined
     * by the data to be shown, so that determine viewholder can be used for data rows and header
     * rows.
     *
     * @param parent    the containing row view
     * @param viewType  the view of row, based on the data
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return createRowView(parent);
    }

    /**
     * Fills the data into the row. If the data is a (default) Article, this method calls the
     * abstract getDataRow method.
     *
     * @param holder     the viewholder for the data
     * @param position   the position (row number) in the recyclerview
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        setDataRow(holder, mArticleList.get(position), position);
    }

    /**
     * Determines is the current time is after 2pm, and if the first row of data has already passed.
     *
     * @param list     the List of pre-grouped data
     * @return         true if after 2pm, false if not
     */
    Boolean needsTrimming(List<Article> list){

        // if no data, or if only one day of data, don't bother
        if ((list == null) || list.size() <=1 ) {
            return false;
        }

        //get today's values for year, month, date, hour
        Date todayDate = new Date();
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(todayDate);
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayMonth = todayCal.get(Calendar.MONTH);
        int todayDay = todayCal.get(Calendar.DATE);
        int todayHour = todayCal.get(Calendar.HOUR_OF_DAY);

        //school ends at 2pm, so after 14:00, remove today's data from the list
        if (todayHour >= 14) {
            //get year, month, and date values of the first schedule in the list
            Date firstDate = list.get(0).getDate();
            Calendar firstCal = Calendar.getInstance();
            firstCal.setTime(firstDate);
            int firstYear = firstCal.get(Calendar.YEAR);
            int firstMonth = firstCal.get(Calendar.MONTH);
            int firstDay = firstCal.get(Calendar.DATE);

            //only remove the first item if it represents today's data
            if ((todayDay == firstDay) && (todayMonth == firstMonth) && (todayYear == firstYear)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A custom viewholder to hold data (Article) row information.
     */
    class ViewHolderArticle extends RecyclerView.ViewHolder {

        View view;
        TextView text2;
        TextView text1;
        ImageView icon;
        ImageView discoveryArrow;
        TextView detailsView;
        CardView cardView;
        FrameLayout frameLayout;
        TextView headerTextview;
        TableRow headerFrame;

        ViewHolderArticle (View v) {
            super(v);
            view = v;
            text1 = v.findViewById(R.id.row_text_primary);
            text2 = v.findViewById(R.id.row_text_secondary);
            icon = v.findViewById(R.id.row_icon);
            detailsView = v.findViewById(R.id.details_textview);
            cardView = v.findViewById(R.id.row_cardview);
            discoveryArrow = v.findViewById(R.id.row_disc_icon);
            frameLayout = view.findViewById(R.id.frame_layout);
            headerTextview = v.findViewById(R.id.header_title);
            headerFrame = v.findViewById(R.id.header_row_frame);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

