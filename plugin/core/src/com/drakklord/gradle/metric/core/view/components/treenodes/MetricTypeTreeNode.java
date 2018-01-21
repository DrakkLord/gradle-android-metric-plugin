package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricResultType;
import com.intellij.codeInspection.ui.InspectionTree;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * Type which belongs to a namespace and contains issues found.
 * Created by DrakkLord on 2016. 03. 13..
 */
public class MetricTypeTreeNode extends MetricAbstractTreeNode {
    private static final Icon FALLBACK_ICON = AllIcons.Nodes.ErrorIntroduction;

    private final GradleMetricResultType myType;

    public MetricTypeTreeNode(GradleMetricResultType pType) {
        super(pType);
        myType = pType;
    }

    @Override
    public String toString() {
        return myType.getType();
    }

    @Override
    public Icon getIcon(boolean expanded) {
        final Icon i = myType.getParent().getParent().getContributor().getTypeIcon(myType.getType());
        if (i != null) {
            return i;
        }
        return FALLBACK_ICON;
    }
}
