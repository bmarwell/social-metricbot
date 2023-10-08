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
package io.github.bmarwell.social.metricbot.bsky;

import java.util.Arrays;

public class MutableBlueSkyConfiguration implements Cloneable {

    private String host = "https://bsky.app";

    private String handle;

    private char[] appSecret;

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String handle) {
        if (handle.startsWith("@")) {
            this.handle = handle.substring(1);
            return;
        }
        this.handle = handle;
    }

    public char[] getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(final char[] appSecret) {
        this.appSecret = appSecret;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public MutableBlueSkyConfiguration withHandle(final String handle) {
        this.setHandle(handle);
        return this;
    }

    public MutableBlueSkyConfiguration withAppSecret(final char[] appSecret) {
        this.setAppSecret(appSecret);
        return this;
    }

    public MutableBlueSkyConfiguration withHost(final String host) {
        this.setHost(host);
        return this;
    }

    @Override
    public MutableBlueSkyConfiguration clone() {
        try {
            final MutableBlueSkyConfiguration clone = (MutableBlueSkyConfiguration) super.clone();
            if (this.appSecret == null) {
                clone.appSecret = new char[0];

                return clone;
            }

            clone.appSecret = Arrays.copyOf(this.appSecret, this.appSecret.length);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
