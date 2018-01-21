package com.drakklord.gradle.metric.core;

import com.intellij.openapi.externalSystem.model.ProjectSystemId;

/**
 * Global constants.
 * Created by DrakkLord on 2015.11.20..
 */
public class Constants {

    public static final String PLUGIN_NAME = "Android-Gradle metric plugin";
    public static final String NO_EXTENSIONS = "no metric processor plugins installed, install at least one and try again or disable this plugin";
    public static final String NO_INVOKER = "failed to get gradle invoker";

    private Constants() {
    }
}
