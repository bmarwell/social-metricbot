package io.github.bmhm.twitter.metricbot.common;

import java.io.Serializable;
import java.util.StringJoiner;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TwitterConfig implements Serializable {

  private static final long serialVersionUID = -959303676758599233L;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.accountname")
  private String accountName;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.debug", defaultValue = "false")
  private boolean debug;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.consumerkey")
  private String consumerKey;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.consumersecret")
  private String consumerSecret;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.accesstoken")
  private String accessToken;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.accesstokensecret")
  private String accessTokenSecret;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.tweetfinder.initialdelay", defaultValue = "5")
  private long initialDelay;

  @Inject
  @ConfigProperty(name = "io.github.bmhm.twitter.metricbot.tweetfinder.rate", defaultValue = "20")
  private long retrieveRate;

  public TwitterConfig() {
    // injection constructor.
  }

  public String getAccountName() {
    return this.accountName;
  }

  public boolean isDebug() {
    return this.debug;
  }

  public String getConsumerKey() {
    return this.consumerKey;
  }

  public String getConsumerSecret() {
    return this.consumerSecret;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public String getAccessTokenSecret() {
    return this.accessTokenSecret;
  }

  public long getTweetFinderInitialDelay() {
    return this.initialDelay;
  }

  public long getTweetFinderRetrieveRate() {
    return this.retrieveRate;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TwitterConfig.class.getSimpleName() + "[", "]")
        .add("accountName='" + this.accountName + "'")
        .add("debug=" + this.debug)
        .add("consumerKey='" + this.consumerKey + "'")
        .add("consumerSecret='" + this.consumerSecret + "'")
        .add("accessToken='" + this.accessToken + "'")
        .add("accessTokenSecret='" + this.accessTokenSecret + "'")
        .add("initialDelay=" + this.initialDelay)
        .add("retrieveRate=" + this.retrieveRate)
        .toString();
  }
}
