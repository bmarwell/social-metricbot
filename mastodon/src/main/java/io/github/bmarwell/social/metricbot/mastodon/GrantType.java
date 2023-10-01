package io.github.bmarwell.social.metricbot.mastodon;

public enum GrantType {
    CLIENT_CREDENTIALS("client_credentials");

    private final String grantType;

    GrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getGrantType() {
        return grantType;
    }
}
