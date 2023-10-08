/*
 * Copyright 2023 The social-metricbot contributors
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
package io.github.bmarwell.social.metricbot.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MastodonConfig implements Serializable {

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.mastodon.accountname")
    private String accountName;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.mastodon.instancehostname")
    private String instanceHostname;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.mastodon.website")
    private String website;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.mastodon.accesstoken")
    private String accessToken;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.mastodon.initialDelay")
    private long initialDelay;

    public MastodonConfig() {
        // injection constructor.
    }

    public String getAccountName() {
        return accountName;
    }

    public String getInstanceHostname() {
        if (!instanceHostname.startsWith("http")) {
            return "https://" + instanceHostname;
        }
        return instanceHostname;
    }

    public String getWebsite() {
        return website;
    }

    public String getRedirectUri() {
        return "urn:ietf:wg:oauth:2.0:oob";
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Duration getTweetFinderInitialDelay() {
        return Duration.ofSeconds(this.initialDelay);
    }
}
