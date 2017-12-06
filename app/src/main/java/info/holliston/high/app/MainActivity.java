package info.holliston.high.app;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.messaging.FirebaseMessaging;

import info.holliston.high.app.datamodel.DownloaderAsyncTask;
import info.holliston.high.app.firebase.HHSBroadcastReceiver;
import info.holliston.high.app.fragment.AboutFragment;

/**
 * This is the options activity, where the app begins. It controls the options lifecycle
 * of the android app and launches any fragment sections needed.
 *
 * @author Tom Reeve
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // a holder for adjusting the toolbar title and parallax image
    ToolbarSettings mToolbarSettings;

    // a helper for managing transitions between fragments
    FragmentOrganizer mFragmentOrganizer;

    // waits for cloud notifications and responds appropriately
    HHSBroadcastReceiver receiver = new HHSBroadcastReceiver();

    //==============================================================================================
    //region Activity lifecycle
    //==============================================================================================


    /**
     * Builds the activity, initializes options views, setup method variables
     *
     * @param savedInstanceState  saved state to restore from when the activity goes to sleep
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create ToolbarSettings object
        mToolbarSettings = makeToolbarSettings();
        setupNavDrawer(mToolbarSettings.getToolbar());

        // set subscriptions so this app receives the relevant push notices
        SetFirebaseSubscriptions();

        // requests async data calls to the server (but will display cached data until it arrives)
        callDataRefresh("all");

        // creates a helper class object to manage fragment transitions
        mFragmentOrganizer = new FragmentOrganizer(this);

        // starts on the Home page
        mFragmentOrganizer.pushHomeFragment();

        // detects if the app was launched by user clicking a cloud notification
        // that a new News post is available. If so, to opens that post's
        // detail page. This is done even though we just called .pushHomeFragment,
        // so that the detail page has somewhere to go if the user pushes the Back button.
        String updateSource = getIntent().getStringExtra("update_data");
        if ((updateSource != null)) {
            callDataRefresh(updateSource);
        }
        String newsTitle = getIntent().getStringExtra("title");
        if ((newsTitle != null) && (!newsTitle.equals(""))) {
            mFragmentOrganizer.showNewNews(newsTitle);
        }
    }

    /**
     * Prepares the fragment to receive broadcast intents,such as the one from the AsyncDownloader
     */
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(DownloaderAsyncTask.APP_RECEIVER));
    }

    /**
     * Stops the fragment from receiving broadcast intents
     */
    @Override
    public void onStop()
    {
        unregisterReceiver(receiver);
        super.onStop();
    }

    /**
     * Handles the Back button. This is overridden so that the back button will close the
     * menu drawer if open.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            // check the number of items in the "back stack." If there aren't any,
            // just go to the Home screen.
            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() <= 1) {
                mFragmentOrganizer.pushHomeFragment();
            } else {
                super.onBackPressed();
            }
        }
    }

    // endregion
    //==============================================================================================
    // region Setup
    //==============================================================================================


    /**
     *  Subscribes this app instance to the Firebase messaging, with relevant topics. Firebase will
     *  push notices to this app/device based on these subscriptions
     */
    private void SetFirebaseSubscriptions() {

        // sets the app to receive notifications for new News postings
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        // set the app to receive notifications for new data available
        FirebaseMessaging.getInstance().subscribeToTopic("updates");

        //TODO: remove before production
        if( BuildConfig.DEBUG ) {
            FirebaseMessaging.getInstance().subscribeToTopic("debug");
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("debug");
        }
    }

    /**
     * Creates the slide-in menu drawer, accessed via the menu icon in the top left of the toolbar
     *
     * @param toolbar  the top toolbar with the title and menu icon
     */
    private void setupNavDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    /**
     * Creates a ToolbarSettings object to help manage changes to the toolbar as the app is used
     *
     * @return the ToolbarSettings object
     */
    private ToolbarSettings makeToolbarSettings() {

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        return new ToolbarSettings(appBarLayout, collapsingToolbarLayout, toolbar);
    }

    /**
     * Creates the Settings (three dots) menu that appears in the top right of the toolbar
     *
     * @param menu the menu created from the menu layout
     * @return true (allows normal menu processing to proceed)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    // endregion
    //==============================================================================================
    // region Navigation direction
    //==============================================================================================

    /**
     * Handles clicks in the manu nav drawer
     *
     * @param item the item that was clicked
     * @return     true, allows normal menu processing to proceed
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == mFragmentOrganizer.getCurrentNav()) {
            //change nothing
        } else if (id == R.id.nav_home) {
            mFragmentOrganizer.pushHomeFragment();
        } else if (id == R.id.nav_schedules) {
            mFragmentOrganizer.pushSchedulesFragment();
        } else if (id == R.id.nav_events) {
            mFragmentOrganizer.pushEventsFragment();
        } else if (id == R.id.nav_lunch) {
            mFragmentOrganizer.pushLunchFragment();
        } else if (id == R.id.nav_dailyann) {
            mFragmentOrganizer.pushDailyannFragment();
        } else if (id == R.id.nav_news) {
            mFragmentOrganizer.pushNewsFragment();
        } else if (id == R.id.nav_sports){
            openSports();
        }else if (id == R.id.nav_website) {
            openWebsite();
        } else if (id == R.id.nav_about) {
            showAbout();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Redirects the user to the HHS website, usually in response to the nav button
     */
    private void openWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url_hhs)));
        startActivity(browserIntent);
    }

    /**
     * Redirects the user to the HHS sports schedules webpage, usually in response to the nav button
     */
    private void openSports() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url_sports)));
        startActivity(browserIntent);
    }

    /**
     * Displays an About Page, usually in response to the nav button
     */
    private void showAbout() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.show(transaction, "about");
    }

    /**
     * Handles clicks within the Settings menu
     *
     * @param item the item that was clicked
     * @return whether the superclass is allowed to proceed with normal menu processing
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if  (id == R.id.action_refresh) {
            callDataRefresh("all");
        }

        return super.onOptionsItemSelected(item);
    }
    // endregion
    //==============================================================================================
    // region Toolbar adjustments
    //==============================================================================================

    /**
     * Sets the title that appears at the top of the collapsing toolbar
     *
     * @param title  the text to be displayed
     */
    public void setToolBarTitle(String title) {
        mToolbarSettings.setToolbarTitle(title);
    }

    /**
     * Sets the image to be shown in the collapsing toolbar
     *
     * @param resource the R.drawable resource to be shown
     */
    public void setAppBarImage(int resource) {
        mToolbarSettings.setToolbarImage(resource, this);
    }

    /**
     * Opens or closes the collapsing toolbar
     *
     * @param expand  true to expand, false to collapse
     */
    public void setAppBarExpanded(Boolean expand) {
        mToolbarSettings.setAppBarExpanded(expand);
    }

    //endregion
    //==============================================================================================
    // region Data & Fragment Transitions
    //==============================================================================================

    /**
     * Starts async tasks to pull new data from the servers
     */
    private void callDataRefresh(String source) {
        if ((source == null) || (source.equals(""))) {
            source = "all";
        }
        new DownloaderAsyncTask(this, source)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Makes the FragmentOrganizer accessible to child fragments
     *
     * @return the FragmentOrganizer instance for this activity
     */
    public FragmentOrganizer getFragmentOrganizer() {
        return mFragmentOrganizer;
    }

    // endregion
}
