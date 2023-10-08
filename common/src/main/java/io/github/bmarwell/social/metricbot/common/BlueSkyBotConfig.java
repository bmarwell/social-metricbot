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
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Default
public class BlueSkyBotConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -310024931543855701L;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.bsky.handle")
    private String handle;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.bsky.appSecret")
    private String appSecret;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.bsky.skipOldPosts", defaultValue = "true")
    private boolean skipOldPosts;

    public BlueSkyBotConfig() {
        // injection only
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String handle) {
        this.handle = handle;
    }

    public char[] getAppSecret() {
        return appSecret.toCharArray();
    }

    public void setAppSecret(final String appSecret) {
        this.appSecret = appSecret;
    }

    public Duration getPostFinderInitialDelay() {
        return Duration.ofSeconds(5L);
    }

    public boolean isSkipOldPosts() {
        return skipOldPosts;
    }

    public void setSkipOldPosts(final boolean skipOldPosts) {
        this.skipOldPosts = skipOldPosts;
    }
}
