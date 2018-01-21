package com.drakklord.gradle.metric.pmd.contributor;

import com.drakklord.gradle.metric.core.contributor.GradleMetricModelHolder;
import com.drakklord.gradle.metric.tooling.pmd.PMDTaskContainer;

import java.util.*;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public class PMDTasksHolder implements GradleMetricModelHolder {

    private final HashMap<String ,PMDTaskContainer> tasks = new HashMap<String ,PMDTaskContainer>();

    PMDTasksHolder(List<PMDTaskContainer> in) {
        for (PMDTaskContainer t : in) {
            tasks.put(t.getName(), t);
        }
    }

    public Map<String ,PMDTaskContainer> getTasks() {
        return Collections.unmodifiableMap(tasks);
    }
}
