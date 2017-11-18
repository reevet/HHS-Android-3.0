package info.holliston.high.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.DownloaderAsyncTask;
import info.holliston.high.app.fragment.AboutFragment;
import info.holliston.high.app.fragment.FragmentOrganizer;

/**
 * This is the main activity, where the app begins. It controls the main lifecycle
 * of the android app and launches any fragment sections needed.
 *
 * @author Tom Reeve
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ToolbarSettings mToolbarSettings;
    FragmentOrganizer mFragmentOrganizer;

    /* ***** OVERRIDE METHODS ********
     * Overriding creation methods from the superclass Activity
     */

    /**
     * Builds the activity, initializes main views, setup method variables
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
        callDataRefresh();

        // creates a helper class object to manage fragment transitions
        mFragmentOrganizer = new FragmentOrganizer(this);

        // sets the initial view for the app, based on a notification or not
        String newsDetail = getIntent().getStringExtra("update_data");
        Intent intent = getIntent();
        if ((newsDetail != null) && (!newsDetail.equals(""))) {
            // this gives the detail somewhere to go if Back is pressed
            mFragmentOrganizer.sendToDetailFragment(Article.NEWS, 0);
        } else {
            mFragmentOrganizer.pushHomeFragment();
        }
    }

    /**
     * Handles the Back button. This is overridden so that the backbutton will close the
     * menu drawer if open.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager manager = getSupportFragmentManager();
            int count = manager.getBackStackEntryCount();
            if (manager.getBackStackEntryCount() <= 1) {
                mFragmentOrganizer.pushHomeFragment();
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * Creates the Settings (three dots) menu that appears in the top right of the toolbar
     *
     * @param menu the menu created from the menu layout
     *
     * @return true (allows normal menu processing to proceed)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* ***** SETUP METHODS ********
     * Used only to setup the activity and its elements
     */

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

    /* ***** NAVIGATION METHODS ********
     * Used to direct to appropriate fragments (pages) based on user input
     */

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

    private void openWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url_hhs)));
        startActivity(browserIntent);
    }

    private void openSports() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url_sports)));
        startActivity(browserIntent);
    }

    private void showAbout() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.show(transaction, "about");
    }

    /**
     * Handles clicks within the Settings menu
     *
     * @param item the item that was clicked
     *
     * @return whether the superclass is allowed to proceed with normal menu processing
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if  (id == R.id.action_refresh) {
            callDataRefresh();
        }

        return super.onOptionsItemSelected(item);
    }

    /* ******  HELPER METHODS  **********/

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
     * Starts async tasks to pull new data from the servers
     */
    private void callDataRefresh() {
        new DownloaderAsyncTask(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Opens or closes the collapsing toolbar
     *
     * @param expand  true to expand, false to collapse
     */
    public void setAppBarExpanded(Boolean expand) {
        mToolbarSettings.setAppBarExpanded(expand);
    }

    /**
     * Makes the FragmentOrganizer accessible to child fragments
     *
     * @return the FragmentOrganizer instance for this activity
     */
    public FragmentOrganizer getFragmentOrganizer() {
        return mFragmentOrganizer;
    }


}
