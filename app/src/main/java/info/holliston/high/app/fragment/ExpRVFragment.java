package info.holliston.high.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.adapter.RVAdapter;
import info.holliston.high.app.adapter.RecyclerItemClickListener;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.DownloaderAsyncTask;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;

/**
 * A fragment with an Expandable RecyclerFragment as its options layout.
 *
 * @author Tom Reeve
 */
public class ExpRVFragment extends Fragment {

    // the views created by this fragment
    private View v;

    // the adapter that manages how data is displayed in the fragment
    private RVAdapter adapter;
    private RecyclerItemClickListener mRecyclerItemClickListener;

    // the type of data stored
    private String mSource;

    //==============================================================================================
    // region Lifecycle
    //==============================================================================================

    /**
     * Creates the fragments views
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity ma = (MainActivity) getActivity();
        ma.setAppBarExpanded(true);

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_rv, container, false);

        return v;
    }

    /**
     * Overridden to load the data for the fragment's adapter
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Article> articleList = getArticles();
        if ((articleList != null) && (articleList.size() > 0)) {
            adapter.setData(articleList);
        } else {
            requestNewDownload();
        }

        RecyclerView rv = v.findViewById(R.id.recyclerview);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(manager);
        rv.setAdapter(adapter);

        // since clicks in News open new fragments, add a click listener
        if (mRecyclerItemClickListener != null) {
            rv.addOnItemTouchListener(mRecyclerItemClickListener);
        }
    }

    /**
     * Prepares the fragment to receive broadcast intents,such as the one from the AsyncDownloader
     */
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(DownloaderAsyncTask.APP_RECEIVER));
    }

    /**
     * Stops the fragment from receiving broadcast intents
     */
    @Override
    public void onStop()
    {
        getActivity().unregisterReceiver(receiver);
        super.onStop();
    }

    // endregion
    //==============================================================================================
    // region Data processing
    //==============================================================================================

    /**
     * Gets the articles from the article store
     *
     * @return  a list of current articles to display
     */
    private List<Article> getArticles(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date checkDate = cal.getTime();

        ArticleDao dao = ArticleDatabase.getInstance(getContext()).articleDao();

        switch (mSource) {
            case Article.SCHEDULES:
            case Article.LUNCH:
            case Article.EVENTS:
                return dao.getArticlesAfter(checkDate, mSource);

            case Article.NEWS:
            case Article.DAILY_ANN:
                return dao.getArticles(mSource);

            default:
               throw new IllegalArgumentException("Invalid URI, cannot insert with source type: " + mSource);
        }
    }

    /**
     * Requests a call to download new data for this fragment only
     */
    protected void requestNewDownload() {
        new DownloaderAsyncTask(getContext(), mSource)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // endregion
    //==============================================================================================
    // region Receiver
    //==============================================================================================

    /*
     * Receive messages for data refresh completion or notification
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = 0;
            String sourceName = "";
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                count = bundle.getInt("count",0);
                sourceName= bundle.getString("sourceName", "");
            }

            //update sections of UI based on which feeds report updated
            if ((count >0) && (sourceName.equals(mSource))) {

                adapter.setData(getArticles());
                adapter.notifyDataSetChanged();

                Snackbar.make(v, "New data downloaded", Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    // endregion
    //==============================================================================================
    // region Accessors
    //==============================================================================================

    public void setSource(String mSource) {
        this.mSource = mSource;
    }

    public void setAdapter(RVAdapter adapter) {
        this.adapter = adapter;
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        mRecyclerItemClickListener = recyclerItemClickListener;
    }
}