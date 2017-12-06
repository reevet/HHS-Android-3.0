package info.holliston.high.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import info.holliston.high.app.GlideApp;
import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.DownloaderAsyncTask;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;

/**
 * The fragment that shows a summary of today's info and recent news
 */
public class HomeFragment extends Fragment {

    private View v;

    //==============================================================================================
    // region Constructor and lifecycle
    //==============================================================================================

    /**
     * Required empty public constructor
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Creates the view to be displayed in the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);

        // Sets tup the toolbar title and image
        MainActivity ma = (MainActivity) getActivity();
        ma.setAppBarExpanded(true);
        ma.setAppBarImage(R.drawable.front_door_full);
        ma.setToolBarTitle(ma.getString(R.string.app_name));

        // Fill data into the display cards
        buildCards();

        return v;
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
    // region Card construction
    //==============================================================================================

    private void buildCards() {

        // establishes the cards to view
        View todayCard = v.findViewById(R.id.today_card);
        View tomorrowCard = v.findViewById(R.id.tomorrow_card);
        View newsCard = v.findViewById(R.id.news_card);

        // gets the articles (two schedules, two lunches, one daily, one news)
        HashMap<String, Article> articles = getArticles();

        // show "today" card, if there's a schedule
        if (articles.containsKey("scheduleToday")) {
            Article schedToday = articles.get("scheduleToday");

            // fill the article data into relevant sections
            buildCardSchedule(schedToday, todayCard);
            buildCardIcon(schedToday, todayCard);

            if (articles.containsKey("lunchToday")) {
                buildCardLunch(articles.get("lunchToday"), todayCard);
            }
            if (articles.containsKey("dailyann")) {
                buildCardDailyann(articles.get("dailyann"), todayCard);
            }
            //display (show, not hide) the card
            showCard(todayCard);

            // sets the click listener to show the schedules list when clicked
            View.OnClickListener cl = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity ma = (MainActivity) getActivity();
                    ma.getFragmentOrganizer().pushSchedulesFragment();
                }
            };
            todayCard.setOnClickListener(cl);
        } else {
            hideCard(todayCard);
        }

        // show "tomorrow" card, if there's a schedule
        if (articles.containsKey("scheduleTomorrow")) {
            // fill the article data into relevant sections
            buildCardSchedule(articles.get("scheduleTomorrow"), tomorrowCard);
            buildCardIcon(articles.get("scheduleTomorrow"), tomorrowCard);
            if (articles.containsKey("lunchTomorrow")) {
                buildCardLunch(articles.get("lunchTomorrow"), tomorrowCard);
            }
            //display (show, not hide) the card
            showCard(tomorrowCard);

            // sets the click listener to show the schedules list when clicked
            View.OnClickListener cl = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity ma = (MainActivity) getActivity();
                    ma.getFragmentOrganizer().pushSchedulesFragment();
                }
            };
            tomorrowCard.setOnClickListener(cl);
        } else {
            hideCard(tomorrowCard);
        }

        // shows the news card (if a post exists)
        if (articles.containsKey("news")) {
            // fill the article data into relevant sections
            buildNewsCard(articles.get("news"), newsCard);
            //display (show, not hide) the card
            showCard(newsCard);
        } else {
            hideCard(newsCard);
        }
    }

    /**
     * Hides a card (usually if there is no data to fill it)
     * @param view  the view to hide
     */
    private void hideCard(View view) {
        view.setVisibility(View.GONE);
    }

