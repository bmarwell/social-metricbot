package io.github.bmarwell.social.metricbot.db;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class SocialMetricBotDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(SocialMetricBotDataSource.class);

    private final AtomicBoolean initialized = new AtomicBoolean();

    @Resource(name = "jdbc/metricbotjpadatasource")
    private DataSource ds1;

    public void initialize() {
        if (this.initialized.get()) {
            return;
        }

        try {
            final Flyway flyway = Flyway.configure()
                    .dataSource(this.ds1)
                    .locations("classpath:/io/github/bmarwell/social/metricbot/db/databasemigrations")
                    // .callbacks("io.github.bmarwell.social.metricbot.db.callbacks")
                    .load();

            flyway.migrate();
            this.initialized.compareAndSet(false, true);
        } catch (final FlywayException flywayException) {
            LOG.error("Unable to run flyway migration.", flywayException);
        }
    }
}
