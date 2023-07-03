package io.github.bmarwell.twitter.metricbot.mastodon;

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