    /**
     * Shows a card
     * @param view  the view to hide
     */
    private void showCard(View view) {
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Fills data into the news card
     * @param article   the news article post to show
     * @param newsCard  the filled news card
     */
    private void buildNewsCard(Article article, View newsCard) {

        // formats and fills the date
        Date postDate = article.getDate();
        String dateString = String.format(Locale.US,"%1$ta, %1$tb %1$te ", postDate);
        TextView postDateTV = v.findViewById(R.id.news_posted);
        String dateText = getActivity().getString(R.string.posted)+" "+ dateString;
        postDateTV.setText(dateText);

        // formats and fills the title
        TextView titleTV = v.findViewById(R.id.news_title);
        titleTV.setText(article.getTitle());

        // trims and fills the post's content
        String details = article.getDetails();
        TextView detailsText = v.findViewById(R.id.news_details);
        // parses the content
        Document document = Jsoup.parse(details);
        details = document.text();
        // trims the length to 120 characters
        int end = (details.length()<=120) ? details.length()-1 : 120;
        details = details.substring(0,end) + "...";
        detailsText.setText(details);

        // fills the thumbnail image
        ImageView iv = v.findViewById(R.id.news_image);
        GlideApp.with(getContext())
                .load(article.getImgSrc())
                .placeholder(R.drawable.sign)
                .into(iv);

        // sets a click listener to view the full article
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity ma = (MainActivity) getActivity();
                ma.getFragmentOrganizer().sendToDetailFragment(Article.NEWS, 0);
            }
        };
        newsCard.setOnClickListener(cl);
    }

    /**
     * Fills data into the schedule part of a card
     *
     * @param article the schedule article to view
     * @param card    card to fill in (typically todayCard or tomorrowCard)
     */
    private void buildCardSchedule(Article article, View card) {

        // formats and fills the date
        Date schedDate = article.getDate();
        Calendar schedCal = Calendar.getInstance();
        schedCal.setTime(schedDate);
        TextView todayHeader = card.findViewById(R.id.card_date);
        todayHeader.setText(String.format(Locale.US, "%1$tA, %1$tB %1$te ", schedCal));

        // formats and fills the title
        TextView schedFull = card.findViewById(R.id.card_schedule);
        schedFull.setText(article.getTitle());
    }

    /**
     * Fills data into the icon-image part of the card
     * @param article  the schedule article the image is matching
     * @param card     the card to fill in (typically todayCard or tomorrowCard)
     */
    private void buildCardIcon(Article article, View card){
        // gets the first letter of the schedule's title
        String schedule = article.getTitle();
        if (schedule.length() >0) {
            char initial = schedule.charAt(0);

            //selects an icon based on that letter
            setIcon(initial, card.findViewById(R.id.card_schedule_icon));
            (card.findViewById(R.id.card_schedule_icon))
                    .setContentDescription(schedule.substring(0,1));
        }
    }

    /**
     * Fills data into the lunch section of the card
     *
     * @param article  the lunch article ro display
     * @param card     the card to fill (typically todayCard or tomorrowCard)
     */
    private void buildCardLunch(Article article, View card) {
        TextView lunchMenu = card.findViewById(R.id.card_lunch);
        if (article != null) {
            // fill and show the lunch title
            lunchMenu.setText(article.getTitle());
            lunchMenu.setVisibility(View.VISIBLE);
        } else {
            lunchMenu.setVisibility(View.GONE);
        }
    }

    /**
     * Shows or hides the Daily Announcements icon
     * @param article  the dailyann post
     * @param card     the card to fill in
     */
    private void buildCardDailyann(Article article, View card) {
        TextView dailyannPost = card.findViewById(R.id.card_dailyann_text);
        ImageView dailyannIcon = card.findViewById(R.id.card_dailyann_icon);

        if (article != null) {
            // shows the parts
            dailyannIcon.setVisibility(View.VISIBLE);
            dailyannPost.setVisibility(View.VISIBLE);
            dailyannPost.setText("Announcements\nPosted!");

            // sets the click listener to show the daily announcements post when clicked
            View.OnClickListener cl = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity ma = (MainActivity) getActivity();
                    ma.getFragmentOrganizer().sendToDetailFragment(Article.DAILY_ANN, 0);
                }
            };
            dailyannIcon.setOnClickListener(cl);
            dailyannPost.setOnClickListener(cl);
        } else {
            dailyannIcon.setVisibility(View.GONE);
            dailyannPost.setVisibility(View.GONE);
        }
    }

    /**
     * Fills an icon image based on a provided letter
     *
     * @param letter  the letter to help us select
     * @param view    the view to put the image in
     */
    private void setIcon(char letter, View view) {
        int icon;

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

        //fills the image into the view
        ImageView iv = (ImageView) view;
        GlideApp.with(getContext())
                .load(icon)
                .placeholder(R.drawable.star_sm)
                .into(iv);
    }



    // endregion
    //==============================================================================================
    // region Data retrieval
    //==============================================================================================

    /**
     * Retrieves latest news, upcoming schedules, and matching lunch and daily announcements
     *
     * @return the articles, with hashmap keys "scheduleToday", "lunchToday", "dailyann",
     *                                         "scheduleTomorrow", "lunchTomorrow", and "news"
     */
    private HashMap<String, Article> getArticles() {
        // sets up a map for returing the articles
        HashMap<String, Article> returnMap = new HashMap<>();

        // sets up the article store
        ArticleDao dao = ArticleDatabase.getInstance(getContext()).articleDao();

        // finds the latest news article
        List<Article> newsList = dao.getArticles(Article.NEWS);
        if ((newsList != null) && (newsList.size()) > 0 ) {
            returnMap.put("news", newsList.get(0));
        }

        // gets upcoming schedule articles
        List<Article> scheduleList = dao.getArticlesAfter(todayWithoutTime(), Article.SCHEDULES);

        // if there is at least one upcoming schedule...
        if ((scheduleList != null) && (scheduleList.size() > 0)) {

            // sets the article for "today"
            Article schedToday = scheduleList.get(0);
            returnMap.put("scheduleToday", schedToday);

            // finds and include daily announcements for the same date
            Article dailyann = getMatchingArticle(schedToday, Article.DAILY_ANN, dao);
            if (dailyann != null) {
                returnMap.put("dailyann", dailyann);
            }

            // finds and includes lunch menus for the same date
            Article lunch = getMatchingArticle(schedToday, Article.LUNCH, dao);
            if (lunch != null) {
                returnMap.put("lunchToday", lunch);
            }

            // if there are at least TWO upcoming schedules....
            if (scheduleList.size() > 1) {
                // finds and sets tomorrow's schedule
                Article schedTomorrow = scheduleList.get(1);
                returnMap.put("scheduleTomorrow", schedTomorrow);

                // finds and includes lunch menus for the same date
                Article lunchTomorrow = getMatchingArticle(schedTomorrow, Article.LUNCH, dao);
                if (lunchTomorrow != null) {
                    returnMap.put("lunchTomorrow", lunchTomorrow);
                }
            }
        }
        return returnMap;
    }

    /**
     * Gets the article from a source (dailyann, lunch, etc) whose date matches the
     * schedule article's date.
     *
     * @param scheduleArticle  the schedule article to match
     * @param type             the type of articles to retrieve
     * @param dao              the database Data Access Object
     * @return                 the matching article, or null if no article matches
     */
    private Article getMatchingArticle(Article scheduleArticle, String type, ArticleDao dao) {
        Date schedDate = scheduleArticle.getDate();

        // gets all articles of the type
        List<Article> tempList = dao.getArticles(type);

        // cycles through the articles, returning the first one with the same date
        for (Article article : tempList) {
            Date artDate = article.getDate();
            if (datesEqual(artDate, schedDate)) {
                return article;
            }
        }
        return null;
    }


    // endregion
    //==============================================================================================
    // region Calendar helpers
    //==============================================================================================

    /**
     * Builds a date for today, with no time (i.e., time = 00:00:00)
     *
     * @return today's date, with 00:00:00 for time
     */
    private Date todayWithoutTime() {
        // sets a comparison date for today at 12 am
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Determines if two dates are one the same day (even if they are different times)
     *
     * @param date1  the first date to check
     * @param date2  the second date to check
     * @return          true if they fall on the same day, false if not
     */private Boolean datesEqual(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance(Locale.US);
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance(Locale.US);
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);

        //returns true if all date components match
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
                && (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
                && (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE));
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
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                count = bundle.getInt("count",0);
            }

            //update sections of UI based on which feeds report updated
            if ((count >0)) {

                buildCards();
                buildCards();
            }
        }
    };
}
