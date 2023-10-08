package io.github.bmarwell.social.metricbot.bsky;

import java.util.Arrays;

public class MutableBlueSkyConfiguration implements Cloneable {

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

    public MutableBlueSkyConfiguration withHandle(final String handle) {
        this.setHandle(handle);
        return this;
    }

    public MutableBlueSkyConfiguration withAppSecret(final char[] appSecret) {
        this.setAppSecret(appSecret);
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
