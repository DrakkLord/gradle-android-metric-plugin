package com.drakklord.gradle.metric.tooling.pmd;

import org.gradle.api.plugins.quality.Pmd;

import java.util.List;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public interface PMDMetricModel {
    List<PMDTaskContainer> getTasks();
}
