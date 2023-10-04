package io.github.bmarwell.social.metricbot.web;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Locale;

public final class GlobalStatusUtil {

    private static final List<String> ACCOUNT_NAME_WORD_BLACKLIST =
            asList("Boutique", "Crazy I Buy", "weather", "Supplements", "DealsSupply");

    private GlobalStatusUtil() {
        // util
    }

    public static boolean containsBlockedWord(
            final String statusText, final String accountHandle, final String accountDisplayName) {
        final var statusTextMatches = ACCOUNT_NAME_WORD_BLACKLIST.stream()
                // none of the blacklisted words should be contained in the status Text
                .anyMatch(blacklisted ->
                        statusText.toLowerCase(Locale.ENGLISH).contains(blacklisted.toLowerCase(Locale.ENGLISH)));

        final var handleMatches = ACCOUNT_NAME_WORD_BLACKLIST.stream()
                // none of the blacklisted words should be contained in the account handle
                .anyMatch(blacklisted ->
                        accountHandle.toLowerCase(Locale.ENGLISH).contains(blacklisted.toLowerCase(Locale.ENGLISH)));

        final var accountDisplayNameMatches = ACCOUNT_NAME_WORD_BLACKLIST.stream()
                // none of the blacklisted words should be contained in the full account name
                .anyMatch(blacklisted -> accountDisplayName
                        .toLowerCase(Locale.ENGLISH)
                        .contains(blacklisted.toLowerCase(Locale.ENGLISH)));

        return handleMatches || accountDisplayNameMatches || statusTextMatches;
    }
}
