package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricResultFile;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.apache.xmlbeans.impl.xb.xsdschema.All;

import javax.swing.*;

/**
 * Metric that points into a particular file.
 * Created by DrakkLord on 2016. 02. 24..
 */
public class MetricFileTreeNode extends MetricAbstractTreeNode {
    private final GradleMetricResultFile fs;
    private final String name;
    private final Icon icon;

    public MetricFileTreeNode(GradleMetricResultFile fsIn) {
        super(fsIn);
        fs = fsIn;

        if (fs.getPsiFile() != null) {
            name = fs.getPsiFile().getName();
            icon = fs.getPsiFile().getFileType().getIcon();
        } else {
            name = "UNKNOWN";
            icon = AllIcons.Nodes.AnonymousClass;
        }
    }

    public String toString() {
        return name;
    }

    @Override
    public Icon getIcon(boolean expanded) {
        return icon;
    }
}