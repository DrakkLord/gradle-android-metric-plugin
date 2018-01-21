package com.drakklord.gradle.metric.core.contributor.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.intellij.openapi.project.Project;

import java.util.HashMap;

/**
 * Namespace that holds metric types which is specific for a metric contributor.
 * Created by DrakkLord on 2016. 03. 13..
 */
public class GradleMetricResultNamespace {
    private final GradleMetricResultContributor parent;
    private final String namespace;
    private final Project project;
    private final HashMap<String, GradleMetricResultType> types = new HashMap<String, GradleMetricResultType>();

    public GradleMetricResultNamespace(GradleMetricResultContributor pContributor, String pNamespace, Project pProject) {
        parent = pContributor;
        namespace = pNamespace;
        project = pProject;
    }

    public String getNamespace() {
        return namespace;
    }

    public GradleMetricResultContributor getParent() {
        return parent;
    }

    public void addMetric(GradleMetricEntryWrapper entry) {
        if (entry == null || !entry.metricNamespace.equals(namespace)) {
            throw new IllegalStateException("invalid gradle metric object");
        }

        GradleMetricResultType ts;
        if (types.containsKey(entry.metricType)) {
            ts = types.get(entry.metricType);
        } else {
            ts = new GradleMetricResultType(this, entry.metricType, project);
        }
        ts.addMetric(entry);

        types.put(entry.metricType, ts);
    }

    public int getProblemCount() {
        int out = 0;
        for (GradleMetricResultType e : types.values()) {
            out += e.getProblemCount();
        }
        return out;
    }

    public HashMap<String, GradleMetricResultType> getChildren() {
        final HashMap<String, GradleMetricResultType> out = new HashMap<String, GradleMetricResultType>();
        out.putAll(types);
        return out;
    }
}
