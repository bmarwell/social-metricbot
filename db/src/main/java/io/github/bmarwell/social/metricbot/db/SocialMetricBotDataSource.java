/*
 * Copyright 2021-2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
