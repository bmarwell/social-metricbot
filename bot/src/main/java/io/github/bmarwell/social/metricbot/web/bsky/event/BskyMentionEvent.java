package io.github.bmarwell.social.metricbot.web.bsky.event;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;

public record BskyMentionEvent(BskyStatus bskyStatus) {}
