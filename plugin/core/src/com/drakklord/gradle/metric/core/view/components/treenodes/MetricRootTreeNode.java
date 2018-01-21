package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformUtils;
import icons.AndroidIcons;

import javax.swing.*;

/**
 * Root node for the metric tree.
 * Created by DrakkLord on 2016. 02. 29..
 */
public class MetricRootTreeNode extends MetricAbstractTreeNode {
    private static final Icon PROJECT_ICON = AndroidIcons.AndroidFile;

    private final Project myProject;

    public MetricRootTreeNode(Project project) {
        super(project);
        myProject = project;
    }

    public String toString() {
        return myProject.getName();
    }

    @Override
    public Icon getIcon(boolean expanded) {
        return PROJECT_ICON;
    }
}
