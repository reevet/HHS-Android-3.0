package info.holliston.high.app.datamodel.parser;

import java.io.InputStream;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

/**
 * A generic interface for parsing the variety of feed formats
 *
 * @author Tom Reeve
 */

public interface ArticleParser {
    List<Article> parse(InputStream in);
}
