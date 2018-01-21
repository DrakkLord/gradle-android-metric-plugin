package com.drakklord.gradle.metric.core.view;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

/**
 * Metric report tool window factory.
 * Created by DrakkLord on 2015. 11. 28..
 */
public class GradleMetricResultsToolWindowFactory implements ToolWindowFactory, DumbAware {
    public static final String ID = "Gradle Metric";

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        GradleMetricResultsView.getInstance(project).createToolWindowContent(toolWindow);
    }
}
