package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AtEmbed(@JsonProperty("record") AtEmbedRecord record) {}
