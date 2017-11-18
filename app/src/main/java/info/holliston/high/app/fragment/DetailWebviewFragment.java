package info.holliston.high.app.fragment;

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

/**
 * Creates a fragment to display html content of a single news article or announcement
 *
 * @author Tom Reeve
 */

public class DetailWebviewFragment extends Fragment {

    private Article mArticle;

    public DetailWebviewFragment() {
        super();
    }

    public void setArticle(Article article) {
        mArticle = article;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(
                R.layout.detail_fragment, container, false);
        if (mArticle != null) {
            WebView webview = v.findViewById(R.id.web_view);
            String webHtml = "<h1>"+mArticle.getTitle() + "</h1> "
                    + limitImgWidth(mArticle.getDetails());
            webview.loadData(webHtml, "text/html",null );

            MainActivity ma = (MainActivity) getActivity();
            ma.setAppBarExpanded(false);
        }
        return v;
    }

    private String limitImgWidth(String html) {
        return "<style>img{max-width:90% !important;height:auto !important;}</style>" + html;
    }
}
