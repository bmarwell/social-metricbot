package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record AtEmbedRecord(@JsonProperty("uri") URI uri, @JsonProperty("cid") String cid) {}
