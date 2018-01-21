package com.drakklord.gradle.metric.core.model;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.GradleMetricModelHolder;
import com.intellij.openapi.module.Module;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class MetricGradleModel {

    private final HashMap<GradleMetricContributor, GradleMetricModelHolder> modelMapping = new HashMap<GradleMetricContributor, GradleMetricModelHolder>();
    private final String mModuleName;
    private final String mGradlePath;

    public MetricGradleModel(String moduleName, String gradlePath, HashMap<GradleMetricContributor, GradleMetricModelHolder> modelMapping) {
        this.modelMapping.putAll(modelMapping);
        this.mModuleName = moduleName;
        this.mGradlePath = gradlePath;
    }

    public String getModuleName() {
        return mModuleName;
    }

    public boolean hasModels() {
        return !modelMapping.isEmpty();
    }

    public GradleMetricModelHolder getModelFor(GradleMetricContributor c) {
        return modelMapping.get(c);
    }

    public String getGradePath() {
        return mGradlePath;
    }

    @Nullable
    public static MetricGradleModel get(@NotNull Module module) {
        GradleMetricFacet facet = GradleMetricFacet.getInstance(module);
        return facet != null ? get(facet) : null;
    }

    @Nullable
    public static MetricGradleModel get(@NotNull GradleMetricFacet androidFacet) {
        return androidFacet.getMetricModel();
    }
}
