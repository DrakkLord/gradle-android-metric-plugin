package com.drakklord.gradle.metric.core.contributor;

/**
 * Created by DrakkLord on 2015.11.23..
 */
public enum GradleMetricSeverity {
    SUGGESTION,
    WARNING,
    ERROR;

    public static GradleMetricSeverity valueOf(int i) {
        if (i < 0 || i >= VALUES.length) {
            throw new IllegalStateException("invalid gradle metric entry value : " + i);
        }
        return VALUES[i];
    }

    private static final GradleMetricSeverity VALUES[] = values();
}
