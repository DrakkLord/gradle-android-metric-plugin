package com.drakklord.gradle.metric.core.model;

import com.android.tools.idea.gradle.project.sync.GradleSyncState;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.drakklord.gradle.metric.core.model.MetricProjectKeys.METRIC_MODEL;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class GradleMetricModelDataService extends AbstractProjectDataService<MetricGradleModel, Void> {

    private final MetricFacetModuleCustomizer customizer = new MetricFacetModuleCustomizer();

    // This constructor is called by the IDE. See this module's plugin.xml file, implementation of extension 'externalProjectDataService'.
    public GradleMetricModelDataService() {
    }

    @NotNull
    @Override
    public Key<MetricGradleModel> getTargetDataKey() {
        return METRIC_MODEL;
    }

    @Override
    public void importData(@NotNull Collection<DataNode<MetricGradleModel>> toImport,
                           @Nullable ProjectData projectData,
                           @NotNull Project project,
                           @NotNull IdeModifiableModelsProvider modelsProvider) {
        if (!toImport.isEmpty()) {
            try {
                doImport(toImport, project, modelsProvider);
            }
            catch (Throwable e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.getClass().getCanonicalName();
                }
                GradleSyncState.getInstance(project).syncFailed(msg);
            }
        }
    }

    private void doImport(final Collection<DataNode<MetricGradleModel>> toImport,
                          final Project project,
                          final IdeModifiableModelsProvider modelsProvider) throws Throwable {
        RunResult result = new WriteCommandAction.Simple(project) {
            @Override
            protected void run() throws Throwable {
                Map<String, MetricGradleModel> androidModelsByModuleName = indexByModuleName(toImport);

                // Module name, build
                for (Module module : modelsProvider.getModules()) {
                    MetricGradleModel metricModel = androidModelsByModuleName.get(module.getName());
                    customizeModule(module, project, modelsProvider, metricModel);
                }
            }
        }.execute();
        Throwable error = result.getThrowable();
        if (error != null) {
            throw error;
        }
    }

    @NotNull
    private static Map<String, MetricGradleModel> indexByModuleName(@NotNull Collection<DataNode<MetricGradleModel>> dataNodes) {
        Map<String, MetricGradleModel> index = Maps.newHashMap();
        for (DataNode<MetricGradleModel> d : dataNodes) {
            MetricGradleModel androidModel = d.getData();
            index.put(androidModel.getModuleName(), androidModel);
        }
        return index;
    }

    private void customizeModule(@NotNull Module module,
                                 @NotNull Project project,
                                 @NotNull IdeModifiableModelsProvider modelsProvider,
                                 @Nullable MetricGradleModel androidModel) {
        customizer.customizeModule(project, module, modelsProvider, androidModel);
    }
}
