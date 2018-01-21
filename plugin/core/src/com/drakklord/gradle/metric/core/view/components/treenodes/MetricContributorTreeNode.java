package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricResultContributor;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.icons.AllIcons;
import icons.AndroidIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Tree node that denotes a metric provider this is the logical root for reports coming from that provider.
 * Created by DrakkLord on 2016. 03. 01..
 */
public class MetricContributorTreeNode extends MetricAbstractTreeNode {
    private static final Icon FALLBACK_ICON = AllIcons.Nodes.WarningIntroduction;

    private final GradleMetricResultContributor myMetricContributor;

    public MetricContributorTreeNode(GradleMetricResultContributor metricContributor) {
        super(metricContributor);
        myMetricContributor = metricContributor;
    }

    @Override
    public String toString() {
        return myMetricContributor.getContributor().getName();
    }

    @Override
    public Icon getIcon(boolean expanded) {
        final Icon i = myMetricContributor.getContributor().getIcon();
        if (i != null) {
            return i;
        }
        return FALLBACK_ICON;
    }

    public boolean appearsBold() {
        return true;
    }
}
