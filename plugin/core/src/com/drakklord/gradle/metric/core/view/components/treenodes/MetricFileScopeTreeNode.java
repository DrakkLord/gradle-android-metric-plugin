package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.drakklord.gradle.metric.core.contributor.model.GradleMetricEntryWrapper;
import com.intellij.icons.AllIcons;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Node that points into a scope of a particular file.
 * Created by DrakkLord on 2016. 03. 14..
 */
public class MetricFileScopeTreeNode extends MetricAbstractTreeNode {

    private final GradleMetricEntryWrapper entry;

    private String text;
    private Icon icon;

    public MetricFileScopeTreeNode(GradleMetricEntryWrapper userObject) {
        super(userObject);
        entry = userObject;

        icon = null;
        text = null;
        final GradleMetricEntryWrapper.PsiInfoPacket info = entry.getPsiTargetInfo();
        if (info != null) {
            icon = info.icon;
            text = info.text;
        } else {
            final GradleMetricEntryWrapper.PsiInfoPacket infoFile = entry.getPsiFileInfo();
            if (infoFile != null) {
                icon = infoFile.icon;
                text = infoFile.text;
            }
        }
        if (icon == null || text == null) {
            icon = AllIcons.Nodes.CustomRegion;
            text = "UNKNOWN"; // TODO should collect the text the entry is trying to point to
        }
    }

    @Override
    public String toString() {
        return text;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean expanded) {
        return icon;
    }

    @Override
    public int getProblemCount(boolean allowSupressed) {
        return 1;
    }

    @Override
    public boolean hasScopeInfo() {
        return entry.lineStart > 0;
    }

    @Override
    public String getScopeInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("line: ").append(entry.lineStart);
        if (entry.lineEnd > 0 && entry.lineEnd < entry.lineStart) {
            sb.append(" - ").append(entry.lineEnd);
        }
        /*
        sb.append(" [").append(entry.psiTarget).append("] - [");
        sb.append(entry.psiDirectTarget).append("] - ");
        sb.append(entry.doc.getLineNumber(entry.psiDirectTarget.getTextOffset()));
        */
        return sb.toString();
    }

    public boolean navigateToScope() {
        return entry.navigateToPSIElementDirect();
    }
}
