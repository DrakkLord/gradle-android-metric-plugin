package com.drakklord.gradle.metric.core.contributor.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.intellij.openapi.project.Project;

import java.util.HashMap;

/**
 * Tree metric group provider which contains a provider and it's namespaces.
 * Created by DrakkLord on 2016. 03. 13..
 */
public class GradleMetricResultContributor {
    private final GradleMetricContributor contributor;
    private final Project project;
    private final HashMap<String, GradleMetricResultNamespace> namespaces = new HashMap<String, GradleMetricResultNamespace>();

    public GradleMetricResultContributor(GradleMetricContributor pContributor, Project pProject) {
        contributor = pContributor;
        project = pProject;
    }

    public GradleMetricContributor getContributor() {
        return contributor;
    }

    public void addMetric(GradleMetricEntryWrapper entry) {
        if (entry == null || entry.source != contributor) {
            throw new IllegalStateException("invalid gradle metric object");
        }

        GradleMetricResultNamespace ns;
        if (namespaces.containsKey(entry.metricNamespace)) {
            ns = namespaces.get(entry.metricNamespace);
        } else {
            ns = new GradleMetricResultNamespace(this ,entry.metricNamespace, project);
        }
        ns.addMetric(entry);

        namespaces.put(entry.metricNamespace, ns);
    }

    public int getProblemCount() {
        int out = 0;
        for (GradleMetricResultNamespace e : namespaces.values()) {
            out += e.getProblemCount();
        }
        return out;
    }

    public HashMap<String, GradleMetricResultNamespace> getChildren() {
        final HashMap<String, GradleMetricResultNamespace> out = new HashMap<String, GradleMetricResultNamespace>();
        out.putAll(namespaces);
        return out;
    }
}
