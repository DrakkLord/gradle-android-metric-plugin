package com.drakklord.gradle.metric.checkstyle.contributor;

import com.drakklord.gradle.metric.core.contributor.GradleMetricModelHolder;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleTaskContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DrakkLord on 2016. 11. 06..
 */
public class CheckstyleTasksHolder implements GradleMetricModelHolder {

    private final HashMap<String ,CheckstyleTaskContainer> tasks = new HashMap<String ,CheckstyleTaskContainer>();

    CheckstyleTasksHolder(List<CheckstyleTaskContainer> in) {
        for (CheckstyleTaskContainer t : in) {
            tasks.put(t.getName(), t);
        }
    }

    public Map<String ,CheckstyleTaskContainer> getTasks() {
        return Collections.unmodifiableMap(tasks);
    }
}
