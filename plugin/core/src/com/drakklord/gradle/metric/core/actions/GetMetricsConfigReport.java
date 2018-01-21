package com.drakklord.gradle.metric.core.actions;

import com.drakklord.gradle.metric.core.components.GradleMetricProjectCoreComponent;
import com.drakklord.gradle.metric.core.resources.PluginBundle;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

/**
 * Created by DrakkLord on 2016. 11. 06..
 */
public class GetMetricsConfigReport extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        if (!ActionPlaces.isMainMenuOrActionSearch(e.getPlace())) {
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

        p.setEnabled(!cp.isSyncInProgress() && !cp.isLastSyncFailed()
                     && !cp.isMetricCheckInProgress() && project.isOpen());

        p.setText(PluginBundle.message("action.report.name"));
        p.setDescription(PluginBundle.message("action.report.description"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null || !project.isOpen()) {
            return;
        }

        final GradleMetricProjectCoreComponent cp = project.getComponent(GradleMetricProjectCoreComponent.class);
        if (cp == null) {
            return;
        }

        if (cp.isSyncInProgress()) {
            cp.addToEventLog("unable to get metrics config report, gradle sync is progress", MessageType.ERROR);
            return;
        }
        if (cp.isLastSyncFailed()) {
            cp.addToEventLog("unable to get metrics config report, gradle last sync failed", MessageType.ERROR);
            return;
        }
        cp.performMetricConfigReport();
    }
}
