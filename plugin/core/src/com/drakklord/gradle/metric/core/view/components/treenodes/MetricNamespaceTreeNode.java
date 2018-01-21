package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricResultNamespace;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * Namespace which is below a contributor and contains all types from the namespace.
 * Created by DrakkLord on 2016. 03. 13..
 */
public class MetricNamespaceTreeNode extends MetricAbstractTreeNode {
    private static final Icon FALLBACK_ICON = AllIcons.Nodes.UpFolder;

    private final GradleMetricResultNamespace myNamespace;

    public MetricNamespaceTreeNode(GradleMetricResultNamespace namespace) {
        super(namespace);
        myNamespace = namespace;
    }

    @Override
    public String toString() {
        return myNamespace.getNamespace();
    }

    @Override
    public Icon getIcon(boolean expanded) {
        final Icon i = myNamespace.getParent().getContributor().getNamespaceIcon(myNamespace.getNamespace());
        if (i != null) {
            return i;
        }
        return FALLBACK_ICON;
    }
}
