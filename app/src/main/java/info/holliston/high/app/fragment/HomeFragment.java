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
import java.util.List;
import java.util.Locale;

import info.holliston.high.app.GlideApp;
import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.DownloaderAsyncTask;
import info.holliston.high.app.datamodel.database.ArticleDao;
import info.holliston.high.app.datamodel.database.ArticleDatabase;


public class HomeFragment extends Fragment {

    private View v;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);

        MainActivity ma = (MainActivity) getActivity();
        ma.setAppBarExpanded(true);
        ma.setAppBarImage(R.drawable.front_door_full);
        ma.setToolBarTitle(ma.getString(R.string.app_name));

        buildCards();

        return v;
    }


    private void buildCards() {
        View todayCard = v.findViewById(R.id.today_card);
        View tomorrowCard = v.findViewById(R.id.tomorrow_card);
        View newsCard = v.findViewById(R.id.news_card);


        ArticleDao dao = ArticleDatabase.getInstance(getContext()).articleDao();
        Date today = new Date();

        List<Article> scheduleList = dao.getArticlesAfter(today, Article.SCHEDULES);

        if ((scheduleList != null) && (scheduleList.size() > 0)) {
            Article schedToday = scheduleList.get(0);
            buildCardSchedule(schedToday, todayCard);
            buildCardIcon(schedToday, todayCard);
            buildCardLunch(getMatchingArticle(schedToday, Article.LUNCH, dao), todayCard);
            buildCardDailyann(getMatchingArticle(schedToday, Article.DAILY_ANN, dao), todayCard);

            showCard(todayCard);

            if (scheduleList.size() > 1 ) {
                Article schedTomorrow = scheduleList.get(1);
                buildCardSchedule(schedTomorrow, tomorrowCard);
                buildCardIcon(schedTomorrow, tomorrowCard);
                buildCardLunch(getMatchingArticle(schedTomorrow, Article.LUNCH, dao), tomorrowCard);

                showCard(tomorrowCard);
            } else {
                hideCard(tomorrowCard);
            }
        } else {
            hideCard(todayCard);
        }

        List<Article> newsList = dao.getArticles(Article.NEWS);
        if ((newsList != null) && (newsList.size() >0)) {
            buildNewsCard(newsList.get(0), newsCard);
            showCard(newsCard);
        } else {
            hideCard(newsCard);
        }
    }

    private Article getMatchingArticle(Article scheduleArticle, String type, ArticleDao dao) {
        Date schedDate = scheduleArticle.getDate();
        List<Article> tempList = dao.getArticles(type);

        for (Article article : tempList) {
            Date artDate = article.getDate();
            if (datesEqual(artDate, schedDate)) {
                return article;
            }
        }
        return null;
        }

    /**
     * Determines is two dates are one the same day (even if they are different times)
     *
     * @param date1  the first date to check
     * @param date2  the second date to check
     * @return          true if they fall on the same day, false if not
     */private Boolean datesEqual(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance(Locale.US);
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance(Locale.US);
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);

        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
                && (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
                && (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE));

    }

    private void hideCard(View view) {
        view.setVisibility(View.GONE);
    }

    private void showCard(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void buildNewsCard(Article article, View newsCard) {

        Date postDate = article.getDate();
        String dateString = String.format(Locale.US,"%1$ta, %1$tb %1$te ", postDate);
        TextView postDateTV = v.findViewById(R.id.news_posted);
        String dateText = getActivity().getString(R.string.posted)+" "+ dateString;
        postDateTV.setText(dateText);

        TextView titleTV = v.findViewById(R.id.news_title);
        titleTV.setText(article.getTitle());



        String details = article.getDetails();
        TextView detailsText = v.findViewById(R.id.news_details);
        Document document = Jsoup.parse(details);
        details = document.text();
        int end = (details.length()<=120) ? details.length()-1 : 120;
        details = details.substring(0,end) + "...";
        detailsText.setText(details);

        ImageView iv = v.findViewById(R.id.news_image);
        GlideApp.with(getContext())
                .load(article.getImgSrc())
                .placeholder(R.drawable.sign)
                .into(iv);
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity ma = (MainActivity) getActivity();
                ma.getFragmentOrganizer().sendToDetailFragment(Article.NEWS, 0);
            }
        };
        newsCard.setOnClickListener(cl);
    }

    private void buildCardSchedule(Article article, View card) {

        Date schedDate = article.getDate();
        Calendar schedCal = Calendar.getInstance();
        schedCal.setTime(schedDate);

        TextView todayHeader = card.findViewById(R.id.card_date);
        todayHeader.setText(String.format(Locale.US, "%1$tA, %1$tB %1$te ", schedCal));

        TextView schedFull = card.findViewById(R.id.card_schedule);
        schedFull.setText(article.getTitle());
    }

    private void buildCardIcon(Article article, View card){
        String schedule = article.getTitle();
        if (schedule.length() >0) {
            char initial = schedule.charAt(0);
            setIcon(initial, card.findViewById(R.id.card_schedule_icon));
            (card.findViewById(R.id.card_schedule_icon))
                    .setContentDescription(schedule.substring(0,1));
        }
    }

    private void buildCardLunch(Article article, View card) {
        TextView lunchMenu = card.findViewById(R.id.card_lunch);
        if (article != null) {
            lunchMenu.setText(article.getTitle());
            lunchMenu.setVisibility(View.VISIBLE);
        } else {
            lunchMenu.setVisibility(View.GONE);
        }
    }

    private void buildCardDailyann(Article article, View card) {
        TextView dailyannPost = card.findViewById(R.id.card_dailyann_text);
        ImageView dailyannIcon = card.findViewById(R.id.card_dailyann_icon);

        if (article != null) {
            dailyannIcon.setVisibility(View.VISIBLE);
            dailyannPost.setVisibility(View.VISIBLE);
            dailyannPost.setText("Announcements\nPosted!");

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

    private void setIcon(char letter, View view) {
        int icon;

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

        ImageView iv = (ImageView) view;

        GlideApp.with(getContext())
                .load(icon)
                .placeholder(R.drawable.star_sm)
                .into(iv);
    }


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

                //MainActivity ma = (MainActivity) getActivity();
                //ma.getFragmentOrganizer().updateFragmentFrame(new HomeFragment());
            }
        }
    };
}
