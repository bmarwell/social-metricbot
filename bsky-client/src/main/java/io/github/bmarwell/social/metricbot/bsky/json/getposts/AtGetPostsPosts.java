package io.github.bmarwell.social.metricbot.bsky.json.getposts;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtEmbed;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtNotificationAuthor;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtPostNotificationRecord;
import java.net.URI;
import java.util.Optional;

public record AtGetPostsPosts(
        @JsonProperty("uri") URI uri,
        @JsonProperty("cid") String cid,
        @JsonProperty("author") AtNotificationAuthor author,
        @JsonProperty("record") AtPostNotificationRecord record,
        @JsonProperty("embed") Optional<AtEmbed> embed) {}
