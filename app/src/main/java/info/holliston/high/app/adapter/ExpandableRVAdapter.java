package info.holliston.high.app.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.silencedut.expandablelayout.ExpandableLayout;

import java.util.HashSet;

import info.holliston.high.app.R;

/**
 * A custom class that adds expansion functionality to a RecyclerView adapter. Based on the
 * com.github.SilenceDut:ExpandableLayout library.
 *
 * @author Tom Reeve
 */

public abstract class ExpandableRVAdapter extends RVAdapter {

    // a list of items that are currently expanded
    private HashSet<Integer> expandedPositionSet = new HashSet<>();

    /**
     * Creates an instance of ExpandableRVAdapter
     *
     * @param context            the context of the activity
     * @param rowLayout          the layout resource of this fragment
     * @param trimIfAfterSchool  true if the today's data should be skipped after 2pm
     */
    ExpandableRVAdapter(Context context, int rowLayout, Boolean trimIfAfterSchool){
        super(context, rowLayout, trimIfAfterSchool);
    }

    /**
     * An extension of RVAdpter's viewholder, to add the expandableLayout and its
     * onClick function
     */
    class ViewHolderArticleExpandable extends RVAdapter.ViewHolderArticle {
        ExpandableLayout expandableLayout;

        ViewHolderArticleExpandable (View v) {
            super(v);
            expandableLayout = v.findViewById(R.id.exp_layout);
        }

        void updateItem(final int position) {
            if (expandableLayout != null) {
                expandableLayout.setOnExpandListener(new ExpandableLayout.OnExpandListener() {
                    @Override
                    public void onExpand(boolean expanded) {
                        registerExpand(position, discoveryArrow);
                    }
                });
                expandableLayout.setExpand(expandedPositionSet.contains(position));
            }
        }
    }

    /**
     * Required override, tells the addapter what type of viewholder to use for a data row
     *
     * @param parent   the parent view
     * @return         the expandable viewholder
     */
    @Override
    protected RecyclerView.ViewHolder createRowView(ViewGroup parent) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(rowLayout, parent, false);

        return new ViewHolderArticleExpandable(rowView);
    }

    /**
     * Registers the expand/collapse function of the expandableRV
     *
     * @param position         the position (row number) of the recyclerview row that was clicked
     * @param discoveryArrow   the expand/collapse indicator arrow icon
     */
    private void registerExpand(int position, ImageView discoveryArrow) {
        if (expandedPositionSet.contains(position)) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(discoveryArrow, "rotation",180, 0);
            anim.setDuration(200);
            anim.start();
            expandedPositionSet = removeExpand(position);
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(discoveryArrow, "rotation",0, 180);
            anim.setDuration(200);
            anim.start();
            expandedPositionSet = addExpand(position);
        }
    }

    private HashSet<Integer> removeExpand(int position) {
        expandedPositionSet.remove(position);
        return expandedPositionSet;
    }

    private HashSet<Integer> addExpand(int position) {
        expandedPositionSet.add(position);
        return expandedPositionSet;
    }

}


