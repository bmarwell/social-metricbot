package io.github.bmarwell.social.metricbot.bsky.json;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;

public record BskyResponseDraft(String postStatus, BskyStatus postToReplyTo) {}
