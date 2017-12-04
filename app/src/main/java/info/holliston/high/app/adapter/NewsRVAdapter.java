package info.holliston.high.app.adapter;

import android.support.v7.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import info.holliston.high.app.GlideApp;
import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

/**
 * A class to manage News data in a RecyclerView
 * NOTE: This class extends RVAdpater, not ExpRVAdapter, because the News view does not have
 * expand/collapse functionality; it sends the full article to a new fragment instead.
 *
 * @author Tom Reeve
 */
public class NewsRVAdapter extends RVAdapter {

    public NewsRVAdapter(MainActivity ma){
        super(ma, R.layout.rv_row_news, false);
    }

    /**
     * Adds data to the data row viewholder
     *
     * @param vh           the viewholder containing the data
     * @param article      the article of data to be shown
     */
    @Override
    protected void setDataRow(RecyclerView.ViewHolder vh, Article article, int position){
        RVAdapter.ViewHolderArticle vha =
                (RVAdapter.ViewHolderArticle) vh;

        // fills the title
        vha.text1.setText(article.getTitle());

        // fills the date
        SimpleDateFormat dfFull = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        String fullString = dfFull.format(article.getDate());
        vha.text2.setText(fullString);

        // fills the image thumbnail
        GlideApp.with(getContext())
                .load(article.getImgSrc())
                .placeholder(R.drawable.h_logo_square)
                .into(vha.icon);
    }
}


