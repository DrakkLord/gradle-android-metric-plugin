package com.drakklord.gradle.metric.tooling.checkstyle;

import org.gradle.api.plugins.quality.Checkstyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public class CheckstyleMetricModelImpl implements Serializable, CheckstyleMetricModel {

    private final ArrayList<CheckstyleTaskContainer> tasks = new ArrayList<CheckstyleTaskContainer>();

    CheckstyleMetricModelImpl(List<CheckstyleTaskContainer> in) {
        tasks.addAll(in);
    }

    public List<CheckstyleTaskContainer> getTasks() {
        return Collections.unmodifiableList(tasks);
    }
}
