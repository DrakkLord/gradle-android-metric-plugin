package com.drakklord.gradle.metric.core.contributor.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.intellij.openapi.project.Project;

import java.util.HashMap;

/**
 * Type group which contains metric entries.
 * Created by DrakkLord on 2016. 03. 13..
 */
public class GradleMetricResultType {
    private final GradleMetricResultNamespace parent;
    private final String type;
    private final Project project;
    private final HashMap<String, GradleMetricResultFile> files = new HashMap<String, GradleMetricResultFile>();

    public GradleMetricResultType(GradleMetricResultNamespace pNamespace, String pType, Project pProject) {
        type = pType;
        parent = pNamespace;
        project = pProject;
    }

    public String getType() {
        return type;
    }

    public GradleMetricResultNamespace getParent() {
        return parent;
    }

    public void addMetric(GradleMetricEntryWrapper entry) {
        if (entry == null || !entry.metricType.equals(type)) {
            throw new IllegalStateException("invalid gradle metric object");
        }

        GradleMetricResultFile tf;
        if (files.containsKey(entry.fileName)) {
            tf = files.get(entry.fileName);
        } else {
            tf = new GradleMetricResultFile(this, entry.fileName, project);
        }
        tf.addMetric(entry);

        files.put(entry.fileName, tf);
    }

    public int getProblemCount() {
        int out = 0;
        for (GradleMetricResultFile e : files.values()) {
            out += e.getProblemCount();
        }
        return out;
    }

    public HashMap<String, GradleMetricResultFile> getChildren() {
        final HashMap<String, GradleMetricResultFile> out = new HashMap<String, GradleMetricResultFile>();
        out.putAll(files);
        return out;
    }
}
