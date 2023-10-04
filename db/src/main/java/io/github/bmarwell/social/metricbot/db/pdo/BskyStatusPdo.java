package io.github.bmarwell.social.metricbot.db.pdo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "BSKY_STATUS")
public class BskyStatusPdo {

    public static final Instant TIME_NOT_SET = Instant.EPOCH;

    @Id
    @Column(name = "STATUS_CID")
    private String cid;

    @Column(name = "POST_TIME", nullable = false)
    private Instant postTime;

    @Column(name = "BOT_RESPONSE_CID", nullable = false)
    private String botResponseCid;

    @Column(name = "RESPONSE_TIME", nullable = false)
    private Instant responseTime;

    public BskyStatusPdo() {
        this.botResponseCid = "";
        this.responseTime = TIME_NOT_SET;
    }

    public BskyStatusPdo(final String cid, final Instant postTime) {
        this.cid = cid;
        this.postTime = postTime;
        this.botResponseCid = "";
        this.responseTime = TIME_NOT_SET;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(final String cid) {
        this.cid = cid;
    }

    public Instant getPostTime() {
        return postTime;
    }

    public void setPostTime(final Instant postTime) {
        this.postTime = postTime;
    }

    public String getBotResponseCid() {
        return botResponseCid;
    }

    public void setBotResponseCid(final String botResponseCid) {
        this.botResponseCid = botResponseCid;
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

        return getCid().equals(that.getCid());
    }

    @Override
    public int hashCode() {
        return getCid().hashCode();
    }
}
