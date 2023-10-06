package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record AtEmbedRecord(@JsonProperty("uri") URI uri, @JsonProperty("cid") String cid) {}
