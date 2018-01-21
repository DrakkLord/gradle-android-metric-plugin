package com.drakklord.gradle.metric.tooling.checkstyle;

import org.gradle.api.plugins.quality.Checkstyle;

import java.util.List;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public interface CheckstyleMetricModel {
    List<CheckstyleTaskContainer> getTasks();
}
