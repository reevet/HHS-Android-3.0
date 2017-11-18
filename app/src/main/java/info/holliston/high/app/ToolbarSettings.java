package info.holliston.high.app;

import android.app.Activity;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

/**
 * Stores and manages toolbar settings on behalf of an activity
 *
 * @author Tom Reeve.
 */

class ToolbarSettings {

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;

    ToolbarSettings(AppBarLayout appBarLayout,
                           CollapsingToolbarLayout collapsingToolbarLayout,
                           Toolbar toolbar) {
        mAppBarLayout = appBarLayout;
        mCollapsingToolbarLayout = collapsingToolbarLayout;
        mCollapsingToolbarLayout.setTitleEnabled(true);
        mToolbar = toolbar;
    }

    Toolbar getToolbar() {
        return mToolbar;
    }

    void setToolbarTitle(String title) {
        mCollapsingToolbarLayout.setTitle(title);
    }

    void setToolbarImage(int resource, Activity ma) {
        ImageView imageView = ma.findViewById(R.id.app_bar_image);

        GlideApp.with(ma)
                .load(resource)
                .centerCrop()
                .into(imageView);
    }

    void setAppBarExpanded(Boolean expand) {
        mAppBarLayout.setExpanded(expand);
    }
}
