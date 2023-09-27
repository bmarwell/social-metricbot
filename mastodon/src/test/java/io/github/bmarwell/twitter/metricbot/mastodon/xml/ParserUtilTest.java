package io.github.bmarwell.twitter.metricbot.mastodon.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ParserUtilTest {

    @Test
    void keeps_line_breaks() {
        var input = """
        <span>Hello, world</span>
        <br>newline""";

        // when
        final String rawText = new ParserUtil().getRawText(input);

        // then
        assertThat(rawText).contains("\nnewline");
    }
}
