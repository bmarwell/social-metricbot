package io.github.bmarwell.twitter.metricbot.mastodon.xml;

import org.jsoup.Jsoup;

public class ParserUtil {

    public String getRawText(String htmlContent) {
        final var document = Jsoup.parse(htmlContent);

        return document.wholeText();
    }
}
