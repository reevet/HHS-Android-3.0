package info.holliston.high.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.DownloaderAsyncTask;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;

/**
 * Creates a fragment to display html content of a single news article or announcement
 *
 * @author Tom Reeve
 */

public class DetailWebviewFragment extends Fragment {

    // the Article to be displayed
    private Article mArticle;

    // the view for this layout
    protected View view;

    //==============================================================================================
    // region Constructor & lifecycle
    //==============================================================================================

    /**
     * Required public constructor
     */
    public DetailWebviewFragment() {
        super();
    }

    /**
     * Creates the fragment's view
     *
     * @param inflater            the fragment inflater
     * @param container           the container that hows the fragment
     * @param savedInstanceState  any info if the fragment was suspended
     * @return                    the view to be displayed
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(
                R.layout.detail_fragment, container, false);
        WebView webview = v.findViewById(R.id.web_view);
        String webHtml;
        if (mArticle != null)  {
            webHtml = formattedArticle();
        } else {
            webHtml = "<p style='text-align:center'>Loading article....</p>";
            new DownloaderAsyncTask(getContext())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        webview.loadData(webHtml, "text/html",null );

        MainActivity ma = (MainActivity) getActivity();
            ma.setAppBarExpanded(false);

        return v;
    }

    /**
     * Sets this fragment to listen for Asycn Download triggers
     */
    @Override
    public void onResume() {
        getActivity().registerReceiver(receiver, new IntentFilter(DownloaderAsyncTask.APP_RECEIVER));
        super.onResume();
    }

    /**
     * Removes the receiver if the fragment stops
     */
    @Override
    public void onStop() {
        getActivity().unregisterReceiver(receiver);
        super.onStop();
    }

    // endregion
    //==============================================================================================
    // region Formatters
    //==============================================================================================

    /**
     * Formats the article to add a title and format the image
     *
     * @return        HTML with the article's formatted content
     */
    private String formattedArticle() {
        return "<h1>"+mArticle.getTitle() + "</h1> "
                + limitImgWidth(mArticle.getDetails());
    }

    /**
     * Resizes the image in the window
     *
     * @param html   the HTML string that contains an image
     * @return       the same HTML, with styling added to the beginning
     */
    private String limitImgWidth(String html) {
        return "<style>img{max-width:90% !important;height:auto !important;}</style>" + html;
    }

    // endregion
    //==============================================================================================
    // region Receiver
    //==============================================================================================

    /**
     * Creates a receiver to receive Async Download notices. This is used when the app is launched
     * because the user clicked a "New News Post" notification, and this view had to wait to
     * receive notice that the new post was downloaded.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = 0;
            String sourceName = "";
            Bundle bundle = intent.getExtras();

            // gets the info from the intent
            if (bundle != null) {
                count = bundle.getInt("count",0);
                sourceName= bundle.getString("sourceName", "");
            }

            //updates sections of UI if news was downloaded
            if ((count >0) && (sourceName.equals(Article.NEWS))) {

                // gets the most recent article
                ArticleDao dao = ArticleDatabase.getInstance(getContext()).articleDao();
                mArticle = dao.getArticles(Article.NEWS).get(0);

                // adds its content to the webview
                WebView webview = view.findViewById(R.id.web_view);
                webview.loadData(formattedArticle(), "text/html", null);
            }
        }
    };

    // endregion
    //==============================================================================================
    // region Accessors
    //==============================================================================================

    /**
     * Sets the article that should be displayed. If possible, this should be set before
     * transitioning to this fragment.
     *
     * @param article   the article to display
     */
    public void setArticle(Article article) {
        mArticle = article;
    }
}
