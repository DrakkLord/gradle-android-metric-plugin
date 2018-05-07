package com.drakklord.gradle.metric.core.view.components;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.drakklord.gradle.metric.core.contributor.model.*;
import com.drakklord.gradle.metric.core.view.components.treenodes.*;
import com.intellij.codeInspection.ui.InspectionTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;

/**
 * Metric report tree view.
 * Created by DrakkLord on 2015. 12. 02..
 */
public class MetricTree extends Tree implements ComponentContainer {

    private final Project myProject;

    public MetricTree(Project project) {
        super(new MetricRootTreeNode(project));
        myProject = project;
        setShowsRootHandles(true);
        UIUtil.setLineStyleAngled(this);

        TreeUtil.installActions(this);
        addTreeWillExpandListener(new ExpandListener());
        setCellRenderer(new CellRenderer());

        removeAllNodes();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
        return this;
    }

    @Override
    public void dispose() {
    }

    public void removeAllNodes() {
        getRoot().removeAllChildren();
//        nodeStructureChanged(getRoot());
    }

    public MetricRootTreeNode getRoot() {
        return (MetricRootTreeNode) getModel().getRoot();
    }

    @Override
    protected boolean isCustomUI() {
        return true;
    }

    @Override
    protected boolean isWideSelection() {
        return false;
    }

    public void clearResults() {
        removeAllNodes();
    }

    public void showResults(HashMap<GradleMetricContributor, GradleMetricResultContributor> entries) {
        removeAllNodes();
        if (entries.size() == 0) {
            updateUI();
            return;
        }

        for (GradleMetricResultContributor e : entries.values()) {
            final MetricContributorTreeNode ctNode = new MetricContributorTreeNode(e);
            getRoot().add(ctNode);

            for (GradleMetricResultNamespace ns : e.getChildren().values()) {
                final MetricNamespaceTreeNode nsNode = new MetricNamespaceTreeNode(ns);
                ctNode.add(nsNode);

                for (GradleMetricResultType tp : ns.getChildren().values()) {
                    final MetricTypeTreeNode tpNode = new MetricTypeTreeNode(tp);
                    nsNode.add(tpNode);

                    for (GradleMetricResultFile fs : tp.getChildren().values()) {
                        final MetricFileTreeNode fsNode = new MetricFileTreeNode(fs);
                        tpNode.add(fsNode);

                        // sort the items from the metric results by their line number
                        final Set<GradleMetricEntryWrapper> treeSetResults = new TreeSet<GradleMetricEntryWrapper>(new MetricEntryWrapperComparator());
                        treeSetResults.addAll(fs.getChildren());

                        final ArrayList<GradleMetricEntryWrapper> addedNodes = new ArrayList<GradleMetricEntryWrapper>();
                        for (GradleMetricEntryWrapper me : treeSetResults) {
                            // check if already added
                            boolean skip = false;
                            for (GradleMetricEntryWrapper anode : addedNodes) {
                                if (anode.equals(me)) {
                                    skip = true;
                                    break;
                                }
                            }
                            if (skip) {
                                continue;
                            }
                            addedNodes.add(me);

                            // add if unique
                            final MetricFileScopeTreeNode scopeNode = new MetricFileScopeTreeNode(me);
                            fsNode.add(scopeNode);
                        }
                    }
                }
            }
        }

        TreeUtil.expandAll(this);
//        TreeUtil.expand(this, 5);
        updateUI();
    }

    private static class MetricEntryWrapperComparator implements Comparator<GradleMetricEntryWrapper> {
        @Override
        public int compare(GradleMetricEntryWrapper o1, GradleMetricEntryWrapper o2) {
            return o1.lineStart - o2.lineStart;
        }
    }

    private class ExpandListener implements TreeWillExpandListener {
        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            final InspectionTreeNode node = (InspectionTreeNode)event.getPath().getLastPathComponent();

            // Smart expand
            if (node.getChildCount() >= 0) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        expandPath(new TreePath(node.getPath()));
                    }
                });
            }
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        }
    }

    private static class CellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            MetricAbstractTreeNode node = (MetricAbstractTreeNode) value;

            append(node.toString(),
                    patchAttr(node, appearsBold(node) ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : getMainForegroundAttributes(node)));

            int problemCount = node.getProblemCount(false);
            if (!leaf) {
                append(" (" + problemCount + " item" + (problemCount > 1 ? "s" : "") +")", patchAttr(node, SimpleTextAttributes.GRAYED_ATTRIBUTES));
            }
            if (node.hasScopeInfo()) {
                append("  " + node.getScopeInfo(), patchAttr(node, SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES));
            }
            if (node.isRoot() && node.getChildCount() == 0) {
                append(" - no issues", patchAttr(node, SimpleTextAttributes.GRAYED_ATTRIBUTES));
            }

            if (!node.isValid()) {
                append(" INVALID", patchAttr(node, SimpleTextAttributes.ERROR_ATTRIBUTES));
            } else {
                setIcon(node.getIcon(expanded));
            }
        }

        public static SimpleTextAttributes patchAttr(InspectionTreeNode node, SimpleTextAttributes attributes) {
            if (!node.isValid()) {
                return new SimpleTextAttributes(attributes.getBgColor(), JBColor.RED, attributes.getWaveColor(), attributes.getStyle() | SimpleTextAttributes.STYLE_STRIKEOUT);
            }
            return attributes;
        }

        private static SimpleTextAttributes getMainForegroundAttributes(InspectionTreeNode node) {
            return SimpleTextAttributes.REGULAR_ATTRIBUTES;
        }

        private static boolean appearsBold(Object node) {
            return ((InspectionTreeNode)node).appearsBold();
        }
    }

    public void navigateToSelection() {
        MetricFileScopeTreeNode nodes[] = getSelectedNodes(MetricFileScopeTreeNode.class, null);
        if (nodes.length ==0) {
            return;
        }

        for (MetricFileScopeTreeNode n : nodes) {
            if (n.navigateToScope()) {
                return;
            }
        }
    }
}
