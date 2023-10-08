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
package io.github.bmarwell.social.metricbot.mastodon;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public class MastodonConfigurationBuilder {

    private String accessToken;

    private URI instanceHost;

    private URI redirectUri = URI.create("urn:ietf:wg:oauth:2.0:oob");

    private List<String> scopes = List.of("read", "write");

    private GrantType grantType = GrantType.CLIENT_CREDENTIALS;

    public MastodonConfigurationBuilder withInstanceHost(URI instanceHost) {
        this.instanceHost = instanceHost;
        return this;
    }

    public MastodonConfigurationBuilder withRedirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public MastodonConfigurationBuilder withScopes(Collection<String> scopes) {
        this.scopes = List.copyOf(scopes);
        return this;
    }

    public MastodonConfigurationBuilder withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public URI getInstanceHost() {
        return instanceHost;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}
