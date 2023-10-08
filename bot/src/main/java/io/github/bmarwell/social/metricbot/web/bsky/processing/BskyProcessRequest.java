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
package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;

/**
 * Fired when a status was found which we should actually reply to.
 * That is, the event is only fired after making sure, we did not reply to it earlier.
 *
 * @param status the status which was found which mentions the bot.
 */
public record BskyProcessRequest(BskyStatus status) {}
