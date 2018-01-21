package com.drakklord.gradle.metric.core.view.components.treenodes;

import com.intellij.codeInspection.ui.InspectionTreeNode;

/**
 * Base node for all tree nodes.
 * Created by DrakkLord on 2016. 03. 15..
 */
public abstract class MetricAbstractTreeNode extends InspectionTreeNode {

    protected MetricAbstractTreeNode(Object userObject) {
        super(userObject);
    }

    /** Check whenever the node can supply file scope related extra info. */
    public boolean hasScopeInfo() {
        return false;
    }

    /** Get scope related extra info, such as line number the anchor is at. */
    public String getScopeInfo() {
        return null;
    }
}
