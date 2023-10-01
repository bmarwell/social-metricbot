package io.github.bmarwell.social.metricbot.web.listener;

import io.github.bmarwell.social.metricbot.db.SocialMetricBotDataSource;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DataSourceInitListener implements ServletContextListener {

    @Inject
    private SocialMetricBotDataSource socialMetricBotDataSource;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        this.socialMetricBotDataSource.initialize();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {}
}
