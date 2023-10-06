package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record AtPostReplyInformation(@JsonProperty("cid") String cid, @JsonProperty("uri") URI uri) {}
