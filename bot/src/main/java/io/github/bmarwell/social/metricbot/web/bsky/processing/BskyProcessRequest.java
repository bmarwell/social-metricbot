package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;

/**
 * Fired when a status was found which we should actually reply to.
 * That is, the event is only fired after making sure, we did not reply to it earlier.
 *
 * @param status the status which was found which mentions the bot.
 */
public record BskyProcessRequest(BskyStatus status) {
}
