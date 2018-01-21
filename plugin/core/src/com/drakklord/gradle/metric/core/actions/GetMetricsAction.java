package com.drakklord.gradle.metric.core.actions;

import com.android.tools.idea.navigator.AndroidProjectViewPane;
import com.drakklord.gradle.metric.core.components.GradleMetricProjectCoreComponent;
import com.drakklord.gradle.metric.core.model.GradleMetricFacet;
import com.drakklord.gradle.metric.core.model.MetricGradleModel;
import com.drakklord.gradle.metric.core.resources.PluginBundle;
import com.intellij.notification.impl.NotificationActionProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

import java.awt.*;

/**
 * Base action to run the metrics check.
 * Created by DrakkLord on 2015.11.13..
 */
public class GetMetricsAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        if (!ActionPlaces.isMainMenuOrActionSearch(e.getPlace()) && !e.getPlace().equals(ActionPlaces.PROJECT_VIEW_POPUP)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        final GradleMetricProjectCoreComponent cp = project.getComponent(GradleMetricProjectCoreComponent.class);
        if (cp == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        if (!cp.isInitialized()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        Presentation p = e.getPresentation();

        MetricGradleModel mm = null;
        if (e.getPlace().equals(ActionPlaces.PROJECT_VIEW_POPUP)) {
            mm = getModelFromActionEvent(e);

            // If this is not a project we can handle just don't show the item
            if (mm == null) {
                e.getPresentation().setEnabledAndVisible(false);
                return;
            }
        }

        p.setEnabled(!cp.isSyncInProgress() && !cp.isLastSyncFailed()
                        && !cp.isMetricCheckInProgress() && project.isOpen());

        String text = null;
        String desc = null;
        text = PluginBundle.message("action.main.name");
        if (mm != null) {
            text += " [" + mm.getModuleName() + "]";
            desc = PluginBundle.message("action.main.description.specific", mm.getModuleName());
        } else {
            desc = PluginBundle.message("action.main.description");
        }
        p.setText(text);
        p.setDescription(desc);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null || !project.isOpen()) {
            return;
        }

        MetricGradleModel mm = null;
        if (e.getPlace().equals(ActionPlaces.PROJECT_VIEW_POPUP)) {
            mm = getModelFromActionEvent(e);

            // If this is not a project we can handle just do nothing
            if (mm == null) {
                return;
            }
        }

        final GradleMetricProjectCoreComponent cp = project.getComponent(GradleMetricProjectCoreComponent.class);
        if (cp == null) {
            return;
        }

        if (cp.isSyncInProgress()) {
            cp.addToEventLog("unable to get metrics gradle sync is progress", MessageType.ERROR);
            return;
        }
        if (cp.isLastSyncFailed()) {
            cp.addToEventLog("unable to get metrics gradle last sync failed", MessageType.ERROR);
            return;
        }
        /*
        if (!cp.hasTasksToExecute()) {
            cp.addToEventLog("unable to get metrics no compatible tasks found", MessageType.ERROR);
            return;
        }
        if (!cp.isExtensionsAvailable()) {
            cp.addToEventLog("unable to get metrics, no extensions available", MessageType.ERROR);
            return;
        }
        */
        if (cp.isMetricCheckInProgress()) {
            return;
        }
        cp.performMetricCheck(mm);
    }

    private MetricGradleModel getModelFromActionEvent(AnActionEvent e) {
        MetricGradleModel mm = null;
        Module m = e.getData(DataKeys.MODULE);
        if (m != null && !m.getName().isEmpty()) {
            GradleMetricFacet facet = GradleMetricFacet.getInstance(m);
            if (facet != null && facet.getMetricModel() != null) {
                mm = facet.getMetricModel();
                if (mm != null && !mm.hasModels()) {
                    return null;
                }
            }
        }
        return mm;
    }
}
