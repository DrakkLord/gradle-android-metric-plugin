package com.drakklord.gradle.metric.core.model;

import com.drakklord.gradle.metric.core.Extensions;
import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.GradleMetricModelHolder;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants;
import com.intellij.openapi.externalSystem.util.Order;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import org.gradle.tooling.model.idea.IdeaModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.model.ExternalProject;
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension;

import java.util.Set;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
@Order(ExternalSystemConstants.UNORDERED)
public class GradleMetricProjectResolver extends AbstractProjectResolverExtension {

    @Override
    public void populateModuleExtraModels(@NotNull IdeaModule gradleModule, @NotNull DataNode<ModuleData> ideModule) {
        ExternalProject myModel = resolverCtx.getExtraProject(gradleModule, ExternalProject.class);
        if (myModel != null && !myModel.getArtifactsByConfiguration().isEmpty()) {
            // Skip the special 'buildSrc' project which is a standalone auto generated project!
            if ("buildSrc".equalsIgnoreCase(myModel.getQName())) {
                nextResolver.populateModuleExtraModels(gradleModule, ideModule);
                return;
            }

            final HashMap<GradleMetricContributor, GradleMetricModelHolder> extTasks = new HashMap<GradleMetricContributor, GradleMetricModelHolder>();
            GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
            for (GradleMetricContributor c : exts) {
                final GradleMetricModelHolder o = c.getModuleModel(gradleModule, resolverCtx);
                if (o != null) {
                    extTasks.put(c, o);
                }
            }

            if (!extTasks.isEmpty()) {
                ideModule.createChild(MetricProjectKeys.METRIC_MODEL,
                                        new MetricGradleModel(gradleModule.getName(), myModel.getQName(), extTasks));
            }
        }
        nextResolver.populateModuleExtraModels(gradleModule, ideModule);
    }

    @NotNull
    @Override
    public Set<Class> getExtraProjectModelClasses() {
        final HashSet<Class> clss = new HashSet<Class>();
        GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
        for (GradleMetricContributor c : exts) {
            c.getProjectResolverModelClasses(clss);
        }
        return clss;
    }

    @NotNull
    @Override
    public Set<Class> getToolingExtensionsClasses() {
        final HashSet<Class> clss = new HashSet<Class>();
        GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
        for (GradleMetricContributor c : exts) {
            c.getProjectResolverModelClasses(clss);
        }
        return clss;
    }
}