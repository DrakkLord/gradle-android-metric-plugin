package com.drakklord.gradle.metric.core.contributor.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.drakklord.gradle.metric.core.contributor.GradleMetricTextUtil;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.PsiActionSupportFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.PsiNavigateUtil;

import javax.swing.*;
import java.io.File;

/**
 * Gradle metric entry which computes extra infos about an entry.
 * Created by DrakkLord on 2016. 03. 14..
 */
public class GradleMetricEntryWrapper extends GradleMetricEntry {
    private final Project myProject;

    public static class PsiInfoPacket {
        public String text;
        public Icon icon;
    }

    public GradleMetricEntryWrapper(GradleMetricEntry source, Project pProject) {
        super(source);
        myProject = pProject;
    }

    public boolean navigateToPSIElementDirect() {
        final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(fileName));
        if (vf == null) {
            return false;
        }

        return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vf);
                if (psiFile != null) {
                    PsiElement target = findTarget();
                    if (target != null && target.isValid()) {
                        PsiNavigateUtil.navigate(target);
//                        final Editor editor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
//                        if (editor != null) {
//                            editor.getSelectionModel().selectLineAtCaret();
//                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public PsiInfoPacket getPsiTargetInfo() {
        final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(fileName));
        if (vf == null) {
            return null;
        }

        return ApplicationManager.getApplication().runReadAction(new Computable<PsiInfoPacket>() {
            @Override
            public PsiInfoPacket compute() {
                final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vf);
                if (psiFile != null) {
                    PsiElement target = findTarget();
                    if (target != null && target.isValid() && target instanceof PsiNamedElement) {
                        final PsiInfoPacket info = new PsiInfoPacket();
                        info.icon = target.getIcon(PsiFile.ICON_FLAG_VISIBILITY);
                        info.text = ((PsiNamedElement)target).getName();
                        return info;
                    }
                }
                return null;
            }
        });
    }

    public PsiInfoPacket getPsiFileInfo() {
        final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(fileName));
        if (vf == null) {
            return null;
        }

        return ApplicationManager.getApplication().runReadAction(new Computable<PsiInfoPacket>() {
            @Override
            public PsiInfoPacket compute() {
                final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vf);
                if (psiFile != null && psiFile.isValid()) {
                    final PsiInfoPacket info = new PsiInfoPacket();
                    info.icon = psiFile.getIcon(PsiFile.ICON_FLAG_VISIBILITY);
                    info.text = psiFile.getName();
                    return info;
                }
                return null;
            }
        });
    }

    /** Try to find the target based on the text pointer given by the element. */
    private PsiElement findTarget() {
        final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(fileName));
        if (vf == null) {
            return null;
        }
        return ApplicationManager.getApplication().runReadAction(new Computable<PsiElement>() {

            @Override
            public PsiElement compute() {
                final PsiFile psiFileLocal = PsiManager.getInstance(myProject).findFile(vf);
                if (psiFileLocal == null) {
                    return null;
                }

                final Document doc = PsiDocumentManager.getInstance(myProject).getDocument(psiFileLocal);
                if (doc == null) {
                    return null;
                }

                int docOffset = doc.getLineStartOffset(Math.max(lineStart - 1, 0));
                if (columnStart > 0) {
                    docOffset += columnStart;
                }

                if (docOffset >= 0) {
                    final PsiElement out = psiFileLocal.findElementAt(docOffset);
                    if (out != null) {
                        return out;
                    }
                }
                return null;
            }
        });
    }
}
