package io.github.bmhm.twitter.metricbot.db;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class TwitterMetricBotDataSource {

  private static final Logger LOG = LoggerFactory.getLogger(TwitterMetricBotDataSource.class);

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
          .locations("classpath:/io/github/bmhm/twitter/metricbot/db/databasemigrations")
          // .callbacks("io.github.bmhm.twitter.metricbot.db.callbacks")
          .load();

      flyway.migrate();
      this.initialized.compareAndSet(false, true);
    } catch (final FlywayException flywayException) {
      LOG.error("Unable to run flyway migration.", flywayException);
    }

  }

}
