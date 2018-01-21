package com.drakklord.gradle.metric.tooling.pmd;

import org.gradle.api.plugins.quality.Pmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public class PMDMetricModelImpl implements Serializable, PMDMetricModel {

    private final ArrayList<PMDTaskContainer> tasks = new ArrayList<PMDTaskContainer>();

    PMDMetricModelImpl(List<PMDTaskContainer> in) {
        tasks.addAll(in);
    }

    public List<PMDTaskContainer> getTasks() {
        return Collections.unmodifiableList(tasks);
    }
}
