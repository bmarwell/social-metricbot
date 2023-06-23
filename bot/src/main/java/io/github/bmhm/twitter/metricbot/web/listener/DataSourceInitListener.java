package io.github.bmhm.twitter.metricbot.web.listener;

import io.github.bmhm.twitter.metricbot.db.TwitterMetricBotDataSource;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DataSourceInitListener implements ServletContextListener {

    @Inject
    private TwitterMetricBotDataSource twitterMetricBotDataSource;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        this.twitterMetricBotDataSource.initialize();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {}
}
