package info.holliston.high.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import java.util.List;

import info.holliston.high.app.adapter.DailyAnnRVAdapter;
import info.holliston.high.app.adapter.EventsExpRVAdapter;
import info.holliston.high.app.adapter.LunchExpRVAdapter;
import info.holliston.high.app.adapter.NewsRVAdapter;
import info.holliston.high.app.adapter.RecyclerItemClickListener;
import info.holliston.high.app.adapter.SchedulesExpRVAdapter;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;
import info.holliston.high.app.fragment.DetailWebviewFragment;
import info.holliston.high.app.fragment.ExpRVFragment;
import info.holliston.high.app.fragment.HomeFragment;

/**
 * Organizes the transitions from one fragment to another.
 *
 * @author Tom Reeve
 */
public class FragmentOrganizer {

    private MainActivity mainActivity;

    // tracks the current fragment, so nav drawer clicks don't re-open the same fragment
    private int mCurrentNav;

    //==============================================================================================
    // region Constructor
    //==============================================================================================


    FragmentOrganizer(MainActivity ma) {
        this.mainActivity = ma;
    }

    // endregion
    //==============================================================================================
    // region Base & Home Pusher
    //==============================================================================================

    /**
     * Pushes a given fragment into the fragment frame holder
     *
     * @param fragment   the new Fragment to be displayed
     */
    private void pushToFragmentFrame(Fragment fragment) {
        int mainFrame = R.id.main_frame;

        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        //transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(mainFrame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Creates and sends the Home fragment
     */
    void pushHomeFragment() {
        HomeFragment homeFragment = new HomeFragment();
        pushToFragmentFrame(homeFragment);

        mCurrentNav = R.id.nav_home;
    }

    // endregion
    //==============================================================================================
    // region List Fragment Pushers
    //==============================================================================================

    /**
     * Creates and sends the Schedules list fragment
     */
    public void pushSchedulesFragment () {

        mainActivity.setAppBarImage(R.drawable.lobby);
        mainActivity.setToolBarTitle(mainActivity.getString(R.string.schedule_title_plural));

        mCurrentNav = R.id.nav_schedules;

        SchedulesExpRVAdapter adapter = new SchedulesExpRVAdapter(mainActivity);
        ExpRVFragment expRvFragment = new ExpRVFragment();
        expRvFragment.setSource(Article.SCHEDULES);
        expRvFragment.setAdapter(adapter);

        pushToFragmentFrame(expRvFragment);
    }

    /**
     * Creates and sends the Events list fragment
     */
    void pushEventsFragment () {

        mainActivity.setAppBarImage(R.drawable.singers);
        mainActivity.setToolBarTitle(mainActivity.getString(R.string.event_title_plural));

        mCurrentNav = R.id.nav_events;

        EventsExpRVAdapter adapter = new EventsExpRVAdapter(mainActivity);
        ExpRVFragment expRvFragment = new ExpRVFragment();
        expRvFragment.setSource(Article.EVENTS);
        expRvFragment.setAdapter(adapter);

        pushToFragmentFrame(expRvFragment);
    }

    /**
     * Creates and sends the Lunch list fragment
     */
    void pushLunchFragment () {

        mainActivity.setAppBarImage(R.drawable.art_lockers);
        mainActivity.setToolBarTitle(mainActivity.getString(R.string.lunch_title_plural));

        mCurrentNav = R.id.nav_lunch;

        LunchExpRVAdapter adapter = new LunchExpRVAdapter(mainActivity);
        ExpRVFragment expRvFragment = new ExpRVFragment();
        expRvFragment.setSource(Article.LUNCH);
        expRvFragment.setAdapter(adapter);

        pushToFragmentFrame(expRvFragment);
    }

    /**
     * Creates and sends the Daily Announcements list fragment
     */
    void pushDailyannFragment () {

        mainActivity.setAppBarImage(R.drawable.fall_musical);
        mainActivity.setToolBarTitle(mainActivity.getString(R.string.daily_ann_title));

        mCurrentNav = R.id.nav_dailyann;

        DailyAnnRVAdapter adapter = new DailyAnnRVAdapter(mainActivity);
        ExpRVFragment expRvFragment = new ExpRVFragment();
        expRvFragment.setSource(Article.DAILY_ANN);
        expRvFragment.setAdapter(adapter);

        expRvFragment.setRecyclerItemClickListener(
                new RecyclerItemClickListener(
                        mainActivity, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        sendToDetailFragment(Article.DAILY_ANN, position);
                    }
                }));

        pushToFragmentFrame(expRvFragment);
    }

    /**
     * Creates and sends the News list fragment
     */
    void pushNewsFragment () {

        mainActivity.setAppBarImage(R.drawable.football);
        mainActivity.setToolBarTitle(mainActivity.getString(R.string.news_title));

        mCurrentNav = R.id.nav_news;

        NewsRVAdapter adapter = new NewsRVAdapter(mainActivity);
        ExpRVFragment expRvFragment = new ExpRVFragment();
        expRvFragment.setSource(Article.NEWS);
        expRvFragment.setAdapter(adapter);

        expRvFragment.setRecyclerItemClickListener(
                new RecyclerItemClickListener(
                        mainActivity, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                sendToDetailFragment(Article.NEWS, position);
                            }
        }));

        pushToFragmentFrame(expRvFragment);
    }

    // endregion
    //==============================================================================================
    // region Detail Fragment Pushers
    //==============================================================================================

    /**
     * Creates a News Detail fragment for a single news article
     *
     * @param i   the index in the list of the relevant article
     */
    public void sendToDetailFragment(String type, int i) {

        Log.d("ClickListener", "Click -- Sending");

        ArticleDao dao = ArticleDatabase.getInstance(mainActivity.getApplicationContext()).articleDao();

        List<Article> articleList = dao.getArticles(type);
        if ((articleList != null) && (articleList.size() > i)) {
            Article article = articleList.get(i);

            sendToDetailFragment(article);
        }
    }

    private void sendToDetailFragment(Article article) {

        DetailWebviewFragment newFragment = new DetailWebviewFragment();
        newFragment.setArticle(article);

        mCurrentNav = -1;

        mainActivity.getFragmentOrganizer().pushToFragmentFrame(newFragment);

    }

    void showNewNews(String title) {
        DetailWebviewFragment newFragment = new DetailWebviewFragment();

        ArticleDao dao = ArticleDatabase.getInstance(mainActivity.getApplicationContext()).articleDao();
        Article newsArticle = dao.getNewsArticleWithTitle(title);

        if ((newsArticle != null) && (newsArticle.getTitle().equals(title))) {
            newFragment.setArticle(newsArticle);
        }
        mainActivity.getFragmentOrganizer().pushToFragmentFrame(newFragment);

        mCurrentNav = -1;
    }

    // endregion
    //==============================================================================================
    // region Accessors
    //==============================================================================================


    int getCurrentNav() {
        return mCurrentNav;
    }
}
