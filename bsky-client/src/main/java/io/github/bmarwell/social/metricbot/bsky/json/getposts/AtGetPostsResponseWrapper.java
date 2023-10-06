package io.github.bmarwell.social.metricbot.bsky.json.getposts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AtGetPostsResponseWrapper(@JsonProperty("posts") List<AtGetPostsPosts> posts) {}
