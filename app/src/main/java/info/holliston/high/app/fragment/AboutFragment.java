package info.holliston.high.app.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import info.holliston.high.app.BuildConfig;
import info.holliston.high.app.R;

/**
 * Creates an About dialog pop-up with app and author info
 */
public class AboutFragment extends DialogFragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Creates the layout and fills in the Version name
     *
     * @param savedInstanceState   any saved info if the app slept while it was open
     * @return                     the dialog box to display
     */

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        View v = View.inflate(getActivity(), R.layout.fragment_about, null);
        String version = getActivity().getString(R.string.about_version) + BuildConfig.VERSION_NAME;
        TextView versionTexView = v.findViewById(R.id.about_app_version);
        versionTexView.setText(version);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                // Add action buttons
                .setNegativeButton(R.string.about_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AboutFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
