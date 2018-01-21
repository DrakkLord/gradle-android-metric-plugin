package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.drakklord.gradle.metric.core.contributor.GradleMetricTextUtil;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricEntryWrapper;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PsiNavigateUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Node that points into a scope of a particular file.
 * Created by DrakkLord on 2016. 03. 14..
 */
public class MetricFileScopeTreeNode extends MetricAbstractTreeNode {

    private final GradleMetricEntryWrapper entry;

    private final String text;
    private final Icon icon;
    private final PsiElement scope;

    public MetricFileScopeTreeNode(GradleMetricEntryWrapper userObject) {
        super(userObject);
        entry = userObject;

        if (entry.psiTarget != null) {
            icon = entry.psiTarget.getIcon(PsiFile.ICON_FLAG_VISIBILITY);
            text = entry.psiTarget.getName();
/*
            if (entry.psiDirectTarget != null) {
                scope = entry.psiDirectTarget;
            } else {
                scope = entry.psiTarget;
            }*/
        } else if (entry.psiFile != null) {
            icon = entry.psiFile.getIcon(PsiFile.ICON_FLAG_VISIBILITY);
            text = entry.psiFile.getName();
//            scope = entry.psiFile;
        } else {
            icon = AllIcons.Nodes.CustomRegion;
            text = "UNKNOWN"; // TODO should collect the text the entry is trying to point to
//            scope = null;
        }

        scope = entry.psiDirectTarget;
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

    public boolean hasScope() {
        return scope != null;
    }

    public boolean navigateToScope() {
        PsiNavigateUtil.navigate(scope);
        return true;
    }
}
