/*
 * Copyright 2020-2026 The social-metricbot contributors
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
package io.github.bmarwell.social.metricbot.conversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testRegexMatchesSlash() {
        final Pattern pattern = Pattern.compile("([0-9\\u00BC-\\u00BE\\u2150-\\u215E/]+)\\s?cup(?:s)?\\b");
        final Matcher matcher = pattern.matcher("1/4 cups");

        while (matcher.find()) {
            assertEquals("1/4", matcher.group(1));
        }
    }

    @Test
    public void testRegexMatchesSlash_prefixes() {
        final Pattern pattern =
                Pattern.compile("\\b((?:[0-9]+\\.)?[0-9\\u00BC-\\u00BE\\u2150-\\u215E/]+)\\s?cup(?:s)?\\b");
        final Matcher matcher = pattern.matcher("▪️4 cups cold water\n▪1/4 cup salt\n▪️1/3 cup maple syrup\n");

        while (matcher.find()) {
            log.info("Find: [{}] g1[{}]", matcher.group(0), matcher.group(1));
            assertThat(matcher.group(1)).isIn("4", "1/4", "1/3");
        }
    }
}
