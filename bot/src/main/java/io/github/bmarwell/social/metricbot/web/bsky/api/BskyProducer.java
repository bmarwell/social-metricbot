package io.github.bmarwell.social.metricbot.web.bsky.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import java.io.Serial;
import java.io.Serializable;

@ApplicationScoped
@Default
public class BskyProducer implements Serializable {

    @Serial
    private static final long serialVersionUID = 5177043145066730288L;
}
