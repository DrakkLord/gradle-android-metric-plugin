package com.drakklord.gradle.metric.core.contributor.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.drakklord.gradle.metric.core.contributor.GradleMetricTextUtil;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PsiNavigateUtil;

import java.io.File;

/**
 * Gradle metric entry which computes extra infos about an entry.
 * Created by DrakkLord on 2016. 03. 14..
 */
public class GradleMetricEntryWrapper extends GradleMetricEntry {

    public final VirtualFile virtualFile;
    public final PsiFile psiFile;
    public final PsiNamedElement psiTarget;
    public final PsiElement psiDirectTarget;
    public Document doc;

    private final Project myProject;

    public GradleMetricEntryWrapper(GradleMetricEntry source, Project pProject) {
        super(source);
        myProject = pProject;

        virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileName));
        if (virtualFile == null) {
            psiFile = null;
            psiTarget = null;
            psiDirectTarget = null;
        } else {
            psiFile = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {

                @Override
                public PsiFile compute() {
                    return PsiManager.getInstance(myProject).findFile(virtualFile);
                }
            });
            if (psiFile != null) {
                final PsiElement[] elems = ApplicationManager.getApplication().runReadAction(new Computable<PsiElement[]>() {
                    @Override
                    public PsiElement[] compute() {
                        return findTarget();
                    }
                });
//                psiTarget = (PsiNamedElement) elems[0];
                psiTarget = null;
                psiDirectTarget = elems[1];
            } else {
                psiTarget = null;
                psiDirectTarget = null;
            }
        }
    }

    private PsiElement[] findTarget() {
        // try to find the target based on the text pointer given by the element
        doc = PsiDocumentManager.getInstance(myProject).getDocument(psiFile);
        if (doc == null) {
            return new PsiElement[] {null, null};
        }

        int docOffset = doc.getLineStartOffset(Math.max(lineStart - 1, 0));
        if (columnStart > 0) {
            docOffset += columnStart;
        }

// doc.createRangeMarker(0, 0)


        PsiElement direct = null;
        if (docOffset >= 0) {
            final PsiElement out = psiFile.findElementAt(docOffset);
            if (out != null) {
                 direct = out;
                 return new PsiElement[] {out, out};
/*
                // return direct hit if it matches
                if (psiInstanceCheck(out)) {
                    return new PsiElement[] {out, direct};
                }

                // try siblings as this is the start of the line
                for (PsiElement e = out.getNextSibling(); e != null; e = e.getNextSibling()) {
                    for (PsiElement c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
                        if (psiInstanceCheck(c)) {
                            return new PsiElement[]{c, c};
                        }
                    }
                    if (psiInstanceCheck(e)) {
                        return new PsiElement[]{e, e};
                    }
                }*/
/*
                // try parent of the found element
                final PsiElement parent = out.getParent();
                if (psiInstanceCheck(parent)) {
                    return new PsiElement[] {parent, direct};
                }

                // try to search the tree of the parent or the found element
                final PsiNamedElement rootElement = PsiTreeUtil.getParentOfType(out, PsiNamedElement.class, true);
                if (psiInstanceCheck(rootElement)) {
                    return new PsiElement[] {rootElement, direct};
                }
*/
            }
        }
/*
        // if these failed try to obtain an element in a wider scope
        if (psiFile instanceof PsiJavaFile) {
            final PsiJavaFile psiJava = (PsiJavaFile) psiFile;

            // this has the assumption that the first in the class list is always the main class
            final PsiClass classes[] = psiJava.getClasses();
            if (classes.length > 0 || classes[0] != null && psiInstanceCheck(classes[0])) {
                return new PsiElement[] {classes[0], direct};
            }
        }

        // find the fist element in the tree we can use
        for (PsiElement e = psiFile.getFirstChild(); e != null; e = e.getNextSibling()) {
            if (psiInstanceCheck(e)) {
                return new PsiElement[] {e, direct != null ? direct : e};
            }
        }
*/
        // failed to find anything that we can use
        return new PsiElement[] {null, null};
    }

    private boolean psiInstanceCheck(PsiElement elem) {
        return elem != null && elem instanceof NavigatablePsiElement && elem instanceof PsiNamedElement && !(elem instanceof PsiWhiteSpace);
    }
}
