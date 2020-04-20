/*
 *  Copyright 2018 The twittermetricbot contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.bmhm.twitter.metricbot.commands;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import io.github.bmhm.twitter.metricbot.twitter.TweetMentionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(
    name = "run",
    description = "runs the bot"
)
public class RunBotCommand implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(TweetMentionManager.class);

  public RunBotCommand() {
    // injection
  }

  @Override
  public void run() {
    LOG.info("Starting MetricBot: [{}].", this);

    try {
      runLoop();
    } catch (final RuntimeException rtEx) {
      LOG.info("Ending.");
    }
  }


  private void runLoop() {
    while (true) {
      try {
        TimeUnit.SECONDS.sleep(1L);
      } catch (final InterruptedException intEx) {
        LOG.info("Ended.", intEx);
      }
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "RunBotCommand{", "}")
        .toString();
  }
}
