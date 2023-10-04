package io.github.bmarwell.social.metricbot.db.pdo;

import io.github.bmarwell.social.metricbot.db.converter.UriConverter;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@Entity
@Table(name = "BSKY_STATUS")
public class BskyStatusPdo {

    public static final Instant TIME_NOT_SET = Instant.EPOCH;

    public static final URI EMPTY_URI = URI.create("");

    @Id
    @Column(name = "STATUS_ATURI")
    @Convert(converter = UriConverter.class)
    private URI atUri;

    @Column(name = "POST_TIME", nullable = false)
    private Instant postTime;

    @Column(name = "BOT_RESPONSE_ATURI", nullable = false)
    private URI botResponseAtUri;

    @Column(name = "RESPONSE_TIME", nullable = false)
    private Instant responseTime;

    public BskyStatusPdo() {
        this.botResponseAtUri = EMPTY_URI;
        this.responseTime = TIME_NOT_SET;
    }

    public BskyStatusPdo(final URI atUri, final Instant postTime) {
        this.atUri = atUri;
        this.postTime = postTime;
        this.botResponseAtUri = EMPTY_URI;
        this.responseTime = TIME_NOT_SET;
    }

    @PrePersist
    public void sanitizeUri() {
        if (this.botResponseAtUri == null) {
            this.botResponseAtUri = EMPTY_URI;
        }
    }

    public URI getAtUri() {
        return atUri;
    }

    public void setAtUri(final URI atUri) {
        this.atUri = atUri;
    }

    public Instant getPostTime() {
        return postTime;
    }

    public void setPostTime(final Instant postTime) {
        this.postTime = postTime;
    }

    public Optional<URI> getBotResponseAtUri() {
        return Optional.ofNullable(this.botResponseAtUri).filter(uri -> uri.equals(EMPTY_URI));
    }

    public void setBotResponseAtUri(final @Nullable URI botResponseAtUri) {
        if (botResponseAtUri == null) {
            this.botResponseAtUri = EMPTY_URI;
            return;
        }
        this.botResponseAtUri = botResponseAtUri;
    }

    public Instant getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(final Instant responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof final BskyStatusPdo that)) {
            return false;
        }

        return getAtUri().equals(that.getAtUri());
    }

    @Override
    public int hashCode() {
        return getAtUri().hashCode();
    }
}
