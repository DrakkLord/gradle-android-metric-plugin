package com.drakklord.gradle.metric.core.contributor.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.util.HashSet;

/**
 * Metric class that contains issues that are already groupped by namespace and type.
 * Created by DrakkLord on 2016. 03. 13..
 */
public class GradleMetricResultFile {
    private final GradleMetricResultType parent;
    private final String file;
    private final Project project;
    private final HashSet<GradleMetricEntryWrapper> entries = new HashSet<GradleMetricEntryWrapper>();

    private final VirtualFile virtualFile;
    private final PsiFile psiFile;

    public GradleMetricResultFile(GradleMetricResultType pType, String pFile, Project pProject) {
        file = pFile;
        parent = pType;
        project = pProject;

        virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(file));
        if (virtualFile == null) {
            throw new IllegalStateException("failed to open local file from the report");
        }

        psiFile = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
                @Override
                public PsiFile compute() {
                    return PsiManager.getInstance(project).findFile(virtualFile);
                }
        });
        if (psiFile == null) {
            throw new IllegalStateException("failed to convert local file into psi file from the report");
        }
    }

    public String getFile() {
        return psiFile.getName();
    }

    public PsiFile getPsiFile() {
        return psiFile;
    }

    public GradleMetricResultType getParent() {
        return parent;
    }

    public void addMetric(GradleMetricEntryWrapper entry) {
        if (entry == null || !entry.fileName.equals(file)) {
            throw new IllegalStateException("invalid gradle metric object");
        }
        entries.add(entry);
    }

    public int getProblemCount() {
        return entries.size();
    }

    public HashSet<GradleMetricEntryWrapper> getChildren() {
        final HashSet<GradleMetricEntryWrapper> out = new HashSet<GradleMetricEntryWrapper>();
        out.addAll(entries);
        return out;
    }
}
