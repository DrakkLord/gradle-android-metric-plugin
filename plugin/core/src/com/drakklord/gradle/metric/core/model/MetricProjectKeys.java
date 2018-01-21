package com.drakklord.gradle.metric.core.model;

import com.android.tools.idea.gradle.project.sync.idea.data.service.AndroidProjectKeys;
import com.intellij.openapi.externalSystem.model.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class MetricProjectKeys {

    @NotNull
    public static final Key<MetricGradleModel> METRIC_MODEL = Key.create(MetricGradleModel.class, AndroidProjectKeys.GRADLE_MODULE_MODEL.getProcessingWeight() + 5);
}
