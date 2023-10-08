/*
 * Copyright 2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.mastodon;

/**
 * @param id       The id field of the mastodon account response.<br>
 *                 The account id.<br>
 *                 Example value: {@code 14715}.<br>
 *                 Cast from integer, but not guaranteed to be numeric.
 * @param acct     The Web-finger account URI. Equal to username for local users, or username@domain for remote users.<br>
 *                 Example value: {@code trwnh@mastodon.social}.
 *                 This is not the display name.
 * @param username The username of the account, not including domain.<br>
 *                 Example value: {@code trwnh}.
 *                 This is not the display name nor does it contain a domain.
 * @param locked   Whether the account manually approves follow requests.
 */
public record MastodonAccount(MastodonAccountId id, String acct, String username, boolean locked) {

    /**
     * The id field of the mastodon account response.
     * <p>
     * Example value: {@code 14715}.
     */
    @Override
    public MastodonAccountId id() {
        return id;
    }

    /**
     * The acct field of the response. Probably the same as {@link #username}.
     * <p>
     * Example value: {@code trwnh}.
     * </p>
     */
    @Override
    public String acct() {
        return acct;
    }

    /**
     * The username field of the response. Probably the same as {@link #acct}.
     * <p>
     * Example value: {@code trwnh}.
     * </p>
     */
    @Override
    public String username() {
        return username;
    }
}
