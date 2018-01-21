package com.drakklord.gradle.metric.core;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.intellij.openapi.extensions.ExtensionPointName;

/**
 * Core class for extension points.
 * Created by DrakkLord on 2016. 11. 01..
 */
public final class Extensions {

    public static final ExtensionPointName<GradleMetricContributor> EP_NAME = ExtensionPointName.create("com.drakklord.metric.GradleMetricContributor");

    private Extensions() {
        // sealed class
    }
}
